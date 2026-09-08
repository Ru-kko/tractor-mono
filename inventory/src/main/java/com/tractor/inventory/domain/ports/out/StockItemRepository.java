package com.tractor.inventory.domain.ports.out;

import com.tractor.inventory.domain.models.StockItem;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockItemRepository {
  Optional<StockItem> findById(UUID tractorId);
  StockItem save(StockItem item);
  List<StockItem> findEachById(Collection<UUID> tractorIds);
}
