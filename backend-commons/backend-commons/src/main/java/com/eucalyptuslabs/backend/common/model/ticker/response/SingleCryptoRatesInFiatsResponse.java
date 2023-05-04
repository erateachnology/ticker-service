package com.eucalyptuslabs.backend.common.model.ticker.response;

import java.util.List;

public record SingleCryptoRatesInFiatsResponse(List<FiatRateEntry> entries) {

  public record FiatRateEntry(String currencySymbol, String formattedPrice) {}
}
