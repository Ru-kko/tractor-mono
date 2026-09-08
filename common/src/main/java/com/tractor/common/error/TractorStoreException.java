package com.tractor.common.error;

public abstract class TractorStoreException extends RuntimeException {

  protected TractorStoreException(String message) {
    super(message);
  }

  public abstract String code();
}
