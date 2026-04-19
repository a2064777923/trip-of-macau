# Phase 23 Wave 3 Summary

## Outcome

Wave 3 aligned the split reward domain across indoor governance, public runtime contracts, seeded data, and live stack verification.

- Added public split reward contracts and controllers in `packages/server` for:
  - `GET /api/v1/redeemable-prizes`
  - `GET /api/v1/game-rewards`
  - `GET /api/v1/reward-presentations/{id}`
- Added new public reward-domain entities, mappers, and DTOs for:
  - `redeemable_prizes`
  - `game_rewards`
  - `reward_rules`
  - `reward_rule_bindings`
  - `reward_presentations`
  - `reward_presentation_steps`
- Extended indoor governance linkage in the admin backend so indoor interaction rules can surface linked reward rules and linked rewards.
- Added the phase smoke harness in [smoke-phase-23-reward-domain.ps1](/D:/Archive/trip-of-macau/scripts/local/smoke-phase-23-reward-domain.ps1).

## Live Fixes During Execution

- Fixed a real admin runtime failure in [AdminRewardDomainServiceImpl.java](/D:/Archive/trip-of-macau/packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminRewardDomainServiceImpl.java):
  - guarded empty owner ID collections before `selectBatchIds(...)`
  - removed the `WHERE id IN ()` SQL failure on reward governance overview
- Fixed the Phase 23 schema migration in [23-phase-23-reward-domain.sql](/D:/Archive/trip-of-macau/scripts/local/mysql/init/23-phase-23-reward-domain.sql):
  - added missing `deleted` soft-delete columns to the new reward-domain tables so the public backend can query them through `BaseEntity`
- Fixed MySQL collation drift in [24-phase-23-reward-seed.sql](/D:/Archive/trip-of-macau/scripts/local/mysql/init/24-phase-23-reward-seed.sql):
  - changed `SET NAMES utf8mb4` to `SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci`
  - avoided `Illegal mix of collations` when replaying the seed on the live `aoxiaoyou` database
- Fixed a PowerShell interpolation bug in [smoke-phase-23-reward-domain.ps1](/D:/Archive/trip-of-macau/scripts/local/smoke-phase-23-reward-domain.ps1):
  - changed `$presentationId?locale=...` to `${presentationId}?locale=...`
  - avoided malformed reward-presentation smoke URLs
- Applied the Phase 23 reward schema and seed to the live local MySQL database used by `8081` and `8080`, then restarted both backends from current source for verification.

## Verification

- `mvn -q "-Dtest=AdminRewardDomainServiceImplTest,IndoorRuleGovernanceServiceTest" test`
  - cwd: `D:\Archive\trip-of-macau\packages\admin\aoxiaoyou-admin-backend`
- `mvn -q "-Dtest=CatalogFoundationServiceImplTest,PublicRewardDomainServiceTest,PublicCatalogServiceImplCarryoverTest" test`
  - cwd: `D:\Archive\trip-of-macau\packages\server`
- `powershell -ExecutionPolicy Bypass -File "D:\Archive\trip-of-macau\scripts\local\smoke-phase-23-reward-domain.ps1"`
  - result: `Phase 23 reward domain smoke passed.`
- Live MySQL reward-domain counts after migration and seed:
  - `reward_rules`: 2
  - `reward_presentations`: 10
  - `redeemable_prizes`: 8
  - `game_rewards`: 6

## Notes

- The Phase 23 backend surface is now runnable against the live local stack on `8081` and `8080`, not just through unit tests.
- Some legacy reward names and descriptions still show encoding damage in seeded content copied from earlier data sources; the reward-domain runtime is operational, but content copy cleanup remains a separate concern from this phase.
