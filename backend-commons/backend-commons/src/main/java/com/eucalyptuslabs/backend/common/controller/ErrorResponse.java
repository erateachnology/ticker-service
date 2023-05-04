package com.eucalyptuslabs.backend.common.controller;

import com.fasterxml.jackson.annotation.JsonInclude;

/** The DTO class used by the {@link ErrorControllerAdvice}. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    String errorId, String timestamp, String errorCode, String errorMessage, String path) {}
