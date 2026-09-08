package com.tractor.catalog.domain.ports.out;

import com.tractor.catalog.domain.models.Category;

import java.util.Optional;

public interface CategoryRepository {
  Optional<Category> findByName(String name);

  Category save(Category category);
}
