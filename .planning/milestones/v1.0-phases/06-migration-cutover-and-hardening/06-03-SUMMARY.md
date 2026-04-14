---
phase: 06-migration-cutover-and-hardening
plan: 03
subsystem: client-live-cutover
tags: [mini-program, live-runtime, smoke, end-to-end]
requires: [06-01, 06-02]
provides:
  - live-first mini-program runtime orchestration without normal catalog mock fallback
  - repeatable end-to-end smoke coverage across admin, public, MySQL, and COS
  - verified public traveler read/write behavior against the seeded live stack
affects: []
tech-stack:
  added: []
  patterns: [live-first client orchestration, write-through runtime verification, cleanup-safe smoke harness]
key-files:
  created:
    - scripts/local/smoke-phase-06-live-cutover.ps1
  modified:
    - packages/client/src/services/api.ts
    - packages/client/src/services/gameService.ts
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/HealthController.java
  removed:
    - packages/client/src/services/gameMock.ts
key-decisions:
  - "Treat seeded public/admin data as the default runtime path and keep mock content out of normal live execution."
  - "Make the Phase 6 smoke harness restore temporary runtime mutations and uploaded assets so the verification loop stays repeatable."
requirements-completed: [OPS-02]
duration: brownfield pass
completed: 2026-04-12
---

# Phase 6: Plan 03 Summary

**The mini-program cutover is now proven end to end against the real backend stack.**

## Accomplishments

- Removed the remaining normal-runtime catalog dependency on `gameMock.ts` and kept `gameService.ts` focused on live public content refresh and real traveler state writes.
- Added `smoke-phase-06-live-cutover.ps1` to exercise the full chain: seed reapply, admin/public health, admin login, dashboard assertions, COS upload, admin runtime write-through to public discover cards, public user login/check-ins/reward redemption, MySQL assertions, and admin traveler inspection.
- Re-ran the full Phase 6 smoke successfully with `userId=7`, `openId=phase6-1776009184993-user`, `runtimeSettingId=28`, and `assetId=7`.
- Confirmed the smoke cleanup path restored the mutated runtime setting and removed the uploaded smoke asset from `content_assets`.

## Verification

- `mvn -q -DskipTests compile` in `packages/server`
- `mvn -q -DskipTests compile` in `packages/admin/aoxiaoyou-admin-backend`
- `npm run build` in `packages/admin/aoxiaoyou-admin-ui`
- `npm run build:weapp` in `packages/client`
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-06-live-cutover.ps1`

## Notes

- The smoke-created traveler remains in MySQL as durable proof that the public write path is live and that the admin traveler views can inspect the result.

---
*Phase: 06-migration-cutover-and-hardening*
*Completed: 2026-04-12*
