package com.backend.ticker.service;

import com.backend.ticker.commons.constants.TickerConstants;
import com.backend.ticker.models.provider.CoinGeckoCryptoPricesResponse;
import com.eucalyptuslabs.backend.common.model.exception.DataNotAvailableException;
import com.eucalyptuslabs.backend.common.model.ticker.response.CompleteRatesResponse;
import com.eucalyptuslabs.backend.common.service.TimeService;
import com.backend.ticker.service.persistence.PriceRepository;
import com.backend.ticker.service.provider.config.ConfigurationCacheService;
import com.backend.ticker.service.provider.rates.crypto.KdSwap.KdSwapRatesCacheService;
import com.backend.ticker.service.provider.rates.crypto.coingecko.CryptoPricesCoinGeckoCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static com.backend.ticker.config.TickerCacheConfig.COMPLETE_RATES_CACHE_NAME_V2;

@Service
public class CompleteRatesServiceV2 {
    private static final String COMPLETE_RATES = "complete rates";
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ConfigurationCacheService configurationCacheService;

    private final CryptoPricesCoinGeckoCacheService cryptoPricesCoinGeckoCacheService;

    private final KdSwapRatesCacheService kdSwapRatesCacheService;
    private final TimeService timeService;

    private final PriceRepository priceRepository;

    @Value("${base.crypto.currency}")
    private String baseCryptoCurrency;

    @Value("${kdswap.price.change.seconds}")
    private Long kdSwapPriceChange;

    public CompleteRatesServiceV2(ConfigurationCacheService configurationCacheService,
                                  CryptoPricesCoinGeckoCacheService cryptoPricesCoinGeckoCacheService,
                                  KdSwapRatesCacheService kdSwapRatesCacheService,
                                  TimeService timeService,
                                  PriceRepository priceRepository) {
        this.configurationCacheService = configurationCacheService;
        this.cryptoPricesCoinGeckoCacheService = cryptoPricesCoinGeckoCacheService;
        this.kdSwapRatesCacheService = kdSwapRatesCacheService;
        this.timeService = timeService;
        this.priceRepository = priceRepository;
    }

    @Cacheable(value = COMPLETE_RATES_CACHE_NAME_V2, key = TickerConstants.FIAT_SYMBOL)
    public CompleteRatesResponse getCompleteRates(String fiatSymbol)
            throws DataNotAvailableException {
        throw new DataNotAvailableException(COMPLETE_RATES);
    }

    @CachePut(cacheNames = COMPLETE_RATES_CACHE_NAME_V2, key = TickerConstants.FIAT_SYMBOL)
    public CompleteRatesResponse updateCompleteRates(String fiatSymbol)
            throws DataNotAvailableException {
        log.debug("Invoked updateCompleteRates with fiatSymbol {}", fiatSymbol);
        try {
            // Get reversed configuration map
            Map<String, String> map = configurationCacheService.getReversedConfiguration();
            log.debug("Retrieved reversed configuration map: {}", map);

            // Get cached data for CoinGecko crypto prices and KdSwap rates
            CoinGeckoCryptoPricesResponse coinGeckoPrices = cryptoPricesCoinGeckoCacheService.getCachedData();
            Map<String, String> kdSwapRatesCacheData = kdSwapRatesCacheService.getCachedData();

            //Get prices and 24h price changes for the given fiat currency
            final String currencySymbol = fiatSymbol.toLowerCase();
            Map<String, String> cryptoRatesMap = coinGeckoPrices.getPrices(currencySymbol, map);
            Map<String, Double> priceChangeMap = coinGeckoPrices.get24hPriceChanges(currencySymbol, map);
            log.debug("Retrieved 24h price changes for currency symbol {}: {}", currencySymbol, priceChangeMap);

            String kadenaPrice = cryptoRatesMap.get(TickerConstants.KADENA_EUCID);
            double kadenaPriceDouble = Double.parseDouble(kadenaPrice);

            // Add missing keys to crypto response entries from KdSwap rates and calculation prices
            List<CompleteRatesResponse.CryptoResponseEntry> cryptoResponseEntries =
                    calculateKdSwapPrice(kdSwapRatesCacheData,cryptoRatesMap, kadenaPriceDouble, fiatSymbol);


            cryptoResponseEntries.addAll(
                    cryptoRatesMap.entrySet().stream()
                            .filter(e -> priceChangeMap.containsKey(e.getKey()))
                            .map(
                                    e ->
                                            new CompleteRatesResponse.CryptoResponseEntry(
                                                    e.getKey(), e.getValue(), priceChangeMap.get(e.getKey())))
                            .toList());

            // Return complete rates response
            CompleteRatesResponse completeRatesResponse = new CompleteRatesResponse(cryptoResponseEntries);
            log.debug("Generated complete rates response: {}", completeRatesResponse);
            return completeRatesResponse;
        } catch (DataNotAvailableException exception) {
            log.error("Failed to retrieve data: {}", exception.getMessage());
            throw new DataNotAvailableException("Failed to update complete rates for fiat symbol {}" + fiatSymbol);
        }
    }

    private List<CompleteRatesResponse.CryptoResponseEntry> calculateKdSwapPrice(Map<String, String> kdSwapRatesCacheData,
                                                                                 Map<String, String> cryptoRatesMap,
                                                                                 double kadenaPriceDouble, String fiatSymbol){
        return kdSwapRatesCacheData.entrySet().stream()
                .filter(e -> !cryptoRatesMap.containsKey(e.getKey()))
                .map(e -> {
                    String kdSwapRate = e.getValue();
                    double kdSwapRateDouble = Double.parseDouble(kdSwapRate);
                    double price = kdSwapRateDouble * kadenaPriceDouble;
                    return new CompleteRatesResponse.CryptoResponseEntry(e.getKey(), BigDecimal.valueOf(price).toPlainString(),
                            calculatePriceChange(e.getKey(), BigDecimal.valueOf(price).toPlainString(), fiatSymbol));
                })
                .collect(Collectors.toList());
    }

    /**
     * This method will calculate the price change for KdSwap rates for given fiat currency and EucId and given time
     * @param eucId
     * @param kdSwapPrice
     * @param fiatSymbol
     * @return
     */

    public Double calculatePriceChange(String eucId, String kdSwapPrice, String fiatSymbol) {
        ZonedDateTime currentTime = timeService.getCurrentZonedDateTime();
        long hourDiff = kdSwapPriceChange;
        ZonedDateTime startTime = currentTime.minusSeconds(hourDiff);
        return priceRepository.findClosestPricesByTime(eucId,fiatSymbol,startTime)
                .map((formattedPrice) -> {
                    BigDecimal priceChange = new BigDecimal(kdSwapPrice).subtract(new BigDecimal(formattedPrice));
                    BigDecimal priceChangePercentage = priceChange.divide(new BigDecimal(formattedPrice), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
                    return priceChangePercentage.doubleValue();
                }
                    ).orElse(null);

    }
}
