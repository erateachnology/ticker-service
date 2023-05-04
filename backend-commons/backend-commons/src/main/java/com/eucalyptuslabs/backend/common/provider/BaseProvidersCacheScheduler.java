package com.eucalyptuslabs.backend.common.provider;

import com.eucalyptuslabs.backend.common.provider.precaching.BasePreCachingService;
import com.eucalyptuslabs.backend.common.provider.precaching.TriggerPreCachingEvent;
import com.eucalyptuslabs.backend.common.service.TimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The base class for the scheduler facilitating providers cache service initialization and
 * refreshing functionalities (see {@link BaseProvidersCacheService}). For the given collection of
 * providers cache services, for each service it maintains the corresponding {@link
 * ProvidersCacheScheduledTask}, which controls the scheduling of initialization and refreshing
 * operations of the cache service. <br>
 * The tasks are scheduled (and rescheduled) usually by {@link BasePreCachingService} when handling
 * a pre-caching event (see {@link TriggerPreCachingEvent}) or at the application startup (see
 * {@link AutoProvidersCacheScheduler}).
 */
public abstract class BaseProvidersCacheScheduler implements DisposableBean {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired private ThreadPoolTaskScheduler taskScheduler;
  @Autowired private TimeService timeService;

  @Value("${providers-cache.initialization-retry.seconds}")
  protected int cacheInitializationRetryIntervalSeconds;

  private final List<? extends BaseProvidersCacheService<?>> providersCacheServices;
  private final Map<BaseProvidersCacheService<?>, ProvidersCacheScheduledTask> scheduledTasksMap =
      new HashMap<>();

  public BaseProvidersCacheScheduler(
      List<? extends BaseProvidersCacheService<?>> providersCacheServices) {
    this.providersCacheServices = providersCacheServices;
  }

  public synchronized void rescheduleTasks() {

    providersCacheServices.forEach(
        providersCacheServices -> {
          if (!scheduledTasksMap.containsKey(providersCacheServices)) {
            scheduledTasksMap.put(
                providersCacheServices,
                new ProvidersCacheScheduledTask(
                    taskScheduler,
                    timeService,
                    cacheInitializationRetryIntervalSeconds,
                    providersCacheServices));
          }
          scheduledTasksMap.get(providersCacheServices).runCache();
        });
  }

  @Override
  public void destroy() {
    scheduledTasksMap.values().forEach(ProvidersCacheScheduledTask::stopTask);
  }
}
