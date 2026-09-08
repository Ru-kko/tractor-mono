# Design — `catalog`

## Starting point

- `catalog` gradle subproject already exists (`settings.gradle` includes it). `catalog/build.gradle`
  already depends on `spring-boot-starter-data-jpa`, `spring-boot-starter-flyway`,
  `spring-boot-starter-kafka`, `spring-boot-starter-webmvc`, `flyway-database-postgresql`, Postgres
  driver, and Lombok, but **not** on `project(':common')` or `springdoc-openapi`.
- `catalog/src/main/java` has zero source files today; `catalog/src/main/resources/application.yaml`
  only sets `spring.application.name: catalog`. There is no `db/migration` folder yet. This feature
  is the first code the module gets.
- `common` already defines the three inbound event records this feature consumes —
  `com.tractor.common.event.TractorAddedEvent`, `TractorOutOfStockEvent`, `TractorBackInStockEvent` —
  and the shared `TractorStoreException` / `ErrorGroup` base (`common/src/main/java/...`). No changes
  to `common` are needed.
- `inventory` already publishes these three events over Kafka via `KafkaEventPublisher`, on topics
  `inventory.tractor-added`, `inventory.tractor-out-of-stock`, `inventory.tractor-back-in-stock`
  (`inventory/src/main/java/com/tractor/inventory/infrastructure/kafka/KafkaEventPublisher.java`).
  Kafka topics, not Spring Modulith application events, are the established cross-module event
  transport in this codebase (see `inventory`'s `OrderPlacedEventListener`, which consumes `order`'s
  events the same way) — this design follows that existing precedent rather than introducing
  `spring-modulith` event publication.
- `monolith` currently wires in `project(':common')`, `project(':cart')`, `project(':inventory')`; it
  needs `project(':catalog')` added so the new controller and Kafka listeners are picked up by
  component scanning (`@SpringBootApplication(scanBasePackages = "com.tractor")`).
- Existing classes touched: none. Everything below is new.

## Classes, ports and signatures

