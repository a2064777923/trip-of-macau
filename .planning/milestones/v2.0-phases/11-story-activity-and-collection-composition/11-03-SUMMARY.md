---
phase: 11-story-activity-and-collection-composition
plan: 03
subsystem: collection
requirements-completed: [COLL-01]
completed: 2026-04-15
---

# Phase 11 Wave 3 Summary

## Outcome

Wave 3 collection work is present end to end and is now backed by an executable smoke check that proves reward and relation metadata survive both admin and public flows.

## Delivered

- Added the relation-aware collection migration in `scripts/local/mysql/init/12-phase-11-collection-graph.sql`.
- Extended collectibles, badges, and rewards with canonical asset ID fields and storyline/city/sub-map bindings via `content_relation_links`.
- Centralized reward ownership under `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminCollectibleController.java`.
- Added the dedicated admin reward authoring surface at `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/RewardManagement.tsx`.
- Upgraded collectible and badge management pages to expose canonical binding selectors and asset-backed authoring fields.
- Extended public reward, collectible, and badge reads so relation metadata is exposed from `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java` and `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/RewardController.java`.
- Kept mini-program reward and collection consumers aligned through `packages/client/src/services/api.ts` and `packages/client/src/services/gameService.ts`.
- Added `scripts/local/smoke-phase-11-composition.ps1` coverage for reward authoring roundtrips and public reward relation reads.

## Verification

- `mvn -DskipTests compile` in `packages/admin/aoxiaoyou-admin-backend`
- `mvn -DskipTests compile` in `packages/server`
- `npm run build` in `packages/admin/aoxiaoyou-admin-ui`
- `npm run build:weapp` in `packages/client`
- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-11-composition.ps1`

## Notes

- The verified reward roundtrip used the seeded `reward_historic_archive` record.
- Public reward verification confirmed storyline, city, and sub-map relation metadata are returned together with the canonical cover asset URL.
