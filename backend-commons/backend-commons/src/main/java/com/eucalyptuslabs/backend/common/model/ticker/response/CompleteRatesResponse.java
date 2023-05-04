package com.eucalyptuslabs.backend.common.model.ticker.response;

import java.util.List;

public record CompleteRatesResponse(List<CryptoResponseEntry> cryptoRates) {

  public record CryptoResponseEntry(String id, String formattedPrice, Double percentChange) {}
}
