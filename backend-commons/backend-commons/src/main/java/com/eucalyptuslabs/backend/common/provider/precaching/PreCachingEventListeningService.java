package com.eucalyptuslabs.backend.common.provider.precaching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The service being the listener for the Spring's Events. It handles the events of type {@link
 * TriggerPreCachingEvent} and dispatches them to all registered pre-caching services (see {@link
 * BasePreCachingService}. It is the responsibility of pre-caching service to determine weather it
 * should react on the particular event.
 */
@Service
public class PreCachingEventListeningService {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired(required = false)
  private List<BasePreCachingService> preCachingServices = List.of();

  @EventListener
  public void listenToPreCachingEvent(TriggerPreCachingEvent event) {
    preCachingServices.forEach(preCachingService -> preCachingService.preCacheServiceValues(event));
  }
}
