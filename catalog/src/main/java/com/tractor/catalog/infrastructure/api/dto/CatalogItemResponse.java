package com.tractor.catalog.infrastructure.api.dto;

import com.tractor.catalog.application.search.CatalogEntrySnapshot;

import java.math.BigDecimal;
import java.util.UUID;

public record CatalogItemResponse(
    UUID tractorId,
    String brand,
    String model,
    int year,
    BigDecimal price,
    int horsepower,
    BigDecimal weight,
    String color,
    String category,
    String description,
    String imageUrl,
    int stock) {

  public static CatalogItemResponse from(CatalogEntrySnapshot snapshot) {
    return new CatalogItemResponse(
        snapshot.tractorId(), snapshot.brand(), snapshot.model(), snapshot.year(), snapshot.price(),
        snapshot.horsepower(), snapshot.weight(), snapshot.color(), snapshot.category(),
        snapshot.description(), snapshot.imageUrl(), snapshot.stock());
  }
}
