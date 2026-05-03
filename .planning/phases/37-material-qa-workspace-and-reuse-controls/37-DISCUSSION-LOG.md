# Phase 37: Material QA Workspace and Reuse Controls - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-05-03
**Phase:** 37-Material QA Workspace and Reuse Controls
**Areas discussed:** QA workspace shape, preview/evidence, QA actions, consistency checks, reuse controls
**Mode:** `/gsd-next` continuation. The user asked to continue, so the agent selected conservative defaults from prior user preferences and Phase 36 evidence instead of stopping for interactive questions.

---

## QA Workspace Shape

| Option | Description | Selected |
| --- | --- | --- |
| Enhance existing material package page | Keeps package production, item status, version history, and quick actions in one place. | ✓ |
| Create separate QA page | Cleaner route, but risks duplicating the material package page and confusing operators. | |
| Replace media library with QA page | Too broad and would mix global media management with package QA. | |

**User's choice:** Inferred default: enhance existing page and avoid duplicate IA.
**Notes:** This follows the user's repeated request to remove overlapping/confusing admin pages and keep the control plane coherent.

---

## Preview And Evidence

| Option | Description | Selected |
| --- | --- | --- |
| Preview-rich detail drawer | Shows thumbnail/player/metadata/version/provenance in one inspection surface. | ✓ |
| Table-only columns | Compact but insufficient for judging images/audio/video. | |
| External files only | Forces operators out of admin and weakens QA evidence. | |

**User's choice:** Inferred default: preview-rich drawer with stable broken-preview states.
**Notes:** This directly addresses prior complaints about assets that cannot be previewed and long links overflowing table cells.

---

## QA Actions

| Option | Description | Selected |
| --- | --- | --- |
| Status-preserving reject/approve/replace/rollback | Keeps lineage and avoids breaking bindings. | ✓ |
| Destructive cleanup of bad assets | Faster visually, but loses evidence and can break references. | |
| Regenerate as full product UI | Out of scope for Phase 37 and conflicts with user's request not to create a global material production workbench. | |

**User's choice:** Inferred default: lineage-preserving actions.
**Notes:** Regeneration can be represented as a request/status/action hook, but Phase 37 should not expose new all-user image generation.

---

## Consistency Checks

| Option | Description | Selected |
| --- | --- | --- |
| Package-scoped manifest/COS/DB report | Directly validates the flagship package and supports smoke evidence. | ✓ |
| Global asset crawler | Useful later, but too broad for this phase. | |
| Manual-only checklist | Too weak for QA-03. | |

**User's choice:** Inferred default: package-scoped report first.
**Notes:** Findings should be blocking/warning/info and should cover missing, stale, oversized, wrong-kind, and broken-COS assets.

---

## Reuse Controls

| Option | Description | Selected |
| --- | --- | --- |
| Extend existing media picker/search metadata | Reuses current uploader/picker and prevents duplicate binaries. | ✓ |
| New package-only picker | Could help QA but creates another selection pattern. | |
| Duplicate assets into each editor | Breaks provenance and wastes storage. | |

**User's choice:** Inferred default: extend existing media picker.
**Notes:** Approved/published assets should be discoverable by package code/item key and hidden by default when rejected/draft.

---

## the agent's Discretion

- Exact route naming may be decided during planning, but avoid overlapping pages.
- Consistency checks may be computed live or saved as latest QA report.
- Browser UAT implementation details are left to planning, but Phase 37 should use the flagship package.

## Deferred Ideas

- General all-user material production studio.
- Full approval workflow with reviewer assignment.
- Phase 38 public DTO asset filtering.
- Phase 39 mini-program journey UAT.
