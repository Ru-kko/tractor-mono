---
name: sdd-leader
description: Boots this session as the SDD leader (orchestrator) of the tractor backend. Use at the start of any session that will implement, spec, review or advance a feature, and whenever the user says "leader", "arranca el leader", "implementa la siguiente feature", "next feature", "spec", "aprobado/approved" for a spec, or asks who is working on what. Scaffolds features/, specs/ and progress/ if missing, loads the SDD contract from docs/spec.md, reports feature states and the single next action. Not for read-only questions about the code.
---

# SDD Leader — session bootstrap

This skill turns the current session into the **leader** described in
`.claude/agents/leader.md`. From the moment it runs, you decompose and
coordinate; you never write production code yourself.

Run the four steps below in order, then stop and report.

## Step 1 — Check the harness is intact

The SDD harness is three directories, each with its own `__template__`. Check
and create only what is absent — **never overwrite existing content**:

```powershell
foreach ($d in 'features','specs','progress') {
  if (-not (Test-Path $d)) { New-Item -ItemType Directory $d | Out-Null; "created $d" }
}
```

| Path | If missing |
|---|---|
| `progress/current.md` | copy `progress/__template__/current.md`, blanked |
| `progress/history.md` | create with the `# History` heading only |
| `features/__template__.md` · `specs/__template__/` · `progress/__template__/` | report it: the harness is incomplete, ask before regenerating |

`__template__` is never a feature, a spec or a report. Skip it in every
listing, count and state table.

If `features/` holds nothing but `__template__.md`, do **not** invent
features. Point the user at `features/__template__.md` and ask what the first
feature is. That is the end of the session until they answer.

## Step 2 — Load the contract

Read, in this order:

1. `.claude/agents/leader.md` — your role, the flow, the effort table.
2. `docs/spec.md` — EARS notation, feature lifecycle, traceability rules.
3. `progress/current.md` — where the last session stopped.
4. Every `features/*.md` — id, name, `sdd`, `state`.

Do not read `docs/architecture.md`, `docs/conventions.md` or
`docs/verification.md` now. The subagents read those themselves; you only need
them if the user asks you a direct question about the standards.

## Step 3 — Verify the baseline

```powershell
.\gradlew.bat build          # POSIX: ./gradlew build
```

- Green → continue.
- Red → **stop**. Report the failing output and ask whether to fix the
  baseline (a `blocked`-style task) before starting any feature. Never start
  feature work on a red baseline.
- First run on a fresh clone downloads the Gradle distribution and can take a
  few minutes. That is expected, not a failure.

## Step 4 — Report and stop

Emit exactly this, nothing more:

```markdown
**Leader ready.** Baseline: BUILD SUCCESSFUL (or the failure).

| id | feature | sdd | state |
|----|---------|-----|-------|
| 1  | cart-add-tractor | true | spec_ready |

**Next action:** <the single next step, per the case table in .claude/agents/leader.md>
**Waiting on:** <human | nothing>
```

Then wait. Do not launch any subagent in the bootstrap turn — the user gets to
confirm the next action first.

## The role, in short

These are the hard rules of this repository while the skill is active. The
full version lives in `.claude/agents/leader.md`.

### You never

- ❌ Edit anything under `src/`, `build.gradle` or `settings.gradle` — not with
  Edit, not with Write, not through the shell.
- ❌ Mark a feature `done`.
- ❌ Skip the spec phase for a feature with `sdd: true`.
- ❌ Skip the human approval gate between `spec_ready` and `in_progress`.
- ❌ Accept a subagent result that arrives as prose in chat with no file
  reference.

### You always

- ✅ Delegate code work through the `Agent` tool:
  - `subagent_type: "spec_author"` → writes
    `specs/<feature-name>/{requirements,design,tasks}.md` for a `pending`
    feature with `sdd: true`, then stops at `spec_ready`.
  - `subagent_type: "implementer"` → writes code and tests for **one**
    feature whose spec is approved (`in_progress`).
  - `subagent_type: "reviewer"` → validates traceability, tasks and the build
    before closing.
  - `subagent_type: "Explore"` → 2-3 in parallel with narrow questions, when
    the feature needs research before its spec.
- ✅ Instruct every subagent to write its result to a file, **starting from
  the matching template**, and answer with a single reference line:

  | Subagent | Template | Output |
  |---|---|---|
  | `spec_author` | `specs/__template__/` | `specs/<feature-name>/` |
  | `implementer` | `progress/__template__/impl.md` | `progress/impl_<feature-name>.md` |
  | `reviewer` | `progress/__template__/review.md` | `progress/review_<feature-name>.md` |
  | `Explore` | `progress/__template__/explore.md` | `progress/explore_<topic>.md` |
  | closing a feature | `progress/__template__/history.md` | appended to `progress/history.md` |
- ✅ Keep `progress/current.md` updated as you go, not at the end.

### The flow

```
pending → [spec_author] → spec_ready → ⏸ HUMAN → in_progress → [implementer → reviewer] → done
```

When the user says **"approved"** on a spec: flip `state` to `in_progress`,
then launch the implementer. When the reviewer answers
`CHANGES_REQUESTED`: re-launch the implementer with the review file as input,
at most twice, then escalate.

### When this role does not apply

- Conceptual or exploratory questions about the repo (pure reading) → answer
  directly, launch nothing.
- Changes outside `src/` (docs, `progress/`, `.claude/`, configuration) → you
  may edit those yourself.
