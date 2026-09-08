package com.tractor.inventory.application.inventoryservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tractor.common.error.TractorNotFoundException;
import com.tractor.common.event.StockUpdatedEvent;
import com.tractor.common.event.TractorBackInStockEvent;
import com.tractor.inventory.application.InventoryService;
import com.tractor.inventory.application.StockSnapshot;
import com.tractor.inventory.domain.models.InvalidTractorDataException;
import com.tractor.inventory.domain.models.StockItem;
import com.tractor.inventory.domain.ports.in.InventoryUseCase;
import com.tractor.inventory.domain.ports.out.EventPublisher;
import com.tractor.inventory.domain.ports.out.StockItemRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RefillStockTest {

  private static final UUID TRACTOR_ID = UUID.randomUUID();

  private final StockItemRepository repository = mock(StockItemRepository.class);
  private final EventPublisher events = mock(EventPublisher.class);
  private final InventoryUseCase service = new InventoryService(repository, events);

  @Test
  void increasesStockAndPublishesStockUpdatedEvent() {
    StockItem item = StockItem.builder().tractorId(TRACTOR_ID).stock(5).price(new BigDecimal("100")).build();
    when(repository.findById(TRACTOR_ID)).thenReturn(Optional.of(item));
    when(repository.save(item)).thenReturn(item);

    StockSnapshot snapshot = service.refillStock(TRACTOR_ID, 3);

    assertEquals(8, snapshot.stock());
    verify(repository).save(item);
    verify(events).publishStockUpdated(new StockUpdatedEvent(TRACTOR_ID, 8));
    verify(events, never()).publishTractorOutOfStock(any());
    verify(events, never()).publishTractorBackInStock(any());
  }

  @Test
  void publishesTractorBackInStockWhenStockWasZero() {
    StockItem item = StockItem.builder().tractorId(TRACTOR_ID).stock(0).price(new BigDecimal("100")).build();
    when(repository.findById(TRACTOR_ID)).thenReturn(Optional.of(item));
    when(repository.save(item)).thenReturn(item);

    StockSnapshot snapshot = service.refillStock(TRACTOR_ID, 3);

    assertEquals(3, snapshot.stock());
    verify(events).publishTractorBackInStock(new TractorBackInStockEvent(TRACTOR_ID));
    verify(events, never()).publishTractorOutOfStock(any());
  }

  @Test
  void rejectsUnknownTractor() {
    when(repository.findById(TRACTOR_ID)).thenReturn(Optional.empty());

    TractorNotFoundException exception = assertThrows(
        TractorNotFoundException.class, () -> service.refillStock(TRACTOR_ID, 3));

    assertEquals(TRACTOR_ID, exception.tractorId());
    verifyNoInteractions(events);
  }

  @Test
  void rejectsNonPositiveAmount() {
    assertThrows(InvalidTractorDataException.class, () -> service.refillStock(TRACTOR_ID, 0));

    verifyNoInteractions(repository);
    verifyNoInteractions(events);
  }
}
