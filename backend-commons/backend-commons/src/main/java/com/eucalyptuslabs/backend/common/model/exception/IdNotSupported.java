package com.eucalyptuslabs.backend.common.model.exception;

public class IdNotSupported extends NotFoundException {

  public IdNotSupported(String id) {
    super("ERROR_NOT_SUPPORTED_BLOCKCHAIN_ID", String.format("Unsupported coin type: %s", id));
  }
}
