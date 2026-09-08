package com.tractor.catalog.infrastructure.api;

import com.tractor.catalog.application.brand.BrandSnapshot;
import com.tractor.catalog.application.brand.CreateBrandCommand;
import com.tractor.catalog.application.category.CategorySnapshot;
import com.tractor.catalog.application.category.CreateCategoryCommand;
import com.tractor.catalog.application.search.SearchCatalogResult;
import com.tractor.catalog.domain.ports.in.CatalogUseCase;
import com.tractor.catalog.infrastructure.api.dto.BrandResponse;
import com.tractor.catalog.infrastructure.api.dto.CategoryResponse;
import com.tractor.catalog.infrastructure.api.dto.CreateBrandRequest;
import com.tractor.catalog.infrastructure.api.dto.CreateCategoryRequest;
import com.tractor.catalog.infrastructure.api.dto.SearchCatalogRequest;
import com.tractor.catalog.infrastructure.api.dto.SearchCatalogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/catalog")
public class CatalogController {
  private final CatalogUseCase catalogUseCase;

  @Operation(summary = "Search the catalog of tractors")
  @ApiResponse(responseCode = "200", description = "Matching catalog entries")
  @ApiResponse(responseCode = "400", description = "Invalid filter, sort or cursor")
  @PostMapping("/search")
  public ResponseEntity<SearchCatalogResponse> search(@RequestBody SearchCatalogRequest request) {
    SearchCatalogResult result = catalogUseCase.search(request.toQuery());
    return ResponseEntity.ok(SearchCatalogResponse.from(result));
  }

  @Operation(summary = "Create a new catalog brand")
  @ApiResponse(responseCode = "201", description = "Brand created")
  @ApiResponse(responseCode = "400", description = "Invalid brand data")
  @ApiResponse(responseCode = "409", description = "Brand already exists")
  @PostMapping("/brand")
  public ResponseEntity<BrandResponse> createBrand(@RequestBody CreateBrandRequest request) {
    BrandSnapshot snapshot = catalogUseCase.createBrand(new CreateBrandCommand(request.name()));
    return ResponseEntity.status(HttpStatus.CREATED).body(BrandResponse.from(snapshot));
  }

  @Operation(summary = "Create a new catalog category")
  @ApiResponse(responseCode = "201", description = "Category created")
  @ApiResponse(responseCode = "400", description = "Invalid category data")
  @ApiResponse(responseCode = "409", description = "Category already exists")
  @PostMapping("/category")
  public ResponseEntity<CategoryResponse> createCategory(@RequestBody CreateCategoryRequest request) {
    CategorySnapshot snapshot = catalogUseCase.createCategory(new CreateCategoryCommand(request.name()));
    return ResponseEntity.status(HttpStatus.CREATED).body(CategoryResponse.from(snapshot));
  }
}
