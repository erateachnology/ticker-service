package com.backend.ticker.service.provider.config.mds;

import com.eucalyptuslabs.backend.common.model.config.partner.PartnerConfig;
import com.eucalyptuslabs.backend.common.model.exception.ProviderFailureException;
import com.eucalyptuslabs.backend.common.provider.BaseProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownContentTypeException;

@Service
public class ConfigMdsProviderService extends BaseProviderService<PartnerConfig> {

  private static final String DATA_NAME = "Configuration-MetadataService";

  @Autowired private RestTemplate restTemplate;

  @Value("${metadata-service.config.cache-provider.url}")
  private String configUrl;

  public ConfigMdsProviderService() {
    super(DATA_NAME);
  }

  @Override
  public PartnerConfig fetchFreshData() throws ProviderFailureException {
    try {
      ResponseEntity<PartnerConfig> response = createConfigRequest();
      return response.getBody();
    } catch (HttpStatusCodeException | UnknownContentTypeException e) {
      throw new ProviderFailureException(e.getMessage());
    }
  }

  private ResponseEntity<PartnerConfig> createConfigRequest() {
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
    return restTemplate.exchange(configUrl, HttpMethod.GET, httpEntity, PartnerConfig.class);
  }
}
