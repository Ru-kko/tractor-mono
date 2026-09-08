package com.tractor.inventory.application.inventoryservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tractor.common.event.OrderLine;
import com.tractor.common.event.OrderPlacedEvent;
import com.tractor.common.event.StockUpdatedEvent;
import com.tractor.common.event.TractorOutOfStockEvent;
import com.tractor.inventory.application.InventoryService;
import com.tractor.inventory.domain.models.InsufficientStockException;
import com.tractor.inventory.domain.models.StockItem;
import com.tractor.inventory.domain.ports.in.InventoryUseCase;
import com.tractor.inventory.domain.ports.out.EventPublisher;
import com.tractor.inventory.domain.ports.out.StockItemRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HandleOrderPlacedTest {

  private static final UUID TRACTOR_A = UUID.randomUUID();
  private static final UUID TRACTOR_B = UUID.randomUUID();

  private final StockItemRepository repository = mock(StockItemRepository.class);
  private final EventPublisher events = mock(EventPublisher.class);
  private final InventoryUseCase service = new InventoryService(repository, events);

  @Test
  void decreasesStockForEachLineAndPublishesStockUpdatedEvents() {
    StockItem itemA = StockItem.builder().tractorId(TRACTOR_A).stock(10).price(new BigDecimal("100")).build();
    StockItem itemB = StockItem.builder().tractorId(TRACTOR_B).stock(5).price(new BigDecimal("200")).build();
    when(repository.findEachById(any())).thenReturn(List.of(itemA, itemB));

    OrderPlacedEvent event = new OrderPlacedEvent(
        UUID.randomUUID(), List.of(new OrderLine(TRACTOR_A, 4), new OrderLine(TRACTOR_B, 2)));

    service.handleOrderPlaced(event);

    assertEquals(6, itemA.getStock());
    assertEquals(3, itemB.getStock());
    verify(repository).save(itemA);
    verify(repository).save(itemB);
    verify(events).publishStockUpdated(new StockUpdatedEvent(TRACTOR_A, 6));
    verify(events).publishStockUpdated(new StockUpdatedEvent(TRACTOR_B, 3));
    verify(events, never()).publishTractorOutOfStock(any());
    verify(events, never()).publishTractorBackInStock(any());
  }

  @Test
  void publishesTractorOutOfStockWhenLineExhaustsStock() {
    StockItem itemA = StockItem.builder().tractorId(TRACTOR_A).stock(4).price(new BigDecimal("100")).build();
    when(repository.findEachById(any())).thenReturn(List.of(itemA));

    OrderPlacedEvent event = new OrderPlacedEvent(UUID.randomUUID(), List.of(new OrderLine(TRACTOR_A, 4)));

    service.handleOrderPlaced(event);

    assertEquals(0, itemA.getStock());
    verify(events).publishTractorOutOfStock(new TractorOutOfStockEvent(TRACTOR_A));
  }

  @Test
  void leavesStockUnchangedWhenALineWouldGoNegative() {
    StockItem itemA = StockItem.builder().tractorId(TRACTOR_A).stock(10).price(new BigDecimal("100")).build();
    StockItem itemB = StockItem.builder().tractorId(TRACTOR_B).stock(1).price(new BigDecimal("200")).build();
    when(repository.findEachById(any())).thenReturn(List.of(itemB, itemA));

    OrderPlacedEvent event = new OrderPlacedEvent(
        UUID.randomUUID(), List.of(new OrderLine(TRACTOR_A, 4), new OrderLine(TRACTOR_B, 5)));

    assertThrows(InsufficientStockException.class, () -> service.handleOrderPlaced(event));

    assertEquals(10, itemA.getStock());
    assertEquals(1, itemB.getStock());
    verify(repository, never()).save(itemA);
    verify(repository, never()).save(itemB);
  }
}
