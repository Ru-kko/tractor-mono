package com.tractor.inventory.application.inventoryservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tractor.inventory.application.InventoryService;
import com.tractor.inventory.domain.models.StockItem;
import com.tractor.inventory.domain.ports.in.InventoryUseCase;
import com.tractor.inventory.domain.ports.out.EventPublisher;
import com.tractor.inventory.domain.ports.out.StockItemRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetCurrentStockTest {

  private static final UUID TRACTOR_ID = UUID.randomUUID();

  private final StockItemRepository repository = mock(StockItemRepository.class);
  private final EventPublisher events = mock(EventPublisher.class);
  private final InventoryUseCase service = new InventoryService(repository, events);

  @Test
  void returnsCurrentStockForKnownTractor() {
    StockItem item = StockItem.builder().tractorId(TRACTOR_ID).stock(7).price(new BigDecimal("100")).build();
    when(repository.findById(TRACTOR_ID)).thenReturn(Optional.of(item));

    Optional<Integer> stock = service.getCurrentStock(TRACTOR_ID);

    assertTrue(stock.isPresent());
    assertEquals(7, stock.get());
  }

  @Test
  void returnsEmptyForUnknownTractor() {
    when(repository.findById(TRACTOR_ID)).thenReturn(Optional.empty());

    Optional<Integer> stock = service.getCurrentStock(TRACTOR_ID);

    assertTrue(stock.isEmpty());
  }
}
