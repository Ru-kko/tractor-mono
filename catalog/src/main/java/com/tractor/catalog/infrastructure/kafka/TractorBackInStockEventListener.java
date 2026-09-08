package com.tractor.catalog.infrastructure.kafka;

import com.tractor.catalog.domain.ports.in.CatalogUseCase;
import com.tractor.common.event.TractorBackInStockEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TractorBackInStockEventListener {
  private final CatalogUseCase useCase;

  @KafkaListener(
      topics = "inventory.tractor-back-in-stock", groupId = "catalog",
      containerFactory = "tractorBackInStockListenerContainerFactory")
  public void onTractorBackInStock(TractorBackInStockEvent event) {
    useCase.handleTractorBackInStock(event);
  }
}
