package com.backend.ticker.models.provider;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CoinGeckoCryptoPricesResponse extends HashMap<String, Map<String, BigDecimal>> {

  public Map<String, String> getPrices(String fiatCurrency, Map<String, String> map) {
    return entrySet().stream()
        .filter(e -> e.getValue().containsKey(fiatCurrency))
        .collect(
            Collectors.toMap(
                e -> map.get(e.getKey()), e -> e.getValue().get(fiatCurrency).toPlainString()));
  }

  public Map<String, Double> get24hPriceChanges(String fiatCurrency, Map<String, String> map) {
    String key = fiatCurrency + "_24h_change";
    return entrySet().stream()
        .filter(e -> e.getValue().containsKey(key) && e.getValue().get(key) != null)
        .collect(
            Collectors.toMap(e -> map.get(e.getKey()), e -> e.getValue().get(key).doubleValue()));
  }
}
