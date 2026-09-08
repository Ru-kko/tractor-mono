package com.tractor.inventory.application.inventoryservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tractor.common.event.TractorAddedEvent;
import com.tractor.common.event.TractorOutOfStockEvent;
import com.tractor.inventory.application.AddTractorCommand;
import com.tractor.inventory.application.InventoryService;
import com.tractor.inventory.application.TractorSnapshot;
import com.tractor.inventory.domain.models.InvalidTractorDataException;
import com.tractor.inventory.domain.models.StockItem;
import com.tractor.inventory.domain.ports.in.InventoryUseCase;
import com.tractor.inventory.domain.ports.out.EventPublisher;
import com.tractor.inventory.domain.ports.out.StockItemRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AddTractorTest {

  private final StockItemRepository repository = mock(StockItemRepository.class);
  private final EventPublisher events = mock(EventPublisher.class);
  private final InventoryUseCase service = new InventoryService(repository, events);

  private AddTractorCommand validCommand() {
    return new AddTractorCommand(
        10, new BigDecimal("15000.00"), "Compact utility tractor", "https://example.com/t.png",
        "utility", "Acme", "X100", 2024, "green", new BigDecimal("1800.5"), 75);
  }

  @Test
  void createsTractorGeneratesUuidPersistsAndPublishesEvent() {
    when(repository.save(any(StockItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

    TractorSnapshot snapshot = service.addTractor(validCommand());

    assertNotNull(snapshot.tractorId());
    assertEquals(10, snapshot.stock());
    assertEquals(new BigDecimal("15000.00"), snapshot.price());

    verify(repository).save(any(StockItem.class));

    ArgumentCaptor<TractorAddedEvent> captor = ArgumentCaptor.forClass(TractorAddedEvent.class);
    verify(events).publishTractorAdded(captor.capture());
    TractorAddedEvent published = captor.getValue();
    assertEquals(snapshot.tractorId(), published.tractorId());
    assertEquals(10, published.stock());
    assertEquals("Acme", published.brand());
    verify(events, never()).publishTractorOutOfStock(any());
    verify(events, never()).publishTractorBackInStock(any());
  }

  @Test
  void publishesTractorOutOfStockWhenCreatedWithZeroStock() {
    when(repository.save(any(StockItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

    AddTractorCommand command = new AddTractorCommand(
        0, new BigDecimal("15000.00"), "Compact utility tractor", "https://example.com/t.png",
        "utility", "Acme", "X100", 2024, "green", new BigDecimal("1800.5"), 75);

    TractorSnapshot snapshot = service.addTractor(command);

    ArgumentCaptor<TractorOutOfStockEvent> captor = ArgumentCaptor.forClass(TractorOutOfStockEvent.class);
    verify(events).publishTractorOutOfStock(captor.capture());
    assertEquals(snapshot.tractorId(), captor.getValue().tractorId());
    verify(events, never()).publishTractorBackInStock(any());
  }

  @Test
  void rejectsNegativeStock() {
    AddTractorCommand command = new AddTractorCommand(
        -1, new BigDecimal("15000.00"), "desc", "img", "cat", "brand", "model", 2024, "color",
        new BigDecimal("100"), 50);

    assertThrows(InvalidTractorDataException.class, () -> service.addTractor(command));
  }

  @Test
  void rejectsNonPositivePrice() {
    AddTractorCommand command = new AddTractorCommand(
        10, BigDecimal.ZERO, "desc", "img", "cat", "brand", "model", 2024, "color",
        new BigDecimal("100"), 50);

    assertThrows(InvalidTractorDataException.class, () -> service.addTractor(command));
  }

  @Test
  void rejectsBlankDescription() {
    AddTractorCommand command = new AddTractorCommand(
        10, new BigDecimal("100"), " ", "img", "cat", "brand", "model", 2024, "color",
        new BigDecimal("100"), 50);

    assertThrows(InvalidTractorDataException.class, () -> service.addTractor(command));
  }

  @Test
  void rejectsNonPositiveWeight() {
    AddTractorCommand command = new AddTractorCommand(
        10, new BigDecimal("100"), "desc", "img", "cat", "brand", "model", 2024, "color",
        BigDecimal.ZERO, 50);

    assertThrows(InvalidTractorDataException.class, () -> service.addTractor(command));
  }

  @Test
  void rejectsNonPositiveHorsepower() {
    AddTractorCommand command = new AddTractorCommand(
        10, new BigDecimal("100"), "desc", "img", "cat", "brand", "model", 2024, "color",
        new BigDecimal("100"), 0);

    assertThrows(InvalidTractorDataException.class, () -> service.addTractor(command));
  }
}
