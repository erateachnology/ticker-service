package com.eucalyptuslabs.backend.common.filters.logging;

import com.eucalyptuslabs.backend.common.filters.RequestCachingFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * The controller's filter which logs the incoming request on the debug level. Request's body is
 * also included in the log message. It should not be used in the production as it may log sensitive
 * data.
 */
@Component
@ConditionalOnProperty(value = "debug.endpoint.logging.request", havingValue = "true")
public class DebugLoggingRequestFilter extends RequestCachingFilter {

  public DebugLoggingRequestFilter() {
    setIncludeQueryString(true);
    setIncludePayload(true);
    setMaxPayloadLength(10000);
    setIncludeHeaders(true);
    setAfterMessagePrefix("Debug request logging: ");
  }

  @Override
  protected void afterRequest(HttpServletRequest request, String message) {
    logger.debug(message);
  }
}
