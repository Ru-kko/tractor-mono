package com.tractor.inventory.infrastructure.jpa;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StockItemJpaRepository extends JpaRepository<StockItemEntity, UUID> {
  List<StockItemEntity> findAllByTractorIdIn(Collection<UUID> tractorIds);
}
