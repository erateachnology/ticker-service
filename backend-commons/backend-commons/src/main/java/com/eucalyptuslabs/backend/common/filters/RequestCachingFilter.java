package com.eucalyptuslabs.backend.common.filters;

import com.eucalyptuslabs.backend.common.controller.ErrorControllerAdvice;
import com.eucalyptuslabs.backend.common.filters.logging.DebugLoggingRequestFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * The controller's filter which caches the request's body for the usage of the {@link
 * ErrorControllerAdvice}.
 */
@Component
@ConditionalOnMissingBean(DebugLoggingRequestFilter.class)
public class RequestCachingFilter extends CommonsRequestLoggingFilter {

  public RequestCachingFilter() {
    setIncludePayload(true);
    setMaxPayloadLength(10000);
  }

  @Override
  protected void beforeRequest(HttpServletRequest request, String message) {}

  @Override
  protected void afterRequest(HttpServletRequest request, String message) {}
}
