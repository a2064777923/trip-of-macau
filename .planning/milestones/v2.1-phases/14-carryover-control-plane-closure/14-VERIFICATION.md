---
phase: 14-carryover-control-plane-closure
status: passed
verified: 2026-04-18
requirements_verified: [CARRY-01, CARRY-02, CARRY-03]
---

# Phase 14 Verification

## Goal

Finish the accepted `v2.0` carryover gaps around collections and rewards, traveler progress, operations and testing visibility, system settings ownership, and milestone-grade verification.

## Outcome

Passed. The original Phase 14 carryover gap is now formally closed.

Phase 20 backfilled the missing verification chain, reran the canonical smoke harness on the current live stack, refreshed the old UAT trail into readable current evidence, and closed the one remaining admin regression question around long-session collection authoring stability.

## Evidence

### Automated Checks

- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-14-carryover.ps1`
- `mvn -q "-Dtest=AdminCollectibleServiceImplTest,AdminUserServiceImplTest" test` in `packages/admin/aoxiaoyou-admin-backend`
- `mvn -q "-Dtest=PublicCatalogServiceImplCarryoverTest" test` in `packages/server`
- `npm run build` in `packages/admin/aoxiaoyou-admin-ui`
- `npm run build:weapp` in `packages/client` via the canonical carryover smoke

### Live Stack Checks

- Verified admin health on `http://127.0.0.1:8081/api/v1/health` and public health on `http://127.0.0.1:8080/api/v1/health` on 2026-04-18.
- Headless admin verification logged into `http://127.0.0.1:5173/admin/#/` and completed two edit/save cycles across:
  - `collectible_lisboeta_night_pass`
  - `badge_lisboeta_pathfinder`
  - `reward_lisboeta_secret_cut`
- Browser evidence was written to:
  - `packages/admin/aoxiaoyou-admin-ui/test-results/phase20-carryover-browser-check.json`
  - `packages/admin/aoxiaoyou-admin-ui/test-results/phase20-collectibles-check.png`
  - `packages/admin/aoxiaoyou-admin-ui/test-results/phase20-badges-check.png`
  - `packages/admin/aoxiaoyou-admin-ui/test-results/phase20-rewards-check.png`

## Requirement Coverage

### CARRY-01

Verified by:

- refreshed live admin proof for collectible, badge, and reward edit/save durability across repeated route switches;
- the Phase 20 route fix that reattached `/collection/rewards` to `RewardManagement` in `packages/admin/aoxiaoyou-admin-ui/src/App.tsx`;
- canonical smoke assertions that admin save/readback preserves storyline, indoor building, indoor floor, and attachment bindings for the showcase entities; and
- targeted machine checks in `AdminCollectibleServiceImplTest` and `PublicCatalogServiceImplCarryoverTest`.

Verdict: satisfied.

### CARRY-02

Verified by:

- the current admin user detail contract exposing `cityProgress`, `subMapProgress`, `collectibleProgress`, `badgeProgress`, `rewardProgress`, `recentCheckIns`, and `recentTriggerLogs`;
- smoke assertions on exact fixture counts for the Phase 14 traveler user; and
- smoke read/write verification of the carryover system settings contract.

Verdict: satisfied.

### CARRY-03

Verified by:

- deterministic SQL reseeding through `19-phase-14-collection-carryover.sql`, `20-phase-14-collection-showcase-seed.sql`, `21-phase-14-progress-and-settings.sql`, and `22-phase-14-carryover-verification-seed.sql`;
- the strengthened `smoke-phase-14-carryover.ps1` harness, which now uses canonical upsert payloads and UTF-8 JSON submission on Windows;
- the refreshed `14-UAT.md`; and
- this verification artifact plus Phase 20 closeout artifacts.

Verdict: satisfied.

## Residual Risks

- Admin production build still emits a large bundle-size warning from Vite. This is non-blocking for the carryover domain but remains a frontend optimization task.
- The broader milestone still has open verification work in Phases 21 and 22. Those remaining gaps do not reopen the carryover closure.

## Final Verdict

`CARRY-01`, `CARRY-02`, and `CARRY-03` are now closed.

The original Phase 14 carryover verification gap identified by the `v2.1` milestone audit is resolved.
