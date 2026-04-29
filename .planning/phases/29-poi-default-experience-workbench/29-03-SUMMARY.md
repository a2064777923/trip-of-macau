---
phase: 29-poi-default-experience-workbench
plan: 03
subsystem: seed-and-smoke
tags: [mysql, utf8mb4, smoke, poi-runtime, public-api]
requires:
  - phase: 29-01
    provides: Admin POI experience facade
  - phase: 29-02
    provides: Dedicated POI workbench UI route and contract
provides:
  - UTF-8 safe A-Ma Temple default POI experience seed
  - Live admin/public POI experience smoke script
  - Phase 29 handoff and verification evidence
affects: [phase-30-storyline-overrides, phase-31-template-governance, phase-32-exploration-progress, phase-33-flagship-content, phase-34-public-runtime]
tech-stack:
  added: []
  patterns:
    - Idempotent SQL seed using code-based upserts and utf8mb4 setup
    - Live smoke across admin facade, template saving, and public runtime
key-files:
  created:
    - scripts/local/mysql/init/40-phase-29-poi-default-experience.sql
    - scripts/local/smoke-phase-29-poi-experience.ps1
    - .planning/phases/29-poi-default-experience-workbench/29-HANDOFF.md
    - .planning/phases/29-poi-default-experience-workbench/29-VERIFICATION.md
  modified: []
key-decisions:
  - "A-Ma Temple is the acceptance fixture for natural POI default experience authoring."
  - "Seeded exploration effects use semantic weight levels instead of fixed percentage grants."
  - "Smoke must prove both admin-authored data and public runtime alignment."
patterns-established:
  - "Phase seeds with Chinese content must start with SET NAMES utf8mb4 and be imported as UTF-8 files."
  - "Smoke scripts should use explicit UTF-8 HttpClient handling and local ignored auth material."
requirements-completed: [STORY-01, LINK-01]
duration: continuation
completed: 2026-04-29
---

# Phase 29 Plan 03: Seed And Smoke Summary

**A-Ma Temple default POI experience seed plus live admin/public smoke proving template reuse and published runtime alignment**

## Performance

- **Duration:** Continuation from prior executor handoff
- **Completed:** 2026-04-29
- **Tasks:** 3
- **Files modified:** 4

## Accomplishments

- Added `40-phase-29-poi-default-experience.sql` with utf8mb4-safe reusable templates, a published `poi_ama_default_walk_in` flow, canonical POI default binding, seven published flow steps, and exploration elements.
- Added `smoke-phase-29-poi-experience.ps1` that logs in through environment or ignored local auth material, reads the admin POI snapshot, saves a step as a template, verifies template search, reads the public POI runtime, and asserts no admin-only status fields leak.
- Imported the Phase 29 seed into local MySQL and verified active A-Ma Temple steps match the seven canonical codes exactly.
- Restarted the admin backend on port 8081 so the new controller was loaded, then ran live smoke successfully.

## Task Commits

No commit was created in this continuation because the shared worktree already contained extensive unrelated dirty changes. The implementation and verification are documented in this summary and the phase verification artifact.

## Files Created/Modified

- `scripts/local/mysql/init/40-phase-29-poi-default-experience.sql` - A-Ma Temple POI default flow, templates, steps, binding, and exploration elements.
- `scripts/local/smoke-phase-29-poi-experience.ps1` - Live admin/public smoke test with explicit UTF-8 HTTP handling.
- `.planning/phases/29-poi-default-experience-workbench/29-HANDOFF.md` - Phase 30-34 handoff.
- `.planning/phases/29-poi-default-experience-workbench/29-VERIFICATION.md` - Phase verification evidence.

## Decisions Made

The seed keeps Phase 29 focused on default POI behavior and intentionally avoids implementing story-mode overrides, cross-domain governance, complete progress UI, flagship material production, or mini-program UX acceptance.

## Deviations from Plan

The SQL seed soft-deletes three older Phase 28 A-Ma default step codes for this flow before upserting the Phase 29 canonical seven-step fixture. This prevents smoke false failures from stale fixture rows while staying scoped to known old codes.

## Issues Encountered

The first smoke attempt failed because the running admin backend still served old classes and mapped `/api/admin/v1/pois/9/experience/default` to static resource fallback. Restarting the admin backend on 8081 fixed the issue.

## Verification

- `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` passed.
- `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` passed with only the existing Vite chunk-size warning.
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-29-poi-experience.ps1` passed.
- Source checks found `SET NAMES utf8mb4`, all seven template codes, all seven step codes, `schemaVersion`, semantic weights, admin/public smoke coverage, save-template check, and public no-status assertions.

## User Setup Required

None for the local stack if `tmp-admin-login.json` or equivalent Phase 29 admin auth environment variables are available.

## Next Phase Readiness

Phase 30 can treat the A-Ma Temple default POI flow as a concrete inheritance target and implement story chapter disable, replace, and append overrides on top of it.

---
*Phase: 29-poi-default-experience-workbench*
*Completed: 2026-04-29*
