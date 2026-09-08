package com.tractor.inventory.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public final class StockItem {

  private final UUID tractorId;
  private int stock;
  private final BigDecimal price;

  public void refill(int amount) {
    this.stock += amount;
  }

  public void reserve(int amount) {
    if (amount > stock) {
      throw new InsufficientStockException(tractorId, stock, amount);
    }
    this.stock -= amount;
  }

  public boolean canReserve(int amount) {
    return amount <= stock;
  }
}
