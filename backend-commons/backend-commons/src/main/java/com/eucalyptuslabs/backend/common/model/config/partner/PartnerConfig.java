package com.eucalyptuslabs.backend.common.model.config.partner;

import com.eucalyptuslabs.backend.common.model.config.ConfigEntry;
import com.eucalyptuslabs.backend.common.model.config.CryptoConfig;

import java.util.List;

import static com.eucalyptuslabs.backend.common.model.config.partner.PartnerConfig.PartnerConfigEntry;

public record PartnerConfig(List<PartnerConfigEntry> cryptocurrencies)
    implements CryptoConfig<PartnerConfigEntry> {

  public record PartnerConfigEntry(String eucId, String partnerSymbol) implements ConfigEntry {}
}
