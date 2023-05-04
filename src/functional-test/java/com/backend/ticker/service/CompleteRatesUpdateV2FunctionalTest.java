package com.backend.ticker.service;

import com.backend.ticker.MockitoSupport;
import com.backend.ticker.service.persistence.PriceRepository;
import com.backend.ticker.service.price.PriceService;
import com.backend.ticker.service.provider.IndependentProvidersCacheScheduler;
import com.backend.ticker.service.provider.config.ConfigurationCacheService;
import com.backend.ticker.service.provider.rates.crypto.KdSwap.KdSwapRatesCacheService;
import com.backend.ticker.service.provider.rates.crypto.coingecko.CryptoPricesCoinGeckoCacheService;
import com.eucalyptuslabs.backend.common.model.exception.DataNotAvailableException;
import com.eucalyptuslabs.backend.common.model.exception.SymbolNotFoundException;
import com.eucalyptuslabs.backend.common.model.ticker.response.CompleteRatesResponse;
import com.eucalyptuslabs.backend.common.service.TimeService;
import com.eucalyptuslabs.functional.backend.common.provider.BaseFunctionalTest;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static com.eucalyptuslabs.backend.common.util.AssertionUtils.loadObject;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompleteRatesUpdateV2FunctionalTest extends BaseFunctionalTest {
    @Autowired
    private MockitoSupport mockitoSupport;

    @MockBean
    private IndependentProvidersCacheScheduler providerCacheScheduler;
    @SpyBean
    private ConfigurationCacheService configurationCacheService;
    @SpyBean
    private CryptoPricesCoinGeckoCacheService cryptoPricesCoinGeckoCacheService;

    @SpyBean
    private KdSwapRatesCacheService kdSwapRatesCacheService;

    @Autowired
    private CompleteRatesServiceV1 completeRatesServiceV1;
    @Autowired
    private CompleteRatesServiceV2 completeRatesServiceV2;

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private PriceService priceService;
    @SpyBean
    private TimeService timeService;

    public CompleteRatesUpdateV2FunctionalTest() throws IOException {
    }

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
    @Transactional
    @DisplayName("Should invoke CoinGecko endpoint and return mapped results when currency is USD")
    public void completeCryptoUsdSuccessFunctionalityTest()
            throws IOException, DataNotAvailableException, SymbolNotFoundException {

        mockitoSupport.mockConfigCacheSuccess();
        mockitoSupport.mockCryptoPricesCacheSucccess();
        mockitoSupport.mockKdSwapRatesCacheDataSuccess();

        CompleteRatesResponse actualResponse = completeRatesServiceV2.updateCompleteRates("USD");

        mockitoSupport.verifyConfigCacheInvocation();
        mockitoSupport.verifyCoinGeckoCacheService();
        mockitoSupport.verifyKdSwapCacheService();

        assertEquals(getExpectedOutputUsdSuccessResponseBody(), actualResponse);

        logsInterceptor.assertLogMessagesProduced(
                emptyList(),
                List.of(
                        "Invoked updateCompleteRates with fiatSymbol USD",
                        "Retrieved reversed configuration map: {kaddex=kaddex@kadena, kadena=kadena}",
                        "Retrieved 24h price changes for currency symbol usd: {kaddex@kadena=-12.506323232704695, kadena=-6.1000975049840065}",
                        "Generated complete rates response: CompleteRatesResponse[cryptoRates=[CryptoResponseEntry[id=kapybara@kadena, formattedPrice=0.000000024773671611000002, percentChange=null], CryptoResponseEntry[id=kaddex@kadena, formattedPrice=0.0262215, percentChange=-12.506323232704695], CryptoResponseEntry[id=kadena, formattedPrice=0.834327, percentChange=-6.1000975049840065]]]"),
                List.of(),
                List.of());
    }

    private CompleteRatesResponse getExpectedOutputUsdSuccessResponseBody() throws IOException {
        return loadObject(
                CompleteRatesResponse.class,
                "dto/expected/success/complete-rates/complete-rates-success-usd-response-v2.json");
    }

    @Test
    @DisplayName("Getting the closest price")
    public void testGetClosestPrice() throws IOException, DataNotAvailableException, SymbolNotFoundException {
        ZonedDateTime currentTime = ZonedDateTime.of(2023, 4, 30, 12, 0, 0, 0, ZoneOffset.UTC);
        Mockito.when(timeService.getCurrentZonedDateTime()).thenReturn(currentTime);

        // Set up test data
        String fiatSymbolUSD = "USD";

        // Setup test data
        Map<ZonedDateTime, CompleteRatesResponse.CryptoResponseEntry> cryptoResponseEntries = Map.of(
                currentTime.minusHours(1), new CompleteRatesResponse.CryptoResponseEntry("kapybara@kadena", "1.1", 0.0),
                currentTime.minusHours(2), new CompleteRatesResponse.CryptoResponseEntry("kapybara@kadena", "1.2", 0.0),
                currentTime.minusHours(3), new CompleteRatesResponse.CryptoResponseEntry("kapybara@kadena", "1.3", 0.0),
                currentTime.minusHours(4), new CompleteRatesResponse.CryptoResponseEntry("kapybara@kadena", "1.4", 0.0),
                currentTime.minusHours(5), new CompleteRatesResponse.CryptoResponseEntry("kapybara@kadena", "1.5", 0.0),
                currentTime.minusHours(6), new CompleteRatesResponse.CryptoResponseEntry("kapybara@kadena", "1.6", 0.0)
        );

        // Save test data into the DB
        for (Map.Entry<ZonedDateTime, CompleteRatesResponse.CryptoResponseEntry> entry : cryptoResponseEntries.entrySet()) {
            priceService.saveHistoricalPrices(List.of(entry.getValue()), fiatSymbolUSD, entry.getKey());
        }

        // Test price changes
        double priceChange = completeRatesServiceV2.calculatePriceChange("kapybara@kadena", "1", fiatSymbolUSD);

        // Verify the results expected percent change = (1.0-1.4)/1.4 * 100% = -28.57142857142857%,
        // round this based on scale of 4 and round up RoundingMode.HALF_UP
        Assertions.assertEquals(-28.57, priceChange);

    }
}
