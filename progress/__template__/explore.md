# Exploration — `<topic>`

> **Template.** Copy to `progress/explore_<topic>.md`. Written by an
> `Explore` (or `general-purpose`) subagent that the `leader` launches before
> a spec, when the feature needs research first. Read-only: an explorer never
> edits code. The chat answer is only
> `explored -> progress/explore_<topic>.md`.

**Question asked by the leader:** `<the one narrow question this exploration
answers>`
**Feature it feeds:** `features/<id>-<feature-name>.md`

## Answer

Two or three sentences, up front. If the question cannot be answered from the
repository, say exactly that instead of speculating.

`<answer>`

## Evidence

Every claim cites `file:line`. No claim without a citation.

| Finding | Where |
|---|---|
| `<what is true>` | `<path/to/File.java:42>` |
| `<what is true>` | `<build.gradle:18>` |

## Current state of the relevant code

Structure, not opinions: which modules, classes and ports already exist, and
which of them the feature will have to touch.

- `<module>` — `<exists | absent>`; layers present: `<application, domain>`
- `<Class>` — `<responsibility, current signature>`

## Constraints found

Things the spec author must respect and would otherwise miss.

- `<e.g. there is no persistence layer yet; no Flyway/JPA dependency in build.gradle>`

## Dead ends

What was searched and returned nothing, so nobody repeats it.

- `<searched for X across src/ — no match>`

## Out of scope

Questions this exploration deliberately did not answer.

- `<...>`
