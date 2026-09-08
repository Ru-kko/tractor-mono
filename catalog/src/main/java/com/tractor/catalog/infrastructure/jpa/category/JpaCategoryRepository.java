package com.tractor.catalog.infrastructure.jpa.category;

import com.tractor.catalog.domain.models.Category;
import com.tractor.catalog.domain.ports.out.CategoryRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class JpaCategoryRepository implements CategoryRepository {
  private final CategoryJpaRepository jpaRepository;

  @Override
  public Optional<Category> findByName(String name) {
    return jpaRepository.findByNameIgnoreCase(name).map(entity -> Category.builder()
        .id(entity.getId())
        .name(entity.getName())
        .build());
  }

  @Override
  public Category save(Category category) {
    UUID id = category.getId() != null ? category.getId() : UUID.randomUUID();
    CategoryEntity saved = jpaRepository.save(new CategoryEntity(id, category.getName()));
    return Category.builder().id(saved.getId()).name(saved.getName()).build();
  }
}
