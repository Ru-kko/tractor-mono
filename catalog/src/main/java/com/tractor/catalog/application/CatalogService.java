package com.tractor.catalog.application;

import com.tractor.catalog.application.brand.BrandSnapshot;
import com.tractor.catalog.application.brand.CreateBrandCommand;
import com.tractor.catalog.application.category.CategorySnapshot;
import com.tractor.catalog.application.category.CreateCategoryCommand;
import com.tractor.catalog.application.search.CatalogCursor;
import com.tractor.catalog.application.search.CatalogCursorCodec;
import com.tractor.catalog.application.search.CatalogEntrySnapshot;
import com.tractor.catalog.application.search.CatalogFilter;
import com.tractor.catalog.application.search.FilterOperator;
import com.tractor.catalog.application.search.SearchCatalogQuery;
import com.tractor.catalog.application.search.SearchCatalogResult;
import com.tractor.catalog.application.search.SortDirection;
import com.tractor.catalog.application.search.SortField;
import com.tractor.catalog.domain.models.Brand;
import com.tractor.catalog.domain.models.CatalogEntry;
import com.tractor.catalog.domain.models.Category;
import com.tractor.catalog.domain.models.DuplicateBrandException;
import com.tractor.catalog.domain.models.DuplicateCategoryException;
import com.tractor.catalog.domain.models.InvalidCatalogDataException;
import com.tractor.catalog.domain.models.InvalidCatalogFilterException;
import com.tractor.catalog.domain.ports.in.CatalogUseCase;
import com.tractor.catalog.domain.ports.out.BrandRepository;
import com.tractor.catalog.domain.ports.out.CategoryRepository;
import com.tractor.catalog.domain.ports.out.CatalogEntryRepository;
import com.tractor.common.event.TractorAddedEvent;
import com.tractor.common.event.TractorBackInStockEvent;
import com.tractor.common.event.TractorOutOfStockEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public final class CatalogService implements CatalogUseCase {

  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int MIN_PAGE_SIZE = 1;
  private static final int MAX_PAGE_SIZE = 100;

  private final CatalogEntryRepository entries;
  private final BrandRepository brands;
  private final CategoryRepository categories;
  private final CatalogCursorCodec cursorCodec = new CatalogCursorCodec();

  @Override
  public SearchCatalogResult search(SearchCatalogQuery query) {
    List<CatalogFilter> filters = query.filters() == null ? List.of() : query.filters();
    validateFilters(filters);

    SortField sortField = parseSortField(query.sortField());
    SortDirection sortDirection = query.sortDirection() != null ? query.sortDirection() : SortDirection.ASC;

    int pageSize = query.pageSize() != null ? query.pageSize() : DEFAULT_PAGE_SIZE;
    if (pageSize < MIN_PAGE_SIZE || pageSize > MAX_PAGE_SIZE) {
      throw new InvalidCatalogFilterException("pageSize must be between 1 and 100");
    }

    CatalogCursor cursor =
        query.cursor() != null ? cursorCodec.decode(query.cursor()) : new CatalogCursor(null, null, 0);

    long totalItems = entries.countMatching(filters);
    List<CatalogEntry> page = entries.findPage(filters, sortField, sortDirection, cursor, pageSize + 1);

    boolean hasNextPage = page.size() > pageSize;
    List<CatalogEntry> pageItems = hasNextPage ? page.subList(0, pageSize) : page;

    String nextCursor = null;
    if (hasNextPage) {
      CatalogEntry last = pageItems.get(pageItems.size() - 1);
      nextCursor = cursorCodec.encode(
          new CatalogCursor(seekValue(last, sortField), last.getTractorId(), cursor.offset() + pageSize));
    }

    int currentPage = cursor.offset() / pageSize + 1;
    int totalPages = (int) Math.ceil((double) totalItems / pageSize);

    List<CatalogEntrySnapshot> snapshots = pageItems.stream().map(this::toSnapshot).toList();

    return new SearchCatalogResult(snapshots, totalItems, currentPage, pageSize, totalPages, nextCursor);
  }

  @Override
  public BrandSnapshot createBrand(CreateBrandCommand command) {
    requireNonBlank(command.name());

    if (brands.findByName(command.name()).isPresent()) {
      throw new DuplicateBrandException(command.name());
    }

    Brand saved = brands.save(Brand.builder().id(UUID.randomUUID()).name(command.name()).build());
    return new BrandSnapshot(saved.getId(), saved.getName());
  }

  @Override
  public CategorySnapshot createCategory(CreateCategoryCommand command) {
    requireNonBlank(command.name());

    if (categories.findByName(command.name()).isPresent()) {
      throw new DuplicateCategoryException(command.name());
    }

    Category saved = categories.save(Category.builder().id(UUID.randomUUID()).name(command.name()).build());
    return new CategorySnapshot(saved.getId(), saved.getName());
  }

  @Override
  public void handleTractorAdded(TractorAddedEvent event) {
    entries.save(CatalogEntry.builder()
        .tractorId(event.tractorId())
        .brand(event.brand())
        .model(event.model())
        .year(event.year())
        .price(event.price())
        .horsepower(event.horsepower())
        .weight(event.weight())
        .color(event.color())
        .category(event.category())
        .description(event.description())
        .imageUrl(event.imageUrl())
        .stock(event.stock())
        .available(true)
        .build());
  }

  @Override
  public void handleTractorOutOfStock(TractorOutOfStockEvent event) {
    entries.findById(event.tractorId()).ifPresent(entry -> {
      entry.markUnavailable();
      entries.save(entry);
    });
  }

  @Override
  public void handleTractorBackInStock(TractorBackInStockEvent event) {
    entries.findById(event.tractorId()).ifPresent(entry -> {
      entry.markAvailable();
      entries.save(entry);
    });
  }

  private SortField parseSortField(String sortField) {
    if (sortField == null) {
      return SortField.PRICE;
    }
    try {
      return SortField.valueOf(sortField);
    } catch (IllegalArgumentException exception) {
      throw new InvalidCatalogFilterException("Unknown sort field: " + sortField);
    }
  }

  private void validateFilters(List<CatalogFilter> filters) {
    for (CatalogFilter filter : filters) {
      if (!filter.field().supports(filter.operator())) {
        throw new InvalidCatalogFilterException(
            "Operator " + filter.operator() + " not supported for field " + filter.field());
      }
      if (filter.operator() == FilterOperator.RANGE && (isBlank(filter.min()) || isBlank(filter.max()))) {
        throw new InvalidCatalogFilterException("RANGE operator requires both min and max");
      }
    }
  }

  private String seekValue(CatalogEntry entry, SortField sortField) {
    return switch (sortField) {
      case PRICE -> entry.getPrice().toString();
      case YEAR -> String.valueOf(entry.getYear());
      case HORSEPOWER -> String.valueOf(entry.getHorsepower());
      case WEIGHT -> entry.getWeight().toString();
    };
  }

  private CatalogEntrySnapshot toSnapshot(CatalogEntry entry) {
    return new CatalogEntrySnapshot(
        entry.getTractorId(), entry.getBrand(), entry.getModel(), entry.getYear(), entry.getPrice(),
        entry.getHorsepower(), entry.getWeight(), entry.getColor(), entry.getCategory(),
        entry.getDescription(), entry.getImageUrl(), entry.getStock());
  }

  private void requireNonBlank(String name) {
    if (name == null || name.isBlank()) {
      throw new InvalidCatalogDataException("name must not be blank");
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
