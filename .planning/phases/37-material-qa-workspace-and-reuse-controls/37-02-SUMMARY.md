---
phase: 37-material-qa-workspace-and-reuse-controls
plan: 37-02
subsystem: ui
tags: [react, antd, material-qa, admin-ui, media-preview]

requires:
  - phase: 37-01
    provides: "Admin material QA backend endpoints, DTOs, consistency check, approve/reject/replace actions"
provides:
  - "Traditional Chinese material QA workspace on the existing story material package page"
  - "Typed frontend client helpers for material QA overview, items, detail, consistency, approve, reject, and replace"
  - "QA filters, health summary cards, consistency report drawer, and QA action modal"
  - "Stable long-path and public URL truncation for material/package/version tables"
affects: [phase-37, phase-38, material-qa, media-picker, public-runtime-assets]

tech-stack:
  added: []
  patterns:
    - "Use package-scoped QA endpoints from the existing material package page instead of creating duplicate admin pages."
    - "Keep broken or incomplete material rows visible as manifest demand slots while making health states explicit."

key-files:
  created:
    - .planning/phases/37-material-qa-workspace-and-reuse-controls/37-02-SUMMARY.md
  modified:
    - packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts
    - packages/admin/aoxiaoyou-admin-ui/src/services/api.ts
    - packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryMaterialPackageManagement.tsx
    - packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryMaterialPackageManagement.scss
    - packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetPreview.tsx

key-decisions:
  - "QA remains embedded in 故事素材包 to avoid a second, confusing material QA route."
  - "素材包行 is treated as a demand slot, not automatically as a usable runtime asset."
  - "Consistency-report table rows use stable finding keys instead of AntD's deprecated rowKey index parameter."

patterns-established:
  - "QA health filters combine backend health states with existing package item rows so operators can keep tracking planned and broken slots."
  - "QA actions require notes/confirmation and use backend asset ids for replacement instead of arbitrary URLs."
  - "Long URLs and COS keys use ellipsis plus tooltip/link affordances."

requirements-completed: [QA-01, QA-02]

duration: 45min
completed: 2026-05-03
---

# Phase 37 Plan 02 Summary

**Existing story material package page upgraded into a QA workspace with filters, health summaries, consistency checks, and version-level QA actions.**

## Performance

- **Duration:** 45 min
- **Started:** 2026-05-03T09:00:00+08:00
- **Completed:** 2026-05-03T09:30:00+08:00
- **Tasks:** 3
- **Files modified:** 5

## Accomplishments

- Added frontend DTOs and API wrappers for all Phase 37 material QA endpoints.
- Added QA filters, health-state cards, runtime exposure/status/kind filters, and a consistency-check drawer to the existing `故事素材包` page.
- Extended the material detail drawer into a QA workspace with findings, version metadata, preview cards, external caption metadata, approve/reject/replace actions, and rollback access.
- Added stable ellipsis handling for long COS keys, canonical URLs, and version asset links so paths do not overflow adjacent table cells.

## Task Commits

Pending atomic commit after summary/state update:

1. **37-02: Upgrade material QA workspace** - commit pending.

## Files Created/Modified

- `packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts` - Added material QA overview/item/detail/finding/action/consistency DTOs.
- `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts` - Added typed QA API helpers for overview, items, detail, consistency check, approve, reject, and replace.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryMaterialPackageManagement.tsx` - Added QA filters, health summary, consistency drawer, QA detail actions, and stable finding row keys.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryMaterialPackageManagement.scss` - Added version asset preview, health card, and long-link truncation styles.
- `packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetPreview.tsx` - Kept reusable preview failed/no-public-link/non-previewable states available for QA usage.

## Decisions Made

- QA controls were added to the existing material package page instead of a separate page because operators already use that page as the package entry point.
- Health cards are clickable filters, but planned/missing rows remain in the table by default to avoid hiding incomplete manifest work.
- The consistency report uses stable composite keys from finding fields to avoid AntD's deprecated `rowKey` index warning.

## Deviations from Plan

### Auto-fixed Issues

**1. AntD consistency table rowKey warning**
- **Found during:** Browser smoke of `一致性檢查`.
- **Issue:** The consistency report table used AntD's deprecated `rowKey={(_, index) => ...}` form, which appeared as a console error in Playwright.
- **Fix:** Added `qaFindingRowKey` using finding severity/code/source/expected/actual/message fields.
- **Files modified:** `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryMaterialPackageManagement.tsx`
- **Verification:** Re-ran browser smoke; `console error` returned zero messages.

## Issues Encountered

- Local admin login initially failed because the running backend process and local `sys_admin` password state were out of sync. The local `admin` test account was restored to a known bcrypt hash for `admin123`, and the admin backend was restarted on `8081`. This was a local database/runtime repair only; no tracked source file was changed for it.
- The running admin backend was an old Java process. It was restarted with the `local` profile and verified via direct login API before browser smoke continued.

## Verification

- `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` passed after implementation.
- `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` passed again after the rowKey fix.
- Browser smoke against `http://localhost:5173/admin/#/content/material-packages` with backend `8081` passed:
- `GET /api/admin/v1/content/material-packages/1/qa/overview` returned 200.
- `GET /api/admin/v1/content/material-packages/1/qa/items` returned 200.
- `POST /api/admin/v1/content/material-packages/1/qa/consistency-check` returned 200 and displayed blocking/warning/info counts.
- `GET /api/admin/v1/content/material-packages/1/qa/items/1` returned 200.
- `GET /api/admin/v1/content/material-packages/1/items/1/versions` returned 200.
- Browser console had zero errors after the rowKey fix.

## User Setup Required

None for source code. Local smoke requires the admin backend on `8081`, admin UI on `5173`, and a valid local admin account.

## Next Phase Readiness

Plan `37-03` can now implement media-picker reuse controls and smoke/docs alignment on top of the QA workspace. `QA-04` is not marked complete here because it belongs to `37-03`.

---
*Phase: 37-material-qa-workspace-and-reuse-controls*
*Completed: 2026-05-03*
