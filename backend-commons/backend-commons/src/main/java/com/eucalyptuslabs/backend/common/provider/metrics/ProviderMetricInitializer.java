package com.eucalyptuslabs.backend.common.provider.metrics;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProviderMetricInitializer extends BaseProviderMetricService {

  @Autowired(required = false)
  private List<MetricDiscriminator> providerDiscriminators = new ArrayList<>();

  @PostConstruct
  public void resetMetrics() {
    resetMetrics(getDataDiscriminators());
  }

  private void resetMetrics(Set<String> dataNames) {
    dataNames.forEach(this::getCacheInitFailureMetric);
    dataNames.forEach(this::getCacheRefreshFailureMetric);
  }

  private Set<String> getDataDiscriminators() {
    return providerDiscriminators.stream()
        .map(MetricDiscriminator::getDiscriminator)
        .collect(Collectors.toSet());
  }
}
