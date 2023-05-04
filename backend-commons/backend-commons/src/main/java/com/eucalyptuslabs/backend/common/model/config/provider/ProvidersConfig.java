package com.eucalyptuslabs.backend.common.model.config.provider;

import com.eucalyptuslabs.backend.common.model.config.ConfigEntry;
import com.eucalyptuslabs.backend.common.model.config.CryptoConfig;

import java.util.List;

import static com.eucalyptuslabs.backend.common.model.config.provider.ProvidersConfig.ProvidersConfigEntry;

public record ProvidersConfig(List<ProvidersConfigEntry> cryptocurrencies)
    implements CryptoConfig<ProvidersConfigEntry> {

  public record ProvidersConfigEntry(
      String eucId, int refreshIntervalSeconds, List<String> discriminators) implements ConfigEntry {

    public enum ProviderCategory {
      FEE
    }
  }
}
