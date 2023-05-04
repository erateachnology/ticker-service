package com.eucalyptuslabs.backend.common.model.exception;

public class ProviderFailureException extends Exception {

  public ProviderFailureException(Exception exception) {
    super(exception);
  }

  public ProviderFailureException(String message) {
    super(message);
  }
}
