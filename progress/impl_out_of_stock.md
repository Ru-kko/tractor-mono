# Implementation — feature `002` `out_of_stock`

> `sdd: false` — no `specs/out_of_stock/` exists by explicit human decision.
> This report is written directly against `features/002-out_of_stock.md`.

**Spec:** N/A (`sdd: false`)
**Module:** `inventory`
**Result:** `done`

## Tasks executed

- [x] T1 — Emit `TractorOutOfStock` when a tractor's stock transitions from >0
      to 0, and also when a tractor is created with `stock: 0` (edge case
      called out explicitly in the feature file).
  - `common/src/main/java/com/tractor/common/event/TractorOutOfStockEvent.java` — new event record.
  - `inventory/src/main/java/com/tractor/inventory/domain/ports/out/EventPublisher.java` — added
    `publishTractorOutOfStock`.
  - `inventory/src/main/java/com/tractor/inventory/infrastructure/kafka/KafkaEventPublisher.java` —
    publishes to new topic `inventory.tractor-out-of-stock`.
  - `inventory/src/main/java/com/tractor/inventory/application/InventoryService.java` —
    `addTractor` fires it immediately when `command.stock() == 0`; `refillStock` and
    `handleOrderPlaced` fire it via the new `publishAvailabilityTransition` helper whenever the
    stock mutation takes a tractor from >0 to exactly 0.
- [x] T2 — Emit `TractorBackInStock` when a tractor's stock transitions from 0
      to >0.
  - `common/src/main/java/com/tractor/common/event/TractorBackInStockEvent.java` — new event record.
  - Same `EventPublisher` / `KafkaEventPublisher` (topic `inventory.tractor-back-in-stock`) /
    `InventoryService` changes as above — `publishAvailabilityTransition` fires this branch when
    `previousStock == 0 && currentStock > 0`. Reachable today only through `refillStock` (the only
    stock-increasing path), since `handleOrderPlaced` only decreases stock and `addTractor` has no
    "previous" state.

Both events are additive: the existing `StockUpdatedEvent` (feature 001) is still published
unconditionally on every mutation, unchanged.

## Design notes / edge cases considered

- **Refill by 0**: `refillStock` already rejects `quantity <= 0` with
  `InvalidTractorDataException` before touching stock or publishing anything (pre-existing
  behaviour from feature 001) — so "refill by 0" never reaches the transition logic, and no new
  event fires. Verified this stays true with `RefillStockTest#rejectsNonPositiveAmount`
  (pre-existing test, `verifyNoInteractions(events)`).
- **Add tractor with stock 0**: fires `TractorOutOfStock` immediately after `TractorAdded`, since
  the tractor comes into existence already unavailable. There is no "previous state" to diff
  against for a brand-new tractor, so this is a direct check on `command.stock() == 0`
  rather than going through `publishAvailabilityTransition`.
- **Add tractor with stock > 0**: no `TractorBackInStock` — that event is specifically a
  *transition* signal; a tractor being created already in stock never was out of stock, so there is
  nothing to transition from. Covered by
  `AddTractorTest#createsTractorGeneratesUuidPersistsAndPublishesEvent` asserting
  `verify(events, never()).publishTractorOutOfStock(any())` /
  `publishTractorBackInStock(any())`.
- **Unrelated stock changes (e.g. 5 → 3, 10 → 8)**: neither event fires. Covered by
  `RefillStockTest#increasesStockAndPublishesStockUpdatedEvent` (5 → 8) and
  `HandleOrderPlacedTest#decreasesStockForEachLineAndPublishesStockUpdatedEvents` (10 → 6, 5 → 3),
  both asserting `verify(events, never())` on both new methods.
- **Order line exactly exhausting stock (N → 0)**: `handleOrderPlaced` fires `TractorOutOfStock`
  per affected line, since `reserve()` is applied per `StockItem` and previous stock is captured
  before the mutation for each item independently.
- **`InsufficientStockException` path**: `reserve()` throws before mutating `stock` (see
  `StockItem.reserve`), so a rejected order line never reaches
  `publishAvailabilityTransition` — no spurious events on a failed reservation. Already implicitly
  covered by the pre-existing `HandleOrderPlacedTest#leavesStockUnchangedWhenALineWouldGoNegative`
  (`verifyNoInteractions` isn't used there but the item's stock fields are asserted unchanged and
  `repository.save` is verified `never()`; no event-publishing call happens because the exception
  is thrown from within `StockItem.reserve` before `InventoryService` ever calls `events.*`).

## New unit tests (Level 1 of `docs/verification.md`)

All added to the existing per-method test files in
`inventory/src/test/java/com/tractor/inventory/application/inventoryservice/`, matching this
module's established "one test file per use-case method" layout:

