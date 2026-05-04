---
phase: 43
plan: 04
subsystem: traveler-ops-verification
status: completed
tags:
  - smoke
  - admin-backend
  - public-backend
  - traveler-progress
  - reward-consistency
key-files:
  created:
    - scripts/local/smoke-phase-43-traveler-ops.ps1
    - scripts/local/fixtures/phase-43-safe-annotation.json
    - scripts/local/fixtures/phase-43-resend-reward.json
    - .planning/phases/43-traveler-progress-and-reward-operations/43-UAT.md
    - .planning/phases/43-traveler-progress-and-reward-operations/43-VERIFICATION.md
  modified:
    - scripts/local/apply-phase-43-traveler-ops-migration.ps1
    - scripts/local/mysql/init/52-phase-43-traveler-progress-reward-ops.sql
    - .planning/STATE.md
requirements-completed:
  - OPS-01
  - OPS-02
  - OPS-03
  - OPS-04
duration: 55 min
completed: 2026-05-04
---

# Phase 43 Plan 04: Smoke Verification and Closure Summary

Built repeatable Phase 43 smoke verification for admin traveler support operations, public/admin reward consistency, and truthful UAT evidence.

## Performance

- **Duration:** 55 min
- **Started:** 2026-05-04T19:08:00+08:00
- **Completed:** 2026-05-04T20:02:43+08:00
- **Tasks:** 2
- **Files modified:** 8

## Accomplishments

- Added `scripts/local/smoke-phase-43-traveler-ops.ps1` with local URL safety, secret redaction, admin/public login, complete OPS-02 timeline filter checks, support operation preview/apply, audit verification, reward resend, and public/admin consistency checks.
- Added UTF-8 fixture payloads for safe annotation and reward resend so Traditional Chinese request text is not embedded as inline PowerShell literals.
- Wrote `43-UAT.md` from a real standard smoke run with `Final outcome: PASS`.
- Wrote `43-VERIFICATION.md` mapping OPS-01 through OPS-04 to commands and concrete evidence.

## Verification

- PASS: `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml`
- PASS: `mvn -q -DskipTests compile -f packages/server/pom.xml`
- PASS: `cd packages/admin/aoxiaoyou-admin-ui; npm run type-check`
- PASS: `cd packages/admin/aoxiaoyou-admin-ui; npm run build`
- PASS: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-43-traveler-ops.ps1 -Quick`
- PASS: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-43-traveler-ops.ps1 -ApplySafeAnnotation`

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Phase 43 grant table lacked public soft-delete column**

- **Found during:** Task 1 smoke execution.
- **Issue:** Public backend `UserGameRewardGrant` extends a base entity with `deleted`; the local migrated `user_game_reward_grants` table did not have that column, causing public dev-bypass login/state build to fail.
- **Fix:** Added `deleted TINYINT NOT NULL DEFAULT 0` to the create-table DDL, added an idempotent `ALTER TABLE` for existing local volumes, and extended migration verification.
- **Files modified:** `scripts/local/mysql/init/52-phase-43-traveler-progress-reward-ops.sql`, `scripts/local/apply-phase-43-traveler-ops-migration.ps1`.
- **Verification:** Migration apply and `-VerifyOnly` both passed; public dev-bypass and public reward reads passed.

**2. [Rule 1 - Bug] PowerShell 5.1 array/count semantics caused false smoke failures**

- **Found during:** Task 1 smoke execution.
- **Issue:** Empty or single-row API arrays were sometimes treated as `$null` or scalar objects under StrictMode, causing `.Count` and hashtable property access failures.
- **Fix:** Added `Get-Count` and `Get-MapValue` helpers, converted reward consistency availability to explicit loaded flags, and made timeline filter validation StrictMode-safe.
- **Files modified:** `scripts/local/smoke-phase-43-traveler-ops.ps1`.
- **Verification:** Quick and standard smoke both passed.

**3. [Rule 1 - Bug] Standard smoke misread redirected subprocess output and reward resend result shape**

- **Found during:** Task 1 standard smoke execution.
- **Issue:** `Invoke-CheckedCommand` read `StartInfo.RedirectStandardOutput` as a Boolean instead of the temporary file path, and the resend assertion only accepted literal `resent/already_present` strings even when the backend returned a valid grant row summary.
- **Fix:** Stored stdout/stderr temp paths explicitly and accepted resend success when the result summary shows `writtenStateRows`, `grantRowId`, or `alreadyGranted`.
- **Files modified:** `scripts/local/smoke-phase-43-traveler-ops.ps1`.
- **Verification:** Standard `-ApplySafeAnnotation` smoke passed with idempotent `already_present` reward resend and public/admin consistency.

**Total deviations:** 3 auto-fixed (2 bugs, 1 blocker).
**Impact on plan:** All fixes were required to make the Phase 43 acceptance evidence truthful against the real local services. No product scope was added.

## Issues Encountered

- The local standard smoke is stateful by design: after the first successful reward resend, later runs exercise the idempotent `already_present` path. This is accepted and documented in the UAT evidence.
- Manual WeChat DevTools/device UAT remains pending and is explicitly not counted as Phase 43 evidence.

## User Setup Required

None - the smoke script uses local services on `127.0.0.1:8081` and `127.0.0.1:8080`, with optional env overrides.

## Next Phase Readiness

Phase 43 is ready for phase-level closure. Phase 44 should focus on management-system IA polish, browser/admin acceptance, and final v3.2 release evidence.