| Type | Module / layer | Kind | Purpose |
|---|---|---|---|
| `CatalogEntry` | `catalog.domain.models` / domain | model | Read-model row for one tractor in the catalog; carries the searchable fields and the available flag |
| `Brand` | `catalog.domain.models` / domain | model | A brand created via `POST /catalog/brand` |
| `Category` | `catalog.domain.models` / domain | model | A category created via `POST /catalog/category` |
| `InvalidCatalogFilterException` | `catalog.domain.models` / domain | exception | Unsupported filter operator, bad `RANGE`, bad sort field, bad page size |
| `InvalidCursorException` | `catalog.domain.models` / domain | exception | Cursor cannot be decoded |
| `InvalidCatalogDataException` | `catalog.domain.models` / domain | exception | Blank/missing brand or category name |
| `DuplicateBrandException` | `catalog.domain.models` / domain | exception | Brand name already exists |
| `DuplicateCategoryException` | `catalog.domain.models` / domain | exception | Category name already exists |
| `CatalogUseCase` | `catalog.domain.ports.in` / domain | port (interface) | Consolidated inbound port: search, create brand/category, handle the 3 events (mirrors `InventoryUseCase`) |
| `CatalogEntryRepository` | `catalog.domain.ports.out` / domain | port (interface) | Persistence for catalog entries, including the filtered/sorted/paged query |
| `BrandRepository` | `catalog.domain.ports.out` / domain | port (interface) | Persistence for brands |
| `CategoryRepository` | `catalog.domain.ports.out` / domain | port (interface) | Persistence for categories |
| `FilterField`, `FilterOperator`, `SortField`, `SortDirection` | `catalog.application.search` / application | enums | Filter/sort vocabulary shared by the use case and the repository adapter |
| `CatalogFilter` | `catalog.application.search` / application | record | One filter clause (`field`, `operator`, `value`, `min`, `max`) |
| `CatalogCursor` | `catalog.application.search` / application | record | Decoded cursor: last seek value, last tractor id, offset |
| `CatalogCursorCodec` | `catalog.application.search` / application | class | Encodes/decodes `CatalogCursor` to/from the opaque cursor string; throws `InvalidCursorException` |
| `SearchCatalogQuery` | `catalog.application.search` / application | record | Use-case input: filters, sort field/direction, cursor string, page size |
| `SearchCatalogResult` | `catalog.application.search` / application | record | Use-case output: items, `totalItems`, `page`, `pageSize`, `totalPages`, `nextCursor` |
| `CatalogEntrySnapshot` | `catalog.application.search` / application | record | Read-only projection of `CatalogEntry` returned to the controller |
| `CreateBrandCommand`, `BrandSnapshot` | `catalog.application.brand` / application | records | Brand use-case input/output |
| `CreateCategoryCommand`, `CategorySnapshot` | `catalog.application.category` / application | records | Category use-case input/output |
| `CatalogService` | `catalog.application` / application | class | Implements `CatalogUseCase`; the only class with business logic; sits at the application layer root because it composes all three verticals (see rationale below) |
| `CatalogController` | `catalog.infrastructure.api` / infrastructure | adapter (in) | Exposes the 3 endpoints |
| `CatalogExceptionHandler` | `catalog.infrastructure.api` / infrastructure | adapter (in) | Maps the 5 new exceptions to HTTP status |
| `SearchCatalogRequest`, `CatalogFilterDto`, `SearchCatalogResponse`, `CatalogItemResponse`, `CreateBrandRequest`, `BrandResponse`, `CreateCategoryRequest`, `CategoryResponse` | `catalog.infrastructure.api.dto` / infrastructure | DTOs | Wire format |
| `CatalogEntryEntity`, `CatalogEntryJpaRepository`, `JpaCatalogEntryRepository` | `catalog.infrastructure.jpa.entry` / infrastructure | adapter (out) | Persists `catalog_tractor`; builds the dynamic filter/sort/keyset query with `Specification` |
| `BrandEntity`, `BrandJpaRepository`, `JpaBrandRepository` | `catalog.infrastructure.jpa.brand` / infrastructure | adapter (out) | Persists `catalog_brand` |
| `CategoryEntity`, `CategoryJpaRepository`, `JpaCategoryRepository` | `catalog.infrastructure.jpa.category` / infrastructure | adapter (out) | Persists `catalog_category` |
| `CatalogJpaConfiguration` | `catalog.infrastructure.jpa` / infrastructure | config | `@EnableJpaRepositories`/`@EntityScan` on base package `catalog.infrastructure.jpa`, which recursively covers the `.entry`/`.brand`/`.category` subpackages below it (mirrors `InventoryJpaConfiguration`) |
| `CatalogKafkaConsumerConfiguration` | `catalog.infrastructure.kafka` / infrastructure | config | 3 named `ConcurrentKafkaListenerContainerFactory` beans, one per event type (see below) |
| `TractorAddedEventListener`, `TractorOutOfStockEventListener`, `TractorBackInStockEventListener` | `catalog.infrastructure.kafka` / infrastructure | adapter (in) | One `@KafkaListener` each, delegating to `CatalogUseCase` |

## Package layout rationale (vertical slicing)

Both the `application` and `infrastructure.jpa` packages are organized **by vertical
(entity/use-case), not by technical kind**, to keep each slice single-responsibility and to avoid
one flat package accumulating every enum, record and adapter of the module regardless of which
part of the feature they belong to:

- `catalog.application.search` — everything the search use case alone needs: the filter/sort
  vocabulary, the cursor codec, and the query/result DTOs.
- `catalog.application.brand` — the brand use-case's command/snapshot pair.
- `catalog.application.category` — the category use-case's command/snapshot pair.
- `catalog.application` (layer root) — `CatalogService` only. It implements the single
  consolidated `CatalogUseCase` port that spans all three verticals (mirrors `InventoryUseCase`'s
  shape in `inventory`), so it does not belong to any one slice; it is kept at the root as the
  class that composes `search`, `brand` and `category` together. Splitting `CatalogService` itself
  into three classes is out of scope for this feature — `CatalogUseCase` is one port with one
  implementation, per the feature file's `## Queries`/`## Events` sections, which do not ask for
  three separate use-case ports.
