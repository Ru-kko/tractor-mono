package com.tractor.catalog.domain.ports.in;

import com.tractor.catalog.application.brand.BrandSnapshot;
import com.tractor.catalog.application.brand.CreateBrandCommand;
import com.tractor.catalog.application.category.CategorySnapshot;
import com.tractor.catalog.application.category.CreateCategoryCommand;
import com.tractor.catalog.application.search.SearchCatalogQuery;
import com.tractor.catalog.application.search.SearchCatalogResult;
import com.tractor.common.event.TractorAddedEvent;
import com.tractor.common.event.TractorBackInStockEvent;
import com.tractor.common.event.TractorOutOfStockEvent;

public interface CatalogUseCase {
  SearchCatalogResult search(SearchCatalogQuery query);

  BrandSnapshot createBrand(CreateBrandCommand command);

  CategorySnapshot createCategory(CreateCategoryCommand command);

  void handleTractorAdded(TractorAddedEvent event);

  void handleTractorOutOfStock(TractorOutOfStockEvent event);

  void handleTractorBackInStock(TractorBackInStockEvent event);
}
