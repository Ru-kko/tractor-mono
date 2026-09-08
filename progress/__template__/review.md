# Review — feature `<id>` `<feature-name>`

> **Template.** Copy to `progress/review_<feature-name>.md`. Written by
> `reviewer`. Checkpoint definitions: `.claude/agents/reviewer.md`. The chat
> answer is only `APPROVED -> progress/review_<feature-name>.md` or
> `CHANGES_REQUESTED -> ...`.

**Verdict:** `APPROVED | CHANGES_REQUESTED`
**Spec:** `specs/<feature-name>/`
**Implementation report:** `progress/impl_<feature-name>.md`

## Requirements ↔ tests traceability

Both directions. An uncovered requirement or an uncovered test is a blocker.

- R1: [x] covered by `AddTractorToCartUseCaseTest#addsTractorToExistingCart`
- R2: [x] covered by `CartControllerTest#mapsUnknownTractorTo404`
- R3: [ ] ← no test verifies it

### Tests without a requirement

- `CartControllerTest#returnsEmptyCart` ← no `Cover:` block

## Completed tasks

- T1: [x]
- T2: [x]
- T3: [ ] ← still `[ ]` in `specs/<feature-name>/tasks.md` with no justification

## Checkpoints

| Id | Checkpoint | Result |
|----|-----------|--------|
| C1 | `.\gradlew.bat clean build` is green | [x] |
| C2 | Every `R<n>` has at least one test | [ ] ← R3 |
| C3 | Every new/changed test declares `Cover: R<n>` | [ ] |
| C4 | Every `T<n>` is `[x]` or justified | [ ] ← T3 |
| C5 | Level 1: happy path + specific exception per use case | [x] |
| C6 | Level 2: `MockMvc` test per endpoint added or changed | [x] |
| C7 | No architecture violation (layers, coupling, table ownership, injection) | [x] |
| C8 | No convention violation (style, naming, springdoc, comments) | [x] |
| C9 | No anti-pattern from `docs/verification.md` | [x] |

## Build output

```
PS> .\gradlew.bat clean build

BUILD SUCCESSFUL in 51s
```

## Required changes

Ordered, each one citing `file:line`. Say what fails, not how to write it —
fixing is the implementer's job. Empty when `APPROVED`.

1. `cart/src/test/.../AddTractorToCartUseCaseTest.java` — add a test for R3.
2. `cart/src/main/.../CartServiceImpl.java:42` — `@Autowired` field, must be
   constructor injection (`docs/architecture.md` §5).
3. `specs/<feature-name>/tasks.md:12` — T3 still `[ ]`; complete it or justify
   it in `progress/impl_<feature-name>.md`.

## Nits

Non-blocking observations. A `docs/conventions.md` violation is **not** a nit
in this repository.

- `<none>`
