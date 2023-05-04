package com.eucalyptuslabs.backend.common.model.exception;

public class NotFoundException extends Exception {

  private final String errorCode;

  public NotFoundException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
