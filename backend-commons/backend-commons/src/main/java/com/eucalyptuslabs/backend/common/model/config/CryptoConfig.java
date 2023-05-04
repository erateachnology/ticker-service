package com.eucalyptuslabs.backend.common.model.config;

import java.util.List;

public interface CryptoConfig<T extends ConfigEntry> {

  List<T> cryptocurrencies();
}
