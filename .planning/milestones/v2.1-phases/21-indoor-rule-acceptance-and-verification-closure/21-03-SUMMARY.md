---
phase: 21-indoor-rule-acceptance-and-verification-closure
plan: 03
subsystem: runtime-verification
requirements-completed: []
completed: 2026-04-19
---

# Phase 21 Plan 03 Summary

## Outcome

The public indoor runtime chain has fresh live proof again, but the overall phase remains partial because WeChat DevTools acceptance was not executed in this terminal session.

## Delivered

- Re-ran the live runtime smoke through the Phase 21 closure wrapper on `8080` and `8081`.
- Reconfirmed the witness-set runtime behavior:
  - `night-market-schedule-overlay` supported
  - `zipcity-guiding-path` supported and returns `path_motion`
  - `royal-palace-dwell-reveal` blocked anonymously with `auth_required`
- Rebuilt the mini-program WeChat target and refreshed the formal Phase 21 verification artifact.

## Verification

- `mvn -q "-Dtest=PublicIndoorRuntimeServiceTest,PublicIndoorRuntimeInteractionServiceTest" test`
- `npm.cmd run build:weapp`
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-21-indoor-closure.ps1 -AdminBaseUrl http://127.0.0.1:8081 -PublicBaseUrl http://127.0.0.1:8080 -Username admin -Password admin123 -SkipAdminTests -SkipAdminBuild -SkipServerTests -SkipClientBuild`

## Closure State

- `RULE-03`: PARTIAL

Reason:
- API/runtime/build evidence passed.
- Manual WeChat DevTools acceptance for visual floor switching, popup/bubble rendering, auth-wall routing, and authenticated retry is still pending.
