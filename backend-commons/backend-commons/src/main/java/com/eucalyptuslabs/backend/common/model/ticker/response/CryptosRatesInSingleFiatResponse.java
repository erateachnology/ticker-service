package com.eucalyptuslabs.backend.common.model.ticker.response;

import java.util.List;

public record CryptosRatesInSingleFiatResponse(List<CryptoRateEntry> entries) {

  public record CryptoRateEntry(String id, String formattedPrice) {}
}
