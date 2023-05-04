package com.backend.ticker;

import com.eucalyptuslabs.backend.common.filters.IdGenerator;
import com.eucalyptuslabs.backend.common.model.config.partner.PartnerConfig;
import com.eucalyptuslabs.backend.common.model.exception.DataNotAvailableException;
import com.eucalyptuslabs.backend.common.model.ticker.response.CompleteRatesResponse;
import com.eucalyptuslabs.backend.common.provider.precaching.TriggerPreCachingEvent;
import com.eucalyptuslabs.backend.common.service.TimeService;
import com.backend.ticker.config.WebsocketsUpdatesPublisherV1;
import com.backend.ticker.config.WebsocketsUpdatesPublisherV2;
import com.backend.ticker.models.provider.CoinGeckoCryptoPricesResponse;
import com.backend.ticker.service.CompleteRatesServiceV1;
import com.backend.ticker.service.CompleteRatesServiceV2;
import com.backend.ticker.service.precaching.CompleteRatesPreCachingService;
import com.backend.ticker.service.provider.config.ConfigurationCacheService;
import com.backend.ticker.service.provider.rates.crypto.KdSwap.KdSwapRatesCacheService;
import com.backend.ticker.service.provider.rates.crypto.coingecko.CryptoPricesCoinGeckoCacheService;
import com.eucalyptuslabs.functional.backend.common.support.BaseMockitoSupport;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.eucalyptuslabs.backend.common.util.AssertionUtils.loadDto;
import static com.eucalyptuslabs.backend.common.util.AssertionUtils.loadObject;
import static org.mockito.Mockito.*;

@Component
public class MockitoSupport extends BaseMockitoSupport {

    @Autowired
    public TimeService timeService;
    @Autowired
    public ConfigurationCacheService configurationCacheService;

    @Autowired
    public CryptoPricesCoinGeckoCacheService cryptoPricesCoinGeckoCacheService;

    @Autowired
    public CompleteRatesPreCachingService completeRatesPreCachingService;

    @Autowired
    public CompleteRatesServiceV1 completeRatesServiceV1;

    @Autowired
    public CompleteRatesServiceV2 completeRatesServiceV2;

    @Autowired
    public WebsocketsUpdatesPublisherV1 websocketsUpdatesPublisherV1;

    @Autowired
    public WebsocketsUpdatesPublisherV2 websocketsUpdatesPublisherV2;
    @Autowired
    public KdSwapRatesCacheService kdSwapRatesCacheServiceMock;
    @MockBean
    private IdGenerator idGenerator;

    public void verifyNoMoreInteractions() {
        verifyNoMoreInteractions(configurationCacheService);
        verifyNoMoreInteractions(cryptoPricesCoinGeckoCacheService);
        verifyNoMoreInteractions(completeRatesPreCachingService);
        verifyNoMoreInteractions(websocketsUpdatesPublisherV1);
        verifyNoMoreInteractions(kdSwapRatesCacheServiceMock);
    }

    public void verifyNoMoreInteractionsV2() {
        verifyNoMoreInteractions(configurationCacheService);
        verifyNoMoreInteractions(cryptoPricesCoinGeckoCacheService);
        verifyNoMoreInteractions(completeRatesPreCachingService);
        verifyNoMoreInteractions(websocketsUpdatesPublisherV1);
        verifyNoMoreInteractions(websocketsUpdatesPublisherV2);
        verifyNoMoreInteractions(kdSwapRatesCacheServiceMock);
    }

    @Override
    public void clearAll() {
        clear(configurationCacheService);
        clear(cryptoPricesCoinGeckoCacheService);
        clear(completeRatesPreCachingService);
        clear(websocketsUpdatesPublisherV1);
        clear(kdSwapRatesCacheServiceMock);
    }

    public void mockCryptoPricesCacheSucccess() throws DataNotAvailableException, IOException {
        CoinGeckoCryptoPricesResponse response =
                loadObject(
                        CoinGeckoCryptoPricesResponse.class,
                        "dto/coingecko/cg-crypto-prices-success-response.json");
        doReturn(response).when(cryptoPricesCoinGeckoCacheService).getCachedData();

    }

    public void mockKdSwapRatesCacheDataSuccess() throws DataNotAvailableException, IOException {
        Map<String, String> expectedCacheData = new HashMap<>();
        expectedCacheData.put("kapybara@kadena", "0.000000029693");
        doReturn(expectedCacheData).when(kdSwapRatesCacheServiceMock).getCachedData();
    }



    public void mockCompleteRatesServiceUpdateCompleteRates() throws DataNotAvailableException, IOException {
        CompleteRatesResponse response = loadObject(
                CompleteRatesResponse.class,
                "dto/expected/success/complete-rates/complete-rates-success-usd-response-v1.json");
        doReturn(response).when(completeRatesServiceV1).updateCompleteRates(anyString());

    }

