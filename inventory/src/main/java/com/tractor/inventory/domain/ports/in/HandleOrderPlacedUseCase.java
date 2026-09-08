package com.tractor.inventory.domain.ports.in;

import com.tractor.common.event.OrderPlacedEvent;

public interface HandleOrderPlacedUseCase {
  void handle(OrderPlacedEvent event);
}
