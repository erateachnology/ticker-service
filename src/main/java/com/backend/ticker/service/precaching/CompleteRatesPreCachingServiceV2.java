package com.backend.ticker.service.precaching;

import com.backend.ticker.service.CompleteRatesServiceV2;
import com.backend.ticker.service.price.PriceService;
import com.backend.ticker.service.provider.rates.crypto.KdSwap.KdSwapRatesCacheService;
import com.backend.ticker.service.provider.rates.crypto.coingecko.CryptoPricesCoinGeckoCacheService;
import com.eucalyptuslabs.backend.common.model.exception.DataNotAvailableException;
import com.eucalyptuslabs.backend.common.model.ticker.response.CompleteRatesResponse;
import com.eucalyptuslabs.backend.common.provider.metrics.ProviderMetricService;
import com.eucalyptuslabs.backend.common.provider.precaching.BasePreCachingService;
import com.eucalyptuslabs.backend.common.service.TimeService;
import com.backend.ticker.config.WebsocketsUpdatesPublisherV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompleteRatesPreCachingServiceV2 extends BasePreCachingService {
    private static final String DATA_NAME = "CompleteRates";
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    private final PriceService priceService;
    private final ProviderMetricService providerMetricService;
    private final CryptoPricesCoinGeckoCacheService cryptoPricesCoinGeckoCacheService;
    private final KdSwapRatesCacheService kdSwapRatesCacheService;
    private final CompleteRatesServiceV2 completeRatesServiceV2;
    private final WebsocketsUpdatesPublisherV2 websocketsUpdatesPublisherV2;
    private final TimeService timeService;
    @Value("${fiat.currencies}")
    private List<String> fiatCurrencies;

    public CompleteRatesPreCachingServiceV2(PriceService priceService,
                                            ProviderMetricService providerMetricService,
                                            CryptoPricesCoinGeckoCacheService cryptoPricesCoinGeckoCacheService,
                                            KdSwapRatesCacheService kdSwapRatesCacheService,
                                            CompleteRatesServiceV2 completeRatesServiceV2,
                                            WebsocketsUpdatesPublisherV2 websocketsUpdatesPublisherV2,
                                            TimeService timeService) {
        super(DATA_NAME, List.of(cryptoPricesCoinGeckoCacheService, kdSwapRatesCacheService));
        this.priceService = priceService;
        this.providerMetricService = providerMetricService;
        this.cryptoPricesCoinGeckoCacheService = cryptoPricesCoinGeckoCacheService;
        this.kdSwapRatesCacheService = kdSwapRatesCacheService;
        this.completeRatesServiceV2 = completeRatesServiceV2;
        this.websocketsUpdatesPublisherV2 = websocketsUpdatesPublisherV2;
        this.timeService = timeService;
    }

    protected void updateServiceValues() throws DataNotAvailableException {
        for (String fiatCurrency : fiatCurrencies) {
            CompleteRatesResponse completeRatesResponse = completeRatesServiceV2.updateCompleteRates(fiatCurrency);
            // Add historical price data into the database
            priceService.saveHistoricalPrices(completeRatesResponse.cryptoRates(), fiatCurrency, timeService.getCurrentZonedDateTime());

            websocketsUpdatesPublisherV2.sendToCurrencyTopic(
                    fiatCurrency, completeRatesResponse);
        }
    }
}
