package com.eucalyptuslabs.backend.common.filters.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * The controller advice which logs endpoint response on debug level. It should not be used in the
 * production as it may log sensitive data.
 */
@ControllerAdvice(basePackages = "com.eucalyptuslabs")
@ConditionalOnProperty(value = "debug.endpoint.logging.response", havingValue = "true")
public class DebugLoggingResponseBodyAdvice implements ResponseBodyAdvice<Object> {
  private final ObjectMapper objectMapper = new ObjectMapper();

  private static final Logger logger =
      LoggerFactory.getLogger(DebugLoggingResponseBodyAdvice.class);

  @Override
  public boolean supports(
      MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
    return true;
  }

  @Override
  public Object beforeBodyWrite(
      Object o,
      MethodParameter methodParameter,
      MediaType mediaType,
      Class<? extends HttpMessageConverter<?>> aClass,
      ServerHttpRequest serverHttpRequest,
      ServerHttpResponse serverHttpResponse) {

    try {
      if (mediaType == MediaType.APPLICATION_JSON) {
        String text = objectMapper.writeValueAsString(o);
        logger.debug(
            "Debug response logging: {}", text.substring(0, Math.min(1000, text.length())));
      } else {
        logger.debug("Debug response logging disabled for media type: {}", mediaType);
      }
    } catch (JsonProcessingException e) {
      logger.debug("Debug response logging: {}", o);
    }

    return o;
  }
}
