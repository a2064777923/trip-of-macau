---
phase: 04-public-progress-and-gameplay-writes
plan: 02
subsystem: api
tags: [controller, smoke, verification]
requires: [04-01]
provides:
  - contract-aligned `/api/v1/user/*` write/read endpoints
  - repeatable Phase 4 smoke harness covering HTTP and MySQL persistence
  - verified local runtime for public user mutations on port 8080
affects: [04-03]
tech-stack:
  added: []
  patterns: [repeatable smoke verification, API-envelope validation, live SQL assertions]
key-files:
  created:
    - scripts/local/smoke-phase-04-user-writes.ps1
  modified:
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/UserController.java
    - packages/server/src/main/resources/application-local.yml
key-decisions:
  - "Phase 4 verification must prove real writes on the running backend, not just controller compilation."
  - "The smoke harness should assert both HTTP response semantics and direct MySQL persistence."
requirements-completed: [PUB-03]
duration: brownfield pass
completed: 2026-04-12
---

# Phase 4: Plan 02 Summary

**The public user controller surface is live and backed by a repeatable local smoke script.**

## Accomplishments

- Exposed the Phase 4 controller surface for login, state/profile/progress reads, current-city updates, preferences, check-ins, and reward redemption under `/api/v1/user`.
- Added `scripts/local/smoke-phase-04-user-writes.ps1` to run the full login/bootstrap -> preferences -> current city -> check-in -> reward redemption chain.
- The smoke harness now waits for `/actuator/health`, exercises the real HTTP endpoints on `8080`, and asserts resulting MySQL rows plus reward inventory deltas.
- Replaced the stale `8080` public-backend process with the latest build so the final verification reflects the actual current code, not an older runtime.

## Verification

- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-04-user-writes.ps1`
- Manual live smoke during development against `http://127.0.0.1:18080` and final confirmation against `http://127.0.0.1:8080`

## Notes

- `gsd-tools verify key-links` is not applicable for Phase 4 plans because the plan frontmatter does not declare `must_haves.key_links`.
- During execution, two public-backend instances plus Navicat were enough to saturate local MySQL `max_connections`; the temporary `18080` instance was shut down after final `8080` verification to restore local capacity.

---
*Phase: 04-public-progress-and-gameplay-writes*
*Completed: 2026-04-12*
