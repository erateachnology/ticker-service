package com.eucalyptuslabs.backend.common.controller;

import com.eucalyptuslabs.backend.common.filters.IdGenerator;
import com.eucalyptuslabs.backend.common.service.TimeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.list;

/** The base class for controller advices. */
public abstract class BaseControllerAdvice {

  protected final Logger logger = LoggerFactory.getLogger(getClass());
  private final ObjectMapper objectMapper;
  private final TimeService timeService;
  private final IdGenerator idGenerator;

  public BaseControllerAdvice(
      ObjectMapper objectMapper, TimeService timeService, IdGenerator idGenerator) {
    this.objectMapper = objectMapper;
    this.timeService = timeService;
    this.idGenerator = idGenerator;
  }

  protected ResponseEntity<ErrorResponse> handleKnownError(
      Exception exception,
      HttpServletRequest request,
      int responseCode,
      String internalCode,
      String message) {
    String errorId = idGenerator.getNextId();
    String timestamp = timeService.getCurrentZonedDateTime().format(DateTimeFormatter.ISO_INSTANT);
    logError(exception, request, responseCode, internalCode, message, null, errorId);
    ErrorResponse response =
        new ErrorResponse(errorId, timestamp, internalCode, message, request.getRequestURI());
    return ResponseEntity.status(responseCode).body(response);
  }

  protected ResponseEntity<ErrorResponse> handleUnknownError(
      Exception exception,
      HttpServletRequest request,
      int responseCode,
      String internalCode,
      String body) {
    String errorId = idGenerator.getNextId();
    String timestamp = timeService.getCurrentZonedDateTime().format(DateTimeFormatter.ISO_INSTANT);
    logError(exception, request, responseCode, internalCode, null, body, errorId);
    ErrorResponse response =
        new ErrorResponse(errorId, timestamp, internalCode, null, request.getRequestURI());
    return ResponseEntity.status(responseCode).body(response);
  }

  private void logError(
      Exception exception,
      HttpServletRequest request,
      int responseCode,
      String internalCode,
      String message,
      String body,
      String errorId) {
    logger.warn(
        String.format(
            "Error ID=%s for %s, responseCode=%d, internalCode=%s, message=%s, body=%s",
            errorId, prepareRequestLog(request), responseCode, internalCode, message, body),
        exception);
  }

  protected String prepareRequestLog(HttpServletRequest request) {
    return String.format(
        "request method=%s, URI=%s, params=%s, body=%s, headers=%s",
        request.getMethod(),
        request.getRequestURI(),
        request.getQueryString(),
        getRequestBody(request),
        list(request.getHeaderNames()));
  }

  protected String getRequestBody(HttpServletRequest request) {
    if (!(request instanceof ContentCachingRequestWrapper)) {
      throw new IllegalStateException("Request caching filter not enabled");
    }
    try {
      byte[] contentAsByteArray = ((ContentCachingRequestWrapper) request).getContentAsByteArray();
      if (contentAsByteArray.length == 0) {
        return "<no request body>";
      }
      Map map = objectMapper.readValue(contentAsByteArray, HashMap.class);
      return obfuscateJson(map).toString();
    } catch (Exception e) {
      logger.warn("Error during the debug logging", e);
      return "<error parsing request body to JSON>";
    }
  }

  public static Object obfuscateJson(Object jsonNode) {
    if (jsonNode instanceof Map<?, ?> map) {
      HashMap<Object, Object> output = new HashMap<>();
      map.forEach((key, value) -> output.put(key, obfuscateJson(value)));
      return output;
    }
    if (jsonNode instanceof Collection<?> collection) {
      ArrayList<Object> output = new ArrayList<>();
      collection.forEach(e -> output.add(obfuscateJson(e)));
      return output;
    }
    if (jsonNode == null) {
      return null;
    }
    return "***";
  }
}
