package com.tractor.catalog.infrastructure.jpa.brand;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandJpaRepository extends JpaRepository<BrandEntity, UUID> {
  Optional<BrandEntity> findByNameIgnoreCase(String name);
}
