package com.tractor.catalog.domain.models;

import com.tractor.common.error.TractorStoreException;

public final class InvalidCursorException extends TractorStoreException {

  public InvalidCursorException(String message) {
    super(message);
  }

  @Override
  public String code() {
    return "INVALID_CURSOR";
  }
}