- `catalog.infrastructure.jpa.entry` — the JPA adapter for the `CatalogEntry` domain model
  (`catalog_tractor` table); named after the domain model (`CatalogEntry`), not the table, for
  consistency with the domain package.
- `catalog.infrastructure.jpa.brand` / `catalog.infrastructure.jpa.category` — the JPA adapters for
  `Brand`/`Category` respectively.
- `catalog.infrastructure.jpa` (layer root) — only `CatalogJpaConfiguration`, the
  `@EnableJpaRepositories`/`@EntityScan` configuration shared by all three slices; it is not owned
  by any one entity, so it stays at the package root that its `basePackages` value already names.

This still respects `docs/architecture.md`'s hexagonal layering: `application` and
`infrastructure.jpa` remain their own top-level packages, unchanged relative to `domain` and the
other `infrastructure.*` adapters (`api`, `kafka`); only the internal organization within those two
packages changes.

New or changed signatures:

```java
public interface CatalogUseCase {
  SearchCatalogResult search(SearchCatalogQuery query);
  BrandSnapshot createBrand(CreateBrandCommand command);
  CategorySnapshot createCategory(CreateCategoryCommand command);
  void handleTractorAdded(TractorAddedEvent event);
  void handleTractorOutOfStock(TractorOutOfStockEvent event);
  void handleTractorBackInStock(TractorBackInStockEvent event);
}

public interface CatalogEntryRepository {
  Optional<CatalogEntry> findById(UUID tractorId);
  CatalogEntry save(CatalogEntry entry);
  long countMatching(List<CatalogFilter> filters);
  List<CatalogEntry> findPage(
      List<CatalogFilter> filters, SortField sortField, SortDirection sortDirection,
      CatalogCursor cursor, int limit);
}

public final class CatalogService implements CatalogUseCase {
  public CatalogService(
      CatalogEntryRepository entries, BrandRepository brands, CategoryRepository categories);
  // constructor injection only, per docs/architecture.md
}
```

`CatalogService.search` validates the query (field/operator compatibility, `RANGE` needs both
bounds, sort field whitelist, page size in `[1, 100]`) before touching the repository, decodes the
cursor with `CatalogCursorCodec`, calls `countMatching` and `findPage(..., limit = pageSize + 1)` to
detect a next page without a second round trip, and maps the domain `CatalogEntry` list plus the
count into `SearchCatalogResult` (`page` comes from the offset carried inside the decoded cursor,
`totalPages = ceil(totalItems / pageSize)`).

## HTTP contract

| Verb | Path | Request body | Success | Errors |
|---|---|---|---|---|
| `POST` | `/catalog/search` | `SearchCatalogRequest` | `200` + `SearchCatalogResponse` | `400 INVALID_CATALOG_FILTER`, `400 INVALID_CURSOR` |
| `POST` | `/catalog/brand` | `{"name": "John Deere"}` | `201` + `BrandResponse` | `400 INVALID_CATALOG_DATA`, `409 DUPLICATE_BRAND` |
| `POST` | `/catalog/category` | `{"name": "Utility"}` | `201` + `CategoryResponse` | `400 INVALID_CATALOG_DATA`, `409 DUPLICATE_CATEGORY` |

Request payload for `POST /catalog/search`:

```json
{
  "filters": [
    {"field": "BRAND", "operator": "EQUALS", "value": "John Deere"},
    {"field": "PRICE", "operator": "RANGE", "min": "10000.00", "max": "50000.00"},
    {"field": "YEAR", "operator": "GREATER_THAN", "value": "2015"}
  ],
  "sortField": "PRICE",
  "sortDirection": "ASC",
  "cursor": null,
  "pageSize": 20
}
```

`filters`, `sortField`, `sortDirection`, `cursor`, and `pageSize` are all optional; absence triggers
R15 (default sort) and R19 (default page size).

