package com.backend.ticker.service.provider;

import com.eucalyptuslabs.backend.common.provider.BaseProvidersCacheScheduler;
import com.backend.ticker.service.provider.rates.crypto.coingecko.CryptoPricesCoinGeckoCacheService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigurationDependentProvidersCacheScheduler extends BaseProvidersCacheScheduler {

  public ConfigurationDependentProvidersCacheScheduler(CryptoPricesCoinGeckoCacheService cryptoPricesCoinGeckoCacheService) {
    super(List.of(cryptoPricesCoinGeckoCacheService));
  }
}
