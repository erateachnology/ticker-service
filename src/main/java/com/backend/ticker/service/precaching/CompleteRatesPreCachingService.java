package com.backend.ticker.service.precaching;

import com.backend.ticker.service.CompleteRatesServiceV1;
import com.backend.ticker.service.provider.rates.crypto.coingecko.CryptoPricesCoinGeckoCacheService;
import com.eucalyptuslabs.backend.common.model.exception.DataNotAvailableException;
import com.eucalyptuslabs.backend.common.model.ticker.response.CompleteRatesResponse;
import com.eucalyptuslabs.backend.common.provider.metrics.ProviderMetricService;
import com.eucalyptuslabs.backend.common.provider.precaching.BasePreCachingService;
import com.backend.ticker.config.WebsocketsUpdatesPublisherV1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CompleteRatesPreCachingService extends BasePreCachingService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String DATA_NAME = "CompleteRates";

    @Autowired
    private ProviderMetricService providerMetricService;

    @Autowired
    private CryptoPricesCoinGeckoCacheService cryptoPricesCoinGeckoCacheService;

    @Autowired
    private CompleteRatesServiceV1 completeRatesServiceV1;

    @Autowired
    private WebsocketsUpdatesPublisherV1 websocketsUpdatesPublisherV1;
    @Value("${fiat.currencies}")
    private List<String> fiatCurrencies;

    public CompleteRatesPreCachingService(CryptoPricesCoinGeckoCacheService cryptoPricesCoinGeckoCacheService) {
        super(DATA_NAME, List.of(cryptoPricesCoinGeckoCacheService));
    }

    protected void updateServiceValues() throws DataNotAvailableException {
        for (String fiatCurrency : fiatCurrencies) {
            CompleteRatesResponse completeRatesResponse = completeRatesServiceV1.updateCompleteRates(fiatCurrency);
            websocketsUpdatesPublisherV1.sendToCurrencyTopic(
                    fiatCurrency, completeRatesResponse);
        }
    }
}
