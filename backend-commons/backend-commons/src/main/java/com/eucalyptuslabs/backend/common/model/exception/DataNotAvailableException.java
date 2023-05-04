package com.eucalyptuslabs.backend.common.model.exception;

public class DataNotAvailableException extends Exception {

  public DataNotAvailableException(String dataType) {
    super(String.format("Data not available: %s", dataType));
  }
}
