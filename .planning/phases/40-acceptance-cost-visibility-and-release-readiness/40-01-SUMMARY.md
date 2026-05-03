---
phase: 40-acceptance-cost-visibility-and-release-readiness
plan: 40-01
subsystem: release-smoke
tags: [smoke, release-readiness, acceptance, gsd]

requires:
  - phase: 36
    provides: material production and COS smoke
  - phase: 37
    provides: material QA smoke
  - phase: 38
    provides: public runtime asset smoke
  - phase: 39
    provides: mini-program story-mode smoke
provides:
  - consolidated Phase 40 release-readiness smoke wrapper
  - client npm smoke alias
  - initial Phase 40 smoke report
affects: [phase-40, acc-01, acc-04]

tech-stack:
  added: []
  patterns:
    - PowerShell UTF-8 smoke wrapper
    - env-gated live checks
    - sanitized Markdown smoke report

key-files:
  created:
    - scripts/local/smoke-phase-40-release-readiness.ps1
    - .planning/phases/40-acceptance-cost-visibility-and-release-readiness/40-SMOKE-REPORT.md
  modified:
    - packages/client/package.json

requirements-completed: []

duration: 25 min
completed: 2026-05-03
---

# Phase 40 Plan 01 Summary

## Accomplishments

- Added `scripts/local/smoke-phase-40-release-readiness.ps1`, which composes Phase 36, 37, 38, and 39 smoke evidence into one sanitized Phase 40 report.
- Added `smoke:phase40:release-readiness` to `packages/client/package.json`.
- Generated `40-SMOKE-REPORT.md` in quick mode with pass/skip states and no raw logs or secret-bearing values.

## Verification

| Command | Result |
| --- | --- |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-40-release-readiness.ps1 -Quick` | Passed |
| `cd packages/client; npm run smoke:phase40:release-readiness -- -Quick` | Passed |
| `Select-String 40-SMOKE-REPORT.md -Pattern 'Bearer ','sk-','COS_SECRET','providerApiKey'` | No matches |

## Evidence Notes

- Quick smoke passed with expected skips for live COS/provider checks, admin QA mutation checks, and build-heavy checks.
- Admin AI observability API was reachable locally and returned recent job/log counts.
- Build checks remain opt-in through `PHASE40_INCLUDE_BUILDS=true` or by omitting `-Quick`.

## Deviations

- The wrapper originally scanned child command output too aggressively and treated benign child-script text containing `Bearer` as a secret leak. This was corrected by sanitizing report evidence and not writing child raw output.

## Next

Proceed to `40-02` for admin monitoring and cost visibility UI/API polish.
