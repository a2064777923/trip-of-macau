---
phase: 04-public-progress-and-gameplay-writes
plan: 03
subsystem: client
tags: [mini-program, api-cutover, async-ui]
requires: [04-02]
provides:
  - live mini-program write flows for login, preferences, current city, check-ins, emergency contact, and reward redemption
  - coherent local cache refresh via server snapshots
  - safer async UI behavior on map, home, settings, rewards, and senior flows
affects: [phase-06-migration-cutover-and-hardening]
tech-stack:
  added: []
  patterns: [server-synced local cache, envelope-aware API client, async UI mutation handling]
key-files:
  modified:
    - packages/client/src/app.ts
    - packages/client/src/services/api.ts
    - packages/client/src/services/gameService.ts
    - packages/client/src/pages/map/index.tsx
    - packages/client/src/pages/rewards/index.tsx
    - packages/client/src/pages/settings/index.tsx
    - packages/client/src/pages/senior/index.tsx
    - packages/client/src/pages/profile/index.tsx
    - packages/client/src/pages/index/index.tsx
key-decisions:
  - "Keep the existing local cache, but refresh it from server-backed user snapshots after live mutations."
  - "Do not leave dirty optimistic state behind when city switching or emergency-contact writes fail."
requirements-completed: [PUB-03]
duration: brownfield pass
completed: 2026-04-12
---

# Phase 4: Plan 03 Summary

**Mini-program gameplay writes now target live public APIs instead of local-only mock mutations.**

## Accomplishments

- Extended `packages/client/src/services/api.ts` so public API calls validate the backend `ApiResponse.code` envelope and expose a dedicated `api.user.*` surface.
- Reworked `gameService.ts` to bootstrap guest sessions, synchronize server user state, and route write paths through live `/api/v1/user/*` endpoints.
- Updated map, rewards, settings, profile, senior, and home page flows for async write behavior.
- Added follow-up UI hardening after live smoke:
  - map check-in now handles remote failures cleanly
  - senior-mode toggles now use reactive local page state instead of a stale one-time snapshot
  - settings rollback local optimistic state when preference writes fail
  - home city switching now waits for the live mutation to succeed before showing success feedback
  - emergency-contact writes only persist locally after the backend accepts them

## Verification

- `npm run build:weapp` in `packages/client`
- Live backend smoke passed on `8080`, covering the exact API surface the client now calls

## Notes

- The mini-program still keeps some client-derived state such as local city unlock presentation, but authoritative progress, preferences, current city, check-ins, and reward redemptions now come from the backend.

---
*Phase: 04-public-progress-and-gameplay-writes*
*Completed: 2026-04-12*
