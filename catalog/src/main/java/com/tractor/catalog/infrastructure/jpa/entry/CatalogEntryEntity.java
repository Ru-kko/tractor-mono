package com.tractor.catalog.infrastructure.jpa.entry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
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

  protected CatalogEntryEntity() {
  }

  public CatalogEntryEntity(
      UUID tractorId, String brand, String model, int year, BigDecimal price, int horsepower,
      BigDecimal weight, String color, String category, String description, String imageUrl,
      int stock, boolean available) {
    this.tractorId = tractorId;
    this.brand = brand;
    this.model = model;
    this.year = year;
    this.price = price;
    this.horsepower = horsepower;
    this.weight = weight;
    this.color = color;
    this.category = category;
    this.description = description;
    this.imageUrl = imageUrl;
    this.stock = stock;
    this.available = available;
  }

  public UUID getTractorId() {
    return tractorId;
  }

  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public int getHorsepower() {
    return horsepower;
  }

  public void setHorsepower(int horsepower) {
    this.horsepower = horsepower;
  }

  public BigDecimal getWeight() {
    return weight;
  }

  public void setWeight(BigDecimal weight) {
    this.weight = weight;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public int getStock() {
    return stock;
  }

  public void setStock(int stock) {
    this.stock = stock;
  }

  public boolean isAvailable() {
    return available;
  }

  public void setAvailable(boolean available) {
    this.available = available;
  }

  public long getVersion() {
    return version;
  }
}
