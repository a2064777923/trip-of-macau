---
phase: 29-poi-default-experience-workbench
plan: 02
subsystem: admin-ui
tags: [react, vite, ant-design, poi-workbench, form-validation]
requires:
  - phase: 29-01
    provides: POI default experience admin facade and typed API helpers
provides:
  - Dedicated POI 地點體驗工作台 admin route
  - Three-panel POI default experience timeline and structured card editor
  - POI management row entry and sidebar navigation
affects: [phase-30-storyline-overrides, phase-31-template-governance, phase-33-flagship-content]
tech-stack:
  added: []
  patterns:
    - Three-panel workbench with selector, visual composition, property editor, and validation strip
    - Quick-add canonical flow presets for POI natural walk-in behavior
    - Invalid form scroll, focus, and shake feedback through focusFirstInvalidField
key-files:
  created:
    - packages/admin/aoxiaoyou-admin-ui/src/pages/POIExperienceWorkbench/index.tsx
    - packages/admin/aoxiaoyou-admin-ui/src/pages/POIExperienceWorkbench/index.scss
  modified:
    - packages/admin/aoxiaoyou-admin-ui/src/pages/POIManagement/index.tsx
    - packages/admin/aoxiaoyou-admin-ui/src/App.tsx
    - packages/admin/aoxiaoyou-admin-ui/src/layouts/DefaultLayout.tsx
key-decisions:
  - "POI default experience authoring is a dedicated Traditional Chinese route under the existing protected map/space admin shell."
  - "Operators edit condition, effect, media, and reward cards first; advanced JSON is collapsed fallback."
  - "The POI list uses normalized row IDs before routing to /space/pois/${record.id}/experience."
patterns-established:
  - "Future domain-specific workbenches should start from structured presets and only expose JSON as an advanced override."
  - "Sidebar-visible future workbench entries must route to dedicated pages, not placeholders or unrelated modules."
requirements-completed: [STORY-01, LINK-01]
duration: continuation
completed: 2026-04-29
---

# Phase 29 Plan 02: POI Workbench Summary

**Traditional Chinese POI 地點體驗工作台 with timeline presets, structured cards, template saving, route wiring, and validation feedback**

## Performance

- **Duration:** Continuation from prior executor handoff
- **Completed:** 2026-04-29
- **Tasks:** 2
- **Files modified:** 5

## Accomplishments

- Added `POIExperienceWorkbench` with POI selector, flow summary, quick-add buttons, ordered timeline, grouped visual composition area, structured property editor, validation findings, and public runtime path display.
- Added seven canonical quick-add presets: `tap_intro`, `start_route_guidance`, `arrival_intro_media`, `release_checkin_tasks`, `pickup_side_clues`, `hidden_dwell_achievement`, and `completion_reward_title`.
- Exposed condition, effect, media, reward, and advanced JSON cards in Traditional Chinese, with save, delete, publish, and save-template actions.
- Registered `/space/poi-experience` and `/space/pois/:poiId/experience` under the protected admin shell and added a direct `地點體驗` row action from POI management.

## Task Commits

No commit was created in this continuation because the shared worktree already contained extensive unrelated dirty changes. The implementation and verification are documented in this summary and the phase verification artifact.

## Files Created/Modified

- `packages/admin/aoxiaoyou-admin-ui/src/pages/POIExperienceWorkbench/index.tsx` - Dedicated workbench UI and behavior.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/POIExperienceWorkbench/index.scss` - Workbench layout, card, timeline, and invalid-state styling.
- `packages/admin/aoxiaoyou-admin-ui/src/App.tsx` - Protected route registration.
- `packages/admin/aoxiaoyou-admin-ui/src/layouts/DefaultLayout.tsx` - Map/space navigation entry.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/POIManagement/index.tsx` - Row-level route action and normalized row ID handling.

## Decisions Made

The workbench keeps POI operators in a POI-context flow instead of sending them to the generic Phase 28 console. This reduces route ownership confusion and prepares Phase 30 to present chapter overrides as a separate, story-specific experience.

## Deviations from Plan

### Auto-fixed Issues

**1. [Review Gate - Bug] Cleared disabled advanced JSON fields before structured save**

- **Found during:** Advisory review of `POIExperienceWorkbench`.
- **Issue:** Editing an existing step populated the collapsed advanced JSON text areas from the saved step. Saving with `advancedJsonEnabled=false` would still submit those hidden JSON values, and the backend correctly rejects advanced JSON unless the switch is enabled.
- **Fix:** Added `normalizeStepSubmitPayload` so disabled advanced JSON fields are removed before create/update requests.
- **Files modified:** `packages/admin/aoxiaoyou-admin-ui/src/pages/POIExperienceWorkbench/index.tsx`.
- **Verification:** Admin UI build passed after the fix.

**2. [Route Alignment] Normalized POI row IDs for exact workbench routing**

- **Found during:** Static route verification.
- **Issue:** Existing POI rows can expose either `id` or `poiId`, while the plan required exact navigation to `/space/pois/${record.id}/experience`.
- **Fix:** Added `poiRows` normalization so row actions have a stable `record.id`.
- **Files modified:** `packages/admin/aoxiaoyou-admin-ui/src/pages/POIManagement/index.tsx`.
- **Verification:** Route source check and admin UI build passed.

**Total deviations:** 2 auto-fixed implementation issues. Both were required for correctness and did not expand scope.

## Issues Encountered

None remaining after the row ID and advanced JSON submit fixes.

## Verification

- `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` passed with only the existing Vite chunk-size warning.
- Source checks found `POI 地點體驗工作台`, all five visual groups, all seven canonical step codes, `條件卡`, `效果卡`, `媒體卡`, `獎勵卡`, `進階 JSON`, `focusFirstInvalidField`, `saveAdminPoiExperienceStepAsTemplate`, and `publicRuntimePath`.
- Route checks found `space/poi-experience`, `space/pois/:poiId/experience`, sidebar label `POI 地點體驗工作台`, row action `地點體驗`, and exact navigation to `/space/pois/${record.id}/experience`.

## User Setup Required

None.

## Next Phase Readiness

Phase 30 can reuse the POI workbench vocabulary when presenting inherited POI effects inside story chapter override cards.

---
*Phase: 29-poi-default-experience-workbench*
*Completed: 2026-04-29*
