# Implementation — feature `003` `catalog`

**Spec:** `specs/catalog/`
**Module:** `catalog`
**Result:** `ready for review`

## Summary

T1–T22 are all implemented and green. `gradle clean build` is `BUILD SUCCESSFUL` across every
module (`common`, `cart`, `catalog`, `inventory`, `monolith`).

### Resolution of the previous blocker (migration-version collision)

The previous session stopped at T21/T22 because catalog's `V1__create_catalog_tractor.sql`
collided with `inventory`'s pre-existing `V1__create_inventory_stock_item.sql` in Flyway's single
shared, global migration-version sequence (every module's `db/migration` folder merges onto one
`classpath:db/migration` location scanned by `monolith`'s one Flyway auto-configuration against one
shared `flyway_schema_history` table — there is no per-module Flyway isolation in this repo).

The human/spec_author remediation was the simplest one: renumber catalog's migrations past
inventory's `V1`. `specs/catalog/design.md`'s `## Persistence` table and `specs/catalog/tasks.md`
T2/T3 were corrected accordingly (now pin `V2__create_catalog_tractor.sql`,
`V3__create_catalog_brand.sql`, `V4__create_catalog_category.sql`, and `design.md` gained an
explicit paragraph documenting the global-version-sequence constraint). `docs/architecture.md`
principle 5 now documents this constraint for future features too.

This session's work to close it out:

1. Renamed the three existing migration files (content unchanged — verified no file hardcodes its
   own old version number anywhere inside the SQL):
   - `V1__create_catalog_tractor.sql` → `V2__create_catalog_tractor.sql`
   - `V2__create_catalog_brand.sql` → `V3__create_catalog_brand.sql`
   - `V3__create_catalog_category.sql` → `V4__create_catalog_category.sql`
2. Re-checked `[x]` T2/T3 in `specs/catalog/tasks.md` (the SQL itself was already correct; only the
   filenames needed to change) and cleared the T21 `BLOCKED` note.
3. Completed T21: `implementation project(':catalog')` was already present in
   `monolith/build.gradle` (added in the previous session); wrote
   `CatalogEventsIntegrationTest` in `monolith`, a `@SpringBootTest(webEnvironment = RANDOM_PORT)`
   + `@EmbeddedKafka` cross-module test mirroring `InventoryOrderPlacedIntegrationTest`.
4. Completed T22: `gradle clean build` is now `BUILD SUCCESSFUL` (evidence below). Manual smoke
   test performed against a running `:monolith:bootRun` instance (transcript below).

`features/003-catalog.md` remains `state: in_progress` — per the SDD flow, only the reviewer flips
it to `done`. `specs/catalog/tasks.md` now has `T1..T22` all checked `[x]`.

## Tasks executed

- [x] T1 — `project(':common')` + springdoc added to `catalog/build.gradle`.
- [x] T2 — `catalog/src/main/resources/db/migration/V1__create_catalog_tractor.sql`.
- [x] T3 — `V2__create_catalog_brand.sql`, `V3__create_catalog_category.sql`.
- [x] T4 — `CatalogEntry`, `Brand`, `Category` in `catalog/domain/models`.
- [x] T5 — 5 domain exceptions in `catalog/domain/models`, all extending `TractorStoreException`.
- [x] T6 — `FilterField`, `FilterOperator`, `SortField`, `SortDirection`, `CatalogFilter` in
      `catalog/application/search`.
- [x] T7 — `CatalogCursor`, `CatalogCursorCodec` in `catalog/application/search`.
- [x] T8 — `SearchCatalogQuery`, `SearchCatalogResult`, `CatalogEntrySnapshot`,
      `CreateBrandCommand`/`BrandSnapshot`, `CreateCategoryCommand`/`CategorySnapshot`.
- [x] T9 — `CatalogEntryRepository`, `BrandRepository`, `CategoryRepository` in
      `catalog/domain/ports/out`.
- [x] T10 — `CatalogUseCase` in `catalog/domain/ports/in`.
- [x] T11 — `CatalogService.handleTractorAdded/handleTractorOutOfStock/handleTractorBackInStock` +
      `HandleTractorAddedTest`, `HandleTractorOutOfStockTest`, `HandleTractorBackInStockTest`.
