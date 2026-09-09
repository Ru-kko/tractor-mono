# Review — feature `003` `catalog`

**Verdict:** `APPROVED`
**Spec:** `specs/catalog/`
**Implementation report:** `progress/impl_catalog.md`

This is a re-review after correction round 1. The previous verdict was
`CHANGES_REQUESTED` for exactly 4 items (missing `Cover: R16`, and 3 lines over
120 columns). All 4 are verified fixed below; nothing else was touched.

## Requirements ↔ tests traceability

Both directions. An uncovered requirement or an uncovered test is a blocker.

Checked by grepping every `Cover:` Javadoc block in `catalog/src/test/**` and
`monolith/src/test/java/com/tractor/monolith/CatalogEventsIntegrationTest.java`, not by trusting
`progress/impl_catalog.md`'s table.

- R1: [x] `HandleTractorAddedTest#createsAnAvailableCatalogEntryFromTheEvent`, `CatalogEventsIntegrationTest#catalogReflectsTractorAddedOutOfStockAndBackInStockEvents`
- R2: [x] `HandleTractorOutOfStockTest#marksAnExistingEntryUnavailable`
- R3: [x] `HandleTractorBackInStockTest#marksAnExistingEntryAvailable`
- R4: [x] `HandleTractorOutOfStockTest#doesNothingWhenTractorIdIsUnknown`, `HandleTractorBackInStockTest#doesNothingWhenTractorIdIsUnknown`
- R5: [x] `SearchTest#returnsExactlyTheAvailableEntriesSuppliedByTheRepositoryPage`
- R6: [x] `SearchTest#happyPathAppliesFiltersSortAndPaginationTogether`, `CatalogControllerTest#searchReturns200WithMatchingEntries`
- R7: [x] `SearchTest#happyPathAppliesFiltersSortAndPaginationTogether`
- R8: [x] `SearchTest#happyPathAppliesFiltersSortAndPaginationTogether`
- R9: [x] `SearchTest#rejectsUnsupportedOperatorForField`, `CatalogControllerTest#searchReturns400OnInvalidCatalogFilter`
- R10: [x] `SearchTest#rejectsRangeOperatorMissingBounds`, `CatalogControllerTest#searchReturns400OnInvalidCatalogFilter`
- R11: [x] `SearchTest#happyPathAppliesFiltersSortAndPaginationTogether`
- R12: [x] `SearchTest#happyPathAppliesFiltersSortAndPaginationTogether`
- R13: [x] `SearchTest#happyPathAppliesFiltersSortAndPaginationTogether`
- R14: [x] `SearchTest#rejectsInvalidSortField`, `CatalogControllerTest#searchReturns400OnInvalidCatalogFilter`
- R15: [x] `SearchTest#defaultsSortToPriceAscendingWhenNotSpecified`
- R16: [x] **FIXED.** `catalog/src/test/java/com/tractor/catalog/application/catalogservice/SearchTest.java:52`
  now reads `Cover: R6, R7, R8, R11, R12, R13, R16, R17, R18` — verified directly by reading the file,
  not by trusting the impl report. `happyPathAppliesFiltersSortAndPaginationTogether`'s body still
  decodes `result.nextCursor()` and asserts `decoded.offset()`/`decoded.lastTractorId()` (lines 79-82),
  so the `Cover:` block now matches the actual test behavior.
- R17: [x] `SearchTest#happyPathAppliesFiltersSortAndPaginationTogether`, `CatalogControllerTest#searchReturns200WithMatchingEntries`
- R18: [x] `SearchTest#happyPathAppliesFiltersSortAndPaginationTogether`
- R19: [x] `SearchTest#defaultsPageSizeToTwentyWhenNotSpecified`
- R20: [x] `SearchTest#rejectsPageSizeBelowMinimum`, `SearchTest#rejectsPageSizeAboveMaximum`, `CatalogControllerTest#searchReturns400OnInvalidCatalogFilter`
- R21: [x] `SearchTest#rejectsUndecodableCursor`, `CatalogControllerTest#searchReturns400OnInvalidCursor`
- R22: [x] `CreateBrandTest#createsBrandWithGeneratedId`, `CatalogControllerTest#createBrandReturns201WithCreatedBrand`
- R23: [x] `CreateBrandTest#rejectsBlankName`, `CatalogControllerTest#createBrandReturns400WhenNameBlank`
- R24: [x] `CreateBrandTest#rejectsDuplicateName`, `CatalogControllerTest#createBrandReturns409WhenNameDuplicated`
- R25: [x] `CreateCategoryTest#createsCategoryWithGeneratedId`, `CatalogControllerTest#createCategoryReturns201WithCreatedCategory`
- R26: [x] `CreateCategoryTest#rejectsBlankName`, `CatalogControllerTest#createCategoryReturns400WhenNameBlank`
- R27: [x] `CreateCategoryTest#rejectsDuplicateName`, `CatalogControllerTest#createCategoryReturns409WhenNameDuplicated`

