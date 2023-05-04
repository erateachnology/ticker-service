package com.eucalyptuslabs.backend.common.provider.precaching;

import com.eucalyptuslabs.backend.common.provider.BaseProvidersCacheService;
import org.springframework.context.ApplicationEvent;

/**
 * The class extending Spring's Event class, representing information about an updated cache. The
 * source parameter is the discriminator of the providers cache service (see {@link
 * BaseProvidersCacheService}) which publishes the event. <br>
 * Such an event with the source of cache service should be interpreted as the information that the
 * cached service has been updated. The services dependent on the source cache should act
 * accordingly. Usually, those services are subclasses of {@link BasePreCachingService}, which
 * re-calculate and pre-cache their values using the updated cache values.
 */
public class TriggerPreCachingEvent extends ApplicationEvent {

  public TriggerPreCachingEvent(String cacheDiscriminator) {
    super(cacheDiscriminator);
  }
}
