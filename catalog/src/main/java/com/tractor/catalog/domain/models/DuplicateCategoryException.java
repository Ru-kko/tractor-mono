package com.tractor.catalog.domain.models;

import com.tractor.common.error.TractorStoreException;

public final class DuplicateCategoryException extends TractorStoreException {

  public DuplicateCategoryException(String name) {
    super("Category already exists: " + name);
  }

  @Override
  public String code() {
    return "DUPLICATE_CATEGORY";
  }
}
