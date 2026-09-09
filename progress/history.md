# History

## `2026-09-08` — feature `002` `out_of_stock` — `done`

**Module:** `inventory`
**Spec:** N/A (`sdd: false`, no `specs/out_of_stock/` by explicit human decision)
**Report:** `progress/impl_out_of_stock.md` · **Review:** none (no reviewer step for `sdd: false`)

**What shipped:** `InventoryService` now publishes `TractorOutOfStock` and `TractorBackInStock`
domain events (new Kafka topics `inventory.tractor-out-of-stock` /
`inventory.tractor-back-in-stock`) whenever a stock mutation crosses the 0 boundary — on
`refillStock` (0 → >0), `handleOrderPlaced` (>0 → 0 per line), and `addTractor` (created with
`stock: 0` fires `TractorOutOfStock` immediately) — in addition to, not instead of, the existing
`StockUpdatedEvent` from feature 001.

**Requirements:** N/A (`sdd: false`, no `R<n>` identifiers) — behaviour-to-test map is in
`progress/impl_out_of_stock.md` under "New unit tests".

**Review rounds:** 0 (no reviewer for this `sdd: false` feature; implementer flipped `state: done`
directly per explicit leader instructions).

**Decisions worth remembering:**
- Transition detection is a pure `previousStock`/`currentStock` diff captured immediately before
  each mutation (`publishAvailabilityTransition` helper in `InventoryService`), not a query against
  persisted state — keeps it correct even though `StockItem` is mutated in place before `save`.
- Refilling by a non-positive amount was already rejected by feature 001's validation, so it never
  reaches the new transition logic — no new guard was needed for that edge case.
- `catalog` module currently has zero Java source in this checkout, so the previously-reported
  `:catalog:test` red baseline did not reproduce here; full `gradle clean build` is green,
  `catalog` included and untouched.

**Left behind on purpose:**
- No consumer/logger surfaces the two new Kafka events anywhere in the codebase yet (same as the
  pre-existing `TractorAdded`/`StockUpdated` events) — proof of correctness lives at the unit-test
  level (exact event asserted on the `EventPublisher` port), not via a manual HTTP smoke test.

## `2026-09-08` — feature `003` `catalog` — `done`

**Module:** `catalog`
**Spec:** `specs/catalog/`
**Report:** `progress/impl_catalog.md` · **Review:** `progress/review_catalog.md`

**What shipped:** The `catalog` module now supports cursor-paginated tractor search
(`POST /catalog/search`) with per-field filter operators (equals/not-equals/greater-than/less-than/
range) and sorting by price/year/horsepower/weight, plus brand and category creation
(`POST /catalog/brand`, `POST /catalog/category`). Catalog entries are kept in sync with inventory
purely via Kafka: `TractorAdded` creates an available entry, `TractorOutOfStock` marks it
unavailable (excluded from search), and `TractorBackInStock` marks it available again. The module
follows a vertical-sliced package layout (`application.search`/`.brand`/`.category`,
`infrastructure.jpa.entry`/`.brand`/`.category`) per the corrected `design.md`.

**Requirements:** R1–R27, all covered — see the traceability map in `progress/impl_catalog.md` and
independently re-verified in `progress/review_catalog.md`.

**Review rounds:** 2 (`CHANGES_REQUESTED` once for a missing `Cover: R16` Javadoc tag and 3 lines
over the 120-column limit; `APPROVED` on re-review after both were fixed with no behavior change).

**Decisions worth remembering:**
- Flyway runs a single global migration-version sequence shared by every module's jar on the
  monolith's classpath (one `classpath:db/migration` location, one `flyway_schema_history` table) —
  module-local `V1`, `V2`, ... numbering is not isolated per module. Catalog's migrations collided
  with `inventory`'s pre-existing `V1__create_inventory_stock_item.sql` and were renumbered to
  `V2__create_catalog_tractor.sql`, `V3__create_catalog_brand.sql`, `V4__create_catalog_category.sql`.
  `docs/architecture.md` principle 5 now documents this global-sequence constraint explicitly for
  future features to plan migration version numbers around.
- Round-1 review correction was narrowly scoped: added the missing `R16` id to an existing `Cover:`
  Javadoc block in `SearchTest` (the test itself already exercised the behavior) and reformatted 3
  over-length lines in `CatalogService.java`/`SearchTest.java` with no semantic change — verified by
  the reviewer via file mtimes that nothing else was touched.

**Left behind on purpose:**
- None flagged as blocking. Reviewer nits (non-blocking, carried from round 0): domain-layer ports
  (`CatalogUseCase`, `CatalogEntryRepository`) import application-layer types, mirroring the same
  pre-existing shape in `inventory`'s `InventoryUseCase`; no class in `catalog/src/main/java` carries
  Javadoc, matching the systemic repo-wide gap already present in `inventory`; JPA entity setters
  bypass the domain's intent-revealing `markAvailable`/`markUnavailable` API (acceptable at the
  mapping-class boundary).

