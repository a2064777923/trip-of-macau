---
phase: 37-material-qa-workspace-and-reuse-controls
plan: 37-01
subsystem: api
tags: [spring-boot, mybatis-plus, material-qa, content-assets]

requires:
  - phase: 36-material-production-pipeline-and-asset-promotion
    provides: material package version lineage, promotion states, content asset links
provides:
  - package-scoped material QA overview/detail/list APIs
  - lineage-preserving reject, approve, and replace actions
  - package-scoped consistency report with bounded COS checks
  - focused backend tests for material QA behavior
affects: [phase-37-admin-ui, phase-37-media-reuse, phase-38-public-runtime-assets]

tech-stack:
  added: []
  patterns: [admin-only package-scoped QA service, version-preserving asset replacement, bounded remote availability checks]

key-files:
  created:
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminStoryMaterialQaRequest.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminStoryMaterialQaResponse.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminStoryMaterialQaService.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminStoryMaterialQaServiceImpl.java
    - packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminStoryMaterialQaServiceTest.java
  modified:
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminStoryMaterialPackageController.java

key-decisions:
  - "Phase 37 QA is package-scoped and builds on existing material item/version/content asset tables instead of adding new schema."
  - "Reject and replace preserve version rows and content_assets records; replacement creates a new current version instead of mutating old binaries."
  - "COS availability checks are explicit, bounded by maxCosChecks, and constrained to COS-like HTTPS hosts."

patterns-established:
  - "QA health state is derived from item, current version, published version, content asset, and consistency findings."
  - "Runtime-impacting QA transitions require confirmation, with super-admin enforcement for publishing transitions."
  - "External caption metadata is reported as info evidence, not treated as failed video generation."

requirements-completed: [QA-01, QA-02, QA-03]

duration: 35 min
completed: 2026-05-03
---

# Phase 37 Plan 37-01: Material QA Backend API Summary

**Package-scoped material QA APIs with health classification, consistency findings, and lineage-preserving asset actions.**

## Performance

- **Duration:** 35 min
- **Started:** 2026-05-03T00:22:00Z
- **Completed:** 2026-05-03T00:57:11Z
- **Tasks:** 3
- **Files modified:** 6

## Accomplishments

- Added admin QA endpoints under `/api/admin/v1/content/material-packages/{packageId}/qa/...` for overview, item list, item detail, reject, approve, replace, and consistency check.
- Implemented health states and findings for missing/current/published assets, stale pointers, wrong kind, oversized files, rejected versions, no public URL, COS HEAD failures, planned demand slots, and external caption metadata.
- Added lineage-preserving QA actions: reject marks versions rejected without deleting assets, approve updates status and current pointer, and replace creates a new version bound to an existing `content_assets.id`.
- Added focused tests covering overview classification, consistency reports, reject/replace lineage, approve, bounded COS checks, and external captions.

## Task Commits

Each task was implemented in this plan commit.

1. **Task 37-01-01: QA DTOs and package routes** - included in plan commit
2. **Task 37-01-02: Health classification and QA actions** - included in plan commit
3. **Task 37-01-03: Focused backend tests** - included in plan commit

## Files Created/Modified

- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminStoryMaterialQaRequest.java` - QA query/action/replace/consistency request DTOs.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminStoryMaterialQaResponse.java` - QA overview, item, detail, finding, action result, and consistency report DTOs.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminStoryMaterialQaService.java` - package-scoped QA service contract.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminStoryMaterialQaServiceImpl.java` - QA orchestration, consistency checks, and safe status transitions.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminStoryMaterialPackageController.java` - QA routes wired into the existing material package controller.
- `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminStoryMaterialQaServiceTest.java` - focused unit tests for QA behavior without live COS/provider dependencies.

## Decisions Made

- Kept QA report computation live and package-scoped for Phase 37, because current UI needs reliable row health and no persistent QA report table is required yet.
- Replacement binds an existing content asset into a new material package version, preserving old versions and binaries for rollback/evidence.
- COS checks only run when explicitly requested and are bounded. The implementation rejects arbitrary non-COS hosts for HEAD probing.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Prevented URL/path lower-casing in QA responses**

- **Found during:** Task 37-01-02
- **Issue:** The initial helper reused status-code normalization for general strings, which could lowercase URL and object-key values.
- **Fix:** Separated code normalization from text/default fallback handling.
- **Files modified:** `AdminStoryMaterialQaServiceImpl.java`
- **Verification:** Backend compile and QA tests pass.
- **Committed in:** plan commit

**2. [Rule 1 - Bug] Defensively filtered versions by item id**

- **Found during:** Task 37-01-03
- **Issue:** Unit tests using mapper mocks exposed that service logic should not rely solely on mapper wrapper filtering.
- **Fix:** Added in-service filtering of material versions by `packageItemId`.
- **Files modified:** `AdminStoryMaterialQaServiceImpl.java`
- **Verification:** `AdminStoryMaterialQaServiceTest` passes.
- **Committed in:** plan commit

---

**Total deviations:** 2 auto-fixed bug fixes.
**Impact on plan:** Both fixes improve correctness without changing scope or schema.

## Issues Encountered

- `rg.exe` was blocked by the Windows app package path in this desktop session, so PowerShell `Select-String` was used for acceptance checks.
- Mockito hit an ambiguous MyBatis-Plus `deleteById` overload in one verification assertion; the test now checks the concrete asset id.

## Verification

- `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` passed.
- `mvn -q -Dtest=AdminStoryMaterialQaServiceTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` passed.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

Ready for 37-02. The admin UI can now call a stable QA backend contract for material package health, details, actions, and consistency reports.

---
*Phase: 37-material-qa-workspace-and-reuse-controls*
*Completed: 2026-05-03*
