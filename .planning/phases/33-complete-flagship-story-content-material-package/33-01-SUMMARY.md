---
phase: 33-complete-flagship-story-content-material-package
plan: 33-01
subsystem: database-api
tags: [spring-boot, mybatis-plus, mysql, admin-api, provenance]
requires:
  - phase: 28-story-and-content-control-plane-completion
    provides: Lottie-aware content assets and reusable content block foundation
  - phase: 30-storyline-mode-and-chapter-override-workbench
    provides: Storyline and chapter orchestration context
provides:
  - Story material package registry schema
  - Admin-only material package CRUD and item CRUD API
  - Server-side package item counters and provenance field persistence
affects: [phase-33, content-assets, story-admin, material-package-ui]
tech-stack:
  added: []
  patterns:
    - MyBatis-Plus entity/mapper/service/controller CRUD
    - Soft-delete filtering with server-side counter recomputation
key-files:
  created:
    - scripts/local/mysql/init/47-phase-33-story-material-package-model.sql
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminStoryMaterialPackageController.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminStoryMaterialPackageServiceImpl.java
  modified: []
key-decisions:
  - "Material packages aggregate and trace existing content assets, story rows, flows, rewards, and exploration rows instead of replacing native domain tables."
  - "Package and item deletes are soft deletes in the admin API; item counters are recomputed server-side from active rows."
patterns-established:
  - "Material package item provenance persists COS key, canonical URL, local path, prompt/script text, and historical/literary notes."
  - "Admin package detail returns ordered active items for operator inspection and later UI aggregation."
requirements-completed: [STORY-03, VER-02]
duration: 38 min
completed: 2026-04-29
---

# Phase 33 Plan 33-01: Material Package Schema and Admin API Summary

**Story material package registry with admin-only CRUD, provenance item tracking, and compile-verified Spring/MyBatis integration**

## Performance

- **Duration:** 38 min
- **Started:** 2026-04-29T22:21:00+08:00
- **Completed:** 2026-04-29T22:59:33+08:00
- **Tasks:** 2
- **Files modified:** 10

## Accomplishments

- Added `story_material_packages` and `story_material_package_items` with UTF-8/utf8mb4 DDL, provenance fields, COS/local path fields, asset references, target references, soft-delete flags, and idempotent `seed_runs` tracking.
- Added admin backend entity, mapper, request DTO, response DTO, service, and controller layers under `/api/admin/v1/content/material-packages`.
- Implemented active-row filtering, per-package `item_key` uniqueness checks, soft delete, asset validation, JSON validation for manifests, and server-side package counter recomputation.

## Task Commits

1. **Task 33-01-01: Create material package schema** - `22501a9` (feat)
2. **Task 33-01-02: Add admin backend package API** - `6cfcc10` (feat)

## Files Created/Modified

- `scripts/local/mysql/init/47-phase-33-story-material-package-model.sql` - DDL for material packages/items and seed run record.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/StoryMaterialPackage.java` - MyBatis entity for package rows.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/StoryMaterialPackageItem.java` - MyBatis entity for manifest/provenance item rows.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/mapper/StoryMaterialPackageMapper.java` - package mapper.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/mapper/StoryMaterialPackageItemMapper.java` - item mapper.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminStoryMaterialPackageRequest.java` - query/upsert request contracts.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminStoryMaterialPackageResponse.java` - summary/detail/item/counter response contracts.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminStoryMaterialPackageService.java` - service contract.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminStoryMaterialPackageServiceImpl.java` - CRUD, validation, soft-delete, and counter logic.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminStoryMaterialPackageController.java` - admin-only REST controller.

## Decisions Made

- Kept the package registry as an aggregation/provenance layer to avoid duplicating or replacing `content_assets`, content blocks, experience flows, rewards, or exploration elements.
- Used soft deletes in service methods even though the item table also has foreign-key cascade semantics for physical database deletion.
- Validated `manifestJson` through Jackson before persistence so malformed manifest payloads fail at the API boundary.

## Deviations from Plan

None - plan executed exactly as written.

---

**Total deviations:** 0 auto-fixed.
**Impact on plan:** No scope changes.

## Issues Encountered

- `mysql` emitted the standard command-line password warning during local import; the import still exited `0`.

## Verification

- `mysql --default-character-set=utf8mb4` import of `47-phase-33-story-material-package-model.sql` exited `0`.
- `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` exited `0`.
- `Select-String` confirmed the controller route, `PackageDetail detail(Long packageId)`, `recomputeCounters`, and package item provenance fields.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

Wave 2 can seed the manifest and package item records through the new schema/API contract. The admin UI package page in Wave 4 can consume `/api/admin/v1/content/material-packages` without adding another backend route.

---

*Phase: 33-complete-flagship-story-content-material-package*
*Completed: 2026-04-29*
