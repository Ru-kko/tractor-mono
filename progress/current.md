# Current session

> **Template.** Copy to `progress/current.md`. This is the session's live
> scratchpad: the `leader` and the `implementer` update it **as they work**,
> not at the end. When a feature closes, its summary moves to
> `progress/history.md` (see `progress/__template__/history.md`) and this file
> goes back to the template.

**Feature in progress:** `<id> — <feature-name>` | —
**State:** `pending | spec_ready | in_progress | done | blocked` | —
**Waiting on:** `human (spec approval) | implementer | reviewer | nothing`

## Plan

Tasks `T1..Tn` of `specs/<feature-name>/tasks.md`, once the spec is approved.

- [ ] T1 — `<...>`

## Log

One line per meaningful step: what was done, which task it closes, what the
build said.

- `<HH:MM>` `<baseline .\gradlew.bat build green>`
- `<HH:MM>` `<T1 done — <Class>.java + <Class>Test.java, :module:test green>`

## Blockers

Required whenever the feature is set to `blocked`: reason, failing task, and
the exact error output. No improvised workarounds.

- `<none>`
