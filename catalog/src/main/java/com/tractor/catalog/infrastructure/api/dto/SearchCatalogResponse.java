package com.tractor.catalog.infrastructure.api.dto;

import com.tractor.catalog.application.search.SearchCatalogResult;

import java.util.List;

public record SearchCatalogResponse(
    List<CatalogItemResponse> items, long totalItems, int page, int pageSize, int totalPages, String nextCursor) {

  public static SearchCatalogResponse from(SearchCatalogResult result) {
    return new SearchCatalogResponse(
        result.items().stream().map(CatalogItemResponse::from).toList(),
        result.totalItems(), result.page(), result.pageSize(), result.totalPages(), result.nextCursor());
  }
}
