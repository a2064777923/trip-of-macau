---
phase: 35-operations-lifecycle-scheduling-and-dependency-workbench
plan: 35-01
subsystem: admin-backend
tags: [lifecycle, schema, dto, mysql]

requires:
  - Phase 34 public runtime lifecycle filtering baseline
provides:
  - Canonical lifecycle status vocabulary
  - Persistent lifecycle operation and impact schema
  - Java entities, mappers, and DTO contracts
affects: [OPS-02, OPS-04, admin-backend, mysql]

tech-stack:
  added: []
  patterns:
    - UTF-8 / utf8mb4-safe SQL migration for lifecycle records
    - Compatibility mapping from old `draft`/`archived` rows into canonical lifecycle labels

key-files:
  created:
    - scripts/local/mysql/init/50-phase-35-lifecycle-operations.sql
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/ContentLifecycleOperation.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/ContentLifecycleOperationImpact.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/mapper/ContentLifecycleOperationMapper.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/mapper/ContentLifecycleOperationImpactMapper.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminLifecycleOperationRequest.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminLifecycleOperationResponse.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminLifecycleImpactResponse.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminLifecycleTargetResponse.java
  modified:
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/enums/ContentStatus.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/content/ContentLifecycleStatusSupport.java

key-decisions:
  - "Keep `draft` and `archived` as compatibility statuses while exposing `editing`, `reviewing`, `published`, `unpublished`, and `deleted` as canonical lifecycle states."
  - "Use logical lifecycle removal (`deleted`) rather than physical deletion."
  - "Persist operation preview, request, result, and impacts as auditable lifecycle records."

requirements-completed:
  - OPS-02
  - OPS-04

duration: 25min
completed: 2026-04-30
---

# Phase 35 Plan 35-01: Lifecycle Foundation Summary

Created the persistent backend foundation for lifecycle operations.

## Accomplishments

- Added canonical lifecycle statuses and Traditional Chinese labels while keeping old `draft` and `archived` rows readable.
- Added action-aware transition helpers for `publish`, `unpublish`, and `remove`.
- Added `content_lifecycle_operations` and `content_lifecycle_operation_impacts` schema with UTF-8-safe migration comments, indexes, preview hash, request/preview/result JSON, and operator audit fields.
- Added MyBatis-Plus entities, mappers, request DTOs, response DTOs, preview payloads, operation summaries, operation details, and impact responses.

## Verification

- `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` exited `0`.
- Phase 35 smoke later imported `50-phase-35-lifecycle-operations.sql` with `--default-character-set=utf8mb4` and persisted operation rows.

## Deviations from Plan

None.

## Next

Ready for Plan 35-02 backend service/controller implementation.
