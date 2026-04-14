---
phase: 02-admin-control-plane-completion
created: 2026-04-12
status: active
---

# Phase 2 Context

## Goal

Make the admin platform the authoritative write/control surface for every mini-program-facing entity and runtime setting needed in the live app.

## Brownfield Reality

- Phase 1 established the canonical MySQL schema and shared enums, but the admin backend still mixes older table/column assumptions with partial mini-program coverage.
- Existing admin pages for cities, POIs, storylines, chapters, and rewards are not fully aligned with the canonical Phase 1 tables.
- Missing admin CRUD remains for `app_runtime_settings`, `content_assets`, `tip_articles`, `notifications`, and `stamps`.
- The admin UI still uses placeholder or legacy surfaces for several mini-program-facing domains and settings.

## Execution Decision

Phase 2 will not treat new domains and old domains separately. The execution scope is:

1. Align existing admin content domains to the canonical schema so current pages stop drifting from the live backend model.
2. Add missing CRUD for the remaining mini-program-facing admin-owned domains.
3. Replace placeholder admin UI surfaces with live forms/tables wired to the real admin APIs.
4. Verify the admin backend and UI against the local runtime rather than compile-only assumptions.

## Acceptance Focus

- Admin writes land in the canonical Phase 1 tables.
- Admin users can manage multilingual copy, publish state, sort order, and asset references.
- The admin UI exposes real forms for the missing mini-program-facing content/settings surfaces.

