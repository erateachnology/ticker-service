package com.backend.ticker.service.provider.rates.crypto.coingecko;

import com.backend.ticker.models.provider.CoinGeckoCryptoPricesResponse;
import com.eucalyptuslabs.backend.common.provider.BaseProvidersCacheService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CryptoPricesCoinGeckoCacheService
    extends BaseProvidersCacheService<CoinGeckoCryptoPricesResponse> {

  private static final String DATA_NAME = "CryptoPricesCache";


  @Value("${crypto-rates.providers-cache.refresh.seconds}")
  private int configRefreshIntervalSeconds;

  @Value("${crypto-rates.providers-cache.enabled:true}")
  private boolean enabled;

  public CryptoPricesCoinGeckoCacheService(CryptoPricesCoinGeckoProviderService cryptoRatesCmcProviderService) {
    super(DATA_NAME, List.of(cryptoRatesCmcProviderService));
  }

  @Override
  public int getRefreshIntervalSeconds() {
    return configRefreshIntervalSeconds;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }
}
