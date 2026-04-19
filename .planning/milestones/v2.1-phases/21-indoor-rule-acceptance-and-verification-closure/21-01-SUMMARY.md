---
phase: 21-indoor-rule-acceptance-and-verification-closure
plan: 01
subsystem: closure-harness
requirements-completed: []
completed: 2026-04-19
---

# Phase 21 Plan 01 Summary

## Outcome

Phase 21 now has a deterministic indoor closure harness instead of scattered prior-phase smoke entrypoints.

## Delivered

- Normalized the Lisboa `1F` witness preparation path in:
  - `scripts/local/seed-lisboeta-indoor.ps1`
  - `scripts/local/mysql/init/25-phase-15-indoor-rule-showcase-seed.sql`
  - `scripts/local/smoke-phase-15-indoor-authoring.ps1`
  - `scripts/local/smoke-phase-16-indoor-rule-governance.ps1`
  - `scripts/local/smoke-phase-17-indoor-runtime.ps1`
- Fixed `scripts/local/smoke-phase-21-indoor-closure.ps1` so child-stage stdout no longer pollutes the returned `stages` JSON payload.
- Added the Phase 21 UAT and verification artifacts for current closure evidence.

## Verification

- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-21-indoor-closure.ps1 -AdminBaseUrl http://127.0.0.1:8081 -PublicBaseUrl http://127.0.0.1:8080 -Username admin -Password admin123 -SkipAdminTests -SkipAdminBuild -SkipServerTests -SkipClientBuild`

## Notes

- The canonical witness floor remains `lisboeta_macau / 1F / floorId=12`.
- The wrapper is now clean enough to be cited directly in formal verification artifacts.
