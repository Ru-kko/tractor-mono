package com.tractor.catalog.domain.ports.out;

import com.tractor.catalog.domain.models.Brand;

import java.util.Optional;

public interface BrandRepository {
  Optional<Brand> findByName(String name);

  Brand save(Brand brand);
}
