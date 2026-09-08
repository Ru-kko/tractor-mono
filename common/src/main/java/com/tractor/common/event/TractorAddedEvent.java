package com.tractor.common.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record TractorAddedEvent(
    UUID tractorId,
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
