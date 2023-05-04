package com.backend.ticker.service.provider.rates.crypto.KdSwap;

import com.backend.ticker.commons.constants.TickerConstants;
import com.backend.ticker.models.provider.KdSwapPairs;
import com.backend.ticker.models.provider.KdSwapResponse;
import com.backend.ticker.models.provider.RateResponse;
import com.eucalyptuslabs.backend.common.model.exception.DataNotAvailableException;
import com.eucalyptuslabs.backend.common.model.exception.ProviderFailureException;
import com.eucalyptuslabs.backend.common.provider.BaseProviderService;
import com.backend.ticker.service.provider.config.ConfigurationCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class KdSwapProviderService extends BaseProviderService<Map<String, String>> {

    private static final String DATA_NOT_AVAILABLE_FROM_KD_SWAP = "Data not available from KDSwap";
    private static final String FAILED_TO_FETCH_DATA_FROM_KDSWAP = "Failed to fetch data from KDSwap: ";
    private static final String EUC_ID_FROM = "{eucIdFrom}";
    private static final String EUC_ID_TO = "{eucIdTo}";
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ConfigurationCacheService configurationCacheService;

    @Value("${kadena-indexer.kdswaps.pairs.url}")
    private String kdSwapsPairsUrl;

    @Value("${kadena-indexer.kdswaps.rate.base.url}")
    private String kdSwapsPairsRateUrl;
    private static final HttpHeaders headers = new HttpHeaders();

    public KdSwapProviderService() {
        super(TickerConstants.DATA_NAME);
    }

    @Override
    public Map<String, String> fetchFreshData() throws ProviderFailureException, DataNotAvailableException {
        log.info("Fetching fresh data from KdSwap");
        // Making REST call to get all pairs
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<KdSwapPairs> pairsResponse = restTemplate.exchange(
                    kdSwapsPairsUrl, HttpMethod.GET, httpEntity, KdSwapPairs.class);

            Optional<List<KdSwapResponse>> optionalPairs = Optional.ofNullable(pairsResponse.getBody().pairs());

            List<KdSwapResponse> pairs = optionalPairs.orElseThrow(() -> {
                return new DataNotAvailableException(DATA_NOT_AVAILABLE_FROM_KD_SWAP);
            });
            log.debug("Got {} pairs from KDSwap", pairs.size());

            // Make REST call to get rates for each pair
            Map<String, String> rates = pairs
                    .stream()
                    .parallel()
                    .map(pair -> {
                        String url = kdSwapsPairsRateUrl.replace(EUC_ID_FROM, pair.eucIdFrom())
                                .replace(EUC_ID_TO, pair.eucIdTo());
                        ResponseEntity<RateResponse> rateResponse = restTemplate.exchange(
                                url, HttpMethod.GET, httpEntity, RateResponse.class);
                        return Optional.ofNullable(rateResponse.getBody())
                                .map(body -> new AbstractMap.SimpleEntry<>(pair.eucIdTo(),  BigDecimal.valueOf(body.rate()).toPlainString()))
                                .orElse(null);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            log.debug("Got {} rates from KDSwap", rates.size());
            return rates;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new DataNotAvailableException(DATA_NOT_AVAILABLE_FROM_KD_SWAP);
            } else {
                throw new ProviderFailureException(FAILED_TO_FETCH_DATA_FROM_KDSWAP + e.getMessage());
            }
        } catch (RestClientException e) {
            throw new ProviderFailureException(FAILED_TO_FETCH_DATA_FROM_KDSWAP + e.getMessage());
        }
    }
}
