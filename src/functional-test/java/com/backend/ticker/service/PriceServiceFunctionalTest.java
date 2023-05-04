package com.backend.ticker.service;

import com.backend.ticker.MockitoSupport;
import com.backend.ticker.models.entity.persistence.Price;
import com.eucalyptuslabs.backend.common.model.exception.DataNotAvailableException;
import com.eucalyptuslabs.backend.common.model.exception.SymbolNotFoundException;
import com.eucalyptuslabs.backend.common.model.ticker.response.CompleteRatesResponse;
import com.backend.ticker.service.persistence.PriceRepository;
import com.backend.ticker.service.price.PriceService;
import com.backend.ticker.service.provider.IndependentProvidersCacheScheduler;
import com.eucalyptuslabs.functional.backend.common.provider.BaseFunctionalTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

public class PriceServiceFunctionalTest extends BaseFunctionalTest {
    
    @Autowired
    private PriceService priceService;
    @Autowired
    private MockitoSupport mockitoSupport;
    
    @Autowired
    private PriceRepository priceRepository;

    @MockBean
    private IndependentProvidersCacheScheduler providerCacheScheduler;

    @Autowired
    private CompleteRatesServiceV2 completeRatesServiceV2;
    

    public PriceServiceFunctionalTest() throws IOException {}

    @BeforeEach
    public void setUpMocks() throws Exception {
        super.setUpMocks();
    }

    @AfterEach
    public void verifyMocks() {
        mockitoSupport.verifyNoMoreInteractionsV2();
        super.verifyMocks();
    }

    @Test
    @DisplayName("Adding historical prices to the database")
    @Transactional
    public void savePricesFunctionalityTest()
            throws IOException, DataNotAvailableException, SymbolNotFoundException {
        String fiatSymbol = "USD";
        List<CompleteRatesResponse.CryptoResponseEntry> cryptoResponseEntries = Arrays.asList(
                new CompleteRatesResponse.CryptoResponseEntry("kapybara@kadena", "0.000000027155170826000003",0.0),
                new CompleteRatesResponse.CryptoResponseEntry("jodie_inu@kadena", "0.0000021341033275180004",0.0)
        );
        ZonedDateTime createdOn = ZonedDateTime.of(2023,03,27, 0, 0, 0, 0, ZoneId.of("UTC"));
        priceService.saveHistoricalPrices(cryptoResponseEntries, fiatSymbol, createdOn);
        List<Price> actualResults = priceRepository.findAll();
        Assertions.assertTrue(actualResults.size() > 0);
        Assertions.assertTrue(actualResults.stream().anyMatch(price -> price.getEucId().equals("kapybara@kadena")));
    }


}
