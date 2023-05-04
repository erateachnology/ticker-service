package com.eucalyptuslabs.backend.common.provider;

import com.eucalyptuslabs.backend.common.model.exception.DataNotAvailableException;
import com.eucalyptuslabs.backend.common.provider.metrics.BaseProviderMetricService;
import com.eucalyptuslabs.backend.common.provider.metrics.MetricDiscriminator;
import com.eucalyptuslabs.backend.common.provider.metrics.ProviderMetricInitializer;
import com.eucalyptuslabs.backend.common.provider.metrics.ProviderMetricService;
import com.eucalyptuslabs.backend.common.provider.precaching.BasePreCachingService;
import com.eucalyptuslabs.backend.common.provider.precaching.PreCachingEventListeningService;
import com.eucalyptuslabs.backend.common.provider.precaching.TriggerPreCachingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The base class for providers cache. A cache serves the data of given type. <br>
 * The data is obtained from one or more providers {@link BaseProviderService}. The providers are
 * used in the order - if the first one fails, then the next one is used. A provider fetches data
 * from 3rd party service and transforms it to the cache target data format. <br>
 * The class works in conjunction with {@link ProvidersCacheScheduledTask} which controls cache
 * initialization and refreshing in the context of scheduling and timely execution. <br>
 * The class implements {@link MetricDiscriminator} interface, which allows {@link
 * ProviderMetricInitializer} to reset cache metrics to zero upon application startup.
 *
 * @param <DATA_TYPE> the target cache data format
 */
public abstract class BaseProvidersCacheService<DATA_TYPE> implements MetricDiscriminator {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired protected ApplicationEventPublisher applicationEventPublisher;

  @Autowired protected ProviderMetricService providerMetricService;

  protected final String dataDiscriminator;
  protected final List<? extends BaseProviderService<DATA_TYPE>> providerServices;

  protected AtomicReference<DATA_TYPE> cachedValue = new AtomicReference<>(null);

  /**
   * The class constructor which requires two arguments.
   *
   * @param dataDiscriminator the data name of this providers cache service
   * @param providerServices the list of data providers
   */
  public BaseProvidersCacheService(
      String dataDiscriminator, List<? extends BaseProviderService<DATA_TYPE>> providerServices) {
    this.dataDiscriminator = dataDiscriminator;
    this.providerServices = providerServices;
  }

  /**
   * The method should return the length of the time interval given in seconds. The cache will get
   * refreshed (after it's initialized) every given time interval.
   *
   * @return an integer representing seconds of the cache refreshing time interval
   */
  public abstract int getRefreshIntervalSeconds();

  /**
   * The method should return cache enablement status. When cache is enabled, it is being scheduled
   * for initialization and refreshing. By default, the cache is always enabled. To be overridden by
   * subclass with specific enablement logic.
   *
   * @return a boolean value indicating whether cache is enabled
   */
  public boolean isEnabled() {
    return true;
  }

  /**
   * The method should return cache providers in the preferred order in which they should be used
   * during cache refreshing. The subsequent providers given by the list are used to get the fresh
   * data until the first provider successfully delivers the data. <br>
   * By default, providers are returned by the same order as they are passed though the constructor.
   * To be overridden by subclass with specific ordering logic.
   *
   * @return a list of ordered providers
   */
  protected List<? extends BaseProviderService<DATA_TYPE>> getProviderServicesOrdered() {
    return providerServices;
  }

  /**
   * The method which initializes the cache. The cache initialization is very similar to the cache
   * refresh - in principle in uses providers in the order to get the data which the cache can serve
   * (see {@link #callProvider()}). <br>
   * The main reason the initialization is separate from the refreshing method is that the
   * initialization is repeated more frequently in case of failures. The cache needs to get the
   * initial value fast, but the refreshing should occur in the intervals. There are also the
   * following differences:
   *
   * <ul>
   *   <li>initialization method will successfully fetch data only once
   *   <li>log messages inform about initialization state than refreshing
   *   <li>failure metrics are gathered with the tag {@code init}, see {@link
   *       BaseProviderMetricService#increaseCacheInitFailureMetric(String)}
   * </ul>
   *
   * After successful initialization, method will skip data fetching in case of further invocations.
   * <br>
   * Once data is fetched, the cache is considered as initialized, see {@link #isInitialized()}.
   * <br>
   * This method is synchronized meaning that cache initialization can't be executed simultaneously
   * by the two concurrent threads.
   *
   * @return a boolean value indicating weather cache initialization was successful
   */
  protected final synchronized boolean initCache() {
    if (isInitialized()) {
      return true;
    }
    log.info("Initializing cache '{}'", dataDiscriminator);
    Optional<DATA_TYPE> firstSuccessful = callProvidersInOrder();
    if (firstSuccessful.isPresent()) {
      log.debug("Cache initialized '{}'", dataDiscriminator);
      cachedValue.set(firstSuccessful.get());
      publishPreCachingEvent();
      return true;
    } else {
      log.warn("Cache initialization failed '{}'", dataDiscriminator);
      providerMetricService.increaseCacheInitFailureMetric(dataDiscriminator);
      return false;
    }
  }

