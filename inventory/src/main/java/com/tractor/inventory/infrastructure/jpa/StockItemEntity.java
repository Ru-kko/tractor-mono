package com.tractor.inventory.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "inventory_stock_item")
public class StockItemEntity {

  @Id
  @Column(name = "tractor_id")
  private UUID tractorId;

  @Column(name = "quantity", nullable = false)
  private int quantity;

  @Column(name = "price", nullable = false)
  private BigDecimal price;

  @Version
  @Column(name = "version", nullable = false)
  private long version;

  protected StockItemEntity() {
  }

  public StockItemEntity(UUID tractorId, int quantity, BigDecimal price) {
    this.tractorId = tractorId;
    this.quantity = quantity;
    this.price = price;
  }

  public UUID getTractorId() {
    return tractorId;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public long getVersion() {
    return version;
  }
}
