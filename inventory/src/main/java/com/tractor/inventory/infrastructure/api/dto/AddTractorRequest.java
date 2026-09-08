package com.tractor.inventory.infrastructure.api.dto;

import java.math.BigDecimal;

public record AddTractorRequest(
    int stock,
    BigDecimal price,
    String description,
    String imageUrl,
    String category,
    String brand,
    String model,
    int year,
    String color,
    BigDecimal weight,
    int horsepower) {
}