  /**
   * The method which refreshes the cache. The cache refreshing is a process of using one provider
   * at the time in the order specified by the method {@link #getProviderServicesOrdered()} until a
   * provider successfully fetches the data from the 3rd party service it connects to (see {@link
   * #callProvidersInOrder()}. After obtaining the new value, previous cache value is replaced with
   * it and served by the cache. Every successful refresh publishes {@link TriggerPreCachingEvent},
   * which could be processed by the listeners (usually @{@link BasePreCachingService}). <br>
   * Cache may be refreshed many times and the process is usually controlled by the {@link
   * ProvidersCacheScheduledTask}. <br>
   * Metrics are gathered per the cache refreshing failure and a single provider failure (see {@link
   * BaseProviderMetricService#increaseCacheRefreshFailureMetric}). There is no repeating in case of
   * failure and the existing cache value is served. The cache refreshing should get attempted with
   * the next iteration.<br>
   * This method is synchronized meaning that cache refreshing can't be executed simultaneously by
   * the two concurrent threads.
   */
  protected final synchronized void callProvider() {
    log.debug("Refreshing cache '{}'", dataDiscriminator);
    Optional<DATA_TYPE> firstSuccessful = callProvidersInOrder();
    if (firstSuccessful.isPresent()) {
      log.debug("Cache refreshed '{}'", dataDiscriminator);
      cachedValue.set(firstSuccessful.get());
      publishPreCachingEvent();
    } else {
      log.warn("cache refreshing failed '{}'", dataDiscriminator);
      providerMetricService.increaseCacheRefreshFailureMetric(dataDiscriminator);
    }
  }

  /**
   * The method indicating weather the cache has been initialized. When cache is initialized it
   * contains the data from one of the providers which can be served through {@link
   * #getCachedData()} method. <br>
   * The method is not synchronized meaning that it will not block waiting on any operation (like
   * initialization or refreshing) to complete.
   *
   * @return a boolean value indicating initialized status
   */
  public boolean isInitialized() {
    return cachedValue.get() != null;
  }

  /**
   * The method returns cached data. The data is obtained from providers either through {@link
   * #initCache()} or {@link #callProvider()} methods. <br>
   * The method is not synchronized meaning that it will not block waiting on any operation (like
   * initialization or refreshing) to complete. The cached value will get returned instantly.
   *
   * @return the most recently cached data
   * @throws DataNotAvailableException if data is requested before cache gets initialized
   */
  public DATA_TYPE getCachedData() throws DataNotAvailableException {
    DATA_TYPE cachedData = cachedValue.get();
    if (cachedData == null) {
      log.warn("No data in the cache '{}'", dataDiscriminator);
      throw new DataNotAvailableException(dataDiscriminator);
    } else {
      return cachedData;
    }
  }

  /**
   * The method iterates over the ordered providers (see {@link #getProviderServicesOrdered()}) and
   * tries to get the successful execution of the {@link #callProvider(BaseProviderService)} method.
   * Once the invocation is successful the obtained value is returned.
   *
   * @return a value obtained from the first successful provider invocation
   */
  private Optional<DATA_TYPE> callProvidersInOrder() {
    return getProviderServicesOrdered().stream()
        .map(this::callProvider)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  /**
   * The method takes a provider as a parameter and performs its data fetch (see {@link
   * BaseProviderService#fetchFreshData()}). <br>
   * In case of the provider failure, the failure metric is being recorded for the provider using
   * its discriminator (see {@link BaseProviderService#getDiscriminator()}).
   *
   * @param providerService which should be used to get the fresh data
   * @return an optional containing the fresh data or an empty optional if the provider call failed
   */
  private Optional<DATA_TYPE> callProvider(BaseProviderService<DATA_TYPE> providerService) {
    try {
      log.debug("Updating cache '{}' - provider '{}'", dataDiscriminator, providerService);
      return Optional.of(providerService.fetchFreshData());
    } catch (Exception exception) {
      log.warn(
          "Error occurred during scheduled cache update '{}' - provider '{}'",
          dataDiscriminator,
          providerService,
          exception);
      providerMetricService.increaseCacheRefreshFailureMetric(providerService.getDiscriminator());
      return Optional.empty();
    }
  }

  /**
   * The method publishes an {@link TriggerPreCachingEvent} event which is routed by Spring Events
   * to the corresponding event listeners. The default listener for this type of event is {@link
   * PreCachingEventListeningService}. <br>
   * The purpose of publishing the event is to let know the service depending on the cache, that a
   * new data is present and the service values can get recalculated and cached for future usage. An
   * event is created with the publishing the cache's discriminator as an event source. This way,
   * when an event is routed to the subclasses of {@link BasePreCachingService}, they can recognize
   * that their cache of interest has been updated (see {@link
   * BasePreCachingService#getRequiredCaches()}).
   */
  private void publishPreCachingEvent() {
    applicationEventPublisher.publishEvent(new TriggerPreCachingEvent(dataDiscriminator));
  }

  /**
   * The method is required by the interface {@link MetricDiscriminator}. It returns the cache data
   * name (discriminator) used by the {@link ProviderMetricInitializer}.
   *
   * @return a string specifying the cache data name (aka discriminator).
   */
  @Override
  public String getDiscriminator() {
    return dataDiscriminator;
  }

  /**
   * The method returns the string representation of the cache object as its data discriminator. It
   * is mainly to facilitate logging.
   *
   * @return a string containing cache discriminator
   */
  @Override
  public String toString() {
    return dataDiscriminator;
  }
}
