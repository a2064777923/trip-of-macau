# Phase 32: Dynamic Exploration and User Progress Model - Discussion Log

> Audit trail only. Do not use as input to planning, research, or execution agents.
> Decisions are captured in `32-CONTEXT.md`; this log preserves the alternatives considered.

**Date:** 2026-04-29
**Phase:** 32-dynamic-exploration-and-user-progress-model
**Mode:** `/gsd-next` routed to `/gsd-discuss-phase 32`; Default mode fallback applied because interactive question tooling is unavailable.
**Areas discussed:** Dynamic progress semantics, scope model, admin workbench, story sessions, recompute and repair, backend/public contract, data migration, UI quality.

---

## Dynamic Progress Semantics

| Option | Description | Selected |
|--------|-------------|----------|
| Element/event source of truth | `exploration_elements` defines current denominator, `user_exploration_events` stores immutable completions, derived state is rebuildable cache. | Yes |
| Keep legacy fixed percentages | Continue relying on `traveler_progress.progress_percent` and fixed count snapshots. |  |
| Hybrid but hidden | Use new calculation internally but do not expose denominator/explanation to operators. |  |

**Selected choice:** Element/event source of truth.
**Notes:** This follows Phase 28 decisions and the user's explicit preference that exploration progress must change dynamically as content changes.

---

## Scope Model

| Option | Description | Selected |
|--------|-------------|----------|
| Broad scoped progress | Support global, city, sub-map, POI, indoor, storyline, chapter, task, collectible, reward, and media scopes. | Yes |
| Only Phase 28 scopes | Keep only city, sub-map, storyline, and chapter for now. |  |
| Separate per-domain progress tables | Build dedicated progress tables for each content domain. |  |

**Selected choice:** Broad scoped progress.
**Notes:** Phase 32 is the user/progress domain completion phase, so planning should not leave POI, indoor, task, collectible, reward, and media as invisible gaps.

---

## Admin Workbench

| Option | Description | Selected |
|--------|-------------|----------|
| Full progress workbench | Upgrade user detail into a drill-down workbench with progress, elements, sessions, timeline, acquisitions, repair, and audit. | Yes |
| Keep drawer and add a few fields | Minimal enhancement to the current detail drawer. |  |
| Table-only logs | Add separate raw tables for events and sessions without a unified user view. |  |

**Selected choice:** Full progress workbench.
**Notes:** The current drawer is too narrow and only shows snapshots. The user has repeatedly asked for intuitive management pages, not raw CRUD.

---

## Story Sessions

| Option | Description | Selected |
|--------|-------------|----------|
| Persist session state | Add durable server-side session visibility and keep permanent events separate from temporary session state. | Yes |
| Keep ephemeral session IDs | Continue returning generated session IDs without admin-inspectable state. |  |
| Merge sessions into permanent progress | Treat story mode as only permanent progress, with no temporary state separation. |  |

**Selected choice:** Persist session state.
**Notes:** Phase 30 locked the distinction between story session progress and permanent exploration state. Phase 32 must make that inspectable.

---

## Recompute And Repair

| Option | Description | Selected |
|--------|-------------|----------|
| Previewed audited repair | Recompute derived state from immutable source data, preview impact, require explicit confirmation, and write audit logs. | Yes |
| Silent auto repair | Recompute or patch derived progress automatically whenever an operator opens a page. |  |
| Direct event mutation | Let admins edit/delete completion events directly. |  |

**Selected choice:** Previewed audited repair.
**Notes:** This is the safest match for `USER-04`: manual recompute or repair must be explicit, safe, and audited.

---

## Backend And Public Contract

| Option | Description | Selected |
|--------|-------------|----------|
| Admin repairs, public summaries | Admin backend owns inspection/repair/audit; public backend owns traveler-facing exploration summaries and event reporting. | Yes |
| Public exposes repair internals | Let public runtime expose the same repair/admin model. |  |
| Admin reads public only | Make admin call public endpoints instead of owning operator APIs. |  |

**Selected choice:** Admin repairs, public summaries.
**Notes:** This preserves the project boundary: admin is the control plane, public is runtime consumption.

---

## Data Migration

| Option | Description | Selected |
|--------|-------------|----------|
| Bridge legacy data | Keep legacy check-ins, traveler progress, rewards, and trigger logs visible and bridge them into the new view. | Yes |
| Ignore legacy progress | Show only new exploration events going forward. |  |
| Rewrite all history | Attempt a destructive migration into only the new model. |  |

**Selected choice:** Bridge legacy data.
**Notes:** The project is brownfield and must not hide existing traveler history.

---

## UI Quality

| Option | Description | Selected |
|--------|-------------|----------|
| Traditional Chinese workbench | Use task-oriented cards, tabs, filters, timeline, drill-down panels, and guarded repair actions. | Yes |
| Dense raw tables | Prioritize data exposure over usability. |  |
| Minimal visual changes | Avoid UI redesign and only add backend fields. |  |

**Selected choice:** Traditional Chinese workbench.
**Notes:** This follows the user's recurring requirement that admin surfaces be useful, attractive, and easy to operate.

---

## Deferred Ideas

- Full mini-program story/progress UI acceptance remains future runtime scope.
- Full publish scheduling and approval lifecycle remains Phase 34 or later.
- Large asynchronous recompute job infrastructure can be deferred if scoped recompute is reliable and extensible.
