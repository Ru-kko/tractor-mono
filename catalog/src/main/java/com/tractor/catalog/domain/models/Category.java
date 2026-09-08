package com.tractor.catalog.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public final class Category {

  private final UUID id;
  private final String name;
}
