package com.eucalyptuslabs.backend.common.model.config;

import java.util.Map;
import java.util.stream.Collectors;

public class CryptoConfigMap<CONFIG extends CryptoConfig<ENTRY>, ENTRY extends ConfigEntry> {

  private final Map<String, ENTRY> idToConfigEntry;

  public CryptoConfigMap(CONFIG partnerConfig) {
    idToConfigEntry =
        partnerConfig.cryptocurrencies().stream()
            .collect(Collectors.toMap(ConfigEntry::eucId, e -> e));
  }

  public ENTRY getConfigEntry(String id) {
    return idToConfigEntry.get(id);
  }
}
