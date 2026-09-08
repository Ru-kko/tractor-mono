# Requirements — `<feature-name>`

> **Template.** Copy `specs/__template__/` to `specs/<feature-name>/` and
> replace every `<...>` placeholder. Written by `spec_author` only, before any
> code exists. Notation and rules: `docs/spec.md`.

**Feature file:** `features/<id>-<feature-name>.md`
**Owning module:** `<catalog | inventory | cart | order | common | monolith>`

## Requirements

EARS, one `SHALL` (or `SHALL NOT`) per requirement. Stable ids `R1..R<n>` —
never renumber once the spec is approved. No `MAY`, `SHOULD`, `CAN`, `MUST`.
Each requirement must be verifiable by one concrete JUnit test; if it is not,
split it or raise it as an open question.

| Pattern | Template |
|---|---|
| Ubiquitous | `The <system> SHALL <do something>.` |
| Event | `WHEN <trigger>, the <system> SHALL <do something>.` |
| State | `WHILE <something is true>, the <system> SHALL <do something>.` |
| Option | `WHERE <condition>, the <system> SHALL <do something>.` |
| Unwanted | `IF <something is true>, the <system> SHALL NOT <do something>.` |

R1. The `<system>` SHALL `<do something>`.

R2. WHEN `<trigger>`, the `<system>` SHALL `<do something>`.

R3. IF `<unwanted condition>`, the `<system>` SHALL NOT `<do something>`.

## Coverage of the acceptance criteria

Every criterion in the `## Acceptance` section of the feature file maps to at
least one requirement. An empty cell means the spec is incomplete.

| Acceptance criterion | Requirements |
|---|---|
| `<criterion as written in the feature file>` | R1, R2 |
| `<criterion>` | R3 |

## Out of scope

Restated from the feature file, so the design does not drift.

- `<what this feature does not do>`

## Open questions

- `<none>` — or a question that prevents writing a complete requirement. Any
  entry here means the feature goes to `state: blocked` and the reason is
  recorded in `progress/spec_<feature-name>.md`. Never guess a requirement.
