package com.tractor.catalog.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public final class CatalogEntry {

  private final UUID tractorId;
  private final String brand;
  private final String model;
  private final int year;
  private final BigDecimal price;
  private final int horsepower;
  private final BigDecimal weight;
  private final String color;
  private final String category;
  private final String description;
  private final String imageUrl;
  private final int stock;
  private boolean available;

  public void markUnavailable() {
    this.available = false;
  }

  public void markAvailable() {
    this.available = true;
  }
}
