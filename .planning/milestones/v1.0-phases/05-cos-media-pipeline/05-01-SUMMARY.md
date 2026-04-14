---
phase: 05-cos-media-pipeline
plan: 01
subsystem: admin-backend
tags: [cos, upload, content-assets, media-pipeline]
requires: [02-03]
provides:
  - environment-backed Tencent COS configuration for the admin backend
  - authenticated multipart upload endpoint under `/api/admin/v1/content/assets/upload`
  - automatic `content_assets` persistence with canonical URL and integrity metadata
  - COS object cleanup when deleting uploaded assets
affects: [05-02, 05-03]
tech-stack:
  added: [Tencent COS Java SDK]
  patterns: [backend-managed object keys, canonical asset metadata persistence, upload-first admin media flow]
key-files:
  created:
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/CosProperties.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/CosConfig.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/StoredAssetMetadata.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/CosAssetStorageService.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminContentAssetUploadRequest.java
  modified:
    - packages/admin/aoxiaoyou-admin-backend/pom.xml
    - packages/admin/aoxiaoyou-admin-backend/src/main/resources/application-local.yml
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminContentManagementController.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminContentManagementService.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminContentManagementServiceImpl.java
key-decisions:
  - "Keep COS uploads in the admin backend because `/admin` remains the authoritative write surface for media."
  - "Treat `content_assets` as the canonical asset ledger instead of introducing a second upload metadata table."
  - "Delete uploaded COS objects when asset rows are deleted so media management stays operationally clean."
requirements-completed: [MED-01, MED-02]
duration: brownfield pass
completed: 2026-04-12
---

# Phase 5: Plan 01 Summary

**The admin backend now owns the real Tencent COS media pipeline.**

## Accomplishments

- Added env-backed COS runtime settings and the Tencent COS Java SDK to `aoxiaoyou-admin-backend`.
- Implemented `CosAssetStorageService` to generate deterministic object keys, upload multipart files, derive canonical URLs, compute SHA-256 checksums, extract image dimensions, and persist ETags.
- Added `/api/admin/v1/content/assets/upload` as an authenticated multipart endpoint that writes into COS and inserts a canonical `content_assets` row.
- Closed the delete path so removing an uploaded asset now deletes the COS object before removing the database row.

## Verification

- `mvn -q -DskipTests compile` in `packages/admin/aoxiaoyou-admin-backend`
- Live admin-backend restart on `http://127.0.0.1:8081` with `APP_COS_*` environment variables
- Phase 5 smoke upload + delete verification against real COS and local MySQL

## Notes

- COS secrets stayed in runtime environment only; no tracked source or planning file contains the SecretId or SecretKey.
- The upload service intentionally keeps object-key generation fully backend-controlled; the admin UI does not choose bucket paths or filenames manually.

---
*Phase: 05-cos-media-pipeline*
*Completed: 2026-04-12*
