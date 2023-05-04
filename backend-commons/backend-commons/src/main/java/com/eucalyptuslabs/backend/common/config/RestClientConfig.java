package com.eucalyptuslabs.backend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration of the Spring's RestTemplate which is our REST client of choice. It uses Apache
 * HTTP client in version HTTP 1.1. It configures connect and read timeouts to reasonable values.
 */
@Configuration
public class RestClientConfig {

  @Value("${rest-client.connect.timeout.seconds:3}")
  private int connectTimeoutSeconds;

  @Value("${rest-client.read.timeout.seconds:5}")
  private int readTimeoutSeconds;

  @Bean
  public RestTemplate getRestTemplate(RestTemplateBuilder builder) {
    return builder
        .setConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
        .setReadTimeout(Duration.ofSeconds(readTimeoutSeconds))
        .build();
  }
}
