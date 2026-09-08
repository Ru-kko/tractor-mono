package com.tractor.inventory.domain.ports.in;

import com.tractor.common.event.OrderPlacedEvent;
import com.tractor.inventory.application.AddTractorCommand;
import com.tractor.inventory.application.StockSnapshot;
import com.tractor.inventory.application.TractorSnapshot;

import java.util.Optional;
import java.util.UUID;

public interface InventoryUseCase {
  StockSnapshot getStock(UUID tractorId);
  Optional<Integer> getCurrentStock(UUID tractorId);
  TractorSnapshot addTractor(AddTractorCommand command);
  StockSnapshot refillStock(UUID tractorId, int quantity);
  void handleOrderPlaced(OrderPlacedEvent event);
}