    public void mockCompleteRatesServiceV2UpdateCompleteRates() throws DataNotAvailableException, IOException {
        CompleteRatesResponse response = loadObject(
                CompleteRatesResponse.class,
                "dto/expected/success/complete-rates/complete-rates-success-usd-response-v2.json");
        doReturn(response).when(completeRatesServiceV2).updateCompleteRates(anyString());

    }

    public void verifyCoinGeckoCacheService() throws DataNotAvailableException, IOException {
        verify(cryptoPricesCoinGeckoCacheService).getCachedData();
    }

    public void verifyKdSwapCacheService() throws DataNotAvailableException, IOException {
        verify(kdSwapRatesCacheServiceMock).getCachedData();
    }

    public void verifyCryptoRatesCacheInvocation() throws DataNotAvailableException {
        verifyCryptoRatesCacheInvocation(1);
    }

    public void verifyCryptoRatesCacheInvocation(int times) throws DataNotAvailableException {
        verify(cryptoPricesCoinGeckoCacheService, times(times)).getCachedData();
    }

    public void verifyConfigCacheInvocation() throws DataNotAvailableException {
        verifyConfigCacheInvocation(1);
    }

    public void verifyConfigCacheInvocation(int times) throws DataNotAvailableException {
        verify(configurationCacheService, times(times)).getCachedData();
    }

    public void mockCryptoRatesCacheServiceSuccess() throws IOException, DataNotAvailableException {
        doReturn(
                loadObject(
                        getCoinGeckoPricesSuccessEndpointResponse(), CoinGeckoCryptoPricesResponse.class))
                .when(cryptoPricesCoinGeckoCacheService)
                .getCachedData();
    }


    public static String getCoinGeckoPricesSuccessEndpointResponse() throws IOException {
        return loadDto("dto/coingecko/cg-crypto-prices-success-response.json");
    }

    public void mockConfigCacheSuccess() throws IOException, DataNotAvailableException {
        doReturn(loadObject(getConfigExpectedResponseBody(), PartnerConfig.class))
                .when(configurationCacheService)
                .getCachedData();
    }

    public static String getConfigExpectedResponseBody() throws IOException {
        return loadDto("dto/config/crypto-config.json");
    }

    public void mockCryptoRatesServiceFailure() throws DataNotAvailableException {
        doThrow(new DataNotAvailableException("CoinGecko"))
                .when(cryptoPricesCoinGeckoCacheService)
                .getCachedData();
    }

    public void mockConfigServiceFailure() throws DataNotAvailableException {
        doThrow(new DataNotAvailableException("config"))
                .when(configurationCacheService)
                .getCachedData();
    }

    public void mockCryptoRatesCacheInitialized() {
        doReturn(true).when(cryptoPricesCoinGeckoCacheService).isInitialized();
    }

    public void mockKdSwapCacheInitialized() {
        doReturn(true).when(kdSwapRatesCacheServiceMock).isInitialized();
    }

    public void mockCryptoRatesCacheNotInitialized() {
        doReturn(false).when(cryptoPricesCoinGeckoCacheService).isInitialized();
    }

    public void verifyCryptoRatesCacheInitializedInvocation() {
        verify(cryptoPricesCoinGeckoCacheService, times(1)).isInitialized();
    }

    public void mockConfigProviderCacheInitialized() {
        doReturn(true).when(configurationCacheService).isInitialized();
    }

    public void verifyCompleteRatesPreCachingServiceInvoked() {
        verify(completeRatesPreCachingService).preCacheServiceValues(any(TriggerPreCachingEvent.class));
    }

    public void verifyCompleteRatesServiceInvoked(String fiat) throws DataNotAvailableException {
        verify(completeRatesServiceV1).updateCompleteRates(fiat);
    }

    public void verifyCompleteRatesServiceV2Invoked(String fiat) throws DataNotAvailableException {
        verify(completeRatesServiceV2).updateCompleteRates(fiat);
    }

    public void verifyCompleteRatesPreCachingServiceInvoked(int times) {
        verify(completeRatesPreCachingService, times(times))
                .preCacheServiceValues(any(TriggerPreCachingEvent.class));
    }

    public void verifyWebsocketsUpdatesPublisherInvoked(String fiat) {
        verify(websocketsUpdatesPublisherV1).sendToCurrencyTopic(eq(fiat), any(CompleteRatesResponse.class));

    }

    public void verifyWebsocketsUpdatesPublisherV2Invoked(String fiat) {
        verify(websocketsUpdatesPublisherV2).sendToCurrencyTopic(eq(fiat), any(CompleteRatesResponse.class));

    }

    public void mockIdGeneratorGetNextId() {
        Mockito.doReturn(new UUID(100, 200).toString()).when(idGenerator).getNextId();
    }

    public void mockTimeServiceGetZonedDateTime() {
        Mockito.doReturn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(100_000_000), ZoneId.of("UTC")))
                .when(timeService)
                .getCurrentZonedDateTime();
    }


}
