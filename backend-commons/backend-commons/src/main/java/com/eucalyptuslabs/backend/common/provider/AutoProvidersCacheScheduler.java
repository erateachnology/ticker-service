package com.eucalyptuslabs.backend.common.provider;

import com.eucalyptuslabs.backend.common.service.TimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.List;
import java.util.Objects;

/**
 * The specialization of the scheduler (see {@link BaseProvidersCacheScheduler}) which commences the
 * scheduling with the application startup.
 */
public abstract class AutoProvidersCacheScheduler extends BaseProvidersCacheScheduler {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired private TimeService timeService;

  @Autowired private ThreadPoolTaskScheduler taskScheduler;

  @Value("${providers-cache.initialization-retry.seconds}")
  private int cacheInitializationRetryIntervalSeconds;

  public AutoProvidersCacheScheduler(
      List<BaseProvidersCacheService<?>> providerCacheGroupRefreshServices) {
    super(providerCacheGroupRefreshServices);
  }

  @EventListener
  public void scheduleTasks(ApplicationReadyEvent event) {

    if (!Objects.equals(event.getApplicationContext().getId(), "application")) {
      return;
    }

    rescheduleTasks();
  }
}
