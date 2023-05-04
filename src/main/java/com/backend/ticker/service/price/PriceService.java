package com.backend.ticker.service.price;

import com.eucalyptuslabs.backend.common.model.ticker.response.CompleteRatesResponse;
import java.time.ZonedDateTime;
import java.util.List;

public interface PriceService {
    void saveHistoricalPrices(List<CompleteRatesResponse.CryptoResponseEntry> cryptoResponseEntries, String fiatSymbol, ZonedDateTime createdOn);
}
