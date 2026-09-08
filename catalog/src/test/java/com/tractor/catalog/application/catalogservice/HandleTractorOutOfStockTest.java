package com.tractor.catalog.application.catalogservice;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tractor.catalog.application.CatalogService;
import com.tractor.catalog.domain.models.CatalogEntry;
import com.tractor.catalog.domain.ports.in.CatalogUseCase;
import com.tractor.catalog.domain.ports.out.BrandRepository;
import com.tractor.catalog.domain.ports.out.CatalogEntryRepository;
import com.tractor.catalog.domain.ports.out.CategoryRepository;
import com.tractor.common.event.TractorOutOfStockEvent;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HandleTractorOutOfStockTest {

  private static final UUID TRACTOR_ID = UUID.randomUUID();

  private final CatalogEntryRepository entries = mock(CatalogEntryRepository.class);
  private final BrandRepository brands = mock(BrandRepository.class);
  private final CategoryRepository categories = mock(CategoryRepository.class);
  private final CatalogUseCase service = new CatalogService(entries, brands, categories);

  private CatalogEntry anAvailableEntry() {
    return CatalogEntry.builder()
        .tractorId(TRACTOR_ID).brand("Acme").model("X100").year(2024).price(new BigDecimal("15000.00"))
        .horsepower(75).weight(new BigDecimal("1800.5")).color("green").category("utility")
        .description("desc").imageUrl("img").stock(0).available(true).build();
  }

  /**
   * Cover: R2
   */
  @Test
  void marksAnExistingEntryUnavailable() {
    CatalogEntry entry = anAvailableEntry();
    when(entries.findById(TRACTOR_ID)).thenReturn(Optional.of(entry));

    service.handleTractorOutOfStock(new TractorOutOfStockEvent(TRACTOR_ID));

    assertFalse(entry.isAvailable());
    verify(entries).save(entry);
  }

  /**
   * Cover: R4
   */
  @Test
  void doesNothingWhenTractorIdIsUnknown() {
    when(entries.findById(TRACTOR_ID)).thenReturn(Optional.empty());

    service.handleTractorOutOfStock(new TractorOutOfStockEvent(TRACTOR_ID));

    verify(entries, never()).save(any(CatalogEntry.class));
  }
}
