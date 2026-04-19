---
phase: 11-story-activity-and-collection-composition
plan: 01
subsystem: story
requirements-completed: [STORY-01, STORY-02, STORY-03]
completed: 2026-04-15
---

# Phase 11 Wave 1 Summary

## Outcome

Wave 1 story-composition work is present in the repository and is now backed by repeatable verification instead of plan-only intent.

## Delivered

- Added the canonical story-composition migration in `scripts/local/mysql/init/10-phase-11-story-composition.sql`.
- Extended storyline contracts to support `cityBindings`, `subMapBindings`, and ordered `attachmentAssetIds` instead of a single-city-only authoring model.
- Extended chapter contracts with `anchorType`, `anchorTargetId`, `anchorTargetCode`, `prerequisiteJson`, `completionJson`, and `rewardJson`.
- Split storyline metadata editing from dedicated chapter composition by routing chapter authoring through `packages/admin/aoxiaoyou-admin-ui/src/pages/StoryChapterManagement/index.tsx`.
- Aligned the public storyline detail contract so chapter anchor and reward metadata flow through `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/StoryLineController.java`.
- Aligned mini-program story consumers so `packages/client/src/services/api.ts` and `packages/client/src/services/gameService.ts` accept binding arrays plus anchor metadata.
- Added `scripts/local/smoke-phase-11-composition.ps1` to prove storyline detail, chapter composition, and public storyline reads end to end.

## Verification

- `mvn -DskipTests compile` in `packages/admin/aoxiaoyou-admin-backend`
- `mvn -DskipTests compile` in `packages/server`
- `npm run build` in `packages/admin/aoxiaoyou-admin-ui`
- `npm run build:weapp` in `packages/client`
- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-11-composition.ps1`

## Notes

- The Phase 11 smoke validates admin roundtrips using the seeded `macau_fire_route` storyline and chapter `311001`.
- PowerShell direct JSON PUTs produced an encoding-sensitive false negative on storyline updates, so the smoke uses UTF-8 temp files with `curl --data-binary` for stable verification.
