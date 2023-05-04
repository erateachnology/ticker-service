package com.eucalyptuslabs.backend.common.model.config.partner;

import com.eucalyptuslabs.backend.common.model.config.CryptoConfigMap;
import com.eucalyptuslabs.backend.common.model.config.partner.PartnerConfig.PartnerConfigEntry;

public class PartnerConfigMap extends CryptoConfigMap<PartnerConfig, PartnerConfigEntry> {

  public PartnerConfigMap(PartnerConfig partnerConfig) {
    super(partnerConfig);
  }

  public String getPartnerSymbol(String id) {
    return getConfigEntry(id).partnerSymbol();
  }
}
