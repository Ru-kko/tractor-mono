package com.tractor.catalog.application.search;

import java.util.List;

public record SearchCatalogQuery(
    List<CatalogFilter> filters,
    String sortField,
    SortDirection sortDirection,
    String cursor,
    Integer pageSize) {
}