- [x] T12 — `CatalogService.search` (validation, cursor decode, pagination/sort mapping).
- [x] T13 — `SearchTest` (happy path + all listed failure/edge cases).
- [x] T14 — `CatalogService.createBrand/createCategory` + `CreateBrandTest`, `CreateCategoryTest`.
- [x] T15 — `CatalogEntryEntity`, `CatalogEntryJpaRepository`, `JpaCatalogEntryRepository`
      (dynamic `Specification`, keyset seek predicate, `countMatching`/`findPage`).
- [x] T16 — `BrandEntity`/`BrandJpaRepository`/`JpaBrandRepository`,
      `CategoryEntity`/`CategoryJpaRepository`/`JpaCategoryRepository`, `CatalogJpaConfiguration`.
- [x] T17 — `CatalogKafkaConsumerConfiguration` (3 named container factories, each with its own
      pinned `JsonDeserializer`), `TractorAddedEventListener`, `TractorOutOfStockEventListener`,
      `TractorBackInStockEventListener`.
- [x] T18 — `CatalogController` + DTOs in `catalog/infrastructure/api`(`.dto`).
- [x] T19 — `CatalogExceptionHandler`.
- [x] T20 — `CatalogControllerTest` (`@WebMvcTest` + `@MockitoBean CatalogUseCase`, real `MockMvc`).
- [x] T21 — `implementation project(':catalog')` confirmed present in `monolith/build.gradle`;
      wrote `monolith/src/test/java/com/tractor/monolith/CatalogEventsIntegrationTest.java`
      (`@SpringBootTest(webEnvironment = RANDOM_PORT)` + `@EmbeddedKafka`, mirroring
      `InventoryOrderPlacedIntegrationTest`): publishes a real `TractorAddedEvent` on
      `inventory.tractor-added`, asserts `POST /catalog/search` (filtered by a unique brand) finds
      it; publishes `TractorOutOfStockEvent` on `inventory.tractor-out-of-stock`, asserts it
      disappears; publishes `TractorBackInStockEvent` on `inventory.tractor-back-in-stock`, asserts
      it reappears. Green: `gradle :monolith:test --tests '*CatalogEventsIntegrationTest*'` →
      `BUILD SUCCESSFUL`, 1 test, 0 failures.
- [x] T22 — `gradle clean build` is `BUILD SUCCESSFUL` across all 5 modules (evidence below); manual
      smoke test performed against a running server (transcript below).

## Root cause of the previous blocker (historical evidence — now resolved)

```
gradle clean build
...
> Task :monolith:test

InventoryOrderPlacedIntegrationTest > orderPlacedEventNeverDecreasesStockBelowZero() FAILED
    java.lang.IllegalStateException at DefaultCacheAwareContextLoaderDelegate.java:195
        Caused by: org.springframework.beans.factory.BeanCreationException at AbstractBeanFactory.java:322
            Caused by: org.springframework.beans.factory.BeanCreationException at AbstractAutowireCapableBeanFactory.java:1815
                Caused by: org.flywaydb.core.api.FlywayException at CompositeMigrationResolver.java:94

InventoryOrderPlacedIntegrationTest > orderPlacedEventDecreasesStockAndPublishesStockUpdated() FAILED
    java.lang.IllegalStateException at DefaultCacheAwareContextLoaderDelegate.java:157

2 tests completed, 2 failed

> Task :monolith:test FAILED
```

Full stack trace, from `monolith/build/reports/tests/test/classes/com.tractor.monolith.InventoryOrderPlacedIntegrationTest.html`:

```
Caused by: org.flywaydb.core.api.FlywayException: Found more than one migration with version 1
Offenders:
-> .../inventory/build/libs/inventory-0.0.1-SNAPSHOT-plain.jar!/db/migration/V1__create_inventory_stock_item.sql (SQL)
-> .../catalog/build/libs/catalog-0.0.1-SNAPSHOT-plain.jar!/db/migration/V1__create_catalog_tractor.sql (SQL)
	at app//org.flywaydb.core.internal.resolver.CompositeMigrationResolver.checkForIncompatibilities(CompositeMigrationResolver.java:94)
	...
	at app//org.springframework.boot.flyway.autoconfigure.FlywayMigrationInitializer.afterPropertiesSet(FlywayMigrationInitializer.java:67)
```

