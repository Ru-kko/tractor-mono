package com.tractor.inventory.infrastructure.jpa;

import com.tractor.inventory.domain.models.StockItem;
import com.tractor.inventory.domain.ports.out.StockItemRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class JpaStockItemRepository implements StockItemRepository {
  private final StockItemJpaRepository jpaRepository;

  @Override
  public Optional<StockItem> findById(UUID tractorId) {
    return jpaRepository.findById(tractorId)
        .map(entity -> StockItem.builder()
                .tractorId(entity.getTractorId())
                .stock(entity.getQuantity())
                .price(entity.getPrice())
                .build()
        );
  }

  @Override
  public StockItem save(StockItem item) {
    StockItemEntity entity = jpaRepository.findById(item.getTractorId())
        .map(existing -> {
          existing.setQuantity(item.getStock());
          return existing;
        })
        .orElseGet(() -> StockItemEntity.builder()
            .tractorId(item.getTractorId())
            .quantity(item.getStock())
            .price(item.getPrice())
            .build()
        );

    StockItemEntity saved = jpaRepository.save(entity);
    return StockItem.builder()
            .tractorId(saved.getTractorId())
            .stock(saved.getQuantity())
            .price(saved.getPrice())
            .build();
  }

  @Override
  public List<StockItem> findEachById(Collection<UUID> tractorIds) {
    return jpaRepository.findAllByTractorIdIn(tractorIds).stream()
        .map(entity -> StockItem.builder()
                .tractorId(entity.getTractorId())
                .stock(entity.getQuantity())
                .price(entity.getPrice())
                .build())
        .toList();
  }
}
