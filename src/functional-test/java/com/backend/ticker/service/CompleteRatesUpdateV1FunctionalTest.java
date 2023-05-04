package com.backend.ticker.service;

import com.backend.ticker.MockitoSupport;
import com.backend.ticker.service.provider.IndependentProvidersCacheScheduler;
import com.backend.ticker.service.provider.config.ConfigurationCacheService;
import com.backend.ticker.service.provider.rates.crypto.KdSwap.KdSwapRatesCacheService;
import com.backend.ticker.service.provider.rates.crypto.coingecko.CryptoPricesCoinGeckoCacheService;
import com.eucalyptuslabs.backend.common.model.exception.DataNotAvailableException;
import com.eucalyptuslabs.backend.common.model.exception.SymbolNotFoundException;
import com.eucalyptuslabs.backend.common.model.ticker.response.CompleteRatesResponse;
import com.eucalyptuslabs.functional.backend.common.provider.BaseFunctionalTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

import static com.eucalyptuslabs.backend.common.util.AssertionUtils.loadObject;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompleteRatesUpdateV1FunctionalTest extends BaseFunctionalTest {

  @Autowired private MockitoSupport mockitoSupport;

  @MockBean private IndependentProvidersCacheScheduler providerCacheScheduler;
  @SpyBean private ConfigurationCacheService configurationCacheService;
  @SpyBean private CryptoPricesCoinGeckoCacheService cryptoPricesCoinGeckoCacheService;

  @SpyBean private KdSwapRatesCacheService kdSwapRatesCacheService;

  @Autowired private CompleteRatesServiceV1 completeRatesServiceV1;

  public CompleteRatesUpdateV1FunctionalTest() throws IOException {}

  @BeforeEach
  public void setUpMocks() throws Exception {
    super.setUpMocks();
  }

  @AfterEach
  public void verifyMocks() {
    mockitoSupport.verifyNoMoreInteractions();
    super.verifyMocks();
  }

  @Test
  @Transactional
  @DisplayName("Should invoke CoinGecko endpoint and return mapped results when currency is USD")
  public void completeCryptoUsdSuccessFunctionalityTest()
      throws IOException, DataNotAvailableException, SymbolNotFoundException {

    mockitoSupport.mockConfigCacheSuccess();
    mockitoSupport.mockCryptoPricesCacheSucccess();
    CompleteRatesResponse actualResponse = completeRatesServiceV1.updateCompleteRates("USD");
    mockitoSupport.verifyConfigCacheInvocation();
    mockitoSupport.verifyCoinGeckoCacheService();

    assertEquals(getExpectedOutputUsdSuccessResponseBody(), actualResponse);

    logsInterceptor.assertLogMessagesProduced(
        emptyList(),
            List.of(
                    "Invoked updateCompleteRates with fiatSymbol USD"),
        List.of(),
        List.of());
  }

  private CompleteRatesResponse getExpectedOutputUsdSuccessResponseBody() throws IOException {
    return loadObject(
        CompleteRatesResponse.class,
            "dto/expected/success/complete-rates/complete-rates-success-usd-response-v1.json");
  }
}