- `AddTractorTest#publishesTractorOutOfStockWhenCreatedWithZeroStock` — happy path for T1's edge
  case; also strengthened `createsTractorGeneratesUuidPersistsAndPublishesEvent` with negative
  assertions that neither new event fires on a normal (`stock: 10`) creation.
- `RefillStockTest#publishesTractorBackInStockWhenStockWasZero` — happy path for T2; also
  strengthened `increasesStockAndPublishesStockUpdatedEvent` (5 → 8) with negative assertions that
  neither new event fires on an unrelated stock increase.
- `HandleOrderPlacedTest#publishesTractorOutOfStockWhenLineExhaustsStock` — happy path for T1 via
  the order-placed path; also strengthened
  `decreasesStockForEachLineAndPublishesStockUpdatedEvents` with negative assertions that neither
  new event fires on an unrelated stock decrease (10 → 6, 5 → 3).

No `Cover: R<n>` Javadoc blocks were added — this feature has `sdd: false` and no
`requirements.md`, so there are no `R<n>` identifiers to reference (per the leader's instructions,
the `## Traceability` section below is marked N/A instead).

## Traceability

N/A — `sdd: false`, no `specs/out_of_stock/requirements.md`, no `R<n>` identifiers exist for this
feature. See "New unit tests" above for the behaviour-to-test map instead.

## Files touched

| File | Change |
|---|---|
| `common/src/main/java/com/tractor/common/event/TractorOutOfStockEvent.java` | new |
| `common/src/main/java/com/tractor/common/event/TractorBackInStockEvent.java` | new |
| `inventory/src/main/java/com/tractor/inventory/domain/ports/out/EventPublisher.java` | added 2 methods |
| `inventory/src/main/java/com/tractor/inventory/infrastructure/kafka/KafkaEventPublisher.java` | added 2 topics + 2 methods |
| `inventory/src/main/java/com/tractor/inventory/application/InventoryService.java` | added transition detection in `addTractor`, `refillStock`, `handleOrderPlaced` + `publishAvailabilityTransition` helper |
| `inventory/src/test/java/com/tractor/inventory/application/inventoryservice/AddTractorTest.java` | new test + strengthened existing one |
| `inventory/src/test/java/com/tractor/inventory/application/inventoryservice/RefillStockTest.java` | new test + strengthened existing one |
| `inventory/src/test/java/com/tractor/inventory/application/inventoryservice/HandleOrderPlacedTest.java` | new test + strengthened existing one |

## Build evidence

```
$ gradle :inventory:test --tests '*AddTractorTest' --tests '*RefillStockTest' --tests '*HandleOrderPlacedTest'
...
> Task :inventory:test

BUILD SUCCESSFUL in 4s
6 actionable tasks: 6 executed

$ gradle clean build
...
> Task :cart:build
> Task :catalog:build
> Task :common:build
> Task :inventory:compileJava
> Task :inventory:test
> Task :inventory:build
> Task :monolith:test
> Task :monolith:build

BUILD SUCCESSFUL in 23s
26 actionable tasks: 26 executed
```

Note on the "known catalog failure" mentioned in the task brief: in this checkout, the `catalog`
module currently has **no Java source at all** (`compileJava NO-SOURCE`, `test NO-SOURCE`) — it is
an empty scaffold subproject with only `build.gradle`. There is therefore nothing to fail; the full
`gradle clean build` is green end-to-end, `catalog` included. `catalog` was not touched.

## Smoke test

This feature adds no new HTTP endpoint and changes no request/response contract — `refillStock`
and `addTractor` are exercised exactly as before via `InventoryController`
(`inventory/src/main/java/com/tractor/inventory/infrastructure/api/InventoryController.java`), and
their existing `InventoryControllerTest` (Level 2, unchanged, still green) already proves the HTTP
contract is unaffected.

The new events (`TractorOutOfStockEvent`, `TractorBackInStockEvent`) are published exclusively
through `KafkaEventPublisher` onto two new Kafka topics
(`inventory.tractor-out-of-stock`, `inventory.tractor-back-in-stock`). There is no consumer, HTTP
projection, or log line anywhere in the codebase today that surfaces a Kafka message back out —
the existing `StockUpdatedEvent`/`TractorAddedEvent` from feature 001 have exactly the same
property, so this is consistent with the established pattern, not a gap introduced here. Booting
the server and hitting `/inventory/...` with `curl` would exercise the same code paths already
proven by `InventoryControllerTest` and would not add observable evidence of the new events beyond
what the Mockito-based unit tests already assert (exact event object published to the
`EventPublisher` port). Per the leader's judgment call in the task brief, a manual smoke test was
skipped in favor of the unit-test evidence above, which is the level at which these events are
actually observable in this codebase.

## Deviations from the spec

- None. Event field shape (`tractorId` only) matches the feature file's minimum requirement
  ("carrying at least the tractor id") and mirrors the existing `StockUpdatedEvent`
  single/few-field style.

## Blockers

- `<none>`
