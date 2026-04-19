---
phase: 14-carryover-control-plane-closure
plan: 02
subsystem: traveler-progress-and-settings
requirements-completed: [CARRY-02]
completed: 2026-04-15
---

# Phase 14 Wave 2 Summary

## Outcome

Traveler progress, operations visibility, and system-default ownership are now backed by explicit admin contracts instead of placeholder surfaces or hidden defaults.

## Delivered

- Added `scripts/local/mysql/init/21-phase-14-progress-and-settings.sql` to persist carryover settings and verification fixtures for traveler progress and runtime defaults.
- Extended admin system settings with a dedicated carryover settings contract:
  - `translationDefaultLocale`
  - `translationEnginePriority`
  - `mediaUploadDefaultPolicyCode`
  - `mapZoomDefaultMinScale`
  - `mapZoomDefaultMaxScale`
  - `indoorZoomDefaultMinScale`
  - `indoorZoomDefaultMaxScale`
- Added `GET/PUT /api/admin/v1/system/carryover-settings` in `AdminSystemManagementController` and `AdminSystemManagementServiceImpl`.
- Extended `AdminUserDetailResponse` and `AdminUserServiceImpl` so user detail now exposes:
  - `cityProgress`
  - `subMapProgress`
  - `collectibleProgress`
  - `badgeProgress`
  - `rewardProgress`
  - `recentCheckIns`
  - `recentTriggerLogs`
- Rebuilt the user detail drawer in `packages/admin/aoxiaoyou-admin-ui/src/pages/UserManagement/index.tsx` to render the new progress snapshots and live activity traces.
- Fixed the live runtime bug in `AdminUserServiceImpl` where empty POI ID lists could generate invalid MyBatis SQL during trigger-log enrichment.

## Verification

- `mvn -q -DskipTests compile` in `packages/admin/aoxiaoyou-admin-backend`
- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-14-carryover.ps1`

## Notes

- The user-detail runtime fix was validated against the real `GET /api/admin/v1/users/{id}` path on port `8081`.
- Progress summaries are now derived from current published content counts plus recorded traveler actions, rather than placeholder UI counters.
