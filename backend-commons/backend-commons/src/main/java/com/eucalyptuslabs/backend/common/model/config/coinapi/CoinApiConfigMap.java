package com.eucalyptuslabs.backend.common.model.config.coinapi;

import com.eucalyptuslabs.backend.common.model.config.CryptoConfigMap;
import com.eucalyptuslabs.backend.common.model.config.coinapi.CoinApiConfig.CoinApiConfigEntry;

public class CoinApiConfigMap extends CryptoConfigMap<CoinApiConfig, CoinApiConfigEntry> {

  public CoinApiConfigMap(CoinApiConfig coinApiConfig) {
    super(coinApiConfig);
  }
}
