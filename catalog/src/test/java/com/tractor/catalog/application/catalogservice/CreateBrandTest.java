package com.tractor.catalog.application.catalogservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tractor.catalog.application.CatalogService;
import com.tractor.catalog.application.brand.BrandSnapshot;
import com.tractor.catalog.application.brand.CreateBrandCommand;
import com.tractor.catalog.domain.models.Brand;
import com.tractor.catalog.domain.models.DuplicateBrandException;
import com.tractor.catalog.domain.models.InvalidCatalogDataException;
import com.tractor.catalog.domain.ports.in.CatalogUseCase;
import com.tractor.catalog.domain.ports.out.BrandRepository;
import com.tractor.catalog.domain.ports.out.CatalogEntryRepository;
import com.tractor.catalog.domain.ports.out.CategoryRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CreateBrandTest {

  private final CatalogEntryRepository entries = mock(CatalogEntryRepository.class);
  private final BrandRepository brands = mock(BrandRepository.class);
  private final CategoryRepository categories = mock(CategoryRepository.class);
  private final CatalogUseCase service = new CatalogService(entries, brands, categories);

  /**
   * Cover: R22
   */
  @Test
  void createsBrandWithGeneratedId() {
    when(brands.findByName("John Deere")).thenReturn(Optional.empty());
    when(brands.save(any(Brand.class))).thenAnswer(invocation -> invocation.getArgument(0));

    BrandSnapshot snapshot = service.createBrand(new CreateBrandCommand("John Deere"));

    assertNotNull(snapshot.id());
    assertEquals("John Deere", snapshot.name());
    verify(brands).save(any(Brand.class));
  }

  /**
   * Cover: R23
   */
  @Test
  void rejectsBlankName() {
    assertThrows(InvalidCatalogDataException.class, () -> service.createBrand(new CreateBrandCommand(" ")));
    verify(brands, never()).save(any(Brand.class));
  }

  /**
   * Cover: R24
   */
  @Test
  void rejectsDuplicateName() {
    when(brands.findByName("John Deere")).thenReturn(Optional.of(Brand.builder().name("John Deere").build()));

    assertThrows(DuplicateBrandException.class, () -> service.createBrand(new CreateBrandCommand("John Deere")));
    verify(brands, never()).save(any(Brand.class));
  }
}
