# Tasks — `catalog`

Ordered, discrete steps. Every task covers at least one `R<n>`, and every `R<n>` appears in at least
one task. A task that cannot be done without deviating from `design.md` means the spec changes
first.

- [x] T1 - Add `implementation project(':common')` and
      `implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0'` to
      `catalog/build.gradle`. Cover: R1, R6

- [x] T2 - Write migration `catalog/src/main/resources/db/migration/V2__create_catalog_tractor.sql`
      creating `catalog_tractor` (all searchable columns, `available` boolean default `true`,
      `version`) with indexes on `available`, `price`, `year`, `horsepower`, `weight`.
      Cover: R1, R2, R3, R5

- [x] T3 - Write migrations `V3__create_catalog_brand.sql` and `V4__create_catalog_category.sql`
      creating `catalog_brand`/`catalog_category` with a case-insensitive unique index on `name`.
      Cover: R22, R24, R25, R27

- [x] T4 - Implement domain models `CatalogEntry`, `Brand`, `Category` in
      `catalog/domain/models`, including `CatalogEntry.markUnavailable()`/`markAvailable()`.
      Cover: R1, R2, R3, R5, R22, R25

- [x] T5 - Implement the 5 domain exceptions (`InvalidCatalogFilterException`,
      `InvalidCursorException`, `InvalidCatalogDataException`, `DuplicateBrandException`,
      `DuplicateCategoryException`) extending `TractorStoreException` in `catalog/domain/models`.
      Cover: R9, R10, R14, R20, R21, R23, R24, R26, R27

- [x] T6 - Implement application-layer filter/sort vocabulary: `FilterField`, `FilterOperator`,
      `SortField`, `SortDirection`, `CatalogFilter` in `catalog/application/search`.
      Cover: R7, R8, R12, R13

- [x] T7 - Implement `CatalogCursor` and `CatalogCursorCodec` (encode/decode, throws
      `InvalidCursorException` on malformed input) in `catalog/application/search`.
      Cover: R16, R21

- [x] T8 - Implement the remaining application DTOs: `SearchCatalogQuery`, `SearchCatalogResult`,
      `CatalogEntrySnapshot` in `catalog/application/search`, and `CreateBrandCommand`,
      `BrandSnapshot` in `catalog/application/brand`, and `CreateCategoryCommand`,
      `CategorySnapshot` in `catalog/application/category`.
      Cover: R6, R17, R22, R25

- [x] T9 - Define the outbound ports `CatalogEntryRepository`, `BrandRepository`,
      `CategoryRepository` in `catalog/domain/ports/out`.
      Cover: R1, R2, R3, R6, R22, R25

- [x] T10 - Define the inbound port `CatalogUseCase` in `catalog/domain/ports/in` with `search`,
      `createBrand`, `createCategory`, `handleTractorAdded`, `handleTractorOutOfStock`,
      `handleTractorBackInStock`.
      Cover: R1, R2, R3, R6, R22, R25

- [x] T11 - Implement `CatalogService.handleTractorAdded/handleTractorOutOfStock/
      handleTractorBackInStock` (constructor-injected ports only) and its
      `CatalogServiceTest` (happy path per event + the "unknown tractor id is a no-op" failure-free
      path, and a negative assertion that no entry is created for R4).
      Cover: R1, R2, R3, R4

- [x] T12 - Implement `CatalogService.search`: filter/operator validation, sort field validation,
      page size validation, cursor decoding, delegation to `CatalogEntryRepository`, and mapping to
      `SearchCatalogResult` (page/totalPages computation).
      Cover: R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, R17, R18, R19, R20, R21

- [x] T13 - Write `CatalogServiceTest` (search) covering: happy path with combined filters +
      sort + pagination, unsupported operator per field type (R9), `RANGE` missing bounds (R10),
      invalid sort field (R14), default sort (R15), default and out-of-bounds page size (R19, R20),
      undecodable cursor (R21), and unavailable entries excluded (R5).
      Cover: R5, R9, R10, R11, R12, R13, R14, R15, R18, R19, R20, R21

- [x] T14 - Implement `CatalogService.createBrand`/`createCategory` with blank-name and duplicate-name
      validation, and `CatalogServiceTest` (brand/category) covering happy path plus the
      `InvalidCatalogDataException`/`DuplicateBrandException`/`DuplicateCategoryException` failure
      paths.
      Cover: R22, R23, R24, R25, R26, R27

- [x] T15 - Implement `CatalogEntryEntity`, `CatalogEntryJpaRepository`
      (`JpaRepository` + `JpaSpecificationExecutor`), and `JpaCatalogEntryRepository` (adapter
      building the dynamic filter/sort/keyset `Specification` and the `countMatching`/`findPage`
      methods) in `catalog/infrastructure/jpa/entry`.
      Cover: R1, R2, R3, R5, R6, R11, R12, R16

- [x] T16 - Implement `BrandEntity`/`BrandJpaRepository`/`JpaBrandRepository` in
      `catalog/infrastructure/jpa/brand` and
      `CategoryEntity`/`CategoryJpaRepository`/`JpaCategoryRepository` in
      `catalog/infrastructure/jpa/category`, plus `CatalogJpaConfiguration`
      (`@EnableJpaRepositories`/`@EntityScan` with base package `catalog.infrastructure.jpa`,
      covering the `.entry`/`.brand`/`.category` subpackages) directly under
      `catalog/infrastructure/jpa`.
      Cover: R22, R24, R25, R27

