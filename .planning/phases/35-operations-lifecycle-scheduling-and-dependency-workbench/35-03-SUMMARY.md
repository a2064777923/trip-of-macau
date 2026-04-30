---
phase: 35-operations-lifecycle-scheduling-and-dependency-workbench
plan: 35-03
subsystem: admin-ui
tags: [operations, lifecycle, ui, traditional-chinese]

requires:
  - 35-02 lifecycle API
provides:
  - Traditional Chinese lifecycle workbench
  - Lifecycle API wrappers and frontend DTOs
  - Operations sidebar/route integration
affects: [OPS-02, OPS-04, admin-ui]

tech-stack:
  added: []
  patterns:
    - Preview drawer before apply/schedule
    - Lifecycle history detail drawer for audit review

key-files:
  created:
    - packages/admin/aoxiaoyou-admin-ui/src/pages/OperationsLifecycle/OperationsLifecycleWorkbench.tsx
    - packages/admin/aoxiaoyou-admin-ui/src/pages/OperationsLifecycle/OperationsLifecycleWorkbench.scss
  modified:
    - packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts
    - packages/admin/aoxiaoyou-admin-ui/src/services/api.ts
    - packages/admin/aoxiaoyou-admin-ui/src/App.tsx
    - packages/admin/aoxiaoyou-admin-ui/src/layouts/DefaultLayout.tsx

key-decisions:
  - "Expose lifecycle operations as a dedicated operations workbench instead of scattered per-domain buttons."
  - "Keep apply/schedule disabled until a preview exists and the operator confirms impact review."
  - "Keep existing testing and operations sidebar items; add lifecycle scheduling as a new item under `測試與營運管理`."

requirements-completed:
  - OPS-02
  - OPS-04

duration: 50min
completed: 2026-04-30
---

# Phase 35 Plan 35-03: Lifecycle Workbench UI Summary

Built the admin lifecycle workbench and wired it into the operations navigation.

## Accomplishments

- Added TypeScript lifecycle types and API functions for catalog, targets, preview, create, apply, cancel, run-due, operations list, and detail.
- Added `/admin/#/ops/lifecycle` route.
- Added sidebar item `生命週期與發布排程` under `測試與營運管理`.
- Added a Traditional Chinese workbench with summary cards, labeled filters, target table, impact preview drawer, apply/schedule panel, operation history, run-due button, cancel schedule action, and detail drawer.
- Added ellipsis handling for long codes/paths in impact/detail UI.

## Verification

- `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` exited `0`.
- The OpenAPI endpoint and live Phase 35 smoke verified the backend contract consumed by these UI wrappers.

## Deviations from Plan

None.

## Next

Ready for Plan 35-04 smoke, verification docs, and traceability updates.
