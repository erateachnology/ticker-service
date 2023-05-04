package com.eucalyptuslabs.backend.common.model.config.coinapi;

import com.eucalyptuslabs.backend.common.model.config.ConfigEntry;
import com.eucalyptuslabs.backend.common.model.config.CryptoConfig;

import java.util.List;

import static com.eucalyptuslabs.backend.common.model.config.coinapi.CoinApiConfig.CoinApiConfigEntry;

public record CoinApiConfig(List<CoinApiConfigEntry> cryptocurrencies)
    implements CryptoConfig<CoinApiConfigEntry> {

  public record CoinApiConfigEntry(
      String eucId,
      int decimals,
      String tatumPartnerSymbol,
      String moralisPartnerSymbol,
      String web3RpcUrl,
      Routes routes)
      implements ConfigEntry {

    public record Routes(
        String height,
        String balance,
        String nonce,
        String transaction,
        String txHistory,
        String broadcastTx) {}
  }
}
