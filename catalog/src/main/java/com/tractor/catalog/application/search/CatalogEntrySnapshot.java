package com.tractor.catalog.application.search;

import java.math.BigDecimal;
import java.util.UUID;

public record CatalogEntrySnapshot(
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
}
