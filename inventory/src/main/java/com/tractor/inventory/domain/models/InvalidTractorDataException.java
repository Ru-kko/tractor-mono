package com.tractor.inventory.domain.models;

import com.tractor.common.error.TractorStoreException;

public final class InvalidTractorDataException extends TractorStoreException {

  public InvalidTractorDataException(String message) {
    super(message);
  }

  @Override
  public String code() {
    return "INVALID_TRACTOR_DATA";
  }
}
