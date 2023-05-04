package com.backend.ticker.service.provider.rates.crypto.coingecko;

import com.backend.ticker.models.provider.CoinGeckoCryptoPricesResponse;
import com.backend.ticker.service.provider.config.ConfigurationCacheService;
import com.eucalyptuslabs.backend.common.model.config.partner.PartnerConfig;
import com.eucalyptuslabs.backend.common.model.exception.DataNotAvailableException;
import com.eucalyptuslabs.backend.common.model.exception.ProviderFailureException;
import com.eucalyptuslabs.backend.common.provider.BaseProviderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownContentTypeException;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CryptoPricesCoinGeckoProviderService
    extends BaseProviderService<CoinGeckoCryptoPricesResponse> {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private static final String DATA_NAME = "CryptoPrices-CoinGecko";

  private final ObjectMapper mapper = new ObjectMapper();

  @Autowired private RestTemplate restTemplate;

  @Autowired private ConfigurationCacheService configurationCacheService;

  @Value("${coingecko.config.cache-provider.url}")
  private String apiUrl;

  @Value("${fiat.currencies}")
  private List<String> fiatCurrencies;

  public CryptoPricesCoinGeckoProviderService() {
    super(DATA_NAME);
  }

  @Override
  public CoinGeckoCryptoPricesResponse fetchFreshData()
      throws ProviderFailureException, DataNotAvailableException {
    try {
      ResponseEntity<CoinGeckoCryptoPricesResponse> response = createRequest();
      return response.getBody();
    } catch (RestClientResponseException e) {
      throw createProviderException(e.getResponseBodyAsString());
    } catch (UnknownContentTypeException e) {
      throw createProviderException(e.getResponseBodyAsString());
    }
  }

  private ResponseEntity<CoinGeckoCryptoPricesResponse> createRequest()
      throws DataNotAvailableException {
    String url =
        String.format(
            "%s/v3/simple/price?ids=%s&vs_currencies=%s&include_24hr_change=true",
            apiUrl, getEnabledCryptoIdsParam(), getEnabledFiatIdsParam());
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<?> httpEntity = new HttpEntity<>(headers);
    return restTemplate.exchange(
        url, HttpMethod.GET, httpEntity, CoinGeckoCryptoPricesResponse.class);
  }
  
  public String getEnabledCryptoIdsParam() throws DataNotAvailableException {
    return configurationCacheService.getCachedData().cryptocurrencies().stream()
        .map(PartnerConfig.PartnerConfigEntry::partnerSymbol)
        .distinct()
        .collect(Collectors.joining(","));
  }
  
  private ProviderFailureException createProviderException(String responseBody) {
    JsonNode error;
    try {
      error = mapper.readTree(responseBody);
    } catch (JsonProcessingException e) {
      log.warn("Unable to parse provider error response: {}", responseBody, e);
      return new ProviderFailureException(responseBody);
    }
    return new ProviderFailureException(error.at("/status/error_message").asText());
  }

  private String getEnabledFiatIdsParam() {
    return String.join(",", fiatCurrencies);
  }
}
