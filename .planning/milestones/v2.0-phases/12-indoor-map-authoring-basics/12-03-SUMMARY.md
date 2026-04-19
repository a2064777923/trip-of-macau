---
phase: 12-indoor-map-authoring-basics
plan: 03
subsystem: testing
tags: [indoor, csv-import, minimap, public-api, taro, smoke-test]
requires:
  - phase: 12-indoor-map-authoring-basics
    provides: floor tile manifests, zoom metadata, and admin authoring contracts for buildings and floors
provides:
  - indoor marker authoring and preview-first CSV import contracts
  - public indoor read APIs for building, floor, and marker payloads
  - mini-program indoor runtime alignment and an end-to-end smoke proof
affects: [mini-program, public-api, admin-ui, indoor-runtime]
tech-stack:
  added: [none]
  patterns: [preview-then-confirm import, normalized relative coordinates, public additive DTO rollout]
key-files:
  created: [scripts/local/mysql/init/18-phase-12-indoor-markers-and-runtime.sql, packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/IndoorController.java, packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicIndoorServiceImpl.java, scripts/local/smoke-phase-12-indoor.ps1]
  modified: [packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/IndoorMarkerAuthoringService.java, packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/MapTileManagement.tsx, packages/client/src/pages/map/indoor/index.tsx, packages/client/src/services/api.ts]
key-decisions:
  - "Marker import is preview-first and confirm-only; invalid CSV rows never write directly into `indoor_nodes`."
  - "Relative coordinates are the shared contract across admin previews, persisted markers, and public runtime rendering."
  - "The public indoor API surface is additive so the mini-program can cut over without breaking unrelated catalog flows."
patterns-established:
  - "Admin marker authoring supports both visual minimap placement and bulk CSV import."
  - "Smoke verification proves admin write -> MySQL/COS -> public read -> client build alignment with real services."
requirements-completed: [INDO-01, INDO-02, INDO-03, INDO-04]
duration: 60 min
completed: 2026-04-14
---

# Phase 12: Indoor Map Authoring Basics Summary

**Indoor markers, public indoor read APIs, and the mini-program indoor runtime now run on live authored data, validated by a real end-to-end smoke chain.**

## Performance

- **Duration:** 60 min
- **Started:** 2026-04-14T22:05:00+08:00
- **Completed:** 2026-04-14T23:05:00+08:00
- **Tasks:** 3
- **Files modified:** 18

## Accomplishments

- Added marker CRUD plus preview-first CSV import/confirm contracts with normalized coordinate validation.
- Built the admin floor marker editor with minimap placement, default marker fallback, and CSV preview/confirm handling.
- Exposed public indoor read endpoints, aligned the mini-program indoor page to live data, and proved the chain with a passing smoke script.

## Task Commits

No dedicated git commit was created during this workspace snapshot. The completed work is present in the working tree.

## Files Created/Modified

- `scripts/local/mysql/init/18-phase-12-indoor-markers-and-runtime.sql` - extends indoor marker persistence, import batches, and runtime metadata fields.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/IndoorMarkerAuthoringService.java` - implements marker CRUD, CSV preview, and confirm-import validation.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/MapTileManagement.tsx` - adds minimap-assisted marker placement and CSV import UI.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/IndoorController.java` - publishes indoor building and floor read APIs for the mini-program.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicIndoorServiceImpl.java` - assembles public indoor payloads from live authored records.
- `packages/client/src/pages/map/indoor/index.tsx` - consumes live indoor APIs instead of staying a hardcoded mock-only page.
- `scripts/local/smoke-phase-12-indoor.ps1` - proves the full indoor pipeline against local services and COS.

## Decisions Made

- Kept marker import preview-first so CSV issues are visible before anything is persisted.
- Standardized on normalized relative coordinates across admin authoring and public runtime payloads.
- Verified the real chain with a PowerShell smoke script rather than treating compile success as sufficient evidence.

## Deviations from Plan

### Auto-fixed Issues

**1. Null-safe marker asset loading**

- **Found during:** Task 1
- **Issue:** `List.of(marker.getIconAssetId(), marker.getAnimationAssetId())` threw when either asset ID was null.
- **Fix:** Replaced the null-unsafe list construction with `Stream.of(...).filter(Objects::nonNull)`.
- **Files modified:** `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/IndoorMarkerAuthoringService.java`
- **Verification:** Manual marker creation and CSV-confirm import both succeeded with null icon/animation assets.

**2. Public indoor service compile break**

- **Found during:** Task 3
- **Issue:** `PublicIndoorServiceImpl` missed `LinkedHashMap` and `Collectors` imports, blocking server compile/package.
- **Fix:** Added the missing imports and rebuilt the public server.
- **Files modified:** `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicIndoorServiceImpl.java`
- **Verification:** `mvn -q -DskipTests compile` succeeded in `packages/server`, and live indoor read endpoints returned the authored floor payload.

**3. Smoke script contract and PowerShell runtime fixes**

- **Found during:** Task 3
- **Issue:** The smoke script initially had malformed JSON/CSV payload generation and PowerShell query-string interpolation bugs around `?locale=...`.
- **Fix:** Rewrote payload generation to use structured JSON and `pscustomobject` CSV rows, mapped CSV preview rows into the exact confirm contract, added public-read retries, and wrapped query-string URLs with explicit interpolation.
- **Files modified:** `scripts/local/smoke-phase-12-indoor.ps1`
- **Verification:** The smoke script passed end-to-end on 2026-04-14, creating floor `10`, importing tiles into COS, creating/importing markers, and reading back `publicMarkerCount = 3`.

---

**Total deviations:** 3 auto-fixed issues
**Impact on plan:** All fixes were correctness or runtime-compatibility repairs required to make the planned indoor chain actually executable.

## Issues Encountered

- PowerShell string interpolation treated `$floorId?locale=...` as a different variable name, which only surfaced once the script reached the public read step against real endpoints.
- The smoke script also had to avoid non-ASCII payload content because the local shell environment could corrupt inline string literals while generating JSON and CSV fixtures.

## User Setup Required

None. The smoke ran against the already configured local admin backend, public backend, MySQL, and Tencent COS runtime.

## Next Phase Readiness

- Indoor authoring basics are now live end-to-end and ready for richer overlay behavior, trigger rules, and icon/media polish in future phases.
- Residual gap: targeted backend unit tests for the indoor foundation, tile pipeline, and marker import flows are still worth adding even though compile/build/smoke coverage is green.

---
*Phase: 12-indoor-map-authoring-basics*
*Completed: 2026-04-14*
