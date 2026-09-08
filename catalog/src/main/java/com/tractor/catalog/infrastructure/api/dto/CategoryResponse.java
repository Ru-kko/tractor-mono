package com.tractor.catalog.infrastructure.api.dto;

import com.tractor.catalog.application.category.CategorySnapshot;

import java.util.UUID;

public record CategoryResponse(UUID id, String name) {

  public static CategoryResponse from(CategorySnapshot snapshot) {
    return new CategoryResponse(snapshot.id(), snapshot.name());
  }
}