Confirmed this is caused solely by adding `catalog`'s migrations: before this feature, `catalog` had
no Java/SQL source at all and `gradle clean build` was `BUILD SUCCESSFUL` (verified at the start of
this session). `monolith` has a single Spring Boot Flyway auto-configuration wired to one Postgres
datasource; `spring.flyway.locations` defaults to `classpath:db/migration`, which is a path every
module's jar contributes to on the monolith's classpath — Flyway merges all of them into **one**
ordered migration chain tracked by **one** `flyway_schema_history` table, so version numbers must be
globally unique across modules, not just unique within a module's own folder. `inventory` already
occupies `V1`; `design.md`'s `## Persistence` table and `tasks.md` T2/T3 pin catalog's migrations to
literally `V1`/`V2`/`V3`, which collides.

This also means `docs/architecture.md`'s migration rule ("Migration scripts for a table live in the
owning module... A module never migrates another module's schema") does not by itself prevent this
collision — module-owned migration *files* still share one global Flyway version sequence today.
`docs/architecture.md` principle 5 has since been updated to document this constraint explicitly,
and `design.md`/`tasks.md` were corrected to renumber catalog's migrations to `V2`-`V4` (resolution
applied this session — see `## Summary`).

## Traceability

Level 4 of `docs/verification.md`. All R1–R27 map to at least one concrete test. R1-R5 now also
have the stronger end-to-end proof added by T21 (real Kafka + real Postgres + real HTTP, no
in-process shortcut), in addition to their existing unit/controller-level tests.

- R1 → `HandleTractorAddedTest#createsAnAvailableCatalogEntryFromTheEvent`,
  `CatalogEventsIntegrationTest#catalogReflectsTractorAddedOutOfStockAndBackInStockEvents`
  (real `TractorAddedEvent` on `inventory.tractor-added` → real Postgres row → real
  `POST /catalog/search` finds it)
- R2 → `HandleTractorOutOfStockTest#marksAnExistingEntryUnavailable`,
  `CatalogEventsIntegrationTest#catalogReflectsTractorAddedOutOfStockAndBackInStockEvents`
  (real `TractorOutOfStockEvent` on `inventory.tractor-out-of-stock` → entry disappears from
  `POST /catalog/search`)
- R3 → `HandleTractorBackInStockTest#marksAnExistingEntryAvailable`,
  `CatalogEventsIntegrationTest#catalogReflectsTractorAddedOutOfStockAndBackInStockEvents`
  (real `TractorBackInStockEvent` on `inventory.tractor-back-in-stock` → entry reappears)
- R4 → `HandleTractorOutOfStockTest#doesNothingWhenTractorIdIsUnknown`,
  `HandleTractorBackInStockTest#doesNothingWhenTractorIdIsUnknown`
- R5 → `SearchTest#returnsExactlyTheAvailableEntriesSuppliedByTheRepositoryPage` (unit-level trust
  of the repository contract), `CatalogEventsIntegrationTest#catalogReflectsTractorAddedOutOfStockAndBackInStockEvents`
  (real Postgres-backed proof: the tractor marked out-of-stock is excluded by
  `JpaCatalogEntryRepository`'s `available = true` predicate against the real `catalog_tractor`
  table, not a mocked repository)
- R6 → `SearchTest#happyPathAppliesFiltersSortAndPaginationTogether`,
  `CatalogControllerTest#searchReturns200WithMatchingEntries`
- R7 → `SearchTest#happyPathAppliesFiltersSortAndPaginationTogether`,
  `SearchTest#rejectsUnsupportedOperatorForField`
- R8 → `SearchTest#happyPathAppliesFiltersSortAndPaginationTogether`,
  `SearchTest#rejectsRangeOperatorMissingBounds`
- R9 → `SearchTest#rejectsUnsupportedOperatorForField`,
  `CatalogControllerTest#searchReturns400OnInvalidCatalogFilter`
- R10 → `SearchTest#rejectsRangeOperatorMissingBounds`,
  `CatalogControllerTest#searchReturns400OnInvalidCatalogFilter`
- R11 → `SearchTest#happyPathAppliesFiltersSortAndPaginationTogether` (two filters forwarded and
  both asserted against the repository call)
- R12 → `SearchTest#happyPathAppliesFiltersSortAndPaginationTogether` (sort by `PRICE`)
- R13 → `SearchTest#happyPathAppliesFiltersSortAndPaginationTogether` (`DESC` direction)
- R14 → `SearchTest#rejectsInvalidSortField`,
  `CatalogControllerTest#searchReturns400OnInvalidCatalogFilter`
