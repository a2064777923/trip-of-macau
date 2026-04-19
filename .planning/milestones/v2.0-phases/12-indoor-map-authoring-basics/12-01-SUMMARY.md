---
phase: 12-indoor-map-authoring-basics
plan: 01
subsystem: api
tags: [spring-boot, mybatis-plus, react, indoor, multilingual]
requires:
  - phase: 09-spatial-model-rebuild
    provides: canonical city, sub-map, and POI bindings reused by indoor buildings
  - phase: 10-media-asset-pipeline-and-library
    provides: shared asset IDs and relation-link patterns reused by indoor covers and attachments
provides:
  - additive MySQL schema for canonical indoor building and floor authoring
  - admin building detail and floor CRUD contracts
  - Traditional Chinese indoor building and floor authoring UI
affects: [indoor-runtime, admin-ui, map-space, public-indoor]
tech-stack:
  added: [none]
  patterns: [additive legacy-table extension, canonical binding validation, shared media asset references]
key-files:
  created: [scripts/local/mysql/init/16-phase-12-indoor-foundation.sql, packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/IndoorFloor.java, packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminIndoorFloorUpsertRequest.java, packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminIndoorBuildingDetailResponse.java]
  modified: [packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminIndoorController.java, packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminIndoorServiceImpl.java, packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/Building.java, packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/IndoorBuildingManagement.tsx]
key-decisions:
  - "Extended the existing `buildings` and `indoor_floors` tables instead of replacing them so seeded local data stayed readable during rollout."
  - "Canonical indoor bindings are map-aware: POI-bound buildings may omit standalone coordinates, while map-bound buildings must keep explicit coordinates."
  - "Indoor buildings and floors reuse shared media asset IDs rather than inventing a parallel upload model."
patterns-established:
  - "Indoor building detail is the authoritative editing shape and includes floor summaries for in-module management."
  - "Floor authoring is multilingual and additive: zh-Hans, zh-Hant, en, and pt fields travel together through DTOs."
requirements-completed: [INDO-01, INDO-02]
duration: 95 min
completed: 2026-04-14
---

# Phase 12: Indoor Map Authoring Basics Summary

**Canonical indoor building and floor authoring now persists multilingual spatial bindings and shared media references through the admin backend and UI.**

## Performance

- **Duration:** 95 min
- **Started:** 2026-04-14T18:40:00+08:00
- **Completed:** 2026-04-14T20:15:00+08:00
- **Tasks:** 3
- **Files modified:** 16

## Accomplishments

- Added the additive indoor foundation migration and entity/DTO surface needed for canonical building and floor authoring.
- Expanded the admin indoor backend from flat building editing into building-detail plus floor CRUD with binding validation.
- Reworked the admin indoor UI so operators can manage floors and shared media inside the indoor module instead of using placeholder forms.

## Task Commits

No dedicated git commit was created during this workspace snapshot. The completed work is present in the working tree.

## Files Created/Modified

- `scripts/local/mysql/init/16-phase-12-indoor-foundation.sql` - extends legacy indoor persistence with canonical bindings, multilingual metadata, area, zoom, and media reference fields.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminIndoorController.java` - exposes indoor building detail and floor CRUD endpoints.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminIndoorServiceImpl.java` - enforces binding-mode validation and floor uniqueness logic.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/Building.java` - carries the expanded indoor building schema.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/IndoorFloor.java` - maps canonical floor metadata and zoom/media fields.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/IndoorBuildingManagement.tsx` - provides the indoor building/floor authoring interface in Traditional Chinese.

## Decisions Made

- Reused the legacy indoor tables with additive columns so existing rows and local seed data stayed compatible.
- Treated city/sub-map/POI IDs as canonical indoor bindings and rejected incoherent combinations server-side.
- Routed floor covers and attachments through the shared media-library primitives established in the earlier media phase.

## Deviations from Plan

None. The plan was executed as intended for the schema, backend, and admin authoring foundation.

## Issues Encountered

None in this wave beyond standard schema-to-DTO wiring.

## User Setup Required

None. This wave did not add new external setup steps beyond the already configured local database and COS runtime.

## Next Phase Readiness

- Indoor buildings and floors are now in a stable shape for tile import and zoom derivation work.
- The admin module owns building/floor authoring, so the next wave can focus on floor map assets rather than reworking schemas again.

---
*Phase: 12-indoor-map-authoring-basics*
*Completed: 2026-04-14*
