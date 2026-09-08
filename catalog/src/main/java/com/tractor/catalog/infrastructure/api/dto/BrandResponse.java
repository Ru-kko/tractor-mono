package com.tractor.catalog.infrastructure.api.dto;

import com.tractor.catalog.application.brand.BrandSnapshot;

import java.util.UUID;

public record BrandResponse(UUID id, String name) {

  public static BrandResponse from(BrandSnapshot snapshot) {
    return new BrandResponse(snapshot.id(), snapshot.name());
  }
}
