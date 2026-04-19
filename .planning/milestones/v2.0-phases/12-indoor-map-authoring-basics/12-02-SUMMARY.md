---
phase: 12-indoor-map-authoring-basics
plan: 02
subsystem: infra
tags: [cos, indoor, image-processing, zip-import, zoom-derivation]
requires:
  - phase: 12-indoor-map-authoring-basics
    provides: canonical building and floor records to attach tile manifests and zoom metadata
provides:
  - floor tile import persistence and COS publication model
  - ZIP preview/import and full-image slicing endpoints
  - admin floor map import workspace with manifest and zoom visibility
affects: [public-indoor, admin-ui, indoor-runtime, cos-assets]
tech-stack:
  added: [none]
  patterns: [backend-owned COS path generation, persisted zoom derivation snapshot, preview-before-import workflow]
key-files:
  created: [scripts/local/mysql/init/17-phase-12-indoor-tile-pipeline.sql, packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/IndoorTilePipelineService.java, packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminIndoorTilePreviewResponse.java]
  modified: [packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminIndoorController.java, packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminSystemManagementServiceImpl.java, packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/MapTileManagement.tsx, packages/admin/aoxiaoyou-admin-ui/src/services/api.ts]
key-decisions:
  - "Tile storage keys are backend-owned and live under `indoor/floors/{floorId}/{timestamp}/...` so operators never manage COS layout manually."
  - "Zoom derivation results are persisted alongside a derivation snapshot for auditability and operator override."
  - "The indoor tile page became the owned floor-map workspace instead of staying a placeholder table."
patterns-established:
  - "ZIP packages are previewed before import and validated before any COS write."
  - "Full floor images can be sliced server-side into a canonical tile manifest and preview asset set."
requirements-completed: [INDO-02, INDO-03]
duration: 110 min
completed: 2026-04-14
---

# Phase 12: Indoor Map Authoring Basics Summary

**Indoor floor maps now import from ZIP packages or full images, publish into COS under stable backend-owned paths, and persist derived zoom settings for the runtime.**

## Performance

- **Duration:** 110 min
- **Started:** 2026-04-14T20:15:00+08:00
- **Completed:** 2026-04-14T22:05:00+08:00
- **Tasks:** 3
- **Files modified:** 14

## Accomplishments

- Extended floor persistence with tile import status, manifest, source metadata, dimensions, and derived zoom fields.
- Implemented ZIP preview/import and full-image slicing flows that upload tiles and previews into COS automatically.
- Replaced the old placeholder tile page with a real indoor floor-map import workspace that shows manifest and zoom results.

## Task Commits

No dedicated git commit was created during this workspace snapshot. The completed work is present in the working tree.

## Files Created/Modified

- `scripts/local/mysql/init/17-phase-12-indoor-tile-pipeline.sql` - adds tile manifest, import source, and zoom derivation persistence.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/IndoorTilePipelineService.java` - validates ZIP/image input and publishes tile assets into COS.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminIndoorController.java` - exposes tile preview/import endpoints.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminSystemManagementServiceImpl.java` - stores indoor zoom-default settings used by derivation.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/MapTileManagement.tsx` - became the actual floor-map import and inspection page.

## Decisions Made

- Kept COS path generation entirely backend-owned to prevent operator-managed file layout drift.
- Stored zoom derivation snapshots with the floor so defaults remain explainable and editable later.
- Reused the existing shared upload and media primitives instead of creating a parallel indoor upload subsystem.

## Deviations from Plan

### Auto-fixed Issues

**1. Local MySQL compatibility for phase 12 migrations**

- **Found during:** Task 1 and Task 2
- **Issue:** Local MySQL did not accept `ADD COLUMN IF NOT EXISTS` in the original migration style.
- **Fix:** Reworked the phase 12 tile/runtime migrations to use `information_schema` checks plus a helper procedure so they apply cleanly on the local database.
- **Files modified:** `scripts/local/mysql/init/17-phase-12-indoor-tile-pipeline.sql`, `scripts/local/mysql/init/18-phase-12-indoor-markers-and-runtime.sql`
- **Verification:** Both migrations were applied successfully to the local `aoxiaoyou` MySQL instance.

---

**Total deviations:** 1 auto-fixed compatibility issue
**Impact on plan:** The compatibility fix was necessary for local execution and did not expand scope.

## Issues Encountered

- The phase depended on live local MySQL behavior rather than just SQL syntax review, so migration compatibility had to be corrected against the actual database runtime.

## User Setup Required

None. COS and local MySQL were exercised through the existing runtime configuration.

## Next Phase Readiness

- Floor maps now have a stable manifest and zoom contract for marker authoring and public runtime reads.
- The admin can preview import outcomes directly, so the next wave can focus on markers and live runtime cutover.

---
*Phase: 12-indoor-map-authoring-basics*
*Completed: 2026-04-14*
