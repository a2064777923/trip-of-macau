---
phase: 20-carryover-verification-and-stability-closure
plan: 01
subsystem: collection-authoring-stability
requirements-completed: [CARRY-01]
completed: 2026-04-18
---

# Phase 20 Wave 1 Summary

## Outcome

The old carryover collection-authoring freeze is now retired with current live evidence, and the reward authoring page is reachable again from the real admin shell.

## Delivered

- Added targeted carryover regression coverage in `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminCollectibleServiceImplTest.java`.
- Reused the strengthened carryover smoke harness to verify current admin save/readback payload shape instead of replaying raw read models.
- Fixed a live admin route drift in `packages/admin/aoxiaoyou-admin-ui/src/App.tsx`:
  - `/collection/rewards` no longer points to a placeholder
  - `RewardManagement` is now imported and mounted correctly
- Ran a headless admin verification pass that:
  - logged into `/admin`
  - opened the collectible, badge, and reward carryover fixtures
  - completed two no-op save cycles across the three pages
  - confirmed the shell stayed responsive across repeated route switches
- Saved browser evidence under `packages/admin/aoxiaoyou-admin-ui/test-results/`.

## Verification

- `mvn -q "-Dtest=AdminCollectibleServiceImplTest" test` in `packages/admin/aoxiaoyou-admin-backend`
- `npm run build` in `packages/admin/aoxiaoyou-admin-ui`
- 2026-04-18 headless admin verification written to `packages/admin/aoxiaoyou-admin-ui/test-results/phase20-carryover-browser-check.json`
- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-14-carryover.ps1`

## Notes

- The live reward-route defect was discovered during Phase 20 verification, not from stale audit notes. Fixing it here was necessary for honest closure of `CARRY-01`.
