---
phase: 01-canonical-backend-foundation
plan: 04
subsystem: infra
tags: [local-runtime, smoke-tests, spring-profiles, docker]
requires: [phase-01-schema-and-public-foundation]
provides:
  - aligned local profile wiring for public/admin backends
  - one-command local public backend helper
  - repeatable smoke harness for MySQL, MongoDB, public health, and admin health
affects: [phase-02-admin-control-plane-completion, phase-03-public-read-apis-cutover, phase-06-migration-cutover-and-hardening]
tech-stack:
  added: []
  patterns: [local runtime contract documented in README, smoke script reuses host MySQL when 3306 is occupied]
key-files:
  created:
    - packages/server/src/main/resources/application-local.yml
    - scripts/local/start-public-backend.cmd
    - scripts/local/smoke-phase-01-foundation.ps1
  modified:
    - docker-compose.local.yml
    - packages/admin/aoxiaoyou-admin-backend/src/main/resources/application-local.yml
    - scripts/local/start-admin-backend.cmd
    - README.md
key-decisions:
  - "Use a dedicated public local profile with Redis health disabled so Phase 1 verification stays focused on the real dependencies that exist locally."
  - "Treat a pre-existing host MySQL on port 3306 as a supported local verification path instead of assuming compose owns the port."
patterns-established:
  - "Local backend verification should be scriptable end-to-end, not a manual checklist."
  - "Smoke tooling should tolerate brownfield developer environments where compose services do not own every default port."
requirements-completed: [OPS-01]
duration: 13min
completed: 2026-04-12
---

# Phase 1: Plan 04 Summary

**Aligned local public/admin backend profiles, repeatable start helpers, and a smoke harness that verifies real health endpoints against local datastores**

## Performance

- **Duration:** 13 min
- **Started:** 2026-04-12T03:46:00Z
- **Completed:** 2026-04-12T03:59:00Z
- **Tasks:** 3
- **Files modified:** 7

## Accomplishments
- Added a dedicated `packages/server` local profile that points at local MySQL and disables Redis health as a Phase 1 blocker.
- Added `scripts/local/start-public-backend.cmd` to mirror the existing admin helper and lock the public backend onto JDK 17 and `SPRING_PROFILES_ACTIVE=local`.
- Added `scripts/local/smoke-phase-01-foundation.ps1` and README instructions that verify real MySQL/Mongo/public/admin health behavior.

## Task Commits

Atomic task commits were intentionally skipped because the repository already contained unrelated in-progress user changes and Phase 1 was executed as a single working-tree pass.

## Files Created/Modified

- `packages/server/src/main/resources/application-local.yml` - Local public-backend datasource/profile contract with Redis health disabled.
- `packages/admin/aoxiaoyou-admin-backend/src/main/resources/application-local.yml` - MongoDB URI aligned to `root:root@127.0.0.1:27017/... ?authSource=admin`.
- `docker-compose.local.yml` - MySQL healthcheck aligned to the actual root password used locally.
- `scripts/local/start-public-backend.cmd` - One-command local public backend launcher.
- `scripts/local/start-admin-backend.cmd` - Admin helper aligned to the same real MySQL and Mongo credentials.
- `scripts/local/smoke-phase-01-foundation.ps1` - Deterministic smoke harness for local datastores and both backend health surfaces.
- `README.md` - Documented Phase 1 local backend foundation flow.

## Decisions Made

- The public local profile should disable Redis health instead of pretending Redis exists locally, because the goal of Phase 1 is reproducible MySQL/Mongo/public/admin foundation verification.
- The smoke script explicitly supports a host-managed MySQL already bound to `3306`, which is common in brownfield local environments and was present in this workspace.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Reused an existing host MySQL instead of forcing compose ownership of port 3306**
- **Found during:** Local smoke verification
- **Issue:** `docker compose -f docker-compose.local.yml up -d mysql` could not bind `3306` because a local `mysqld` was already listening there.
- **Fix:** Updated the smoke harness to detect and reuse a reachable local MySQL on `3306` when `root / Abc123456` can access database `aoxiaoyou`.
- **Files modified:** `scripts/local/smoke-phase-01-foundation.ps1`
- **Verification:** `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-01-foundation.ps1`
- **Committed in:** none (dirty worktree; no atomic commit created)

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** The smoke path became more reliable in real local environments without changing the Phase 1 runtime contract.

## Issues Encountered

- Docker Desktop needed to be running for the Mongo compose container path.
- The compose file emits a non-blocking warning that its `version` attribute is obsolete.

## User Setup Required

None - no external service configuration required.

## Verification

All checks passed.

- `node .codex/get-shit-done/bin/gsd-tools.cjs verify artifacts .planning/phases/01-canonical-backend-foundation/01-04-PLAN.md`
- `node .codex/get-shit-done/bin/gsd-tools.cjs verify key-links .planning/phases/01-canonical-backend-foundation/01-04-PLAN.md`
- `mvn -q -f packages/admin/aoxiaoyou-admin-backend/pom.xml -DskipTests compile` with JDK 17
- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-01-foundation.ps1`

## Next Phase Readiness

- Phase 2 and later backend phases now have a shared, documented, runnable local environment instead of implicit operator knowledge.
- Integration work can be regression-checked quickly with the same smoke harness used during Phase 1 closeout.

---
*Phase: 01-canonical-backend-foundation*
*Completed: 2026-04-12*
