package com.tractor.catalog.infrastructure.jpa.category;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, UUID> {
  Optional<CategoryEntity> findByNameIgnoreCase(String name);
}
