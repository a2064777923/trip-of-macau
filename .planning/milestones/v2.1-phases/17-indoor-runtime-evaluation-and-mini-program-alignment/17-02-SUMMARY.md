---
phase: 17-indoor-runtime-evaluation-and-mini-program-alignment
plan: 02
subsystem: ui
tags: [taro, react, wechat-mini-program, indoor-runtime, auth-gating]
requires:
  - phase: 17-indoor-runtime-evaluation-and-mini-program-alignment
    provides: public indoor runtime snapshot and interaction contract
provides:
  - dedicated indoor runtime client service
  - live indoor runtime page integration
  - auth-safe indoor interaction gating
  - supported indoor effect rendering
affects: [mini-program indoor page, auth wall flow, future indoor runtime UAT]
tech-stack:
  added: [none]
  patterns: [runtime service centralization, read-only anonymous browsing, supported-effect rendering guards]
key-files:
  created:
    - packages/client/src/services/indoorRuntime.ts
  modified:
    - packages/client/src/services/api.ts
    - packages/client/src/pages/map/indoor/index.tsx
    - packages/client/src/pages/map/indoor/index.scss
key-decisions:
  - "Centralized runtime fetch and interaction logic in one client module instead of re-parsing JSON across the page."
  - "Anonymous users can open indoor maps and view passive overlays, but guarded interactions route through the existing auth flow."
  - "Unsupported behaviors render visible fallback state instead of crashing the indoor page."
patterns-established:
  - "Indoor page state is sourced from the runtime endpoint and layered over the existing tile renderer."
  - "Trigger submissions use explicit runtime DTOs plus `blockedReason` feedback for graceful UI handling."
requirements-completed: [RULE-03]
duration: multi-session
completed: 2026-04-17
---

# Phase 17 Plan 02 Summary

**The mini-program indoor page now consumes live authored runtime data, renders the supported behavior subset, and keeps anonymous users on a safe read-only path.**

## Accomplishments

- Added typed indoor runtime DTOs and a dedicated `indoorRuntime.ts` integration layer.
- Refactored the indoor page to load live runtime snapshots, render supported overlays and path motion, and guard floor-switch flicker with loading state.
- Routed guarded indoor interactions through the existing auth contract instead of allowing mock or silent mutation paths.
- Preserved safe degradation for unsupported authored behaviors through explicit `blockedReason` handling.

## Files Created Or Modified

- `packages/client/src/services/api.ts`
- `packages/client/src/services/indoorRuntime.ts`
- `packages/client/src/pages/map/indoor/index.tsx`
- `packages/client/src/pages/map/indoor/index.scss`

## Verification

- `npm run build:weapp` in `packages/client`
- Runtime smoke path exercised against live `8080` using Lisboa `1F`
- Anonymous runtime interaction on `royal-palace-dwell-reveal` returns `auth_required`
- Supported runtime interaction on `zipcity-guiding-path` returns `path_motion` and `bubble`

## Notes

- Phase 17 intentionally keeps unsupported effect categories visible but non-executable so authoring gaps can still be inspected on the client.
- Full multilingual authoring depth remains a later-phase concern, but the indoor runtime surface is now wired for locale-aware reads.
