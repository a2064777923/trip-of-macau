---
phase: 29-poi-default-experience-workbench
status: clean
depth: standard
files_reviewed: 14
findings:
  critical: 0
  warning: 0
  info: 0
  total: 0
resolved_during_review: 1
reviewed: 2026-04-29
---

# Phase 29 Code Review

## Scope

Reviewed the Phase 29 POI default experience backend facade, DTOs, admin UI workbench, route wiring, seed, and smoke script:

- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminPoiExperienceController.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminPoiExperienceService.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminPoiExperienceServiceImpl.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminPoiExperienceRequest.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminPoiExperienceResponse.java`
- `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts`
- `packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/POIExperienceWorkbench/index.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/POIExperienceWorkbench/index.scss`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/POIManagement/index.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/App.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/layouts/DefaultLayout.tsx`
- `scripts/local/mysql/init/40-phase-29-poi-default-experience.sql`
- `scripts/local/smoke-phase-29-poi-experience.ps1`

## Result

No open findings remain.

## Resolved During Review

### R-01 Disabled advanced JSON submitted during normal structured edit

- **Severity before fix:** Warning
- **File:** `packages/admin/aoxiaoyou-admin-ui/src/pages/POIExperienceWorkbench/index.tsx`
- **Issue:** Existing steps populated collapsed advanced JSON fields from persisted step JSON. Saving with `advancedJsonEnabled=false` would still submit those fields, and the backend rejects advanced JSON when the explicit switch is disabled.
- **Fix:** Added `normalizeStepSubmitPayload` to clear `advancedTriggerConfigJson`, `advancedConditionConfigJson`, and `advancedEffectConfigJson` before create/update requests unless `advancedJsonEnabled=true`.
- **Verification:** `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` passed after the fix.

## Checks

- Backend facade stays under protected `/api/admin/v1/**` routes.
- Service writes through canonical Phase 28 `experience_*` tables and enforces `poi`, `default_experience_flow`, `default_poi`, and `walk_in`.
- Structured step fields compile to versioned trigger, condition, and effect JSON.
- The workbench uses Traditional Chinese copy and keeps advanced JSON as fallback.
- The seed starts with `SET NAMES utf8mb4` and uses code-based upserts.
- The smoke script uses explicit UTF-8 `HttpClient` handling and does not contain tracked credentials.
