---
phase: 03-public-read-apis-cutover
plan: 03
status: completed
completed: 2026-04-12
---

# Phase 3 Plan 03 Summary

Rewired the mini-program read layer from direct mock arrays to real public API reads while preserving the local gameplay/check-in write simulation for the later Phase 4 write cutover.

## Delivered

- Replaced the old mini-program API bindings with live public endpoint bindings in `packages/client/src/services/api.ts`.
- Added `refreshPublicContent()` and live DTO-to-view-model mapping in `packages/client/src/services/gameService.ts`.
- Updated home, map, story, tips, tip detail, tip notifications, discover, rewards, stamps, and profile pages to refresh public content asynchronously before rendering live read data.
- Switched `packages/client/config/dev.js` to `USE_MOCK: 'false'`.

## Outcome

`npm run build:weapp` completed successfully, producing a real mini-program build that targets the Phase 3 public backend APIs instead of the mock read path.
