package com.backend.ticker.service;

import com.backend.ticker.models.entity.persistence.Price;
import com.backend.ticker.service.persistence.PriceRepository;
import com.backend.ticker.service.price.impl.PriceServiceImpl;
import com.eucalyptuslabs.backend.common.model.ticker.response.CompleteRatesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import static org.mockito.Mockito.*;

public class PriceServiceUnitTest {
    @InjectMocks
    private PriceServiceImpl priceService;

    @Mock
    private PriceRepository priceRepository;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSaveHistoricalPrices() {
        // Set up test data
        String fiatSymbol = "USD";
        ZonedDateTime createdOn = ZonedDateTime.now();
        List<CompleteRatesResponse.CryptoResponseEntry> cryptoResponseEntries = Arrays.asList(
                new CompleteRatesResponse.CryptoResponseEntry("kapybara@kadena", "0.000000027155170826000003",0.0),
                new CompleteRatesResponse.CryptoResponseEntry("jodie_inu@kadena", "0.0000021341033275180004",0.0)
        );
        List<Price> prices = Arrays.asList(
                new Price(null, "kapybara@kadena", fiatSymbol, createdOn, "0.000000027155170826000003"),
                new Price(null, "jodie_inu@kadena", fiatSymbol, createdOn, "0.0000021341033275180004")
        );

        // Call the method being tested
        priceService.saveHistoricalPrices(cryptoResponseEntries, fiatSymbol, createdOn);

        // Verify that the repository's saveAll method was called with the expected prices
        verify(priceRepository,times(1)).saveAll(prices);
    }
}

