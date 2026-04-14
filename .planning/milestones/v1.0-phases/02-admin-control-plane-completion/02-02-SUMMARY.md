---
phase: 02-admin-control-plane-completion
plan: 02
subsystem: admin-backend
tags: [content-crud, runtime-settings, assets, tips, notifications, stamps]
requires: [01]
provides:
  - live admin CRUD APIs for runtime settings, assets, tips, notifications, and stamps
  - canonical request/response DTO coverage for mini-program-facing admin content domains
  - startup resilience when local Mongo bootstrap credentials drift
affects: [phase-03-public-read-apis-cutover, phase-05-cos-media-pipeline, phase-06-migration-cutover-and-hardening]
tech-stack:
  added:
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminContentManagementController.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminContentManagementService.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminContentManagementServiceImpl.java
  patterns:
    - one canonical content-management surface for mini-program-facing admin domains
    - best-effort Mongo bootstrap so document-store drift does not prevent HTTP startup
key-files:
  created:
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminContentManagementController.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminContentManagementService.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminContentManagementServiceImpl.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/AppRuntimeSetting.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/ContentAsset.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/TipArticle.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/Notification.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/Stamp.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/mapper/AppRuntimeSettingMapper.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/mapper/ContentAssetMapper.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/mapper/TipArticleMapper.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/mapper/NotificationMapper.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/mapper/StampMapper.java
  modified:
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/config/MongoConfig.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminSystemManagementServiceImpl.java
key-decisions:
  - "Expose the missing mini-program-facing admin content domains under one `/api/admin/v1/content` surface instead of scattering them across unrelated legacy modules."
  - "Treat Mongo bootstrap as optional for current admin HTTP startup because the active Phase 2 control plane is MySQL-backed."
requirements-completed: [ADM-01, ADM-02]
completed: 2026-04-12
---

# Phase 2: Plan 02 Summary

**Added the missing admin content/runtime CRUD so the admin backend can manage every remaining mini-program-facing content surface introduced in Phase 1.**

## Accomplishments

- Added `/api/admin/v1/content` CRUD coverage for runtime settings, content assets, tips, notifications, and stamps.
- Introduced canonical entity, mapper, request, and response types for the missing mini-program-facing admin domains.
- Hardened admin startup by making Mongo document bootstrap best-effort instead of a hard failure when local credentials drift.
- Verified the new content endpoints against a real local backend on port `18081` with JWT-authenticated requests.

## Files Created/Modified

- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminContentManagementController.java` - runtime/content CRUD endpoints.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminContentManagementService.java` - service contract for the new content domains.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminContentManagementServiceImpl.java` - CRUD implementation and cross-domain validation.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/AppRuntimeSetting.java` - canonical runtime-setting entity.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/ContentAsset.java` - canonical asset entity.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/TipArticle.java` - canonical tip/article entity.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/Notification.java` - canonical notification entity.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/Stamp.java` - canonical stamp entity.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/config/MongoConfig.java` - graceful Mongo bootstrap fallback.

## Issues Encountered

- The local admin process initially failed to boot because startup tried to create Mongo collections with credentials that did not match the current local Mongo instance.

## Resolution

- Wrapped Mongo bootstrap in a best-effort warning path so HTTP startup continues even when local document-store credentials are invalid.
- Kept the current Phase 2 control plane on MySQL, where the canonical content domains already live.

## Verification

The new content-management surface responded successfully with a real JWT:

- `GET /api/admin/v1/content/runtime-settings?pageNum=1&pageSize=5` -> `code=0`, `total=6`
- `GET /api/admin/v1/content/assets?pageNum=1&pageSize=5` -> `code=0`, `total=0`
- `GET /api/admin/v1/content/tips?pageNum=1&pageSize=5` -> `code=0`, `total=0`
- `GET /api/admin/v1/content/notifications?pageNum=1&pageSize=5` -> `code=0`, `total=0`
- `GET /api/admin/v1/content/stamps?pageNum=1&pageSize=5` -> `code=0`, `total=0`
- `POST/PUT/DELETE /api/admin/v1/content/runtime-settings` -> create `id=15`, update to `published`, delete succeeds

## Next Phase Readiness

- Phase 3 can consume runtime settings and the remaining admin-owned content domains through canonical public read services.
- Phase 5 can layer Tencent COS upload handling onto the new `content_assets` registry instead of inventing another media table.

---
*Phase: 02-admin-control-plane-completion*
*Completed: 2026-04-12*
