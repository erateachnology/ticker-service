package com.eucalyptuslabs.backend.common.provider;

import com.eucalyptuslabs.backend.common.model.exception.DataNotAvailableException;
import com.eucalyptuslabs.backend.common.model.exception.ProviderFailureException;
import com.eucalyptuslabs.backend.common.provider.metrics.ProviderMetricService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static com.eucalyptuslabs.backend.common.util.AssertionUtils.awaitUntilAsserted;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class BaseProvidersCacheServiceTest {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  static class TestProvidersCacheService extends BaseProvidersCacheService<String> {

    private final Supplier<Integer> configurationSupplier;

    public TestProvidersCacheService(
        Supplier<Integer> configurationSupplier,
        List<? extends BaseProviderService<String>> baseProviderServices) {
      super("TestCache_" + System.currentTimeMillis(), baseProviderServices);
      this.configurationSupplier = configurationSupplier;
    }

    @Override
    public int getRefreshIntervalSeconds() {
      return configurationSupplier.get();
    }
  }

  private static final int CONFIGURATION_REFRESH_INTERVAL_SECONDS = 1;
  private static final int DATA_FETCH_TIME = 1000;
  private final ApplicationEventPublisher applicationEventPublisher =
      mock(ApplicationEventPublisher.class);
  private final ProviderMetricService providerMetricService = mock(ProviderMetricService.class);
  private final BaseProviderService<String> providerServiceNo1 = mock(BaseProviderService.class);
  private final BaseProviderService<String> providerServiceNo2 = mock(BaseProviderService.class);
  private final Supplier<Integer> configurationSupplier = mock(Supplier.class);
  private final TestProvidersCacheService cacheService =
      new TestProvidersCacheService(
          configurationSupplier, List.of(providerServiceNo1, providerServiceNo2));
  private final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();

  @BeforeEach
  public void setup() {

    cacheService.providerMetricService = providerMetricService;
    cacheService.applicationEventPublisher = applicationEventPublisher;

    doReturn(CONFIGURATION_REFRESH_INTERVAL_SECONDS).when(configurationSupplier).get();

    taskScheduler.initialize();
  }

  String initializationData = "init-data";
  String refreshData = "refresh-data";

  @Test
  public void shouldNotBlockOnValueReadDuringCacheInit()
      throws DataNotAvailableException, ProviderFailureException, InterruptedException {

    doAnswer(
            (invocation) -> {
              Thread.sleep(DATA_FETCH_TIME);
              return initializationData;
            })
        .when(providerServiceNo1)
        .fetchFreshData();

    Future<Boolean> initTask = taskScheduler.submit(cacheService::initCache);
    assertCacheReadNotBlocking(initTask);
    assertEquals(initializationData, cacheService.getCachedData());
  }

  @Test
  public void shouldNotBlockOnValueReadDuringCacheRefresh()
      throws DataNotAvailableException, ProviderFailureException, InterruptedException {
    doReturn(initializationData)
        .doAnswer(
            (invocation) -> {
              Thread.sleep(DATA_FETCH_TIME);
              return refreshData;
            })
        .when(providerServiceNo1)
        .fetchFreshData();

    cacheService.initCache();

    Future<?> refreshTask = taskScheduler.submit(cacheService::callProvider);
    assertCacheReadNotBlocking(refreshTask);
    assertEquals(refreshData, cacheService.getCachedData());
  }

  @Test
  public void shouldNotAllowConcurrentInvocationsOfCacheInit()
      throws DataNotAvailableException, ProviderFailureException, ExecutionException,
          InterruptedException {
    doThrow(new RuntimeException("first provider failure"))
        .when(providerServiceNo1)
        .fetchFreshData();
    doAnswer(
            (invocation) -> {
              Thread.sleep(DATA_FETCH_TIME);
              return refreshData;
            })
        .when(providerServiceNo2)
        .fetchFreshData();

    AtomicLong initTaskExecutionTimeNo1 = new AtomicLong();
    AtomicLong initTaskExecutionTimeNo2 = new AtomicLong();

    Future<?> initTaskNo1 =
        taskScheduler.submit(
            () -> {
              long startTime = System.currentTimeMillis();
              cacheService.initCache();
              initTaskExecutionTimeNo1.set(System.currentTimeMillis() - startTime);
            });

    awaitUntilAsserted(() -> verify(providerServiceNo1).fetchFreshData());

    Future<?> initTaskNo2 =
        taskScheduler.submit(
            () -> {
              long startTime = System.currentTimeMillis();
              cacheService.initCache();
              initTaskExecutionTimeNo2.set(System.currentTimeMillis() - startTime);
            });

    initTaskNo1.get();
    initTaskNo2.get();

    Long elapsedTime = initTaskExecutionTimeNo1.get() + initTaskExecutionTimeNo2.get();

    assertTrue(elapsedTime >= DATA_FETCH_TIME, elapsedTime.toString());
    assertTrue(elapsedTime < 2L * DATA_FETCH_TIME, elapsedTime.toString());

    verify(providerServiceNo2, times(1)).fetchFreshData();
  }

  @Test
  public void shouldNotAllowConcurrentInvocationsOfCacheRefresh()
      throws DataNotAvailableException, ProviderFailureException, ExecutionException,
          InterruptedException {
    doReturn(initializationData)
        .doAnswer(
            (invocation) -> {
              Thread.sleep(DATA_FETCH_TIME);
              return refreshData;
            })
        .when(providerServiceNo1)
        .fetchFreshData();

    cacheService.initCache();

    AtomicLong refreshTaskExecutionTimeNo1 = new AtomicLong();
    AtomicLong refreshTaskExecutionTimeNo2 = new AtomicLong();

    Future<?> refreshTaskNo1 =
        taskScheduler.submit(
            () -> {
              long startTime = System.currentTimeMillis();
              cacheService.callProvider();
              refreshTaskExecutionTimeNo1.set(System.currentTimeMillis() - startTime);
            });
    Future<?> refreshTaskNo2 =
        taskScheduler.submit(
            () -> {
              long startTime = System.currentTimeMillis();
              cacheService.callProvider();
              refreshTaskExecutionTimeNo2.set(System.currentTimeMillis() - startTime);
            });

    refreshTaskNo1.get();
    refreshTaskNo2.get();

    Long elapsedTime = refreshTaskExecutionTimeNo1.get() + refreshTaskExecutionTimeNo2.get();

    assertTrue(elapsedTime >= 2L * DATA_FETCH_TIME, elapsedTime.toString());

    verify(providerServiceNo1, times(3)).fetchFreshData();
  }

  private void assertCacheReadNotBlocking(Future<?> initTask) throws InterruptedException {
    while (!initTask.isDone()) {
      long startTime = System.currentTimeMillis();
      try {
        cacheService.getCachedData();
      } catch (DataNotAvailableException e) {
      }
      Thread.sleep(1);
      Long endTime = System.currentTimeMillis() - startTime;
      assertTrue(endTime < DATA_FETCH_TIME, endTime.toString());
    }
  }
}
