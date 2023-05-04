package com.eucalyptuslabs.backend.common.model.config.provider;

import com.eucalyptuslabs.backend.common.model.config.CryptoConfigMap;
import com.eucalyptuslabs.backend.common.model.config.provider.ProvidersConfig.ProvidersConfigEntry;

public class ProvidersConfigMap extends CryptoConfigMap<ProvidersConfig, ProvidersConfigEntry> {

  public ProvidersConfigMap(ProvidersConfig feesConfig) {
    super(feesConfig);
  }
}
