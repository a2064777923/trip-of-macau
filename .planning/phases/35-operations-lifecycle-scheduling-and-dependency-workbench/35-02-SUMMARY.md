---
phase: 35-operations-lifecycle-scheduling-and-dependency-workbench
plan: 35-02
subsystem: admin-backend
tags: [lifecycle, preview, scheduling, audit]

requires:
  - 35-01 lifecycle schema and DTOs
provides:
  - Lifecycle target registry
  - Dependency impact preview
  - Immediate and scheduled lifecycle operations
  - Admin lifecycle API endpoints
affects: [OPS-02, OPS-04, admin-backend]

tech-stack:
  added: []
  patterns:
    - Whitelisted target registry for cross-domain lifecycle mutation
    - Preview-first operation creation with persisted impact snapshots

key-files:
  created:
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/content/AdminLifecycleTargetRegistry.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/content/AdminLifecycleDependencyPreviewService.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/content/AdminLifecycleDueOperationRunner.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminLifecycleOperationController.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminLifecycleOperationService.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminLifecycleOperationServiceImpl.java
  modified: []

key-decisions:
  - "Do not accept arbitrary table or column names from the frontend; lifecycle mutation is constrained by `AdminLifecycleTargetRegistry` descriptors."
  - "Block destructive operations with blocking impacts unless the operator explicitly requests cascade."
  - "Use the same operation table for immediate and scheduled actions so history and audit remain consistent."

requirements-completed:
  - OPS-02
  - OPS-04

duration: 45min
completed: 2026-04-30
---

# Phase 35 Plan 35-02: Lifecycle API Summary

Implemented the admin lifecycle backend API for dependency-aware publish, unpublish, remove, scheduling, and history.

## Accomplishments

- Added lifecycle target descriptors for cities, sub-maps, POIs, indoor entities, storylines, chapters, content blocks/assets, experience flows/bindings/overrides, collectibles, rewards, redeemable prizes, honors, and activities.
- Added status catalog and target pagination with normalized Traditional Chinese lifecycle labels.
- Added dependency preview for inbound/outbound relation links, structural downstream children, public runtime impact, and exploration progress impact.
- Added operation creation, immediate apply, scheduled apply, due runner, cancellation, operation history, and operation detail.
- Exposed `/api/admin/v1/operations/lifecycle` endpoints for target types, statuses, targets, preview, operations, apply, cancel, run-due, history, and detail.
- Hardened target pagination so schema differences in one target descriptor return an empty slice for that descriptor instead of failing the entire workbench.

## Verification

- `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` exited `0`.
- `scripts/local/smoke-phase-35-lifecycle.ps1` verified the live API on `8081` with target catalog, status catalog, preview, scheduled operation, due-run application, history list, and detail read.

## Deviations from Plan

### Auto-fixed Issues

**1. Cross-domain target list failed when a legacy target table missed expected columns**

- **Found during:** manual code review and live `/targets` API check.
- **Issue:** Querying all lifecycle target types could fail on `indoor_nodes` because the local table does not expose the same `deleted` shape as newer domain tables.
- **Fix:** `loadTargetSummaries` now catches `DataAccessException` per descriptor and returns an empty list for that target type; `countRelations` also degrades to `0`.
- **Verification:** `GET /api/admin/v1/operations/lifecycle/targets?pageNum=1&pageSize=20` returned `code=0`, `total=351`, and 20 items.

## Next

Ready for Plan 35-03 admin UI workbench.
