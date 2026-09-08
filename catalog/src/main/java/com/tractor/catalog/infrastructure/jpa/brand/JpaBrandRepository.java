package com.tractor.catalog.infrastructure.jpa.brand;

import com.tractor.catalog.domain.models.Brand;
import com.tractor.catalog.domain.ports.out.BrandRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class JpaBrandRepository implements BrandRepository {
  private final BrandJpaRepository jpaRepository;

  @Override
  public Optional<Brand> findByName(String name) {
    return jpaRepository.findByNameIgnoreCase(name).map(entity -> Brand.builder()
        .id(entity.getId())
        .name(entity.getName())
        .build());
  }

  @Override
  public Brand save(Brand brand) {
    UUID id = brand.getId() != null ? brand.getId() : UUID.randomUUID();
    BrandEntity saved = jpaRepository.save(new BrandEntity(id, brand.getName()));
    return Brand.builder().id(saved.getId()).name(saved.getName()).build();
  }
}
