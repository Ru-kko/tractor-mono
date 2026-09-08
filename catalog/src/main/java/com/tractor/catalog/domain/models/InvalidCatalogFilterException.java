package com.tractor.catalog.domain.models;

import com.tractor.common.error.TractorStoreException;

public final class InvalidCatalogFilterException extends TractorStoreException {

  public InvalidCatalogFilterException(String message) {
    super(message);
  }

  @Override
  public String code() {
    return "INVALID_CATALOG_FILTER";
  }
}
