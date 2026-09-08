package com.tractor.catalog.domain.models;

import com.tractor.common.error.TractorStoreException;

public final class DuplicateBrandException extends TractorStoreException {

  public DuplicateBrandException(String name) {
    super("Brand already exists: " + name);
  }

  @Override
  public String code() {
    return "DUPLICATE_BRAND";
  }
}
