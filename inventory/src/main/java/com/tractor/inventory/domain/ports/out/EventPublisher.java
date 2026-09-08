package com.tractor.inventory.domain.ports.out;

import com.tractor.common.event.StockUpdatedEvent;
import com.tractor.common.event.TractorAddedEvent;
import com.tractor.common.event.TractorBackInStockEvent;
import com.tractor.common.event.TractorOutOfStockEvent;

public interface EventPublisher {

  void publishTractorAdded(TractorAddedEvent event);

  void publishStockUpdated(StockUpdatedEvent event);

  void publishTractorOutOfStock(TractorOutOfStockEvent event);

  void publishTractorBackInStock(TractorBackInStockEvent event);
}
