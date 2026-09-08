package com.tractor.catalog.infrastructure.api.dto;

import com.tractor.catalog.application.search.SearchCatalogQuery;
import com.tractor.catalog.application.search.SortDirection;
import com.tractor.catalog.domain.models.InvalidCatalogFilterException;

import java.util.List;

public record SearchCatalogRequest(
    List<CatalogFilterDto> filters, String sortField, String sortDirection, String cursor, Integer pageSize) {

  public SearchCatalogQuery toQuery() {
    List<CatalogFilterDto> safeFilters = filters == null ? List.of() : filters;
    return new SearchCatalogQuery(
        safeFilters.stream().map(CatalogFilterDto::toFilter).toList(),
        sortField, parseSortDirection(), cursor, pageSize);
  }

  private SortDirection parseSortDirection() {
    if (sortDirection == null) {
      return null;
    }
    try {
      return SortDirection.valueOf(sortDirection);
    } catch (IllegalArgumentException exception) {
      throw new InvalidCatalogFilterException("Unknown sort direction: " + sortDirection);
    }
  }
}
