package com.tractor.catalog.infrastructure.jpa.entry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "catalog_tractor")
public class CatalogEntryEntity {

  @Id
  @Column(name = "tractor_id")
  private UUID tractorId;

  @Column(name = "brand", nullable = false)
  private String brand;

  @Column(name = "model", nullable = false)
  private String model;

  @Column(name = "year", nullable = false)
  private int year;

  @Column(name = "price", nullable = false)
  private BigDecimal price;

  @Column(name = "horsepower", nullable = false)
  private int horsepower;

  @Column(name = "weight", nullable = false)
  private BigDecimal weight;

  @Column(name = "color", nullable = false)
  private String color;

  @Column(name = "category", nullable = false)
  private String category;

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "image_url", nullable = false)
  private String imageUrl;

  @Column(name = "stock", nullable = false)
  private int stock;

  @Column(name = "available", nullable = false)
  private boolean available;

  @Version
  @Column(name = "version", nullable = false)
  private long version;
}
