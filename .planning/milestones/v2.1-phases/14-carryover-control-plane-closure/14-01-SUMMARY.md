---
phase: 14-carryover-control-plane-closure
plan: 01
subsystem: collection-carryover
requirements-completed: [CARRY-01]
completed: 2026-04-15
---

# Phase 14 Wave 1 Summary

## Outcome

Collection, badge, and reward carryover authoring is now closed end to end across MySQL schema, admin contracts, admin UI, public payloads, and mini-program parsing.

## Delivered

- Added the carryover schema migration in `scripts/local/mysql/init/19-phase-14-collection-carryover.sql` for preset/config fields and example-content fields across `collectibles`, `badges`, and `rewards`.
- Added the showcase seed in `scripts/local/mysql/init/20-phase-14-collection-showcase-seed.sql` with canonical city, sub-map, storyline, indoor-building, indoor-floor, and attachment relations.
- Extended admin collectible, badge, and reward DTOs, services, and controller flows so indoor bindings and preset-first carryover fields round-trip through `packages/admin/aoxiaoyou-admin-backend`.
- Rebuilt the admin collection authoring surfaces in `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/*.tsx` so operators can inspect and edit the carryover bindings and preset fields.
- Extended public reward, collectible, and badge responses in `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java` with:
  - `popupPresetCode`
  - `displayPresetCode`
  - `triggerPresetCode`
  - `exampleContent`
  - `relatedIndoorBuildings`
  - `relatedIndoorFloors`
  - `attachmentAssetUrls`
- Kept mini-program runtime parsing aligned through `packages/client/src/services/api.ts`, `packages/client/src/services/gameService.ts`, and `packages/client/src/types/game.ts`.

## Verification

- `mvn -q -DskipTests compile` in `packages/admin/aoxiaoyou-admin-backend`
- `mvn -q -DskipTests compile` in `packages/server`
- `npm run build` in `packages/admin/aoxiaoyou-admin-ui`
- `npm run build:weapp` in `packages/client`
- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-14-carryover.ps1`

## Notes

- The verified showcase chain uses `collectible_lisboeta_night_pass`, `badge_lisboeta_pathfinder`, and `reward_lisboeta_secret_cut`.
- The public payload contract is additive; existing client reward and collection handling was preserved while the new carryover fields were surfaced.