All R1–R27 are covered by at least one test declaring the matching `Cover:` id.

### Tests without a requirement

None found. Every `Cover:` block in `catalog/src/test/**` and `CatalogEventsIntegrationTest` names
requirement ids that exist in `specs/catalog/requirements.md`, and each id maps to a real,
currently-passing test.

## Completed tasks

- T1–T22: all `[x]` in `specs/catalog/tasks.md`.
- "Definition of done" checklist at the bottom of `specs/catalog/tasks.md`: all `[x]`, and now
  actually true — every test declares a `Cover: R<n>` block and, after this round's fix, `R16` is
  among them.

Sanity-checked history, both confirmed correct on disk (unchanged from round 0, re-confirmed by
timestamp: these files were last modified 17:17–17:45, before this correction round started):
- Migration renumbering: `catalog/src/main/resources/db/migration/` contains
  `V2__create_catalog_tractor.sql`, `V3__create_catalog_brand.sql`, `V4__create_catalog_category.sql`
  (no `V1` collision with `inventory`'s `V1__create_inventory_stock_item.sql`), and
  `docs/architecture.md:55-58` documents the new global-Flyway-sequence bullet as claimed.
- Security bypass: `SPRING_AUTOCONFIGURE_EXCLUDE=...ServletWebSecurityAutoConfiguration` used for the
  manual smoke test does **not** appear in any checked-in `application.properties`/`application.yaml`/
  `build.gradle`. It only appears as a `@TestPropertySource` value in
  `monolith/src/test/java/com/tractor/monolith/CatalogEventsIntegrationTest.java:42-45`, mirroring the
  pre-existing `InventoryOrderPlacedIntegrationTest`. No residue left behind.

## Correction round 1 verification (this review)

Verified independently, not taken on the implementer's word:

1. `SearchTest.java:52` — `Cover:` block now includes `R16` (confirmed by reading the file directly).
2. Line-length check via `awk '{ if (length($0) > 120) print NR": "length($0) }'` on both
   `catalog/src/main/java/com/tractor/catalog/application/CatalogService.java` and
   `catalog/src/test/java/com/tractor/catalog/application/catalogservice/SearchTest.java`: zero
   violations in either file now.
   - `CatalogService.java` around the former line 62/63: the cursor-decode ternary is now wrapped
     across two lines (`query.cursor() != null ? cursorCodec.decode(query.cursor()) : new
     CatalogCursor(null, null, 0);`), same expression, same semantics, just reformatted.
   - `SearchTest.java` former lines 92 and 144: both `when(entries.findPage(...))` stub chains are
     now wrapped across two lines with the same arguments in the same order; no assertion or stub
     value changed.
3. `progress/impl_catalog.md`'s traceability table (line 179-180) now reads: `R16 →
   SearchTest#happyPathAppliesFiltersSortAndPaginationTogether (Cover: Javadoc now lists R16
   explicitly; nextCursor decodes to the expected offset/tractorId, asserted in the test body)` —
   points at the corrected test and the corrected Javadoc block.
4. `gradle clean build` (system gradle, no wrapper) run independently: `BUILD SUCCESSFUL in 29s`,
   29/29 tasks executed, no failing tests.
5. Scope check: compared file mtimes against `progress/review_catalog.md`'s round-0 write time
   (18:02:48). Only `CatalogService.java` (18:03:42), `SearchTest.java` (18:03:48),
   `progress/impl_catalog.md` (18:04:55), and `progress/current.md` (18:05:06) were modified after
   that point. `catalog/build.gradle`, `docs/architecture.md`, `features/003-catalog.md`,
   `inventory/.../OrderPlacedEventListener.java`, and `monolith/build.gradle` all predate the round-0
   review (17:17–17:45) and were not touched again — scope of this correction round was exactly the
   4 items requested, nothing more.

## Checkpoints

| Id | Checkpoint | Result |
|----|-----------|--------|
| C1 | `gradle clean build` is green | [x] `BUILD SUCCESSFUL in 29s`, 29/29 tasks executed |
| C2 | Every `R<n>` has at least one test | [x] R1–R27 all covered, including R16 after the fix |
| C3 | Every new/changed test declares `Cover: R<n>` | [x] every test has a `Cover:` block naming real requirements; `SearchTest#happyPathAppliesFiltersSortAndPaginationTogether` now includes `R16` |
| C4 | Every `T<n>` is `[x]` or justified | [x] |
| C5 | Level 1: happy path + specific exception per use case | [x] `CatalogService` search/brand/category/event-handlers all covered |
| C6 | Level 2: `MockMvc` test per endpoint added or changed, + cross-module test | [x] `CatalogControllerTest` (`@WebMvcTest`+`@MockitoBean`+real `MockMvc`); `CatalogEventsIntegrationTest` (`@SpringBootTest RANDOM_PORT` + `@EmbeddedKafka` + `TestRestTemplate`, no hardcoded `localhost:8080`, uses Awaitility not `Thread.sleep`) |
| C7 | No architecture violation (layers, coupling, table ownership, injection) | [x] no `@Autowired` field in production code; all 5 exceptions extend `TractorStoreException`; tables are `catalog_*` only, no cross-module joins/FKs; vertical-sliced package layout matches `design.md` exactly (`application.search`/`.brand`/`.category`, `infrastructure.jpa.entry`/`.brand`/`.category`) |
| C8 | No convention violation (style, naming, springdoc, comments) | [x] all 3 previously-flagged lines now ≤120 columns; verified with `awk` line-length scan on both files, zero violations |
| C9 | No anti-pattern from `docs/verification.md` | [x] no `assertDoesNotThrow`-only, no mocked domain objects, no `Thread.sleep`, no `@Disabled`, no hardcoded `localhost:8080` |