Response payload for `POST /catalog/search`:

```json
{
  "items": [
    {
      "tractorId": "b7f1...", "brand": "John Deere", "model": "X100", "year": 2020,
      "price": 32000.00, "horsepower": 120, "weight": 2500.5, "color": "green",
      "category": "utility", "description": "...", "imageUrl": "...", "stock": 4
    }
  ],
  "totalItems": 137,
  "page": 1,
  "pageSize": 20,
  "totalPages": 7,
  "nextCursor": "eyJvZmZzZXQiOjIwLCJzZWVrIjoiMzIwMDAuMDAiLCJ0cmFjdG9ySWQiOiJiN2Yx...In0="
}
```

`nextCursor` is `null` when the current page is the last one. `POST /catalog/brand` and
`POST /catalog/category` return `{"id": "<uuid>", "name": "<name>"}`.

springdoc annotations (`@Operation`, `@ApiResponse`) required on all three controller methods, per
`docs/conventions.md`.

## Persistence

| Table | Owning module | Columns | Migration |
|---|---|---|---|
| `catalog_tractor` | `catalog` | `tractor_id (PK, UUID)`, `brand`, `model`, `year`, `price`, `horsepower`, `weight`, `color`, `category`, `description`, `image_url`, `stock`, `available (boolean, default true)`, `version` | `catalog/src/main/resources/db/migration/V2__create_catalog_tractor.sql` |
| `catalog_brand` | `catalog` | `id (PK, UUID)`, `name (unique)` | `catalog/src/main/resources/db/migration/V3__create_catalog_brand.sql` |
| `catalog_category` | `catalog` | `id (PK, UUID)`, `name (unique)` | `catalog/src/main/resources/db/migration/V4__create_catalog_category.sql` |

**Migration versions are a global sequence, not a per-module one.** Flyway resolves every module's
`db/migration` folder onto one shared `classpath:db/migration` location, scanned by `monolith`'s
single Flyway auto-configuration against one shared `flyway_schema_history` table — version numbers
are not private to a module and must be coordinated across the whole repo. Currently taken: `inventory`
`V1`. This feature takes `catalog` `V2`-`V4`.

`catalog_tractor.tractor_id` is a plain UUID copied from the event payload — never a foreign key to
any `inventory_*` table. `catalog_tractor` gets B-tree indexes on `available` and on each sortable
column (`price`, `year`, `horsepower`, `weight`) to keep the keyset query in the search use case
efficient. `catalog_brand.name` and `catalog_category.name` get a case-insensitive unique index
(`lower(name)`) so `DuplicateBrandException`/`DuplicateCategoryException` are also enforced at the
database level, not only in `CatalogService`.

## Cross-module interaction

- `inventory` publishes `TractorAddedEvent` on Kafka topic `inventory.tractor-added` → consumed by
  `catalog`'s `TractorAddedEventListener` → `CatalogUseCase.handleTractorAdded` → new row in
  `catalog_tractor` (R1).
- `inventory` publishes `TractorOutOfStockEvent` on `inventory.tractor-out-of-stock` → consumed by
  `catalog`'s `TractorOutOfStockEventListener` → `CatalogUseCase.handleTractorOutOfStock` → the
  matching `catalog_tractor` row is marked `available = false` (R2, R4).
- `inventory` publishes `TractorBackInStockEvent` on `inventory.tractor-back-in-stock` → consumed by
  `catalog`'s `TractorBackInStockEventListener` → `CatalogUseCase.handleTractorBackInStock` → the
  matching `catalog_tractor` row is marked `available = true` (R3, R4).
- No call ever goes the other way (`catalog` never calls `inventory` or reads its tables); the read
  model is self-sufficient for search.

