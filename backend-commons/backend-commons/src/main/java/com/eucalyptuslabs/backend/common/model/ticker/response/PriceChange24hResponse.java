package com.eucalyptuslabs.backend.common.model.ticker.response;

import java.util.List;

public record PriceChange24hResponse(List<PriceChange24hEntry> entries) {

  public record PriceChange24hEntry(String id, Double percentChange) {}
}
