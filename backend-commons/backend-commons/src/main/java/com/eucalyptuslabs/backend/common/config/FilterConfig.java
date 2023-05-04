package com.eucalyptuslabs.backend.common.config;

import com.eucalyptuslabs.backend.common.filters.AssignReferenceIdFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The controller filter's configuration class. Its purpose is to register {@link
 * AssignReferenceIdFilter} at the first place in the chain of filters, so the reference ID (used
 * for logging) is assigned at the very beginning of a request processing.
 */
@Configuration
public class FilterConfig {

  @Autowired AssignReferenceIdFilter assignReferenceIdFilter;

  @Bean
  public FilterRegistrationBean<AssignReferenceIdFilter> filterRegistrationBean() {
    FilterRegistrationBean<AssignReferenceIdFilter> registrationBean =
        new FilterRegistrationBean<>();
    registrationBean.setFilter(assignReferenceIdFilter);
    registrationBean.setOrder(1);
    return registrationBean;
  }
}
