---
phase: 11-story-activity-and-collection-composition
plan: 02
subsystem: activity
requirements-completed: [ACT-01]
completed: 2026-04-15
---

# Phase 11 Wave 2 Summary

## Outcome

Wave 2 activity work is live across admin authoring, public activity reads, and mini-program discover consumption, and now has a dedicated smoke path.

## Delivered

- Added the canonical activity migration in `scripts/local/mysql/init/11-phase-11-activity-authoring.sql`.
- Extended the activity domain with `activityType`, localized title/summary/description/HTML fields, organizer metadata, signup windows, publish windows, pinning, and asset references.
- Delivered full admin CRUD under `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminOperationsController.java`.
- Rebuilt `packages/admin/aoxiaoyou-admin-ui/src/pages/OperationsManagement/index.tsx` into a Traditional Chinese authoring surface for activities and global tasks.
- Added the public activity catalog at `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ActivityController.java`.
- Aligned discover-card sourcing in `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java` so activity spotlight cards come from published activity data.
- Kept mini-program consumers aligned through `packages/client/src/services/api.ts` and `packages/client/src/services/gameService.ts`.
- Added `scripts/local/smoke-phase-11-composition.ps1` coverage for admin activity roundtrips and public activity/discover reads.

## Verification

- `mvn -DskipTests compile` in `packages/admin/aoxiaoyou-admin-backend`
- `mvn -DskipTests compile` in `packages/server`
- `npm run build` in `packages/admin/aoxiaoyou-admin-ui`
- `npm run build:weapp` in `packages/client`
- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-11-composition.ps1`

## Notes

- The verified admin activity roundtrip used the seeded `macau_fortress_night_walk` record.
- Public discover verification confirmed an activity card is emitted from `/api/v1/discover/cards` alongside the dedicated `/api/v1/activities` catalog.
