package com.eucalyptuslabs.backend.common.provider.precaching;

import com.eucalyptuslabs.backend.common.provider.BaseProvidersCacheService;
import com.eucalyptuslabs.backend.common.provider.metrics.MetricDiscriminator;
import com.eucalyptuslabs.backend.common.provider.metrics.ProviderMetricService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The base class for value pre-caching service. Its main parameter is the collection of providers
 * cache services which are required for this pre-caching service to be able to calculate downstream
 * data. <br>
 * The listeners of Spring Events {@link PreCachingEventListeningService} notifies this service with
 * an event by invoking the method {@link #preCacheServiceValues(TriggerPreCachingEvent)}, published
 * by the providers cache service upon successful cache update (see {@link
 * BaseProvidersCacheService}). The event holds the publishing cache discriminator. If the
 * discriminator matches one of the required cache services (see {@link
 * #isRelevantCacheCondition(TriggerPreCachingEvent)}) it means that the value in the cache of
 * interest has been updated and the downstream data can get re-calculated and pre-cached. <br>
 * Before service values updating happens, additionally the initialization status of all required
 * caches is checked (see {@link #areRequiredCachesInitializedCondition()}). If all caches are
 * initialized, the service may proceed with the updating and pre-caching of the new values (see
 * {@link #updateServiceValues()}).
 */
public abstract class BasePreCachingService implements MetricDiscriminator {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private ProviderMetricService providerMetricService;

  private final AtomicBoolean isInitialized = new AtomicBoolean(false);
  private final String dataName;
  private final List<BaseProvidersCacheService<?>> requiredCaches;

  protected BasePreCachingService(
      String dataName, List<BaseProvidersCacheService<?>> requiredCaches) {
    this.dataName = dataName;
    this.requiredCaches = requiredCaches;
  }

  protected abstract void updateServiceValues() throws Exception;

  public void preCacheServiceValues(TriggerPreCachingEvent triggerPreCachingEvent) {
    boolean relevantCacheCondition = isRelevantCacheCondition(triggerPreCachingEvent);
    if (!relevantCacheCondition) {
      return;
    }
    boolean cacheInitializedCondition = areRequiredCachesInitializedCondition();
    if (!cacheInitializedCondition) {
      return;
    }
    try {
      updateServiceValues();
      isInitialized.set(true);
    } catch (Exception e) {
      log.warn("Service pre-caching failed", e);
      providerMetricService.increaseCacheRefreshFailureMetric(dataName);
    }
  }

  public boolean isEnabled() {
    return requiredCaches.stream().allMatch(BaseProvidersCacheService::isEnabled);
  }

  public boolean isInitialized() {
    return isInitialized.get();
  }

  private boolean areRequiredCachesInitializedCondition() {
    return requiredCaches.stream().allMatch(BaseProvidersCacheService::isInitialized);
  }

  private boolean isRelevantCacheCondition(TriggerPreCachingEvent triggerPreCachingEvent) {
    return requiredCaches.stream()
        .anyMatch(
            cacheService ->
                cacheService.getDiscriminator().equals(triggerPreCachingEvent.getSource()));
  }

  public String getDiscriminator() {
    return dataName;
  }

  public List<BaseProvidersCacheService<?>> getRequiredCaches() {
    return requiredCaches;
  }

  @Override
  public String toString() {
    return dataName;
  }
}
