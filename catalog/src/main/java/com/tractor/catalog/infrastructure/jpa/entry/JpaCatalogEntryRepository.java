package com.tractor.catalog.infrastructure.jpa.entry;

import com.tractor.catalog.application.search.CatalogCursor;
import com.tractor.catalog.application.search.CatalogFilter;
import com.tractor.catalog.application.search.SortDirection;
import com.tractor.catalog.application.search.SortField;
import com.tractor.catalog.domain.models.CatalogEntry;
import com.tractor.catalog.domain.models.InvalidCatalogFilterException;
import com.tractor.catalog.domain.ports.out.CatalogEntryRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class JpaCatalogEntryRepository implements CatalogEntryRepository {
  private final CatalogEntryJpaRepository jpaRepository;

  @Override
  public Optional<CatalogEntry> findById(UUID tractorId) {
    return jpaRepository.findById(tractorId).map(this::toDomain);
  }

  @Override
  public CatalogEntry save(CatalogEntry entry) {
    CatalogEntryEntity entity = jpaRepository.findById(entry.getTractorId())
        .map(existing -> updateFrom(existing, entry))
        .orElseGet(() -> toEntity(entry));

    return toDomain(jpaRepository.save(entity));
  }

  @Override
  public long countMatching(List<CatalogFilter> filters) {
    return jpaRepository.count(buildSpecification(filters));
  }

  @Override
  public List<CatalogEntry> findPage(
      List<CatalogFilter> filters, SortField sortField, SortDirection sortDirection, CatalogCursor cursor,
      int limit) {
    Specification<CatalogEntryEntity> specification =
        buildSpecification(filters).and(seekSpecification(sortField, sortDirection, cursor));
    Pageable pageable = PageRequest.of(0, limit, toSort(sortField, sortDirection));

    return jpaRepository.findAll(specification, pageable).stream().map(this::toDomain).toList();
  }

  private Specification<CatalogEntryEntity> buildSpecification(List<CatalogFilter> filters) {
    Specification<CatalogEntryEntity> specification = (root, query, cb) -> cb.isTrue(root.get("available"));
    for (CatalogFilter filter : filters) {
      specification = specification.and((root, query, cb) -> toPredicate(filter, root, cb));
    }
    return specification;
  }

  private Predicate toPredicate(CatalogFilter filter, Root<CatalogEntryEntity> root, CriteriaBuilder cb) {
    return switch (filter.field()) {
      case BRAND -> stringPredicate(root.get("brand"), filter, cb);
      case MODEL -> stringPredicate(root.get("model"), filter, cb);
      case COLOR -> stringPredicate(root.get("color"), filter, cb);
      case YEAR -> this.<Integer>numericPredicate(root.get("year"), filter, cb, Integer::valueOf);
      case HORSEPOWER -> this.<Integer>numericPredicate(root.get("horsepower"), filter, cb, Integer::valueOf);
      case PRICE -> this.<BigDecimal>numericPredicate(root.get("price"), filter, cb, BigDecimal::new);
      case WEIGHT -> this.<BigDecimal>numericPredicate(root.get("weight"), filter, cb, BigDecimal::new);
    };
  }

  private Predicate stringPredicate(
      Path<String> path, CatalogFilter filter, CriteriaBuilder cb) {
    return switch (filter.operator()) {
      case EQUALS -> cb.equal(path, filter.value());
      case NOT_EQUALS -> cb.notEqual(path, filter.value());
      default -> throw new InvalidCatalogFilterException(
          "Operator " + filter.operator() + " not supported for field " + filter.field());
    };
  }

  private <T extends Comparable<T>> Predicate numericPredicate(
      Path<T> path, CatalogFilter filter, jakarta.persistence.criteria.CriteriaBuilder cb, Function<String, T> parser) {
    return switch (filter.operator()) {
      case EQUALS -> cb.equal(path, parser.apply(filter.value()));
      case NOT_EQUALS -> cb.notEqual(path, parser.apply(filter.value()));
      case GREATER_THAN -> cb.greaterThan(path, parser.apply(filter.value()));
      case LESS_THAN -> cb.lessThan(path, parser.apply(filter.value()));
      case RANGE -> cb.between(path, parser.apply(filter.min()), parser.apply(filter.max()));
    };
  }

  private Specification<CatalogEntryEntity> seekSpecification(
      SortField sortField, SortDirection direction, CatalogCursor cursor) {
    if (cursor == null || cursor.seek() == null || cursor.lastTractorId() == null) {
      return (root, query, cb) -> cb.conjunction();
    }
    return switch (sortField) {
      case PRICE -> this.<BigDecimal>seekSpecification(
          "price", new BigDecimal(cursor.seek()), cursor.lastTractorId(), direction);
      case WEIGHT -> this.<BigDecimal>seekSpecification(
          "weight", new BigDecimal(cursor.seek()), cursor.lastTractorId(), direction);
      case YEAR -> this.<Integer>seekSpecification(
          "year", Integer.valueOf(cursor.seek()), cursor.lastTractorId(), direction);
      case HORSEPOWER -> this.<Integer>seekSpecification(
          "horsepower", Integer.valueOf(cursor.seek()), cursor.lastTractorId(), direction);
    };
  }

  private <T extends Comparable<T>> Specification<CatalogEntryEntity> seekSpecification(
      String attribute, T seekValue, UUID lastTractorId, SortDirection direction) {
    return (root, query, cb) -> {
      Path<T> path = root.get(attribute);
      Path<UUID> idPath = root.get("tractorId");
      Predicate strictlyBeyond = direction == SortDirection.ASC
          ? cb.greaterThan(path, seekValue)
          : cb.lessThan(path, seekValue);
      Predicate tieBreak = cb.and(
          cb.equal(path, seekValue),
          direction == SortDirection.ASC ? cb.greaterThan(idPath, lastTractorId) : cb.lessThan(idPath, lastTractorId));
      return cb.or(strictlyBeyond, tieBreak);
    };
  }

  private Sort toSort(SortField sortField, SortDirection direction) {
    Sort.Direction dir = direction == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC;
    String attribute = switch (sortField) {
      case PRICE -> "price";
      case YEAR -> "year";
      case HORSEPOWER -> "horsepower";
      case WEIGHT -> "weight";
    };
    return Sort.by(dir, attribute).and(Sort.by(dir, "tractorId"));
  }

  private CatalogEntryEntity updateFrom(CatalogEntryEntity entity, CatalogEntry entry) {
    entity.setBrand(entry.getBrand());
    entity.setModel(entry.getModel());
    entity.setYear(entry.getYear());
    entity.setPrice(entry.getPrice());
    entity.setHorsepower(entry.getHorsepower());
    entity.setWeight(entry.getWeight());
    entity.setColor(entry.getColor());
    entity.setCategory(entry.getCategory());
    entity.setDescription(entry.getDescription());
    entity.setImageUrl(entry.getImageUrl());
    entity.setStock(entry.getStock());
    entity.setAvailable(entry.isAvailable());
    return entity;
  }

  private CatalogEntryEntity toEntity(CatalogEntry entry) {
    return CatalogEntryEntity.builder()
            .tractorId(entry.getTractorId())
            .brand(entry.getBrand())
            .model(entry.getModel())
            .year(entry.getYear())
            .price(entry.getPrice())
            .horsepower(entry.getHorsepower())
            .weight(entry.getWeight())
            .color(entry.getColor())
            .category(entry.getCategory())
            .description(entry.getDescription())
            .imageUrl(entry.getImageUrl())
            .stock(entry.getStock())
            .available(entry.isAvailable())
            .build();
  }

  private CatalogEntry toDomain(CatalogEntryEntity entity) {
    return CatalogEntry.builder()
        .tractorId(entity.getTractorId())
        .brand(entity.getBrand())
        .model(entity.getModel())
        .year(entity.getYear())
        .price(entity.getPrice())
        .horsepower(entity.getHorsepower())
        .weight(entity.getWeight())
        .color(entity.getColor())
        .category(entity.getCategory())
        .description(entity.getDescription())
        .imageUrl(entity.getImageUrl())
        .stock(entity.getStock())
        .available(entity.isAvailable())
        .build();
  }
}
