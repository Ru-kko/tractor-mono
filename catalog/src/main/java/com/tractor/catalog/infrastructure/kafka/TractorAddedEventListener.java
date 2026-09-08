package com.tractor.catalog.infrastructure.kafka;

import com.tractor.catalog.domain.ports.in.CatalogUseCase;
import com.tractor.common.event.TractorAddedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TractorAddedEventListener {
  private final CatalogUseCase useCase;

  @KafkaListener(
      topics = "inventory.tractor-added", groupId = "catalog",
      containerFactory = "tractorAddedListenerContainerFactory")
  public void onTractorAdded(TractorAddedEvent event) {
    useCase.handleTractorAdded(event);
  }
}