## Build output

```
$ gradle clean build
...
> Task :monolith:test
> Task :monolith:check
> Task :monolith:build

BUILD SUCCESSFUL in 29s
29 actionable tasks: 29 executed
```

Verified independently (not copy-pasted from `progress/impl_catalog.md`); all 5 modules
(`common`, `cart`, `catalog`, `inventory`, `monolith`) built and tested green, no Flyway version
collision, no failing tests.

## Required changes

None. All 4 items from the previous round are fixed and verified.

## Nits

Non-blocking observations, carried over from round 0 (unchanged, not regressions of this fix):

- `catalog/src/main/java/com/tractor/catalog/domain/ports/in/CatalogUseCase.java:3-8` and
  `catalog/src/main/java/com/tractor/catalog/domain/ports/out/CatalogEntryRepository.java:3-6` —
  domain-layer ports import application-layer types (`BrandSnapshot`, `SearchCatalogQuery`,
  `CatalogFilter`, `SortField`, etc.), inverting the textbook hexagonal dependency direction (domain
  should not depend on application). This is not a regression introduced by this feature — it is an
  exact mirror of the already-merged `inventory/src/main/java/com/tractor/inventory/domain/ports/in/InventoryUseCase.java`,
  which has the identical shape, and `design.md` explicitly calls out that it mirrors it. Not treated
  as a blocker given that precedent, but worth revisiting at the architecture-doc level across both
  modules if this is meant to be tightened.
- No class in `catalog/src/main/java/com/tractor/catalog/**` carries a Javadoc comment, despite
  `docs/conventions.md`'s "Javadoc on public API." This exactly matches the pre-existing, already
  `done` `inventory` module (0/23 files have Javadoc either), so it is a systemic, repo-wide gap
  rather than something specific to this feature; flagged for a future cross-module cleanup rather
  than blocking `catalog` alone.
- `catalog/src/main/java/com/tractor/catalog/infrastructure/jpa/entry/CatalogEntryEntity.java:90-179` —
  public setters for every column bypass `CatalogEntry.markAvailable()`/`markUnavailable()`'s
  intent-revealing API at the JPA boundary; acceptable for a mapping class but worth noting.
