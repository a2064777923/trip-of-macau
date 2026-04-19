---
phase: 15-indoor-interaction-rule-authoring
plan: 03
subsystem: verification-and-seed
tags: [indoor, lisboeta, smoke, uat, seed, cos]
provides:
  - deterministic Lisboeta Phase 15 showcase fixtures
  - live smoke proof on local 8080/8081
  - resumable UAT checklist for indoor rule authoring
affects: [admin-backend, admin-ui, mysql, local-runtime]
key-files:
  created:
    - scripts/local/mysql/init/25-phase-15-indoor-rule-showcase-seed.sql
    - scripts/local/smoke-phase-15-indoor-authoring.ps1
    - .planning/phases/15-indoor-interaction-rule-authoring/15-UAT.md
  modified:
    - packages/admin/aoxiaoyou-admin-ui/package.json
requirements-completed: [RULE-01, RULE-02]
completed: 2026-04-15
---

# Phase 15.03 Summary

Wave 3 added real authored examples and live proof instead of leaving Phase 15 as empty scaffolding.

- Added `25-phase-15-indoor-rule-showcase-seed.sql` with deterministic Lisboeta 1F showcase scenarios covering:
  - a schedule-gated overlay
  - a dwell-triggered collectible/story reveal
  - a chained interaction with a `path_motion` effect
- Added `smoke-phase-15-indoor-authoring.ps1`, which:
  - verifies the local public server on `8080`
  - logs into the admin backend on `8081`
  - ensures the Lisboeta indoor baseline exists
  - validates and upserts structured indoor nodes through the canonical Phase 15 endpoints
  - reads the authored node back and asserts exact rule-graph fields
  - runs targeted backend tests and an admin UI production build
- Added a resumable `15-UAT.md` covering schedule overlays, trigger chains, motion paths, draft recovery, CSV preview fields, and the “raw JSON is secondary” requirement.
- Added an admin UI package script for the Phase 15 smoke entrypoint.

## Live Verification

- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-15-indoor-authoring.ps1` passed on 2026-04-15 against live local services on `http://127.0.0.1:8080` and `http://127.0.0.1:8081`.
- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-15-indoor-authoring.ps1 -Scenario seed -SkipTests -SkipBuild` passed and authored the three Lisboeta showcase nodes:
  - `1f-phase15-night-market-overlay`
  - `1f-phase15-royal-palace-dwell`
  - `1f-phase15-zipcity-path`
- A direct authenticated asset upload probe to `POST /api/admin/v1/content/assets/upload` also succeeded against COS during this verification pass.
