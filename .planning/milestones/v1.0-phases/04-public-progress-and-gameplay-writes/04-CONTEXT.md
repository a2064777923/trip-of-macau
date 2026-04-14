---
phase: 04-public-progress-and-gameplay-writes
created: 2026-04-12
status: active
---

# Phase 4 Context

## Goal

Move the mini-program's stateful gameplay and preference writes to live public APIs backed by MySQL.

## Brownfield Reality

- Phase 3 already cut live public reads over to `packages/server`, but traveler progress and write-side behavior still mutates only local storage in `packages/client/src/services/gameService.ts`.
- The canonical MySQL schema already contains `user_profiles`, `user_progress`, `user_checkins`, `user_preferences`, and `reward_redemptions`, but the public backend still points its legacy user flow at the old `users` table.
- The mini-program has a partially prepared HTTP layer in `packages/client/src/services/api.ts`, including bearer-token support, but it does not yet validate the backend's `ApiResponse.code` envelope and therefore cannot safely consume public write failures.
- Existing local gameplay state includes default mock progress, preferences, emergency contact data, and reward redemption logic that should be preserved or bootstrapped instead of silently reset on first live login.

## Execution Decision

Phase 4 will execute as one end-to-end user-state cutover:

1. Replace the public backend's legacy user persistence path with canonical MySQL-backed profile, preference, progress, check-in, and redemption services.
2. Add a lightweight public JWT flow so mini-program write endpoints are authenticated while public read endpoints remain anonymous.
3. Expose contract-aligned `/api/v1/user/*` endpoints plus any small bridging APIs required to persist current-city and gameplay mutations cleanly.
4. Bootstrap first-login traveler state from the current mini-program snapshot when needed so the move off local mock writes does not wipe the existing experience.
5. Rewire the mini-program's write-side gameplay, settings, and login flows to those live endpoints and keep local cached state synchronized from server snapshots.

## Acceptance Focus

- Check-ins, reward redemption, profile bootstrap/login, preferences, emergency contact data, and current-city progress persist through real backend APIs.
- Local mini-program behavior remains coherent after the cutover: stamps, EXP, redeemed rewards, and preference toggles update correctly in the UI.
- Backend writes are validated, transactional where inventory/progress mutation matters, and verifiable against the real local MySQL database and running public service.
