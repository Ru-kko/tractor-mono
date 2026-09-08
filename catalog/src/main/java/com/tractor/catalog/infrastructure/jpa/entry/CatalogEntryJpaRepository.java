package com.tractor.catalog.infrastructure.jpa.entry;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CatalogEntryJpaRepository
    extends JpaRepository<CatalogEntryEntity, UUID>, JpaSpecificationExecutor<CatalogEntryEntity> {
}