- [x] T17 - Implement `CatalogKafkaConsumerConfiguration` with three named
      `ConcurrentKafkaListenerContainerFactory` beans (one per event type, each with its own pinned
      `JsonDeserializer`), and the three listeners `TractorAddedEventListener`,
      `TractorOutOfStockEventListener`, `TractorBackInStockEventListener` in
      `catalog/infrastructure/kafka`, each delegating to `CatalogUseCase`.
      Cover: R1, R2, R3

- [x] T18 - Implement `CatalogController` with the three endpoints and springdoc annotations, and
      the request/response DTOs (`SearchCatalogRequest`, `CatalogFilterDto`, `SearchCatalogResponse`,
      `CatalogItemResponse`, `CreateBrandRequest`, `BrandResponse`, `CreateCategoryRequest`,
      `CategoryResponse`) in `catalog/infrastructure/api`.
      Cover: R6, R17, R22, R25

- [x] T19 - Implement `CatalogExceptionHandler` mapping `InvalidCatalogFilterException` →
      `400 INVALID_CATALOG_FILTER`, `InvalidCursorException` → `400 INVALID_CURSOR`,
      `InvalidCatalogDataException` → `400 INVALID_CATALOG_DATA`, `DuplicateBrandException` →
      `409 DUPLICATE_BRAND`, `DuplicateCategoryException` → `409 DUPLICATE_CATEGORY`.
      Cover: R9, R10, R14, R20, R21, R23, R24, R26, R27

- [x] T20 - Write `CatalogControllerTest` (`@WebMvcTest(CatalogController.class)` +
      `@MockitoBean CatalogUseCase`) covering `POST /catalog/search` happy path, `400` on
      `InvalidCatalogFilterException`/`InvalidCursorException`, `POST /catalog/brand` happy path
      (`201`) and its `400`/`409` paths, `POST /catalog/category` happy path (`201`) and its
      `400`/`409` paths.
      Cover: R6, R9, R10, R14, R17, R20, R21, R22, R23, R24, R25, R26, R27

- [x] T21 - Add `implementation project(':catalog')` to `monolith/build.gradle` and write one
      `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `@EmbeddedKafka` cross-module test in
      `monolith` (mirroring `InventoryOrderPlacedIntegrationTest`) that publishes a real
      `TractorAddedEvent` on `inventory.tractor-added`, then a `TractorOutOfStockEvent` on
      `inventory.tractor-out-of-stock`, and asserts `POST /catalog/search` no longer returns that
      tractor; then publishes `TractorBackInStockEvent` and asserts it reappears.
      Cover: R1, R2, R3, R4, R5

- [x] T22 - Run `./gradlew clean build`, fix anything red, and record the traceability map plus a
      manual smoke-test transcript (`curl` against `/catalog/search`, `/catalog/brand`,
      `/catalog/category`) in `progress/impl_catalog.md`.
      Cover: R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, R16, R17, R18, R19,
      R20, R21, R22, R23, R24, R25, R26, R27

## Requirement → tasks map

| Requirement | Tasks |
|---|---|
| R1 | T1, T2, T4, T9, T10, T11, T15, T17, T21, T22 |
| R2 | T2, T4, T9, T10, T11, T15, T17, T21, T22 |
| R3 | T2, T4, T9, T10, T11, T15, T17, T21, T22 |
| R4 | T11, T21, T22 |
| R5 | T2, T4, T12, T13, T15, T21, T22 |
| R6 | T1, T8, T9, T10, T12, T13, T15, T18, T20, T22 |
| R7 | T6, T12, T13, T22 |
| R8 | T6, T12, T13, T22 |
| R9 | T5, T12, T13, T19, T20, T22 |
| R10 | T5, T12, T13, T19, T20, T22 |
| R11 | T12, T13, T15, T22 |
| R12 | T6, T12, T13, T15, T22 |
| R13 | T6, T12, T13, T22 |
| R14 | T5, T12, T13, T19, T20, T22 |
| R15 | T12, T13, T22 |
| R16 | T7, T15, T22 |
| R17 | T8, T12, T18, T20, T22 |
| R18 | T12, T13, T22 |
| R19 | T12, T13, T22 |
| R20 | T5, T12, T13, T19, T20, T22 |
| R21 | T5, T7, T12, T13, T19, T20, T22 |
| R22 | T3, T4, T8, T9, T10, T14, T16, T18, T20, T22 |
| R23 | T5, T14, T19, T20, T22 |
| R24 | T3, T5, T14, T16, T19, T20, T22 |
| R25 | T3, T4, T8, T9, T10, T14, T16, T18, T20, T22 |
| R26 | T5, T14, T19, T20, T22 |
| R27 | T3, T5, T14, T16, T19, T20, T22 |

## Definition of done

All of these hold before the `implementer` hands over to the `reviewer`:

- [x] Every task above is `[x]`.
- [x] Every test declares its `Cover: R<n>` Javadoc block.
- [x] `.\gradlew.bat clean build` ends with `BUILD SUCCESSFUL`.
- [x] Traceability map written to `progress/impl_catalog.md`.
- [x] Smoke-test transcript in the same file.
