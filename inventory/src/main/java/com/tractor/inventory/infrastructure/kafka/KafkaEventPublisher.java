package com.tractor.inventory.infrastructure.kafka;

import com.tractor.common.event.StockUpdatedEvent;
import com.tractor.common.event.TractorAddedEvent;
import com.tractor.common.event.TractorBackInStockEvent;
import com.tractor.common.event.TractorOutOfStockEvent;
import com.tractor.inventory.domain.ports.out.EventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public final class KafkaEventPublisher implements EventPublisher {

  private static final String TRACTOR_ADDED_TOPIC = "inventory.tractor-added";
  private static final String STOCK_UPDATED_TOPIC = "inventory.stock-updated";
  private static final String TRACTOR_OUT_OF_STOCK_TOPIC = "inventory.tractor-out-of-stock";
  private static final String TRACTOR_BACK_IN_STOCK_TOPIC = "inventory.tractor-back-in-stock";

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public void publishTractorAdded(TractorAddedEvent event) {
    kafkaTemplate.send(TRACTOR_ADDED_TOPIC, event.tractorId().toString(), event);
  }

  @Override
  public void publishStockUpdated(StockUpdatedEvent event) {
    kafkaTemplate.send(STOCK_UPDATED_TOPIC, event.tractorId().toString(), event);
  }

  @Override
  public void publishTractorOutOfStock(TractorOutOfStockEvent event) {
    kafkaTemplate.send(TRACTOR_OUT_OF_STOCK_TOPIC, event.tractorId().toString(), event);
  }

  @Override
  public void publishTractorBackInStock(TractorBackInStockEvent event) {
    kafkaTemplate.send(TRACTOR_BACK_IN_STOCK_TOPIC, event.tractorId().toString(), event);
  }
}
