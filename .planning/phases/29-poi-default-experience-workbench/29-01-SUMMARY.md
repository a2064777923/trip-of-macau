---
phase: 29-poi-default-experience-workbench
plan: 01
subsystem: backend-and-admin-api
tags: [spring-boot, mybatis-plus, react-types, poi-experience, experience-flows]
requires:
  - phase: 28-01
    provides: Versioned experience orchestration tables and public runtime contract
provides:
  - Protected admin POI default experience facade
  - Typed admin UI API contract for POI experience snapshots and step editing
  - Structured POI step payload compiler into schemaVersion 1 JSON
affects: [phase-30-storyline-overrides, phase-31-template-governance, phase-32-exploration-progress, phase-34-public-runtime]
tech-stack:
  added: []
  patterns:
    - POI-specific facade over canonical experience_* tables
    - Structured admin card payloads compiled to versioned runtime JSON
    - Reusable template saving from authored POI flow steps
key-files:
  created:
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminPoiExperienceController.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminPoiExperienceService.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminPoiExperienceServiceImpl.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminPoiExperienceRequest.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminPoiExperienceResponse.java
  modified:
    - packages/admin/aoxiaoyou-admin-ui/src/services/api.ts
    - packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts
key-decisions:
  - "POI authoring uses a dedicated admin facade, but persists only through Phase 28 experience_flows, experience_flow_steps, experience_bindings, and experience_templates."
  - "The facade enforces ownerType=poi, bindingRole=default_experience_flow, flowType=default_poi, and mode=walk_in."
  - "Advanced JSON remains available only as an explicit fallback and must still include schemaVersion."
patterns-established:
  - "Domain-specific admin workbenches should expose operator concepts while delegating persistence to canonical shared orchestration tables."
  - "Reward rule IDs remain non-versioned where the Phase 28 contract treats them as an array, while trigger, condition, and effect JSON are versioned objects."
requirements-completed: [STORY-01, LINK-01]
duration: continuation
completed: 2026-04-29
---

# Phase 29 Plan 01: POI Facade Summary

**Protected POI default experience facade that atomically loads, saves, validates, and templates walk-in flows on top of the canonical experience model**

## Performance

- **Duration:** Continuation from prior executor handoff
- **Completed:** 2026-04-29
- **Tasks:** 2
- **Files modified:** 7

## Accomplishments

- Added `/api/admin/v1/pois/{poiId}/experience/**` endpoints for default snapshot loading, default-flow upsert, structured step create/update/delete, and save-step-as-template.
- Implemented `AdminPoiExperienceServiceImpl` so a POI default flow always uses `default_poi` / `walk_in` semantics and a single active `default_experience_flow` binding.
- Added structured request and response DTOs for POI summary, flow, binding, steps, validation findings, templates, and public runtime path.
- Added admin UI types and API helpers for the POI workbench without local-only DTO assumptions.

## Task Commits

No commit was created in this continuation because the shared worktree already contained extensive unrelated dirty changes. The implementation and verification are documented in this summary and the phase verification artifact.

## Files Created/Modified

- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminPoiExperienceController.java` - Protected POI experience admin endpoints.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminPoiExperienceServiceImpl.java` - Canonical flow, binding, step, validation, and template-save behavior.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminPoiExperienceRequest.java` - Structured flow and step authoring payloads.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminPoiExperienceResponse.java` - Snapshot, flow, binding, step, POI summary, and validation DTOs.
- `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts` - Typed POI experience API helpers.
- `packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts` - Frontend POI experience DTO types.

## Decisions Made

The POI-specific facade deliberately hides shared table details from the UI while still enforcing the Phase 28 canonical vocabulary. This keeps later story chapter overrides able to inherit the same POI flow without a second POI-only schema.

## Deviations from Plan

No scope deviations. Execution was resumed from a handoff, so commits were not created to avoid accidentally staging unrelated dirty worktree changes.

## Issues Encountered

The live admin backend on port 8081 initially ran stale classes, so the new controller was not mapped. Restarting only the admin backend loaded the compiled `AdminPoiExperienceController` and unblocked smoke verification.

## Verification

- `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` passed.
- `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` passed with only the existing Vite chunk-size warning.
- Source checks found the POI facade route, `default_experience_flow`, `default_poi`, `walk_in`, `schemaVersion`, `save-template`, canonical step types, typed frontend DTOs, and all six API helpers.

## User Setup Required

None.

## Next Phase Readiness

Phase 30 can inherit a POI default flow through `story_chapter_anchor` style bindings and then implement disable, replace, and append overrides against the same canonical flow steps.

---
*Phase: 29-poi-default-experience-workbench*
*Completed: 2026-04-29*
