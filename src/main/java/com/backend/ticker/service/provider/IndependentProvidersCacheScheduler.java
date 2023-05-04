package com.backend.ticker.service.provider;

import com.eucalyptuslabs.backend.common.provider.AutoProvidersCacheScheduler;
import com.backend.ticker.service.provider.config.ConfigurationCacheService;
import com.backend.ticker.service.provider.rates.crypto.KdSwap.KdSwapRatesCacheService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndependentProvidersCacheScheduler extends AutoProvidersCacheScheduler {

  public IndependentProvidersCacheScheduler(ConfigurationCacheService configurationCacheService,
                                            KdSwapRatesCacheService kdSwapRatesCacheService) {
    super(List.of(configurationCacheService, kdSwapRatesCacheService));
  }
}
