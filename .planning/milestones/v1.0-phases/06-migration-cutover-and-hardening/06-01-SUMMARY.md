---
phase: 06-migration-cutover-and-hardening
plan: 01
subsystem: data-migration
tags: [mysql, seed, mock-migration, runtime-settings]
requires: []
provides:
  - deterministic Phase 6 seed/import flow for the former mini-program mock dataset
  - canonical MySQL/runtime-setting representation for live public/admin reads
  - repeatable local seed reapply entrypoint for smoke verification
affects: [06-02, 06-03]
tech-stack:
  added: []
  patterns: [deterministic sql seed, PowerShell seed runner, canonical mock-to-runtime normalization]
key-files:
  created:
    - scripts/local/mysql/init/06-live-backend-mock-migration.sql
    - scripts/local/apply-phase-06-mock-seed.ps1
  modified:
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/CatalogFoundationServiceImpl.java
key-decisions:
  - "Seed the former client mock dataset into canonical MySQL tables and runtime settings instead of preserving a second mock-only representation."
  - "Normalize mock-only shape mismatches during import so the public/admin contract stays coherent after cutover."
requirements-completed: [DATA-02]
duration: brownfield pass
completed: 2026-04-12
---

# Phase 6: Plan 01 Summary

**The former mini-program mock dataset now lives as a repeatable canonical MySQL seed instead of an app-only fallback.**

## Accomplishments

- Added `06-live-backend-mock-migration.sql` to upsert the live mini-program content model into canonical tables and runtime settings with deterministic identifiers and published-state defaults.
- Added `apply-phase-06-mock-seed.ps1` so the Phase 6 dataset can be reapplied locally through `mysql --default-character-set=utf8mb4` without manual SQL editing.
- Closed the remaining seed-to-runtime mapping gaps in the public backend so seeded discover cards and travel recommendation profiles resolve through the same runtime-setting contract the admin system manages.
- Established a verified seeded catalog baseline that the live public health endpoint now reports as `cities=3`, `storylines=4`, `storyChapters=14`, with discover curated cards and travel recommendation profiles configured.

## Verification

- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/apply-phase-06-mock-seed.ps1`
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-06-live-cutover.ps1`
- `GET http://127.0.0.1:8080/api/v1/health` after seed reapply confirmed the seeded published-catalog counts and runtime-setting readiness flags

## Notes

- The Phase 6 seed is intentionally idempotent so local verification can safely reapply it before each smoke run.
- Canonical live content now comes from MySQL tables plus admin-managed runtime settings, not `gameMock.ts` or one-off SQL edits.

---
*Phase: 06-migration-cutover-and-hardening*
*Completed: 2026-04-12*
