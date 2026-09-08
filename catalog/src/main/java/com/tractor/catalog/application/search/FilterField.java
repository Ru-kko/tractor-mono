package com.tractor.catalog.application.search;

import java.util.Set;

public enum FilterField {
  BRAND,
  MODEL,
  COLOR,
  YEAR,
  PRICE,
  HORSEPOWER,
  WEIGHT;

  private static final Set<FilterField> STRING_FIELDS = Set.of(BRAND, MODEL, COLOR);

  public boolean supports(FilterOperator operator) {
    if (STRING_FIELDS.contains(this)) {
      return operator == FilterOperator.EQUALS || operator == FilterOperator.NOT_EQUALS;
    }
    return true;
  }
}
