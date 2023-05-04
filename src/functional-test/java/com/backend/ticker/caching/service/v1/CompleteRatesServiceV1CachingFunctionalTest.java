package com.backend.ticker.caching.service.v1;

import com.backend.ticker.MockitoSupport;
import com.backend.ticker.service.CompleteRatesServiceV1;
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

import static com.integration.backend.ticker.v1.TickerApiIntegrationTest.invokeCompleteRatesEndpoint;
import static java.util.Collections.emptyList;


public class CompleteRatesServiceV1CachingFunctionalTest extends BaseFunctionalTest {

  @MockBean private IndependentProvidersCacheScheduler providerCacheScheduler;
  @SpyBean private ConfigurationCacheService configurationCacheService;
  @SpyBean private CryptoPricesCoinGeckoCacheService cryptoPricesCoinGeckoCacheService;

  @SpyBean private KdSwapRatesCacheService kdSwapRatesCacheService;

  @Autowired private CompleteRatesServiceV1 completeRatesServiceV1;

  @Autowired private MockitoSupport mockitoSupport;


  @BeforeEach
  public void setUpMocks() throws Exception {
    super.setUpMocks();
    mockitoSupport.mockConfigCacheSuccess();
    mockitoSupport.mockCryptoPricesCacheSucccess();
    mockitoSupport.mockKdSwapRatesCacheDataSuccess();
    completeRatesServiceV1.updateCompleteRates("USD");
    completeRatesServiceV1.updateCompleteRates("EUR");
    verifyServicesInvoked();
  }

  @AfterEach
  public void verifyMocks() {
    mockitoSupport.verifyNoMoreInteractions();
    super.verifyMocks();
  }

  private void verifyServicesInvoked() throws Exception {}

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
            "Invoked updateCompleteRates with fiatSymbol EUR"),
        List.of(),
        List.of());
  }
}
