package com.tractor.inventory.domain.models;

import com.tractor.common.error.TractorStoreException;
import java.util.UUID;

public final class InsufficientStockException extends TractorStoreException {

  private final UUID tractorId;
  private final int currentStock;
  private final int requestedAmount;

  public InsufficientStockException(UUID tractorId, int currentStock, int requestedAmount) {
    super("Insufficient stock for tractor " + tractorId + ": current=" + currentStock
        + ", requested=" + requestedAmount);
    this.tractorId = tractorId;
    this.currentStock = currentStock;
    this.requestedAmount = requestedAmount;
  }

  public UUID tractorId() {
    return tractorId;
  }

  public int currentStock() {
    return currentStock;
  }

  public int requestedAmount() {
    return requestedAmount;
  }

  @Override
  public String code() {
    return "INSUFFICIENT_STOCK";
  }
}
