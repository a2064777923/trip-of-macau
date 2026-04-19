---
phase: 20-carryover-verification-and-stability-closure
plan: 02
subsystem: traveler-progress-and-settings-proof
requirements-completed: [CARRY-02]
completed: 2026-04-18
---

# Phase 20 Wave 1 Summary

## Outcome

Traveler progress visibility and carryover settings ownership remain correct on the current live stack and now have fresh proof instead of stale pending checkpoints.

## Delivered

- Reverified the current admin backend contracts for:
  - `cityProgress`
  - `subMapProgress`
  - `collectibleProgress`
  - `badgeProgress`
  - `rewardProgress`
  - `recentCheckIns`
  - `recentTriggerLogs`
- Reverified the dedicated carryover settings contract for:
  - `translationDefaultLocale`
  - `translationEnginePriority`
  - `mediaUploadDefaultPolicyCode`
  - `mapZoomDefaultMinScale`
  - `mapZoomDefaultMaxScale`
  - `indoorZoomDefaultMinScale`
  - `indoorZoomDefaultMaxScale`
- Confirmed that no additional code fix was required in the progress or settings services after rerunning the live-stack checks.
- Reused the existing targeted admin backend proof in `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminUserServiceImplTest.java`.

## Verification

- `mvn -q "-Dtest=AdminUserServiceImplTest" test` in `packages/admin/aoxiaoyou-admin-backend`
- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-14-carryover.ps1`
- Live health checks on:
  - `http://127.0.0.1:8081/api/v1/health`
  - `http://127.0.0.1:8080/api/v1/health`

## Notes

- The smoke rerun locked the current traveler fixture counts to exact values, which makes future regressions easier to spot than the original Phase 14 pending UAT text.
