---
phase: 35-operations-lifecycle-scheduling-and-dependency-workbench
plan: 35-04
subsystem: verification
tags: [smoke, verification, docs, traceability]

requires:
  - 35-01 lifecycle foundation
  - 35-02 lifecycle API
  - 35-03 lifecycle workbench
provides:
  - Repeatable Phase 35 lifecycle smoke
  - Phase verification and handoff docs
  - v3.0 requirements, roadmap, audit, and state traceability closure
affects: [OPS-02, OPS-04, v3.0]

tech-stack:
  added: []
  patterns:
    - Env-backed smoke auth
    - UTF-8-safe MySQL seed import with `--default-character-set=utf8mb4`
    - Smoke restores seeded public content after lifecycle mutation

key-files:
  created:
    - scripts/local/smoke-phase-35-lifecycle.ps1
    - .planning/phases/35-operations-lifecycle-scheduling-and-dependency-workbench/35-HANDOFF.md
    - .planning/phases/35-operations-lifecycle-scheduling-and-dependency-workbench/35-VERIFICATION.md
  modified:
    - .planning/phases/35-operations-lifecycle-scheduling-and-dependency-workbench/35-VALIDATION.md
    - .planning/REQUIREMENTS.md
    - .planning/ROADMAP.md
    - .planning/v3.0-MILESTONE-AUDIT.md
    - .planning/STATE.md

key-decisions:
  - "Smoke uses `PHASE35_ADMIN_BEARER_TOKEN` or `PHASE35_ADMIN_USERNAME`/`PHASE35_ADMIN_PASSWORD`; no credentials are stored in tracked files."
  - "Lifecycle smoke mutates the seeded flagship storyline to unpublished, verifies public filtering when `8080` is reachable, then restores it to published."
  - "Full approval workflow and full WeChat DevTools experiential acceptance remain future scope."

requirements-completed:
  - OPS-02
  - OPS-04

duration: 40min
completed: 2026-04-30
---

# Phase 35 Plan 35-04: Verification and Traceability Summary

Added repeatable live smoke verification and closed Phase 35 traceability only after compile, build, and smoke passed.

## Accomplishments

- Added `scripts/local/smoke-phase-35-lifecycle.ps1`.
- The smoke imports Phase 28-35 seed SQL with `--default-character-set=utf8mb4`, resolves MySQL locally, authenticates through env-backed admin credentials, previews a lifecycle action, creates a scheduled operation, runs due operations, verifies history/detail, checks public filtering when available, restores the seeded storyline, and asserts operation persistence.
- Added handoff and verification documents with exact prerequisites, commands, endpoints, env vars, and deferred scope.
- Updated validation rows only after compile/build/smoke passed.
- Updated v3.0 requirements, roadmap, milestone audit, and state to mark `OPS-02`, `OPS-04`, and `OPS-LIFECYCLE-01` closed by Phase 35 evidence.

## Verification

- `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` exited `0`.
- `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` exited `0`.
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-35-lifecycle.ps1` exited `0`.
- Smoke success line: `Phase 35 lifecycle smoke passed`.
- Additional live check: `GET /api/admin/v1/operations/lifecycle/targets?pageNum=1&pageSize=20` returned `code=0`, `total=351`, and 20 items after descriptor-level schema degradation was added.

## Deviations from Plan

### Auto-fixed Issues

**1. MySQL scalar parsing included a column header**

- **Found during:** Task 35-04-01 smoke execution.
- **Issue:** The final persistence assertion received `COUNT(*)` plus the numeric result.
- **Fix:** Added `--batch`, `--raw`, and `--skip-column-names`, then parsed the last non-empty output line.
- **Verification:** Phase 35 smoke exited `0`.

**2. Re-importing Phase 29 seed collided on default POI flow steps**

- **Found during:** Task 35-04-01 smoke re-run.
- **Issue:** `40-phase-29-poi-default-experience.sql` can collide on `experience_flow_steps.uk_experience_flow_steps_code` when re-imported.
- **Fix:** Added the same targeted local cleanup pattern used by Phase 34 for known `poi_ama_default_walk_in` seed steps before importing that SQL.
- **Verification:** Phase 35 smoke exited `0` on a repeated run.

## Issues Encountered

- Admin backend startup still logs a local Mongo authentication warning on this workstation, but the HTTP stack and MySQL-backed admin lifecycle endpoints are healthy. The Phase 35 smoke does not depend on Mongo.
- Full approval workflow is not implemented in Phase 35.
- Full WeChat DevTools experiential acceptance remains deferred.

## Next

Phase 35 is ready for milestone completion or security review follow-up.