- R15 → `SearchTest#defaultsSortToPriceAscendingWhenNotSpecified`
- R16 → `SearchTest#happyPathAppliesFiltersSortAndPaginationTogether` (`Cover:` Javadoc now lists
  `R16` explicitly; `nextCursor` decodes to the expected offset/tractorId, asserted in the test body)
- R17 → `SearchTest#happyPathAppliesFiltersSortAndPaginationTogether`,
  `CatalogControllerTest#searchReturns200WithMatchingEntries`
- R18 → `SearchTest#happyPathAppliesFiltersSortAndPaginationTogether` (`pageSize=2` trims the page)
- R19 → `SearchTest#defaultsPageSizeToTwentyWhenNotSpecified`
- R20 → `SearchTest#rejectsPageSizeBelowMinimum`, `SearchTest#rejectsPageSizeAboveMaximum`,
  `CatalogControllerTest#searchReturns400OnInvalidCatalogFilter`
- R21 → `SearchTest#rejectsUndecodableCursor`, `CatalogControllerTest#searchReturns400OnInvalidCursor`
- R22 → `CreateBrandTest#createsBrandWithGeneratedId`,
  `CatalogControllerTest#createBrandReturns201WithCreatedBrand`
- R23 → `CreateBrandTest#rejectsBlankName`, `CatalogControllerTest#createBrandReturns400WhenNameBlank`
- R24 → `CreateBrandTest#rejectsDuplicateName`,
  `CatalogControllerTest#createBrandReturns409WhenNameDuplicated`
- R25 → `CreateCategoryTest#createsCategoryWithGeneratedId`,
  `CatalogControllerTest#createCategoryReturns201WithCreatedCategory`
- R26 → `CreateCategoryTest#rejectsBlankName`,
  `CatalogControllerTest#createCategoryReturns400WhenNameBlank`
- R27 → `CreateCategoryTest#rejectsDuplicateName`,
  `CatalogControllerTest#createCategoryReturns409WhenNameDuplicated`

## Files touched

| File | Change |
|---|---|
| `catalog/build.gradle` | `project(':common')` + springdoc dependency added |
| `monolith/build.gradle` | `project(':catalog')` dependency added |
| `catalog/src/main/resources/db/migration/V2__create_catalog_tractor.sql` | new, then renamed from `V1__...` this session |
| `catalog/src/main/resources/db/migration/V3__create_catalog_brand.sql` | new, then renamed from `V2__...` this session |
| `catalog/src/main/resources/db/migration/V4__create_catalog_category.sql` | new, then renamed from `V3__...` this session |
| `catalog/src/main/java/com/tractor/catalog/domain/models/*.java` (8 files) | new — `CatalogEntry`, `Brand`, `Category`, 5 exceptions |
| `catalog/src/main/java/com/tractor/catalog/domain/ports/in/CatalogUseCase.java` | new |
| `catalog/src/main/java/com/tractor/catalog/domain/ports/out/*.java` (3 files) | new |
| `catalog/src/main/java/com/tractor/catalog/application/search/*.java` (9 files) | new |
| `catalog/src/main/java/com/tractor/catalog/application/brand/*.java` (2 files) | new |
| `catalog/src/main/java/com/tractor/catalog/application/category/*.java` (2 files) | new |
| `catalog/src/main/java/com/tractor/catalog/application/CatalogService.java` | new |
| `catalog/src/main/java/com/tractor/catalog/infrastructure/jpa/entry/*.java` (3 files) | new |
| `catalog/src/main/java/com/tractor/catalog/infrastructure/jpa/brand/*.java` (3 files) | new |
| `catalog/src/main/java/com/tractor/catalog/infrastructure/jpa/category/*.java` (3 files) | new |
| `catalog/src/main/java/com/tractor/catalog/infrastructure/jpa/CatalogJpaConfiguration.java` | new |
| `catalog/src/main/java/com/tractor/catalog/infrastructure/kafka/*.java` (4 files) | new |
| `catalog/src/main/java/com/tractor/catalog/infrastructure/api/CatalogController.java` | new |
| `catalog/src/main/java/com/tractor/catalog/infrastructure/api/CatalogExceptionHandler.java` | new |
| `catalog/src/main/java/com/tractor/catalog/infrastructure/api/dto/*.java` (8 files) | new |
| `catalog/src/test/java/com/tractor/catalog/application/catalogservice/*.java` (6 files) | new |
| `catalog/src/test/java/com/tractor/catalog/infrastructure/api/CatalogControllerTest.java` | new |
| `monolith/src/test/java/com/tractor/monolith/CatalogEventsIntegrationTest.java` | new — T21 cross-module test |
| `specs/catalog/tasks.md` | `T1..T22` checked `[x]`; T2/T3 migration filenames updated to `V2`-`V4`; T21 `BLOCKED` note cleared |
| `specs/catalog/design.md` | (spec_author, prior to this session) `## Persistence` table renumbered to `V2`-`V4`, global-version-sequence note added |
| `docs/architecture.md` | (spec_author, prior to this session) principle 5 documents the global Flyway version sequence |
| `features/003-catalog.md` | remains `state: in_progress` (reviewer flips to `done`) |

