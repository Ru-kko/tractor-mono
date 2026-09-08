# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

`tractor-back` (Gradle root project `back`), a Spring Boot 4.1 / Java 21 backend, currently at the
very start of its life: `src/` is still the unmodified Spring Boot skeleton
(`com.tractor.BackApplication`) and no feature modules exist yet. The target architecture (modular
monolith with `catalog`, `inventory`, `cart`, `order`, `common`, `monolith` gradle subprojects) is
documented but not yet scaffolded — do not assume any of those modules exist without checking
`settings.gradle` first.

This repo runs on **Spec Driven Development (SDD)** with a fixed multi-agent workflow. Read
`docs/spec.md` before doing anything that touches `features/`, `specs/`, or `progress/`.

## Commands

```powershell
.\gradlew.bat build                              # compile + test, all modules
.\gradlew.bat clean build                         # full verification before closing any feature
.\gradlew.bat test                                # all modules, tests only
.\gradlew.bat :cart:test                          # a single module (once modules exist)
.\gradlew.bat :cart:test --tests '*ControllerTest' # a single test class pattern
.\gradlew.bat :monolith:bootRun                   # run the app for a manual smoke test
```

Test reports land at `<module>/build/reports/tests/test/index.html`. On Windows PowerShell, use
`curl.exe` explicitly when smoke-testing (`curl` is aliased to `Invoke-WebRequest`).

A build must end `BUILD SUCCESSFUL` before any feature is marked `done`. Never start new feature
work on a red baseline.


## Architecture rules (`docs/architecture.md` — the reviewer enforces these literally)

1. **Modular monolith, hexagonal per module.** Each module (`catalog`, `inventory`, `cart`, `order`,
   `common`, `monolith`) is its own gradle subproject with `application` / `domain` /
   `infrastructure` packages. Modules never depend on each other directly; `monolith` wires
   everything with `spring-modulith`.
2. **Exclusive table ownership** — this is the rule most likely to be violated by an inattentive
   change:
   - Every table is prefixed with its owning module (`catalog_tractor`, `cart_line`, ...).
   - Entity/mapping classes for a table live only in that module's `infrastructure`.
   - No cross-module joins, FKs, views or native queries. A foreign id from another module is
     stored as a plain value, not a relation.
   - Migrations for a table live under `<module>/src/main/resources/db/migration/`, owned by that
     module only.
   - Cross-module data flows through the other module's public API or domain events — never
     through its tables. `common` owns no tables.
3. Constructor injection only — `@Autowired` on fields is a rejection-worthy violation.
4. All exceptions extend `TractorStoreException`.
5. Single responsibility per class.

## Conventions (`docs/conventions.md`)

Java 21, 2-space indentation, ≤120-column lines, opening brace on the declaration line, closing
brace on its own line. Naming: `PascalCase` classes/interfaces (no `I` prefix), `camelCase`
methods/variables, `UPPER_CASE` constants, lowercase packages. Test classes are `<ClassUnderTest>Test`
in a mirrored package under `src/test`. No comments explaining what code already says; Javadoc on
public API; `springdoc` annotations on every public controller method. This codebase intentionally
optimizes for looking like it was written by one person — match existing style exactly rather than
introducing a variant.

## Verification levels (`docs/verification.md` — required, in order)

1. **Use case unit tests** (mandatory, every use case): happy path asserting concrete
   return/interaction + at least one failure path asserting a specific `TractorStoreException`
   subtype. No Spring context; ports mocked with Mockito, domain objects never mocked.
2. **Controller integration tests** (mandatory for any HTTP change): `@WebMvcTest` + `@MockitoBean`
   (not `@MockBean`, removed in Spring Boot 4) through real `MockMvc`, not direct method calls.
   Cross-module behavior gets one `@SpringBootTest(webEnvironment = RANDOM_PORT)` test in
   `monolith`.
3. **Manual smoke test** with a running server before closing a session — paste the request/response
   transcript into `progress/impl_<name>.md` as evidence.
4. **Requirements traceability** (mandatory for `sdd: true` features): every `R<n>` ↔ test, both
   directions, documented in a `## Traceability` section.

Anti-patterns that get a feature rejected outright: claiming "it works" with no test,
`assertDoesNotThrow` as the only assertion, calling the controller method directly and calling it
an integration test, mocking domain objects, `Thread.sleep()` for async waits, hardcoded
`localhost:8080`, `@Disabled`-ing a failing test, marking `done` without a green
`clean build`.
