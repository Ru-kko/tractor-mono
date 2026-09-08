package com.tractor.catalog.domain.ports.out;

import com.tractor.catalog.application.search.CatalogCursor;
import com.tractor.catalog.application.search.CatalogFilter;
import com.tractor.catalog.application.search.SortDirection;
import com.tractor.catalog.application.search.SortField;
import com.tractor.catalog.domain.models.CatalogEntry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CatalogEntryRepository {
  Optional<CatalogEntry> findById(UUID tractorId);

  CatalogEntry save(CatalogEntry entry);

  long countMatching(List<CatalogFilter> filters);

  List<CatalogEntry> findPage(
      List<CatalogFilter> filters, SortField sortField, SortDirection sortDirection,
      CatalogCursor cursor, int limit);
}
