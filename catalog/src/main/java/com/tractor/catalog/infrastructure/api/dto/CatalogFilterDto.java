package com.tractor.catalog.infrastructure.api.dto;

import com.tractor.catalog.application.search.CatalogFilter;
import com.tractor.catalog.application.search.FilterField;
import com.tractor.catalog.application.search.FilterOperator;
import com.tractor.catalog.domain.models.InvalidCatalogFilterException;

public record CatalogFilterDto(String field, String operator, String value, String min, String max) {

  public CatalogFilter toFilter() {
    return new CatalogFilter(parseField(), parseOperator(), value, min, max);
  }

  private FilterField parseField() {
    try {
      return FilterField.valueOf(field);
    } catch (IllegalArgumentException | NullPointerException exception) {
      throw new InvalidCatalogFilterException("Unknown filter field: " + field);
    }
  }

  private FilterOperator parseOperator() {
    try {
      return FilterOperator.valueOf(operator);
    } catch (IllegalArgumentException | NullPointerException exception) {
      throw new InvalidCatalogFilterException("Unknown filter operator: " + operator);
    }
  }
}
