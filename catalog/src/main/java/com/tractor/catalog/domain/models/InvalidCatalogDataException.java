package com.tractor.catalog.domain.models;

import com.tractor.common.error.TractorStoreException;

public final class InvalidCatalogDataException extends TractorStoreException {

  public InvalidCatalogDataException(String message) {
    super(message);
  }

  @Override
  public String code() {
    return "INVALID_CATALOG_DATA";
  }
}
