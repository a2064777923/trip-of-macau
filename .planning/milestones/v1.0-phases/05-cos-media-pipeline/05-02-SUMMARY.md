---
phase: 05-cos-media-pipeline
plan: 02
subsystem: admin-ui
tags: [admin-ui, upload, assets, control-plane]
requires: [05-01]
provides:
  - upload-first asset workflow in the existing System Management page
  - admin UI multipart API binding for backend-mediated COS uploads
  - immediate asset refresh across reward/runtime/tip/notification/stamp selectors
affects: [05-03, phase-06-migration-cutover-and-hardening]
tech-stack:
  added: []
  patterns: [upload-first admin UX, read-only storage metadata, selector refresh after asset mutation]
key-files:
  modified:
    - packages/admin/aoxiaoyou-admin-ui/src/services/api.ts
    - packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts
    - packages/admin/aoxiaoyou-admin-ui/src/pages/SystemManagement/index.tsx
key-decisions:
  - "Make asset creation upload-first instead of metadata-first so operators do not hand-enter object keys or canonical URLs."
  - "Keep storage metadata visible but read-only in edit mode because the backend is now responsible for bucket/object naming."
requirements-completed: [MED-01, MED-02]
duration: brownfield pass
completed: 2026-04-12
---

# Phase 5: Plan 02 Summary

**The `/admin` asset console now uploads real files instead of faking asset records.**

## Accomplishments

- Added `uploadAdminContentAsset()` in the admin UI service layer using multipart `FormData`.
- Extended admin asset typing for upload payloads and richer asset metadata display.
- Reworked the Assets tab in `SystemManagement` so new assets are uploaded through a drag-and-drop flow while edit mode exposes bucket, region, object key, canonical URL, checksum, and ETag as read-only metadata.
- Refreshed all asset-dependent admin selectors after upload/delete so newly uploaded assets become immediately available to runtime settings, rewards, tips, notifications, and stamps.

## Verification

- `npm run build` in `packages/admin/aoxiaoyou-admin-ui`
- Live Phase 5 smoke used the real admin upload API that the UI now calls

## Notes

- The existing manual asset CRUD route remains available on the backend for compatibility, but the admin UI now guides operators through the safer upload-first path.

---
*Phase: 05-cos-media-pipeline*
*Completed: 2026-04-12*
