package com.tractor.catalog.infrastructure.kafka;

import com.tractor.catalog.domain.ports.in.CatalogUseCase;
import com.tractor.common.event.TractorOutOfStockEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TractorOutOfStockEventListener {
  private final CatalogUseCase useCase;

  @KafkaListener(
      topics = "inventory.tractor-out-of-stock", groupId = "catalog",
      containerFactory = "tractorOutOfStockListenerContainerFactory")
  public void onTractorOutOfStock(TractorOutOfStockEvent event) {
    useCase.handleTractorOutOfStock(event);
  }
}
