package com.backend.ticker.models.provider;

import java.util.Map;

public record FiatRatesResponse(String base, Map<String, Double> rates) {
}
