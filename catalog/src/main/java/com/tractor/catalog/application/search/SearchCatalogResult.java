package com.tractor.catalog.application.search;

import java.util.List;

public record SearchCatalogResult(
    List<CatalogEntrySnapshot> items,
    long totalItems,
    int page,
    int pageSize,
    int totalPages,
    String nextCursor) {
}
