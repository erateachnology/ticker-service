package com.backend.ticker.service.precaching;

import com.backend.ticker.service.provider.ConfigurationDependentProvidersCacheScheduler;
import com.backend.ticker.service.provider.config.ConfigurationCacheService;
import com.eucalyptuslabs.backend.common.provider.precaching.BasePreCachingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigurationPreCachingService extends BasePreCachingService {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

  private static final String DATA_NAME = "Configuration-Cache";

  @Autowired
  private ConfigurationDependentProvidersCacheScheduler
      configurationDependentProvidersCacheScheduler;

  public ConfigurationPreCachingService(ConfigurationCacheService configurationCacheService) {
    super(DATA_NAME, List.of(configurationCacheService));
  }

  protected void updateServiceValues() {
    configurationDependentProvidersCacheScheduler.rescheduleTasks();
  }
}
