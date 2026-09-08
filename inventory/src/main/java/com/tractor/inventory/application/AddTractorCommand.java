package com.tractor.inventory.application;

import java.math.BigDecimal;

public record AddTractorCommand(
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
