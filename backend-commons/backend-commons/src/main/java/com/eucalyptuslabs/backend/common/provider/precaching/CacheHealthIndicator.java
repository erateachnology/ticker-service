package com.eucalyptuslabs.backend.common.provider.precaching;

import com.eucalyptuslabs.backend.common.provider.BaseProvidersCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The service implementing Spring's {@link HealthIndicator} interface, which hooks in the Spring
 * Actuator's readiness endpoint. <br>
 * It reports the overall application status based on the status of all enabled caches (see {@link
 * BaseProvidersCacheService}). If the all of currently enabled cache services are initialized the
 * reported application status is 'up'. If at least one of the enabled caches in not ready, the
 * status is 'down'.
 */
@Component
public class CacheHealthIndicator implements HealthIndicator {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired(required = false)
  private List<BasePreCachingService> preCachingServices = List.of();

  @Override
  public Health health() {
    List<BasePreCachingService> notInitializedServices =
        preCachingServices.stream()
            .filter(BasePreCachingService::isEnabled)
            .filter(preCachingService -> !preCachingService.isInitialized())
            .toList();
    boolean allCachesInitialized = notInitializedServices.isEmpty();
    if (allCachesInitialized) {
      return Health.up().build();
    } else {
      logger.warn(
          "Caches not initialized: {}",
          notInitializedServices.stream().map(BasePreCachingService::toString).toList());
      return Health.outOfService().build();
    }
  }
}
