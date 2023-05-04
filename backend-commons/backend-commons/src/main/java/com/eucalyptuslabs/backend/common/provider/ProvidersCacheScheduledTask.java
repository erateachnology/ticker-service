package com.eucalyptuslabs.backend.common.provider;

import com.eucalyptuslabs.backend.common.service.TimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import java.util.concurrent.ScheduledFuture;

/**
 * The class controls a given providers cache service (see {@link BaseProvidersCacheService}) in the
 * context of scheduled executions of initialization and refreshing functionalities. The instance of
 * the class is being created by {@link BaseProvidersCacheScheduler} and the controlling process is
 * started with the invocation of method {@link #runCache()}. <br>
 * The instance maintains the status of scheduled task execution. The scheduled task is the method
 * {@link #runCacheTask()}. Depending on the underlying cache state, it either initializes or
 * refreshes the cache. After the task execution (successful or not), another run of the task is
 * scheduled. <br>
 * In case of scheduled task unrecoverable failure, the controlling process relies on the scheduler
 * to get resumed (see {@link BaseProvidersCacheScheduler#rescheduleTasks()}).
 */
public class ProvidersCacheScheduledTask {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  private final TaskScheduler taskScheduler;

  private final TimeService timeService;

  private final BaseProvidersCacheService<?> providersCacheService;
  private final int cacheInitializationRetryIntervalSeconds;

  protected boolean shouldFinish = false;
  protected ScheduledFuture<?> currentTask;

  public ProvidersCacheScheduledTask(
      TaskScheduler taskScheduler,
      TimeService timeService,
      int cacheInitializationRetryIntervalSeconds,
      BaseProvidersCacheService<?> providersCacheService) {
    this.taskScheduler = taskScheduler;
    this.timeService = timeService;
    this.cacheInitializationRetryIntervalSeconds = cacheInitializationRetryIntervalSeconds;
    this.providersCacheService = providersCacheService;
  }

  public synchronized void runCache() {
    if (currentTask == null || currentTask.isDone()) {
      scheduleInit(0);
    }
  }

  public synchronized void stopTask() {
    shouldFinish = true;
  }

  private synchronized void runCacheTask() {
    if (shouldFinish) {
      return;
    }
    if (!providersCacheService.isInitialized()) {
      runScheduledInit();
    } else {
      runScheduledRefresh();
    }
  }

  private void runScheduledRefresh() {
    if (providersCacheService.isEnabled()) {
      providersCacheService.callProvider();
    }
    scheduleRefresh();
  }

  private void runScheduledInit() {
    if (providersCacheService.isEnabled()) {
      boolean initialized = providersCacheService.initCache();
      if (initialized) {
        scheduleRefresh();
      } else {
        scheduleInit();
      }
    } else {
      scheduleInit();
    }
  }

  private void scheduleInit() {
    scheduleInit(cacheInitializationRetryIntervalSeconds);
  }

  private void scheduleInit(int delay) {
    scheduleTask(delay);
  }

  private void scheduleRefresh() {
    int refreshIntervalSeconds = providersCacheService.getRefreshIntervalSeconds();
    scheduleTask(refreshIntervalSeconds);
  }

  private void scheduleTask(int delay) {
    currentTask =
        taskScheduler.schedule(
            this::runCacheTask, timeService.getCurrentInstant().plusSeconds(delay));
  }
}
