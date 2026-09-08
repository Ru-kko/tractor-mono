---
id: n
name: cart-add-tractor
sdd: true
state: pending
module: cart
---

# Add a tractor to the shopping cart

> **Template.** Copy to `features/<id>-<name>.md` and replace the front matter
> and every `<...>` placeholder. Written by a human — agents only ever change
> the `state` field. Lifecycle: `docs/spec.md`.

## Context

Why this feature exists and which existing behaviour it touches. A few lines;
the reasoning belongs in `specs/<name>/design.md`.

## Acceptance

Plain criteria, one per line. `spec_author` turns each into at least one EARS
requirement `R<n>` in `specs/<name>/requirements.md`. Anything not written
here will not be specced, and anything ambiguous here gets the feature sent
back as `blocked`.

- A customer can add a tractor with a quantity to their cart.
- Adding a tractor that is not in the catalog is rejected with a 404.
- Adding the same tractor twice accumulates the quantity in a single line.

## Out of scope

What this feature explicitly does not do, so the spec does not grow.

- Stock reservation (belongs to `inventory`).
- Checkout.

---

> Front matter reference:
> - `id` — integer, position in the queue. The leader always picks the lowest
>   non-`done`, non-`blocked` id.
> - `name` — kebab-case, matches the folder `specs/<name>/`.
> - `sdd` — `true` requires the full spec flow. `false` only for trivial
>   changes that do not touch `src/`.
> - `state` — `pending` | `spec_ready` | `in_progress` | `done` | `blocked`.
>   Only agents flip it: `spec_author` → `spec_ready`, leader →
>   `in_progress`, implementer → `done` after an `APPROVED` review.
> - `module` — owning module (`catalog`, `inventory`, `cart`, `order`,
>   `common`, `monolith`). A feature owns exactly one.
