---
phase: 14-carryover-control-plane-closure
plan: 03
subsystem: verification
requirements-completed: [CARRY-03]
completed: 2026-04-15
---

# Phase 14 Wave 3 Summary

## Outcome

Phase 14 now has deterministic seed data, a runnable cross-surface smoke harness, targeted backend tests, and a resumable UAT scaffold instead of relying on conversation memory.

## Delivered

- Added deterministic verification seed support in:
  - `scripts/local/mysql/init/22-phase-14-carryover-verification-seed.sql`
  - `scripts/local/mysql/init/19-phase-14-collection-carryover.sql`
  - `scripts/local/mysql/init/20-phase-14-collection-showcase-seed.sql`
- Finished `scripts/local/smoke-phase-14-carryover.ps1` so it now:
  - reapplies the Phase 14 schema and showcase seed
  - validates admin health on `8081`
  - validates public health on `8080`
  - checks carryover system settings
  - checks traveler progress detail
  - checks admin collectible, badge, and reward carryover surfaces
  - checks public collectible, badge, and reward payloads
  - runs `npm run build:weapp`
- Added targeted backend tests:
  - `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminUserServiceImplTest.java`
  - `packages/server/src/test/java/com/aoxiaoyou/tripofmacau/PublicCatalogServiceImplCarryoverTest.java`
- Repaired the pre-existing `packages/server/src/test/java/com/aoxiaoyou/tripofmacau/CatalogFoundationServiceImplTest.java` constructor drift so server tests compile again against the current service signature.

## Verification

- `mvn -q -Dtest=AdminUserServiceImplTest test` in `packages/admin/aoxiaoyou-admin-backend`
- `mvn -q "-Dtest=CatalogFoundationServiceImplTest,PublicCatalogServiceImplCarryoverTest" test` in `packages/server`
- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-14-carryover.ps1`

## Notes

- The verified local runtime used admin on `http://127.0.0.1:8081` and public API on `http://127.0.0.1:8080`.
- Both services were restarted on 2026-04-15 so smoke exercised the current Phase 14 code instead of stale JVMs.
