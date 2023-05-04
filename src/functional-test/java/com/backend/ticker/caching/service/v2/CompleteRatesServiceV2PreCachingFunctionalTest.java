package com.backend.ticker.caching.service.v2;

import com.backend.ticker.MockitoSupport;
import com.backend.ticker.service.CompleteRatesServiceV2;
import com.backend.ticker.service.precaching.CompleteRatesPreCachingService;
import com.backend.ticker.service.precaching.CompleteRatesPreCachingServiceV2;
import com.backend.ticker.service.provider.IndependentProvidersCacheScheduler;
import com.backend.ticker.service.provider.rates.crypto.KdSwap.KdSwapRatesCacheService;
import com.backend.ticker.service.provider.rates.crypto.coingecko.CryptoPricesCoinGeckoCacheService;
import com.eucalyptuslabs.backend.common.model.exception.DataNotAvailableException;
import com.eucalyptuslabs.backend.common.provider.precaching.TriggerPreCachingEvent;
import com.backend.ticker.config.WebsocketsUpdatesPublisherV2;
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

import static com.integration.backend.ticker.v1.MetricsIntegrationTest.invokePrometheusEndpoint;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.containsString;

public class CompleteRatesServiceV2PreCachingFunctionalTest extends BaseFunctionalTest {
    @MockBean
    private IndependentProvidersCacheScheduler providerCacheScheduler;

    @MockBean
    private CompleteRatesServiceV2 completeRatesServiceV2;

    @MockBean
    private WebsocketsUpdatesPublisherV2 websocketsUpdatesPublisherV2;


    @SpyBean
    private CryptoPricesCoinGeckoCacheService cryptoPricesCoinGeckoCacheService;

    @SpyBean
    private KdSwapRatesCacheService kdSwapRatesCacheService;

    @Autowired
    private CompleteRatesPreCachingService completeRatesPreCachingService;

    @Autowired
    private CompleteRatesPreCachingServiceV2 completeRatesPreCachingServiceV2;
    @Autowired
    private MockitoSupport mockitoSupport;

    public CompleteRatesServiceV2PreCachingFunctionalTest() {
    }

    @BeforeEach
    public void setUpMocks() throws Exception {
        super.setUpMocks();
        mockitoSupport.mockCryptoRatesCacheInitialized();
    }

    @AfterEach
    public void verifyMocks() {
        mockitoSupport.verifyNoMoreInteractionsV2();
        super.verifyMocks();
    }

    public void verifyServicesInvoked() throws DataNotAvailableException {

        for (String fiat : new String[]{"USD", "EUR"}) {
            mockitoSupport.verifyCompleteRatesServiceV2Invoked(fiat);
            mockitoSupport.verifyWebsocketsUpdatesPublisherV2Invoked(fiat);
        }
    }

    @Test
    @Transactional
    @DisplayName(
            "Complete rates pre-caching service should get skipped when CoinGecko cache is not initialized")
    public void
    completeRatesServicePreCachingSkippingInCaseOfCoinGeckoCacheNotInitializedFunctionalityTest() {

        mockitoSupport.mockCryptoRatesCacheNotInitialized();

        completeRatesPreCachingService.preCacheServiceValues(
                new TriggerPreCachingEvent("CryptoPricesCache"));

        mockitoSupport.verifyCryptoRatesCacheInitializedInvocation();

        logsInterceptor.assertLogMessagesProduced(emptyList(), List.of(), List.of(), List.of());

        invokePrometheusEndpoint(localActuatorPort)
                .body(
                        containsString(
                                "cache_failure_total{action=\"refresh\",cacheType=\"CompleteRates\",} 0.0"));
    }

    @Test
    @Transactional
    @DisplayName(
            "Complete rates pre-caching service should invoke the service for each fiat currency")
    public void completeRatesServicePreCachingForEachFiatCurrencyFunctionalityTest()
            throws Exception {

        mockitoSupport.mockCryptoRatesCacheInitialized();
        mockitoSupport.mockCryptoPricesCacheSucccess();
        mockitoSupport.mockCompleteRatesServiceV2UpdateCompleteRates();
        mockitoSupport.mockKdSwapRatesCacheDataSuccess();
        mockitoSupport.mockKdSwapCacheInitialized();
        completeRatesPreCachingServiceV2.preCacheServiceValues(
                new TriggerPreCachingEvent("KDSwapRatesCache"));

        mockitoSupport.verifyCryptoRatesCacheInitializedInvocation();

        verifyServicesInvoked();

        logsInterceptor.assertLogMessagesProduced(emptyList(), List.of(), List.of(), List.of());
    }
}
