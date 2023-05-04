package com.backend.ticker.caching.service.v2;

import com.backend.ticker.MockitoSupport;
import com.backend.ticker.service.CompleteRatesServiceV2;
import com.backend.ticker.service.provider.IndependentProvidersCacheScheduler;
import com.backend.ticker.service.provider.config.ConfigurationCacheService;
import com.backend.ticker.service.provider.rates.crypto.KdSwap.KdSwapRatesCacheService;
import com.backend.ticker.service.provider.rates.crypto.coingecko.CryptoPricesCoinGeckoCacheService;
import com.eucalyptuslabs.functional.backend.common.provider.BaseFunctionalTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.integration.backend.ticker.v2.TickerApiIntegrationTestV2.invokeCompleteRatesEndpoint;
import static java.util.Collections.emptyList;

public class CompleteRatesServiceV2CachingFunctionalTest extends BaseFunctionalTest {
    @MockBean
    private IndependentProvidersCacheScheduler providerCacheScheduler;
    @SpyBean
    private ConfigurationCacheService configurationCacheService;
    @SpyBean
    private CryptoPricesCoinGeckoCacheService cryptoPricesCoinGeckoCacheService;

    @SpyBean
    private KdSwapRatesCacheService kdSwapRatesCacheService;

    @Autowired
    private CompleteRatesServiceV2 completeRatesServiceV2;

    @Autowired
    private MockitoSupport mockitoSupport;


    @BeforeEach
    public void setUpMocks() throws Exception {
        super.setUpMocks();
        mockitoSupport.mockConfigCacheSuccess();
        mockitoSupport.mockCryptoPricesCacheSucccess();
        mockitoSupport.mockKdSwapRatesCacheDataSuccess();
        completeRatesServiceV2.updateCompleteRates("USD");
        completeRatesServiceV2.updateCompleteRates("EUR");
        verifyServicesInvoked();
    }

    @AfterEach
    public void verifyMocks() {
        mockitoSupport.verifyNoMoreInteractionsV2();
        super.verifyMocks();
    }

    private void verifyServicesInvoked() throws Exception {
    }

    @Test
    @Transactional
    @DisplayName("Complete rates service should return value cached per currency parameter")
    public void completeRatesServiceCachingByCurrencyFunctionalityTest() throws Exception {

        invokeCompleteRatesEndpoint(localServerPort, "USD").statusCode(200);

        invokeCompleteRatesEndpoint(localServerPort, "EUR").statusCode(200);

        verifyServicesInvoked();

        invokeCompleteRatesEndpoint(localServerPort, "USD").statusCode(200);

        invokeCompleteRatesEndpoint(localServerPort, "EUR").statusCode(200);

        verifyServicesInvoked();

        logsInterceptor.assertLogMessagesProduced(
                emptyList(),
                List.of(
                        "Invoked updateCompleteRates with fiatSymbol USD",
                        "Retrieved reversed configuration map: {kaddex=kaddex@kadena, kadena=kadena}",
                        "Retrieved 24h price changes for currency symbol usd: {kaddex@kadena=-12.506323232704695, kadena=-6.1000975049840065}",
                        "Generated complete rates response: CompleteRatesResponse[cryptoRates=[CryptoResponseEntry[id=kapybara@kadena, formattedPrice=0.000000024773671611000002, percentChange=null], CryptoResponseEntry[id=kaddex@kadena, formattedPrice=0.0262215, percentChange=-12.506323232704695], CryptoResponseEntry[id=kadena, formattedPrice=0.834327, percentChange=-6.1000975049840065]]]",
                        "Invoked updateCompleteRates with fiatSymbol EUR",
                        "Retrieved reversed configuration map: {kaddex=kaddex@kadena, kadena=kadena}",
                        "Retrieved 24h price changes for currency symbol eur: {kaddex@kadena=-11.70725627391259, kadena=-5.047735168671771}",
                        "Generated complete rates response: CompleteRatesResponse[cryptoRates=[CryptoResponseEntry[id=kapybara@kadena, formattedPrice=0.000000024214700886, percentChange=null], CryptoResponseEntry[id=kaddex@kadena, formattedPrice=0.02562987, percentChange=-11.70725627391259], CryptoResponseEntry[id=kadena, formattedPrice=0.815502, percentChange=-5.047735168671771]]]"),
                List.of(),
                List.of());
    }
}
