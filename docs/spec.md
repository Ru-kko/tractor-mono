# Spec Driven Development

> This project follows a Kyro-Style flow: Requirements -> Design -> Tasks -> Code. The code should
> not be written until an human has aproved the spec.

## Structure

Every new feature (`sdd: true` in `features/<id>-<feature-name>.md`) has a dedicated folder when is
leaving `pending` state in `specs/<feature-name>/`:

```
specs/<feature-name>/
├── requirements.md   # What's needed (EARS notation)
├── design.md         # How it will be implemented (Contracts, Technical Desicions, Diagrams, etc)
└── tasks.md          # Steps to implement the feature
```

The `<feature-name>` matches with `name` attribute in `features/<id>-<feature-name>.md` file.

## Feature Lifecycle

| State | Description |
|-------|------------|
| pending | Without a spec, `spec_author` must be the first to act. |
| spec_ready | Spec draft is ready for review. Waiting for human approval. |
| in_progress | Spec is approved. `implemeter` is working on it. |
| done | `reviewer` has approved the implementation. |
| blocked | The feature is blocked by a reason in `progress/current.md`. |


## Human approval gate

The requirements are written using EARS (Easy Approach to Requirements Syntax). Each requirement is
a numbered paragraph following one of these five patterns: 

| Pattern | Template |
|---------|---------|
| Ubiquitous | `The <system> SHALL <do something>.` |
| Event | `WHEN <dispatcher>, the <system> SHALL <do something>.` |
| State | `WHILE <something is true>, the <system> SHALL <do something>.` |
| Option | `WHERE <condition>, the <system> SHALL <do something>.` |
| Unwanted | `IF <something is true>, the <system> SHALL NOT <do something>.` |

### Rules

- Every feature requeriment must have a stable identifier: `R1`, `R2`, ..., `R<n>`.
- Every requeriment MUST be verificable.
- Don't blend multiple `SHAL` statements in a single requeriment. Each requeriment should have a
  single `SHALL` statement.
- Don't use weak verbs like `MAY`, `SHOULD`, `CAN`, `MUST`, etc. Use only `SHALL` and `SHALL NOT`.
  
Example:

```
R1. The TractorStore SHALL provide a REST API to manage tractors.
R2. WHEN a tractor is added to the TractorStore, the TractorStore SHALL send a notification to
  the TractorStoreAdmin.
```

## `design.md` - Technical Design

Capture before implementation:

- What classes, interfaces, and methods will be created.
- What new contracts will be created or modified.
- What exceptions will be thrown and how they will be handled.
- What new dependencies will be added and why.
- What alternatives were considered and why they were rejected.

This is NOT first-principles engineering. Rely on `docs/architecture.md` and `docs/conventions.md`.
The `design.md` file documents the points where your feature pushes the boundaries of those rules.

## `tasks.md` - Implementation Checklist

Specify the steps to implement the feature. Each task has a checkbox and cover at least one
requirement `R<n>`.

Example:

```
- [ ] T1 - Implement the `TractorStore` class to manage tractors. Cover: R1, R2
- [ ] T2 - Implement the `TractorStoreAdmin` class to manage notifications. Cover: R2
```


The `implementer` should check the tasks as they are completed with `[x]`. The `reviewer` will
reject if exists any task is `[ ]` without a valid reason.

## Tracebility

- Every test case must be linked to a requirement `R<n>`.
  ```java
  @Test
  /**
   * Cover: R1, R2
   */
  public void testAddTractor() {
    ...
  }
  ```
- Every requirement `R<n>` must be linked to a test case.
- The `reviewer` will reject if exists any requirement `R<n>` without a test case or any test case
  without a requirement `R<n>`.