## Build evidence

```
$ gradle :catalog:test
BUILD SUCCESSFUL in 4s
6 actionable tasks: 2 executed, 4 up-to-date
```

```
$ gradle :monolith:test --tests '*CatalogEventsIntegrationTest*'
BUILD SUCCESSFUL in 11s
14 actionable tasks: 3 executed, 11 up-to-date
```

`monolith/build/test-results/test/TEST-com.tractor.monolith.CatalogEventsIntegrationTest.xml`:
`tests="1" skipped="0" failures="0" errors="0"`.

```
$ gradle clean build
...
> Task :cart:build
> Task :catalog:compileJava
> Task :catalog:test
> Task :catalog:build
> Task :common:build
> Task :inventory:compileJava
> Task :inventory:test
> Task :inventory:build
> Task :monolith:compileJava
> Task :monolith:test
> Task :monolith:check
> Task :monolith:build

BUILD SUCCESSFUL in 33s
29 actionable tasks: 29 executed
```

Full `clean build` is green across all 5 modules (`common`, `cart`, `catalog`, `inventory`,
`monolith`), no `FlywayException`, no failing tests.

## Smoke test

Performed against a running `:monolith:bootRun` instance. Preconditions: `back-postgres-1`
(Postgres 16, `localhost:5432`) was already running via Docker; no Kafka broker was available in
this environment, so the consumer containers (`inventory`, `catalog` listener groups) logged
continuous `Connection to node -1 (localhost/127.0.0.1:9092) could not be established` retries in
the background — this does not block HTTP, and is expected/inert for this smoke test since none of
the exercised endpoints (`/catalog/search`, `/catalog/brand`, `/catalog/category`) depend on Kafka.
Default Spring Security auto-configuration (no custom `SecurityFilterChain` exists anywhere in the
codebase) was bypassed for this manual run only via
`SPRING_AUTOCONFIGURE_EXCLUDE=org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration`
(the same exclusion `CatalogEventsIntegrationTest`/`InventoryOrderPlacedIntegrationTest` already
use via `@TestPropertySource`), since generated-password Basic auth is a dev-only artifact unrelated
to this feature.

```
$ curl -s -w "\nHTTP_STATUS:%{http_code}\n" -X POST http://localhost:8080/catalog/brand \
    -H "Content-Type: application/json" -d '{"name":"SmokeTestBrand"}'
{"id":"15d7ee22-dc88-4b5c-a043-d5767f17a4ea","name":"SmokeTestBrand"}
HTTP_STATUS:201

$ curl -s -w "\nHTTP_STATUS:%{http_code}\n" -X POST http://localhost:8080/catalog/category \
    -H "Content-Type: application/json" -d '{"name":"SmokeTestCategory"}'
{"id":"6b47e5a2-62c7-4d01-a92f-3bf84e6cc282","name":"SmokeTestCategory"}
HTTP_STATUS:201

$ curl -s -w "\nHTTP_STATUS:%{http_code}\n" -X POST http://localhost:8080/catalog/search \
    -H "Content-Type: application/json" \
    -d '{"filters":[],"sortField":null,"sortDirection":null,"cursor":null,"pageSize":5}'
{"items":[{"tractorId":"28169236-9ab4-4047-b406-e833885a196b","brand":"Acme","model":"X100","year":2024,
"price":15000.00,"horsepower":75,"weight":1800.50,"color":"green","category":"utility",
"description":"Compact utility tractor","imageUrl":"https://example.com/t.png","stock":10}, ... 5 items],
"totalItems":7,"page":1,"pageSize":5,"totalPages":2,
"nextCursor":"eyJvZmZzZXQiOjUsInNlZWsiOiIyMDAwMC4wMCIsInRyYWN0b3JJZCI6IjE2NjNiODI2LTM3N2YtNGZjYS04OWJmLWY5NDc4MzQ5ZmZjZCJ9"}
HTTP_STATUS:200
```

