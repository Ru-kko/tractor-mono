package com.tractor.inventory.infrastructure.workers;

import com.tractor.common.event.OrderPlacedEvent;
import com.tractor.inventory.domain.ports.in.HandleOrderPlacedUseCase;
import com.tractor.inventory.domain.ports.in.InventoryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderPlacedEventListener {
  private final InventoryUseCase useCase;

  @KafkaListener(topics = "order.order-placed", groupId = "inventory")
  public void onOrderPlaced(OrderPlacedEvent event) {
    useCase.handleOrderPlaced(event);
  }
}
