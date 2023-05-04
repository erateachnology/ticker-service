package com.eucalyptuslabs.backend.common.provider.metrics;

/**
 * The interface requiring the object to deliver its name (aka discriminator). The primary usage of
 * the discriminator is to gather various metrics for the object labeled with the discriminator (see
 * {@link ProviderMetricInitializer} and {@link ProviderMetricService}).
 */
public interface MetricDiscriminator {

  String getDiscriminator();
}