(The 7 items returned are real rows accumulated in `back-postgres-1` from prior test/dev runs
against the persistent local Postgres instance — including the tractor created by
`CatalogEventsIntegrationTest`'s own real Kafka event, still present under its own unique brand.
This is not fabricated: it is the true state of the shared local database at smoke-test time.)

Error paths:

```
$ curl -s -w "\nHTTP_STATUS:%{http_code}\n" -X POST http://localhost:8080/catalog/brand \
    -H "Content-Type: application/json" -d '{"name":"SmokeTestBrand"}'
{"message":"Brand already exists: SmokeTestBrand","code":"DUPLICATE_BRAND"}
HTTP_STATUS:409

$ curl -s -w "\nHTTP_STATUS:%{http_code}\n" -X POST http://localhost:8080/catalog/search \
    -H "Content-Type: application/json" \
    -d '{"filters":[{"field":"NOT_A_FIELD","operator":"EQUALS","value":"x"}],"pageSize":5}'
{"message":"Unknown filter field: NOT_A_FIELD","code":"INVALID_CATALOG_FILTER"}
HTTP_STATUS:400

$ curl -s -w "\nHTTP_STATUS:%{http_code}\n" -X POST http://localhost:8080/catalog/brand \
    -H "Content-Type: application/json" -d '{"name":"   "}'
{"message":"name must not be blank","code":"INVALID_CATALOG_DATA"}
HTTP_STATUS:400
```

Server stopped after the smoke test.

## Deviations from the spec

- None implemented. `catalog/src/main/java/com/tractor/catalog/application/search/SearchCatalogQuery.java`
  types `sortField` as `String` rather than the `SortField` enum (with `CatalogService.search`
  parsing/validating it) so that `T13`'s "invalid sort field (R14)" case is unit-testable at the
  `CatalogService` level without a Spring context — `design.md`'s "Classes, ports and signatures"
  table does not pin concrete Java types for `SearchCatalogQuery`'s fields, so this is an
  implementation-level choice consistent with `design.md`'s stated behavior ("`CatalogService.search`
  validates the query... sort field whitelist..."), not a deviation from anything explicit in the
  spec.

## Blockers

None remaining. The previous session's Flyway migration-version collision (see
`## Root cause of the previous blocker` above) was resolved by `spec_author`/human renumbering
catalog's migrations to `V2`-`V4` in `design.md`/`tasks.md`; this session applied that decision to
the actual files and completed `T21`/`T22` on top of it.

## Correction round 1 (reviewer `CHANGES_REQUESTED`, see `progress/review_catalog.md`)

1. Added `R16` to the `Cover:` Javadoc block of
   `SearchTest#happyPathAppliesFiltersSortAndPaginationTogether`
   (`catalog/src/test/java/com/tractor/catalog/application/catalogservice/SearchTest.java:52`), now
   reading `Cover: R6, R7, R8, R11, R12, R13, R16, R17, R18`. Corrected the `## Traceability` row for
   R16 above to point at this test's actual, corrected `Cover:` block.
2. Reformatted `catalog/src/main/java/com/tractor/catalog/application/CatalogService.java:62`
   (was 123 columns) by wrapping the cursor-decode ternary onto two lines; behavior unchanged.
3. Reformatted `SearchTest.java:92` (was 121 columns) by wrapping the `entries.findPage(...)` stub
   call onto two lines; behavior unchanged.
4. Reformatted `SearchTest.java:144` (was 121 columns) by wrapping the `entries.findPage(...)` stub
   call onto two lines; behavior unchanged.

Verified no line in either file exceeds 120 columns (`awk 'length($0) > 120'` over both files,
0 matches). Ran `gradle :catalog:test --tests '*SearchTest'` (`BUILD SUCCESSFUL`) then
`gradle clean build`:

```
$ gradle clean build
...
> Task :monolith:test
> Task :monolith:check
> Task :monolith:build

BUILD SUCCESSFUL in 33s
29 actionable tasks: 29 executed
```

No other files touched; no task/spec/requirement changed.
