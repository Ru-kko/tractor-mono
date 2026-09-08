package com.tractor.catalog.infrastructure.api;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tractor.catalog.application.brand.BrandSnapshot;
import com.tractor.catalog.application.category.CategorySnapshot;
import com.tractor.catalog.application.search.CatalogEntrySnapshot;
import com.tractor.catalog.application.search.SearchCatalogResult;
import com.tractor.catalog.domain.models.DuplicateBrandException;
import com.tractor.catalog.domain.models.DuplicateCategoryException;
import com.tractor.catalog.domain.models.InvalidCatalogDataException;
import com.tractor.catalog.domain.models.InvalidCatalogFilterException;
import com.tractor.catalog.domain.models.InvalidCursorException;
import com.tractor.catalog.domain.ports.in.CatalogUseCase;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CatalogController.class)
@ContextConfiguration(classes = {CatalogController.class, CatalogExceptionHandler.class})
class CatalogControllerTest {

  private static final UUID TRACTOR_ID = UUID.randomUUID();

  private final MockMvc mockMvc;

  @MockitoBean
  private CatalogUseCase catalogUseCase;

  CatalogControllerTest(@Autowired MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  private CatalogEntrySnapshot aSnapshot() {
    return new CatalogEntrySnapshot(
        TRACTOR_ID, "John Deere", "X100", 2020, new BigDecimal("32000.00"), 120,
        new BigDecimal("2500.5"), "green", "utility", "desc", "img", 4);
  }

  /**
   * Cover: R6, R17
   */
  @Test
  void searchReturns200WithMatchingEntries() throws Exception {
    SearchCatalogResult result = new SearchCatalogResult(List.of(aSnapshot()), 137, 1, 20, 7, "next-cursor");
    when(catalogUseCase.search(any())).thenReturn(result);

    mockMvc.perform(post("/catalog/search")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"filters": [{"field": "BRAND", "operator": "EQUALS", "value": "John Deere"}],
                 "sortField": "PRICE", "sortDirection": "ASC", "pageSize": 20}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", org.hamcrest.Matchers.hasSize(1)))
        .andExpect(jsonPath("$.items[0].tractorId", is(TRACTOR_ID.toString())))
        .andExpect(jsonPath("$.totalItems", is(137)))
        .andExpect(jsonPath("$.page", is(1)))
        .andExpect(jsonPath("$.pageSize", is(20)))
        .andExpect(jsonPath("$.totalPages", is(7)))
        .andExpect(jsonPath("$.nextCursor", is("next-cursor")));
  }

  /**
   * Cover: R9, R10, R14, R20
   */
  @Test
  void searchReturns400OnInvalidCatalogFilter() throws Exception {
    when(catalogUseCase.search(any())).thenThrow(new InvalidCatalogFilterException("bad filter"));

    mockMvc.perform(post("/catalog/search")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_CATALOG_FILTER"));
  }

  /**
   * Cover: R21
   */
  @Test
  void searchReturns400OnInvalidCursor() throws Exception {
    when(catalogUseCase.search(any())).thenThrow(new InvalidCursorException("bad cursor"));

    mockMvc.perform(post("/catalog/search")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"cursor": "not-decodable"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_CURSOR"));
  }

  /**
   * Cover: R22
   */
  @Test
  void createBrandReturns201WithCreatedBrand() throws Exception {
    UUID brandId = UUID.randomUUID();
    when(catalogUseCase.createBrand(any())).thenReturn(new BrandSnapshot(brandId, "John Deere"));

    mockMvc.perform(post("/catalog/brand")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name": "John Deere"}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(brandId.toString())))
        .andExpect(jsonPath("$.name", is("John Deere")));
  }

  /**
   * Cover: R23
   */
  @Test
  void createBrandReturns400WhenNameBlank() throws Exception {
    when(catalogUseCase.createBrand(any())).thenThrow(new InvalidCatalogDataException("name must not be blank"));

    mockMvc.perform(post("/catalog/brand")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name": " "}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_CATALOG_DATA"));
  }

  /**
   * Cover: R24
   */
  @Test
  void createBrandReturns409WhenNameDuplicated() throws Exception {
    when(catalogUseCase.createBrand(any())).thenThrow(new DuplicateBrandException("John Deere"));

    mockMvc.perform(post("/catalog/brand")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name": "John Deere"}
                """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("DUPLICATE_BRAND"));
  }

  /**
   * Cover: R25
   */
  @Test
  void createCategoryReturns201WithCreatedCategory() throws Exception {
    UUID categoryId = UUID.randomUUID();
    when(catalogUseCase.createCategory(any())).thenReturn(new CategorySnapshot(categoryId, "Utility"));

    mockMvc.perform(post("/catalog/category")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name": "Utility"}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(categoryId.toString())))
        .andExpect(jsonPath("$.name", is("Utility")));
  }

  /**
   * Cover: R26
   */
  @Test
  void createCategoryReturns400WhenNameBlank() throws Exception {
    when(catalogUseCase.createCategory(any()))
        .thenThrow(new InvalidCatalogDataException("name must not be blank"));

    mockMvc.perform(post("/catalog/category")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name": ""}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_CATALOG_DATA"));
  }

  /**
   * Cover: R27
   */
  @Test
  void createCategoryReturns409WhenNameDuplicated() throws Exception {
    when(catalogUseCase.createCategory(any())).thenThrow(new DuplicateCategoryException("Utility"));

    mockMvc.perform(post("/catalog/category")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name": "Utility"}
                """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("DUPLICATE_CATEGORY"));
  }
}