Because `monolith/src/main/resources/application.properties` pins a single global
`spring.kafka.consumer.properties.spring.json.value.default.type=...OrderPlacedEvent` with
`use.type.headers=false` (needed by `inventory`'s existing listener), a second `@KafkaListener` in
`catalog` cannot rely on that global default — it would deserialize every message as
`OrderPlacedEvent`. `CatalogKafkaConsumerConfiguration` therefore defines three named
`ConcurrentKafkaListenerContainerFactory` beans local to `catalog`, each built from its own
`ConsumerFactory` with a `JsonDeserializer` pinned to the one event type that listener expects
(`TRUSTED_PACKAGES = com.tractor.common.event`, `USE_TYPE_INFO_HEADERS = false`,
`VALUE_DEFAULT_TYPE` = the specific event class), and each `@KafkaListener` references its factory by
name (`containerFactory = "tractorAddedListenerContainerFactory"`, etc.). This avoids touching
`application.properties` (out of `spec_author`'s and, ideally, `implementer`'s reach for the global
`inventory`-owned property) while still giving each listener a correctly-typed deserializer.

## Exceptions

| Exception | Thrown when | HTTP mapping |
|---|---|---|
| `InvalidCatalogFilterException` | Unsupported operator for a field, `RANGE` missing `min`/`max`, unknown sort field, page size outside `[1, 100]` | `400` `INVALID_CATALOG_FILTER` |
| `InvalidCursorException` | Cursor string cannot be base64/JSON-decoded, or decodes to a shape incompatible with the requested sort field | `400` `INVALID_CURSOR` |
| `InvalidCatalogDataException` | Blank or missing `name` on brand/category creation | `400` `INVALID_CATALOG_DATA` |
| `DuplicateBrandException` | Brand `name` already exists (case-insensitive) | `409` `DUPLICATE_BRAND` |
| `DuplicateCategoryException` | Category `name` already exists (case-insensitive) | `409` `DUPLICATE_CATEGORY` |

All five extend `TractorStoreException` and live in `catalog.domain.models`, matching
`InvalidTractorDataException`'s placement in `inventory`.

## New dependencies

| Dependency | Module | Why it is needed | Why nothing already present suffices |
|---|---|---|---|
| `project(':common')` | `catalog` | Reuse `TractorAddedEvent`, `TractorOutOfStockEvent`, `TractorBackInStockEvent`, `TractorStoreException` | These types are defined once in `common`; every other module that reacts to inventory events depends on it the same way |
| `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0` | `catalog` | `@Operation`/`@ApiResponse` annotations required by `docs/conventions.md` on `CatalogController` | `catalog/build.gradle` does not currently declare it (unlike `inventory/build.gradle`, which already does) |
| `project(':catalog')` | `monolith` | `MonolithApplication`'s `@SpringBootApplication(scanBasePackages = "com.tractor")` must pick up `CatalogController`, `CatalogJpaConfiguration`, and `CatalogKafkaConsumerConfiguration`, i.e. the module's actual Spring entrypoint wiring | `monolith/build.gradle` currently depends on `project(':common')`, `project(':cart')`, `project(':inventory')` only, not `project(':catalog')` |

No other new dependencies: JPA, Flyway, Kafka, and the Postgres driver are already declared in
`catalog/build.gradle`.

## Rejected alternative

- **Offset-based pagination (`page`/`size` query params, `OFFSET n LIMIT size` in SQL)** — rejected
  because the feature file explicitly requires cursor-based navigation (R16); plain `OFFSET` also
  degrades on large tables and shifts results when rows are inserted/updated between pages (an
  out-of-stock transition mid-pagination would reorder an offset-based page silently). The chosen
  keyset-with-embedded-offset cursor (`CatalogCursor`) keeps stable seek semantics for navigation
  while still reporting a numeric `page`/`totalPages` pair, satisfying both R16 and R17 without a
  second, contradictory pagination scheme.
- **Consuming the three inventory events as Spring Modulith `ApplicationModuleListener`s (in-process
  events) instead of Kafka** — rejected because `inventory` already publishes them exclusively on
  Kafka topics (`KafkaEventPublisher`), and `spring-modulith` in this codebase is used only for
  module boundary verification (`ApplicationModuleTest`), not as an alternate event bus; introducing
  a second transport for the same three events would mean `inventory` publishing twice or `catalog`
  guessing which transport carries which event.
