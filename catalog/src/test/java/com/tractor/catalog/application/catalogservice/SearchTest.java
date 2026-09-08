package com.tractor.catalog.application.catalogservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tractor.catalog.application.CatalogService;
import com.tractor.catalog.application.search.CatalogCursor;
import com.tractor.catalog.application.search.CatalogCursorCodec;
import com.tractor.catalog.application.search.CatalogFilter;
import com.tractor.catalog.application.search.FilterField;
import com.tractor.catalog.application.search.FilterOperator;
import com.tractor.catalog.application.search.SearchCatalogQuery;
import com.tractor.catalog.application.search.SearchCatalogResult;
import com.tractor.catalog.application.search.SortDirection;
import com.tractor.catalog.application.search.SortField;
import com.tractor.catalog.domain.models.CatalogEntry;
import com.tractor.catalog.domain.models.InvalidCatalogFilterException;
import com.tractor.catalog.domain.models.InvalidCursorException;
import com.tractor.catalog.domain.ports.in.CatalogUseCase;
import com.tractor.catalog.domain.ports.out.BrandRepository;
import com.tractor.catalog.domain.ports.out.CatalogEntryRepository;
import com.tractor.catalog.domain.ports.out.CategoryRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SearchTest {

  private final CatalogEntryRepository entries = mock(CatalogEntryRepository.class);
  private final BrandRepository brands = mock(BrandRepository.class);
  private final CategoryRepository categories = mock(CategoryRepository.class);
  private final CatalogUseCase service = new CatalogService(entries, brands, categories);
  private final CatalogCursorCodec cursorCodec = new CatalogCursorCodec();

  private CatalogEntry anEntry(String tractorId, String price) {
    return CatalogEntry.builder()
        .tractorId(UUID.fromString(tractorId)).brand("Acme").model("X100").year(2024)
        .price(new BigDecimal(price)).horsepower(75).weight(new BigDecimal("1800.5")).color("green")
        .category("utility").description("desc").imageUrl("img").stock(3).available(true).build();
  }

  /**
   * Cover: R6, R7, R8, R11, R12, R13, R16, R17, R18
   */
  @Test
  void happyPathAppliesFiltersSortAndPaginationTogether() {
    List<CatalogFilter> filters = List.of(
        new CatalogFilter(FilterField.BRAND, FilterOperator.EQUALS, "Acme", null, null),
        new CatalogFilter(FilterField.PRICE, FilterOperator.RANGE, null, "10000.00", "50000.00"));

    CatalogEntry first = anEntry("00000000-0000-0000-0000-000000000001", "40000.00");
    CatalogEntry second = anEntry("00000000-0000-0000-0000-000000000002", "30000.00");
    CatalogEntry third = anEntry("00000000-0000-0000-0000-000000000003", "20000.00");

    when(entries.countMatching(filters)).thenReturn(5L);
    when(entries.findPage(eq(filters), eq(SortField.PRICE), eq(SortDirection.DESC), any(CatalogCursor.class), eq(3)))
        .thenReturn(List.of(first, second, third));

    SearchCatalogResult result = service.search(
        new SearchCatalogQuery(filters, "PRICE", SortDirection.DESC, null, 2));

    assertEquals(2, result.items().size());
    assertEquals(first.getTractorId(), result.items().get(0).tractorId());
    assertEquals(second.getTractorId(), result.items().get(1).tractorId());
    assertEquals(5L, result.totalItems());
    assertEquals(1, result.page());
    assertEquals(2, result.pageSize());
    assertEquals(3, result.totalPages());
    assertNotNull(result.nextCursor());

    CatalogCursor decoded = cursorCodec.decode(result.nextCursor());
    assertEquals(2, decoded.offset());
    assertEquals(second.getTractorId(), decoded.lastTractorId());
  }

  /**
   * Cover: R5
   */
  @Test
  void returnsExactlyTheAvailableEntriesSuppliedByTheRepositoryPage() {
    CatalogEntry onlyAvailableEntry = anEntry("00000000-0000-0000-0000-000000000004", "25000.00");
    when(entries.countMatching(List.of())).thenReturn(1L);
    when(entries.findPage(
            eq(List.of()), eq(SortField.PRICE), eq(SortDirection.ASC), any(CatalogCursor.class), anyInt()))
        .thenReturn(List.of(onlyAvailableEntry));

    SearchCatalogResult result = service.search(new SearchCatalogQuery(List.of(), null, null, null, null));

    assertEquals(1, result.items().size());
    assertEquals(onlyAvailableEntry.getTractorId(), result.items().get(0).tractorId());
    assertNull(result.nextCursor());
  }

  /**
   * Cover: R9
   */
  @Test
  void rejectsUnsupportedOperatorForField() {
    List<CatalogFilter> filters = List.of(
        new CatalogFilter(FilterField.BRAND, FilterOperator.GREATER_THAN, "Acme", null, null));

    assertThrows(InvalidCatalogFilterException.class,
        () -> service.search(new SearchCatalogQuery(filters, null, null, null, null)));
    verifyNoInteractions(entries);
  }

  /**
   * Cover: R10
   */
  @Test
  void rejectsRangeOperatorMissingBounds() {
    List<CatalogFilter> filters = List.of(
        new CatalogFilter(FilterField.PRICE, FilterOperator.RANGE, null, "10000.00", null));

    assertThrows(InvalidCatalogFilterException.class,
        () -> service.search(new SearchCatalogQuery(filters, null, null, null, null)));
    verifyNoInteractions(entries);
  }

  /**
   * Cover: R14
   */
  @Test
  void rejectsInvalidSortField() {
    assertThrows(InvalidCatalogFilterException.class,
        () -> service.search(new SearchCatalogQuery(List.of(), "MILEAGE", null, null, null)));
    verifyNoInteractions(entries);
  }

  /**
   * Cover: R15
   */
  @Test
  void defaultsSortToPriceAscendingWhenNotSpecified() {
    when(entries.countMatching(List.of())).thenReturn(0L);
    when(entries.findPage(
            eq(List.of()), eq(SortField.PRICE), eq(SortDirection.ASC), any(CatalogCursor.class), anyInt()))
        .thenReturn(List.of());

    service.search(new SearchCatalogQuery(List.of(), null, null, null, 10));

    org.mockito.Mockito.verify(entries)
        .findPage(eq(List.of()), eq(SortField.PRICE), eq(SortDirection.ASC), any(CatalogCursor.class), eq(11));
  }

  /**
   * Cover: R19
   */
  @Test
  void defaultsPageSizeToTwentyWhenNotSpecified() {
    when(entries.countMatching(List.of())).thenReturn(0L);
    when(entries.findPage(any(), any(), any(), any(), anyInt())).thenReturn(List.of());

    SearchCatalogResult result = service.search(new SearchCatalogQuery(List.of(), null, null, null, null));

    assertEquals(20, result.pageSize());
    org.mockito.Mockito.verify(entries).findPage(eq(List.of()), any(), any(), any(), eq(21));
  }

  /**
   * Cover: R20
   */
  @Test
  void rejectsPageSizeBelowMinimum() {
    assertThrows(InvalidCatalogFilterException.class,
        () -> service.search(new SearchCatalogQuery(List.of(), null, null, null, 0)));
    verifyNoInteractions(entries);
  }

  /**
   * Cover: R20
   */
  @Test
  void rejectsPageSizeAboveMaximum() {
    assertThrows(InvalidCatalogFilterException.class,
        () -> service.search(new SearchCatalogQuery(List.of(), null, null, null, 101)));
    verifyNoInteractions(entries);
  }

  /**
   * Cover: R21
   */
  @Test
  void rejectsUndecodableCursor() {
    assertThrows(InvalidCursorException.class,
        () -> service.search(new SearchCatalogQuery(List.of(), null, null, "not-a-valid-cursor!!", null)));
    verifyNoInteractions(entries);
  }
}
