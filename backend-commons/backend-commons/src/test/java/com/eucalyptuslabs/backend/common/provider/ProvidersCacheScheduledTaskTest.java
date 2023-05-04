package com.eucalyptuslabs.backend.common.provider;

import static com.eucalyptuslabs.backend.common.provider.BaseProvidersCacheServiceTest.TestProvidersCacheService;
import static com.eucalyptuslabs.backend.common.util.AssertionUtils.awaitUntilAsserted;
import static org.mockito.Mockito.*;

import com.eucalyptuslabs.backend.common.model.exception.DataNotAvailableException;
import com.eucalyptuslabs.backend.common.model.exception.ProviderFailureException;
import com.eucalyptuslabs.backend.common.provider.metrics.ProviderMetricService;
import com.eucalyptuslabs.backend.common.service.TimeService;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class ProvidersCacheScheduledTaskTest {

  private final TimeService timeService = new TimeService();
  private final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
  private final ApplicationEventPublisher applicationEventPublisher =
      mock(ApplicationEventPublisher.class);
  private final ProviderMetricService providerMetricService = mock(ProviderMetricService.class);
  private final BaseProviderService<String> providerService = mock(BaseProviderService.class);
  private final Supplier<Integer> configurationSupplier = mock(Supplier.class);
  private final TestProvidersCacheService cacheService =
      new TestProvidersCacheService(configurationSupplier, List.of(providerService));
  private final ProvidersCacheScheduledTask scheduledTask =
      new ProvidersCacheScheduledTask(taskScheduler, timeService, 1, cacheService);

  @BeforeEach
  public void setup() throws DataNotAvailableException, ProviderFailureException {

    cacheService.providerMetricService = providerMetricService;
    cacheService.applicationEventPublisher = applicationEventPublisher;

    doReturn("data-123").when(providerService).fetchFreshData();
    doReturn(1).when(configurationSupplier).get();

    taskScheduler.initialize();
  }

  @AfterEach
  public void stopTask() {
    scheduledTask.stopTask();
    awaitUntilAsserted(() -> scheduledTask.currentTask.isDone());
  }

  @Test
  public void shouldInitializeAndRefreshCache() throws InterruptedException {

    scheduledTask.runCache();

    awaitUntilAsserted(350, () -> verify(providerService).fetchFreshData());

    Thread.sleep(1000);

    awaitUntilAsserted(350, () -> verify(providerService, times(2)).fetchFreshData());

    Thread.sleep(1000);

    awaitUntilAsserted(350, () -> verify(providerService, times(3)).fetchFreshData());
  }

  @Test
  public void shouldNotInitializeCacheTwice() throws InterruptedException {

    scheduledTask.runCache();

    awaitUntilAsserted(350, () -> verify(providerService).fetchFreshData());

    scheduledTask.runCache();

    Thread.sleep(1000);

    awaitUntilAsserted(350, () -> verify(providerService, times(2)).fetchFreshData());
  }

  @Test
  public void shouldRunCacheAgainAfterFailure() throws InterruptedException {

    scheduledTask.runCache();

    awaitUntilAsserted(350, () -> verify(providerService).fetchFreshData());

    doThrow(new RuntimeException("config not available")).when(configurationSupplier).get();

    Thread.sleep(1000);

    awaitUntilAsserted(350, () -> verify(providerService, times(2)).fetchFreshData());

    Thread.sleep(1000);

    awaitUntilAsserted(350, () -> verify(providerService, times(2)).fetchFreshData());

    scheduledTask.runCache();

    awaitUntilAsserted(350, () -> verify(providerService, times(3)).fetchFreshData());
  }
}
