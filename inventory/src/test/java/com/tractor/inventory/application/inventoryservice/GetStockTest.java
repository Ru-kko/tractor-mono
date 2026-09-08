package com.tractor.inventory.application.inventoryservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tractor.common.error.TractorNotFoundException;
import com.tractor.inventory.application.InventoryService;
import com.tractor.inventory.application.StockSnapshot;
import com.tractor.inventory.domain.models.StockItem;
import com.tractor.inventory.domain.ports.in.InventoryUseCase;
import com.tractor.inventory.domain.ports.out.EventPublisher;
import com.tractor.inventory.domain.ports.out.StockItemRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetStockTest {

  private static final UUID TRACTOR_ID = UUID.randomUUID();

  private final StockItemRepository repository = mock(StockItemRepository.class);
  private final EventPublisher events = mock(EventPublisher.class);
  private final InventoryUseCase service = new InventoryService(repository, events);

  @Test
  void returnsCurrentStockForKnownTractor() {
    StockItem item = StockItem.builder().tractorId(TRACTOR_ID).stock(42).price(new BigDecimal("100")).build();
    when(repository.findById(TRACTOR_ID)).thenReturn(Optional.of(item));

    StockSnapshot snapshot = service.getStock(TRACTOR_ID);

    assertEquals(TRACTOR_ID, snapshot.tractorId());
    assertEquals(42, snapshot.stock());
  }

  @Test
  void rejectsUnknownTractor() {
    when(repository.findById(TRACTOR_ID)).thenReturn(Optional.empty());

    TractorNotFoundException exception =
        assertThrows(TractorNotFoundException.class, () -> service.getStock(TRACTOR_ID));

    assertEquals(TRACTOR_ID, exception.tractorId());
  }
}
