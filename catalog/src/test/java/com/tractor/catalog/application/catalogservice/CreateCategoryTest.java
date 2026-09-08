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
import com.tractor.catalog.application.category.CategorySnapshot;
import com.tractor.catalog.application.category.CreateCategoryCommand;
import com.tractor.catalog.domain.models.Category;
import com.tractor.catalog.domain.models.DuplicateCategoryException;
import com.tractor.catalog.domain.models.InvalidCatalogDataException;
import com.tractor.catalog.domain.ports.in.CatalogUseCase;
import com.tractor.catalog.domain.ports.out.BrandRepository;
import com.tractor.catalog.domain.ports.out.CatalogEntryRepository;
import com.tractor.catalog.domain.ports.out.CategoryRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CreateCategoryTest {

  private final CatalogEntryRepository entries = mock(CatalogEntryRepository.class);
  private final BrandRepository brands = mock(BrandRepository.class);
  private final CategoryRepository categories = mock(CategoryRepository.class);
  private final CatalogUseCase service = new CatalogService(entries, brands, categories);

  /**
   * Cover: R25
   */
  @Test
  void createsCategoryWithGeneratedId() {
    when(categories.findByName("Utility")).thenReturn(Optional.empty());
    when(categories.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

    CategorySnapshot snapshot = service.createCategory(new CreateCategoryCommand("Utility"));

    assertNotNull(snapshot.id());
    assertEquals("Utility", snapshot.name());
    verify(categories).save(any(Category.class));
  }

  /**
   * Cover: R26
   */
  @Test
  void rejectsBlankName() {
    assertThrows(InvalidCatalogDataException.class, () -> service.createCategory(new CreateCategoryCommand(null)));
    verify(categories, never()).save(any(Category.class));
  }

  /**
   * Cover: R27
   */
  @Test
  void rejectsDuplicateName() {
    when(categories.findByName("Utility")).thenReturn(Optional.of(Category.builder().name("Utility").build()));

    assertThrows(DuplicateCategoryException.class,
        () -> service.createCategory(new CreateCategoryCommand("Utility")));
    verify(categories, never()).save(any(Category.class));
  }
}
