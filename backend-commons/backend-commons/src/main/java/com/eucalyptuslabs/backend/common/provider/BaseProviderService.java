package com.eucalyptuslabs.backend.common.provider;

import com.eucalyptuslabs.backend.common.model.exception.DataNotAvailableException;
import com.eucalyptuslabs.backend.common.model.exception.ProviderFailureException;
import com.eucalyptuslabs.backend.common.provider.metrics.MetricDiscriminator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base class for data provider used in providers cache class (see {@link
 * BaseProvidersCacheService}). <br>
 * The main purpose of classes inheriting from this base class is to deliver the implementation of
 * the data fetching method. The provider should integrate with 3rd party service to obtain the
 * data, which are then stored and served by the providers cache class. <br>
 * The class implement the {@link MetricDiscriminator} interface, because the failure metrics in
 * case of failed provider call are gathered.
 *
 * @param <DATA_TYPE> data type returned by the provider
 */
public abstract class BaseProviderService<DATA_TYPE> implements MetricDiscriminator {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  private final String providerDiscriminator;

  /**
   * The class constructor requiring one parameter.
   *
   * @param providerDiscriminator the name of this provider
   */
  public BaseProviderService(String providerDiscriminator) {
    this.providerDiscriminator = providerDiscriminator;
  }

  /**
   * The method required by the {@link MetricDiscriminator} interface.
   *
   * @return a string containing the provider's name (discriminator)
   */
  public String getDiscriminator() {
    return providerDiscriminator;
  }

  /**
   * The abstract method to be implemented by the subclass. The implementation of this method should
   * provide as the result a value obtained from external service. The response from the provider
   * should get transformed into required data format.
   *
   * @return the data from external provider
   * @throws ProviderFailureException in case of communication failure or response transformation
   *     failure
   * @throws DataNotAvailableException in case of missing input data required to make a call to
   *     external service
   */
  public abstract DATA_TYPE fetchFreshData()
      throws ProviderFailureException, DataNotAvailableException;

  /**
   * The method returning provider name as the object's string representation. Method overridden to
   * facilitate logging.
   *
   * @return a string containing the provider's discriminator
   */
  @Override
  public String toString() {
    return providerDiscriminator;
  }
}
