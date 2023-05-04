package com.backend.ticker.service;

import com.backend.ticker.commons.constants.TickerConstants;
import com.backend.ticker.models.provider.CoinGeckoCryptoPricesResponse;
import com.eucalyptuslabs.backend.common.model.exception.DataNotAvailableException;
import com.eucalyptuslabs.backend.common.model.ticker.response.CompleteRatesResponse;
import com.backend.ticker.service.provider.config.ConfigurationCacheService;
import com.backend.ticker.service.provider.rates.crypto.coingecko.CryptoPricesCoinGeckoCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.backend.ticker.config.TickerCacheConfig.COMPLETE_RATES_CACHE_NAME_V1;

@Service
public class CompleteRatesServiceV1 {
    private static final String COMPLETE_RATES = "complete rates";
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Value("${base.crypto.currency}")
    String baseCryptoCurrency;
    @Autowired
    private ConfigurationCacheService configurationCacheService;
    @Autowired
    private CryptoPricesCoinGeckoCacheService cryptoPricesCoinGeckoCacheService;

    @Cacheable(value = COMPLETE_RATES_CACHE_NAME_V1, key = TickerConstants.FIAT_SYMBOL)
    public CompleteRatesResponse getCompleteRates(String fiatSymbol)
            throws DataNotAvailableException {
        throw new DataNotAvailableException(COMPLETE_RATES);
    }

    @CachePut(cacheNames = COMPLETE_RATES_CACHE_NAME_V1, key = TickerConstants.FIAT_SYMBOL)
    public CompleteRatesResponse updateCompleteRates(String fiatSymbol)
            throws DataNotAvailableException {
        log.debug("Invoked updateCompleteRates with fiatSymbol {}", fiatSymbol);
        try {
            final String currencySymbol = fiatSymbol.toLowerCase();

            Map<String, String> map = configurationCacheService.getReversedConfiguration();

            CoinGeckoCryptoPricesResponse coinGeckoPrices = cryptoPricesCoinGeckoCacheService.getCachedData();
            Map<String, String> cryptoRatesMap = coinGeckoPrices.getPrices(currencySymbol, map);

            Map<String, Double> priceChangeMap = coinGeckoPrices.get24hPriceChanges(currencySymbol, map);

            List<CompleteRatesResponse.CryptoResponseEntry> cryptoResponseEntries =
                    cryptoRatesMap.entrySet().stream()
                            .filter(e -> priceChangeMap.containsKey(e.getKey()))
                            .map(
                                    e ->
                                            new CompleteRatesResponse.CryptoResponseEntry(
                                                    e.getKey(), e.getValue(), priceChangeMap.get(e.getKey())))
                            .collect(Collectors.toList());

            return new CompleteRatesResponse(cryptoResponseEntries);
        } catch (DataNotAvailableException exception) {
            log.error("Failed to retrieve data: {}", exception.getMessage());
            throw new DataNotAvailableException("Failed to update complete rates for fiat symbol {}" + fiatSymbol);
        }
    }

}
