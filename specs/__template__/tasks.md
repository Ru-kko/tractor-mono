# Tasks — `<feature-name>`

> **Template.** Copy `specs/__template__/` to `specs/<feature-name>/`.
> Written by `spec_author`, executed and checked off by `implementer`, audited
> by `reviewer`. Rules: `docs/spec.md`.

Ordered, discrete steps. Every task covers at least one `R<n>`, and every
`R<n>` appears in at least one task. Tests are their own tasks, never an
afterthought. A task that cannot be done without deviating from
`design.md` means the spec changes first.

- [ ] T1 - `<action: create X in <module>/application>`. Cover: R1
- [ ] T2 - `<action: write <Class>Test, happy path + specific exception>`. Cover: R1, R2
- [ ] T3 - `<action: expose endpoint with springdoc annotations>`. Cover: R2
- [ ] T4 - `<action: write <Controller>Test with @WebMvcTest>`. Cover: R2, R3
- [ ] T5 - `<action: migration V<n>__<name>.sql in the owning module>`. Cover: R3

## Requirement → tasks map

Filled in by `spec_author`; a requirement with no task is a spec bug.

| Requirement | Tasks |
|---|---|
| R1 | T1, T2 |
| R2 | T2, T3, T4 |
| R3 | T4, T5 |

## Definition of done

All of these hold before the `implementer` hands over to the `reviewer`:

- [ ] Every task above is `[x]`.
- [ ] Every test declares its `Cover: R<n>` Javadoc block.
- [ ] `.\gradlew.bat clean build` ends with `BUILD SUCCESSFUL`.
- [ ] Traceability map written to `progress/impl_<feature-name>.md`.
- [ ] Smoke-test transcript in the same file, if the feature exposes HTTP.
