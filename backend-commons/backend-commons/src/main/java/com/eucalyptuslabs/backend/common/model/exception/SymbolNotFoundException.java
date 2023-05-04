package com.eucalyptuslabs.backend.common.model.exception;

public class SymbolNotFoundException extends NotFoundException {

  public SymbolNotFoundException(String currencySymbol) {
    super("SYMBOL_NOT_FOUND", String.format("Symbol not found: %s", currencySymbol));
  }
}
