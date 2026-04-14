---
phase: 06-migration-cutover-and-hardening
plan: 02
subsystem: admin-ops-visibility
tags: [dashboard, health, integration, live-travelers]
requires: [06-01]
provides:
  - probe-based integration health in admin dashboard and health endpoints
  - seeded-content completeness visibility for operators
  - admin traveler views backed by live `user_profiles`/`user_progress`/`user_checkins` data
affects: [06-03]
tech-stack:
  added: []
  patterns: [probe-based dashboard health, admin read-model cutover, live traveler observability]
key-files:
  created:
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/TravelerProfile.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/TravelerProgress.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/TravelerCheckin.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/mapper/TravelerProfileMapper.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/mapper/TravelerProgressMapper.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/mapper/TravelerCheckinMapper.java
  modified:
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/DashboardStatsResponse.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/DashboardServiceImpl.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserServiceImpl.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminTestConsoleServiceImpl.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/HealthController.java
    - packages/admin/aoxiaoyou-admin-ui/src/pages/Dashboard/index.tsx
    - packages/admin/aoxiaoyou-admin-ui/src/services/api.ts
    - packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts
key-decisions:
  - "Expose real database, public API, COS, and seed-migration probes instead of optimistic dashboard booleans."
  - "Cut admin traveler views away from the legacy `users` table so operators see the same traveler data the public mini-program writes."
requirements-completed: [ADM-03, OPS-03]
duration: brownfield pass
completed: 2026-04-12
---

# Phase 6: Plan 02 Summary

**The admin system now reports real integration health and real traveler activity instead of stale placeholder state.**

## Accomplishments

- Extended the admin dashboard and health responses with probe-based integration status for MySQL, the public API, Tencent COS, and Phase 6 seed-migration completion.
- Added seeded-content completeness visibility so operators can inspect published catalog/runtime counts without opening MySQL directly.
- Rewired dashboard stats, admin user management, and test-console traveler reads to use `user_profiles`, `user_progress`, and `user_checkins` data instead of the legacy `users` table.
- Updated the admin dashboard UI to surface the new integration-health and content-summary fields end to end.

## Verification

- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-06-live-cutover.ps1`
- Post-smoke admin dashboard snapshot on `http://127.0.0.1:8081/api/admin/v1/dashboard/stats` confirmed `totalUsers=7`, `publishedCities=3`, `publishedRuntimeSettings=8`, `seedMigration=completed`, `publicApiHealthy=true`, and `cosHealthy=true`
- `mvn -q -DskipTests compile` in `packages/admin/aoxiaoyou-admin-backend`

## Notes

- The admin test console still uses existing `test_accounts` rows as operator handles, but traveler state shown and mutated by those flows now comes from the live traveler tables.

---
*Phase: 06-migration-cutover-and-hardening*
*Completed: 2026-04-12*
