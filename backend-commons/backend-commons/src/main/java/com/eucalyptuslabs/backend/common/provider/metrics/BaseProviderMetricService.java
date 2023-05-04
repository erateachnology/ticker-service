package com.eucalyptuslabs.backend.common.provider.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseProviderMetricService {

  private static final String CACHE_FAILURE_METRIC = "cache.failure";
  private static final String CACHE_INIT_ACTION = "init";
  private static final String CACHE_REFRESH_ACTION = "refresh";

  @Autowired private MeterRegistry meterRegistry;

  public void increaseCacheInitFailureMetric(String cacheDataNameTag) {
    getCacheInitFailureMetric(cacheDataNameTag).increment();
  }

  public void increaseCacheRefreshFailureMetric(String cacheDataNameTag) {
    getCacheRefreshFailureMetric(cacheDataNameTag).increment();
  }

  protected Counter getCacheInitFailureMetric(String cacheDataNameTag) {
    return getCacheFailureMetric(cacheDataNameTag, CACHE_INIT_ACTION);
  }

  protected Counter getCacheRefreshFailureMetric(String cacheDataNameTag) {
    return getCacheFailureMetric(cacheDataNameTag, CACHE_REFRESH_ACTION);
  }

  private Counter getCacheFailureMetric(String cacheDataNameTag, String action) {
    return meterRegistry.counter(
        CACHE_FAILURE_METRIC, "action", action, "cacheType", cacheDataNameTag);
  }
}
