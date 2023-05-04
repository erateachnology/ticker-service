package com.backend.ticker.service.provider.config;

import com.backend.ticker.service.provider.config.mds.ConfigMdsProviderService;
import com.eucalyptuslabs.backend.common.model.config.partner.PartnerConfig;
import com.eucalyptuslabs.backend.common.model.exception.DataNotAvailableException;
import com.eucalyptuslabs.backend.common.provider.BaseProvidersCacheService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConfigurationCacheService extends BaseProvidersCacheService<PartnerConfig> {

  private static final String DATA_NAME = "ConfigurationCache";

  @Value("${config.providers-cache.refresh.seconds}")
  private int configRefreshIntervalSeconds;

  @Value("${config.providers-cache.enabled:true}")
  private boolean enabled;

  public ConfigurationCacheService(ConfigMdsProviderService configMdsProviderService) {
    super(DATA_NAME, List.of(configMdsProviderService));
  }

  @Override
  public int getRefreshIntervalSeconds() {
    return configRefreshIntervalSeconds;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  public Map<String, String> getReversedConfiguration() throws DataNotAvailableException {
    return getCachedData().cryptocurrencies().stream()
        .collect(
            Collectors.toMap(
                PartnerConfig.PartnerConfigEntry::partnerSymbol,
                PartnerConfig.PartnerConfigEntry::eucId));
  }
}
