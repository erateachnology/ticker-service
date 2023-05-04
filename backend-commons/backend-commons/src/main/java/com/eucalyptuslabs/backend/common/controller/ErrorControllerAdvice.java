package com.eucalyptuslabs.backend.common.controller;

import com.eucalyptuslabs.backend.common.filters.IdGenerator;
import com.eucalyptuslabs.backend.common.service.TimeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * The common and default controller advice for logging unhandled exceptions. It logs the failed
 * request, header names and request body structure (in case of JSON) without values.
 *
 * <p>To override this default controller advice one has to create another controller advice and
 * annotate it with
 *
 * <pre>{@code
 * @ControllerAdvice(basePackageClasses = YourController.class)
 * @Order(value = Ordered.HIGHEST_PRECEDENCE)
 * public class YourControllerAdvice extends ErrorControllerAdvice { }
 * }</pre>
 */
@ControllerAdvice(basePackages = "com.eucalyptuslabs.backend")
public class ErrorControllerAdvice extends BaseControllerAdvice {

  public ErrorControllerAdvice(
      ObjectMapper objectMapper, TimeService timeService, IdGenerator idGenerator) {
    super(objectMapper, timeService, idGenerator);
  }

  @ExceptionHandler(value = Exception.class)
  public ResponseEntity<ErrorResponse> generalException(
      HttpServletRequest request, Exception exception) {
    return handleUnknownError(exception, request, 500, "HTTP-500", null);
  }

  @ExceptionHandler(value = HttpStatusCodeException.class)
  public ResponseEntity<ErrorResponse> httpStatusCodeException(
      HttpServletRequest request, HttpStatusCodeException exception) {
    return handleUnknownError(
        exception,
        request,
        exception.getStatusCode().value(),
        "HTTP-" + exception.getStatusCode().value(),
        exception.getResponseBodyAsString());
  }
}
