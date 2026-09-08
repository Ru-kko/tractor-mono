package com.tractor.catalog.application.catalogservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tractor.catalog.application.CatalogService;
import com.tractor.catalog.domain.models.CatalogEntry;
import com.tractor.catalog.domain.ports.in.CatalogUseCase;
import com.tractor.catalog.domain.ports.out.BrandRepository;
import com.tractor.catalog.domain.ports.out.CatalogEntryRepository;
import com.tractor.catalog.domain.ports.out.CategoryRepository;
import com.tractor.common.event.TractorAddedEvent;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class HandleTractorAddedTest {

  private final CatalogEntryRepository entries = mock(CatalogEntryRepository.class);
  private final BrandRepository brands = mock(BrandRepository.class);
  private final CategoryRepository categories = mock(CategoryRepository.class);
  private final CatalogUseCase service = new CatalogService(entries, brands, categories);

  /**
   * Cover: R1
   */
  @Test
  void createsAnAvailableCatalogEntryFromTheEvent() {
    UUID tractorId = UUID.randomUUID();
    TractorAddedEvent event = TractorAddedEvent.builder()
        .tractorId(tractorId)
        .stock(5)
        .price(new BigDecimal("15000.00"))
        .description("Compact utility tractor")
        .imageUrl("https://example.com/t.png")
        .category("utility")
        .brand("Acme")
        .model("X100")
        .year(2024)
        .color("green")
        .weight(new BigDecimal("1800.5"))
        .horsepower(75)
        .build();
    when(entries.save(any(CatalogEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

    service.handleTractorAdded(event);

    ArgumentCaptor<CatalogEntry> captor = ArgumentCaptor.forClass(CatalogEntry.class);
    verify(entries).save(captor.capture());
    CatalogEntry saved = captor.getValue();
    assertEquals(tractorId, saved.getTractorId());
    assertEquals("Acme", saved.getBrand());
    assertEquals("X100", saved.getModel());
    assertEquals(2024, saved.getYear());
    assertEquals(new BigDecimal("15000.00"), saved.getPrice());
    assertEquals(75, saved.getHorsepower());
    assertEquals(new BigDecimal("1800.5"), saved.getWeight());
    assertEquals("green", saved.getColor());
    assertEquals("utility", saved.getCategory());
    assertEquals(5, saved.getStock());
    assertTrue(saved.isAvailable());
  }
}
