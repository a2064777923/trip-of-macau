---
status: complete
phase: 14-carryover-control-plane-closure
source:
  - 14-01-SUMMARY.md
  - 14-02-SUMMARY.md
  - 14-03-SUMMARY.md
  - ../20-carryover-verification-and-stability-closure/20-01-SUMMARY.md
  - ../20-carryover-verification-and-stability-closure/20-02-SUMMARY.md
  - ../20-carryover-verification-and-stability-closure/20-03-SUMMARY.md
started: 2026-04-15T15:51:47.2038582+08:00
updated: 2026-04-18T11:14:52.0604479+08:00
---

## Current Test

[testing complete]

## Tests

### 1. Collection carryover authoring
expected: Open `/admin` and inspect the Phase 14 collectible, badge, and reward showcase records. Each form should show editable bindings for city, sub-map, storyline, indoor building, and indoor floor, plus preset-driven popup/display/trigger fields and example content. Saving a small edit should persist without dropping existing bindings or reverting to placeholder fields.
result: pass
evidence: 2026-04-18 headless admin verification completed two cross-page edit/save cycles for `collectible_lisboeta_night_pass`, `badge_lisboeta_pathfinder`, and `reward_lisboeta_secret_cut`, recorded in `packages/admin/aoxiaoyou-admin-ui/test-results/phase20-carryover-browser-check.json`. The same pass exposed that `/collection/rewards` still pointed to a placeholder; Phase 20 reattached that route to `RewardManagement` in `packages/admin/aoxiaoyou-admin-ui/src/App.tsx` and the second verification cycle passed.

### 2. Reward and collectible runtime alignment
expected: Open the mini-program or inspect its live runtime payloads. The public API-backed reward and collectible data should include the carryover preset fields, indoor bindings, and example content without breaking existing collection or reward displays.
result: pass
evidence: `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-14-carryover.ps1` passed on 2026-04-18 and asserted the public payloads for `collectible_lisboeta_night_pass`, `badge_lisboeta_pathfinder`, and `reward_lisboeta_secret_cut`. `mvn -q "-Dtest=PublicCatalogServiceImplCarryoverTest" test` in `packages/server` also passed.

### 3. Traveler progress visibility
expected: In `/admin` user management, open the Phase 14 traveler fixture. The detail drawer should show city progress, sub-map progress, collectible progress, badge progress, reward progress, recent check-ins, and recent trigger logs with real values instead of placeholder counters.
result: pass
evidence: the 2026-04-18 smoke rerun verified the fixture user detail on admin `8081` with exact counts: city `1/4`, sub-map `1/3`, collectibles `4/4`, badges `1/3`, rewards `0/6`, recent check-ins `3`, recent trigger logs `3`. `mvn -q "-Dtest=AdminUserServiceImplTest" test` in `packages/admin/aoxiaoyou-admin-backend` also passed.

### 4. System settings ownership
expected: In `/admin` system management, open the carryover settings section. It should load and save translation default locale, translation engine priority, media upload default policy, and map/indoor zoom defaults through the dedicated settings surface instead of hidden defaults.
result: pass
evidence: the 2026-04-18 smoke rerun verified and round-tripped `translationDefaultLocale=zh-Hant`, `translationEnginePriority=google,bing,tencent`, `mediaUploadDefaultPolicyCode=compressed`, `mapZoomDefaultMinScale=8`, `mapZoomDefaultMaxScale=18`, `indoorZoomDefaultMinScale=20`, and `indoorZoomDefaultMaxScale=0.5` through `GET/PUT /api/admin/v1/system/carryover-settings`.

### 5. Smoke rerun readiness
expected: After restarting services if needed, `scripts/local/smoke-phase-14-carryover.ps1` should rerun successfully on a clean local state and confirm admin `8081`, public `8080`, carryover settings, traveler progress, and carryover catalog payloads are all live.
result: pass
evidence: the canonical smoke reran cleanly on 2026-04-18, reseeded the Phase 14 SQL fixtures, verified admin `8081`, public `8080`, admin save/readback, public carryover payloads, and finished with a successful `npm run build:weapp`.

## Summary

total: 5
passed: 5
issues: 0
pending: 0
skipped: 0
blocked: 0

## Superseded Issue

- The original Phase 14 UAT failure on collection authoring freeze is now superseded by fresh evidence:
  - the admin collection pages survived repeated edit/save/navigation cycles on the live stack;
  - the reward route placeholder drift was fixed during the closure pass; and
  - the same showcase entities still round-tripped through admin and public APIs after the rerun.

## Gaps

[]
