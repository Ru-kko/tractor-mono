# Implementation — feature `<id>` `<feature-name>`

> **Template.** Copy to `progress/impl_<feature-name>.md`. Written by
> `implementer`. This file is the evidence the `reviewer` audits — the chat
> answer is only `done -> progress/impl_<feature-name>.md`.

**Spec:** `specs/<feature-name>/`
**Module:** `<module>`
**Result:** `done | blocked`

## Tasks executed

Mirrors `specs/<feature-name>/tasks.md`. Anything left `[ ]` needs a
justification here or the reviewer rejects the feature.

- [x] T1 — `<what was actually done, and where: path/to/Class.java>`
- [x] T2 — `<...>`
- [ ] T3 — not done: `<justification>`

## Traceability

Level 4 of `docs/verification.md`. Every `R<n>` maps to at least one concrete
test, and every test declares its `Cover:` Javadoc block.

- R1 → `AddTractorToCartUseCaseTest#addsTractorToExistingCart`, `CartControllerTest#returnsCartWithAddedTractor`
- R2 → `AddTractorToCartUseCaseTest#rejectsUnknownTractor`
- R3 → `CartCheckoutIntegrationTest#reservesStockOnCheckout`

## Files touched

| File | Change |
|---|---|
| `<module>/src/main/java/com/tractor/<module>/application/<Class>.java` | new |
| `<module>/src/test/java/com/tractor/<module>/application/<Class>Test.java` | new |
| `settings.gradle` | `<module>` added as subproject |

## Build evidence

```
PS> .\gradlew.bat clean build

BUILD SUCCESSFUL in 42s
```

Paste the real tail of the output, including the failure output if it was red
at any point and how it was fixed. "It works" is not evidence.

## Smoke test

Level 3 of `docs/verification.md`. Delete only if the feature exposes no HTTP.
Use `curl.exe` in PowerShell (bare `curl` is an alias for `Invoke-WebRequest`).

```
PS> curl.exe -s http://localhost:8080/actuator/health
{"status":"UP"}

PS> curl.exe -s -X POST http://localhost:8080/<resource> -H 'Content-Type: application/json' -d '{"...":"..."}' -i
HTTP/1.1 201
{"...":"..."}

PS> curl.exe -s -X POST http://localhost:8080/<resource> -H 'Content-Type: application/json' -d '{"...":"does-not-exist"}' -i
HTTP/1.1 404
{"code":"<CODE>"}
```

## Deviations from the spec

Anything implemented differently from `design.md`, and why. If a deviation
was needed and not agreed, the correct move was to stop — record that here.

- `<none>`

## Blockers

Only if `Result: blocked`. Failing task, exact error output, and what was
tried. No improvised workarounds.

- `<none>`
