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

