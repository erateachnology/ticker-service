package com.eucalyptuslabs.backend.common.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * The controller's filter which adds to the Logback's logging context a reference ID. This
 * reference ID is attached to the current thread and added to every log produced along the way of
 * request processing.
 */
@Component
public class AssignReferenceIdFilter extends OncePerRequestFilter {

  @Autowired private IdGenerator referenceIdGenerator;

  @Override
  protected void doFilterInternal(
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse,
      FilterChain filterChain)
      throws ServletException, IOException {
    String refId = referenceIdGenerator.getNextId();
    httpServletRequest.setAttribute("refId", refId);
    MDC.put("refId", refId);
    filterChain.doFilter(httpServletRequest, httpServletResponse);
    MDC.clear();
  }
}
