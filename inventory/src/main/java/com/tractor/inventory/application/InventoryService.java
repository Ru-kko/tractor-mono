package com.tractor.inventory.application;

import com.tractor.common.error.ErrorGroup;
import com.tractor.common.error.TractorNotFoundException;
import com.tractor.common.event.OrderLine;
import com.tractor.common.event.OrderPlacedEvent;
import com.tractor.common.event.StockUpdatedEvent;
import com.tractor.common.event.TractorAddedEvent;
import com.tractor.common.event.TractorBackInStockEvent;
import com.tractor.common.event.TractorOutOfStockEvent;
import com.tractor.inventory.domain.models.InvalidTractorDataException;
import com.tractor.inventory.domain.models.StockItem;
import com.tractor.inventory.domain.ports.in.InventoryUseCase;
import com.tractor.inventory.domain.ports.out.EventPublisher;
import com.tractor.inventory.domain.ports.out.StockItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService implements InventoryUseCase {
  private final StockItemRepository repository;
  private final EventPublisher events;

  @Override
  public StockSnapshot getStock(UUID tractorId) {
    StockItem item = repository.findById(tractorId)
            .orElseThrow(() -> new TractorNotFoundException(tractorId));

    return new StockSnapshot(item.getTractorId(), item.getStock());
  }

  @Override
  public Optional<Integer> getCurrentStock(UUID tractorId) {
    return repository.findById(tractorId).map(StockItem::getStock);
  }

  @Override
  @Transactional
  public TractorSnapshot addTractor(AddTractorCommand command) {
    validate(command);

    UUID tractorId = UUID.randomUUID();
    StockItem item = StockItem.builder()
      .tractorId(tractorId)
      .stock(command.stock())
      .price(command.price())
      .build();

    repository.save(item);

    events.publishTractorAdded(
      TractorAddedEvent.builder()
        .tractorId(tractorId)
        .stock(command.stock())
        .price(command.price())
        .description(command.description())
        .imageUrl(command.imageUrl())
        .category(command.category())
        .brand(command.brand())
        .model(command.model())
        .year(command.year())
        .color(command.color())
        .weight(command.weight()).horsepower(command.horsepower())
        .build()
    );

    if (item.getStock() == 0) {
      events.publishTractorOutOfStock(TractorOutOfStockEvent.builder().tractorId(tractorId).build());
    }

    return TractorSnapshot.builder()
      .tractorId(item.getTractorId())
      .stock(item.getStock())
      .price(item.getPrice())
      .build();
  }

  @Override
  @Transactional
  public StockSnapshot refillStock(UUID tractorId, int quantity) {
    if (quantity <= 0) {
      throw new InvalidTractorDataException("quantity must be greater than zero");
    }

    StockItem item = repository.findById(tractorId)
      .orElseThrow(() -> new TractorNotFoundException(tractorId));

    int previousStock = item.getStock();
    item.refill(quantity);
    repository.save(item);

    events.publishStockUpdated(
      StockUpdatedEvent.builder()
        .tractorId(item.getTractorId())
        .stock(item.getStock())
        .build()
    );

    publishAvailabilityTransition(item.getTractorId(), previousStock, item.getStock());

    return StockSnapshot.builder()
      .tractorId(item.getTractorId())
      .stock(item.getStock())
      .build();
  }

  @Override
  @Transactional
  public void handleOrderPlaced(OrderPlacedEvent event) {
     Map<UUID, Integer> lines = event.lines()
            .stream()
             .collect(
                     Collectors.toMap(OrderLine::tractorId, OrderLine::quantity, Integer::sum)
             );

    List<StockItem> items = repository.findEachById(
            lines.keySet()
    );

    if (items.size() != lines.size()) {
      Set<UUID> persistedItems = items.stream().collect(
              Collectors.toMap(StockItem::getTractorId, StockItem::getStock)
      ).keySet();

      List<TractorNotFoundException> errors = lines
              .keySet()
              .stream()
              .filter( id -> !persistedItems.contains(id))
              .map(TractorNotFoundException::new)
              .toList();

      throw new ErrorGroup("Some tractors not found", errors);
    }

    for (StockItem item : items) {
      int quantity = lines.get(item.getTractorId());
      int previousStock = item.getStock();
      item.reserve(quantity);
      repository.save(item);
      events.publishStockUpdated(
              StockUpdatedEvent.builder()
                      .tractorId(item.getTractorId())
                      .stock(item.getStock())
                      .build()
      );
      publishAvailabilityTransition(item.getTractorId(), previousStock, item.getStock());
    }
  }

  private void publishAvailabilityTransition(UUID tractorId, int previousStock, int currentStock) {
    if (previousStock > 0 && currentStock == 0) {
      events.publishTractorOutOfStock(TractorOutOfStockEvent.builder().tractorId(tractorId).build());
    } else if (previousStock == 0 && currentStock > 0) {
      events.publishTractorBackInStock(TractorBackInStockEvent.builder().tractorId(tractorId).build());
    }
  }

  private void validate(AddTractorCommand command) {
    if (command.stock() < 0) {
      throw new InvalidTractorDataException("stock must be greater than or equal to zero");
    }
    if (command.price() == null || command.price().signum() <= 0) {
      throw new InvalidTractorDataException("price must be greater than zero");
    }
    requireNonBlank(command.description(), "description");
    requireNonBlank(command.imageUrl(), "imageUrl");
    requireNonBlank(command.category(), "category");
    requireNonBlank(command.brand(), "brand");
    requireNonBlank(command.model(), "model");
    requireNonBlank(command.color(), "color");

    if (command.weight() == null || command.weight().signum() <= 0) {
      throw new InvalidTractorDataException("weight must be greater than zero");
    }
    if (command.horsepower() <= 0) {
      throw new InvalidTractorDataException("horsepower must be greater than zero");
    }
  }

  private void requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new InvalidTractorDataException(field + " must not be blank");
    }
  }
}
