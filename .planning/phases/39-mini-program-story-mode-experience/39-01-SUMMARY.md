---
phase: 39-mini-program-story-mode-experience
plan: 39-01
subsystem: mini-program
tags: [taro, story-runtime, media, lottie]
requires:
  - phase: 38-public-runtime-asset-consumption
    provides: Public runtime media availability and fallback metadata.
provides:
  - Typed mini-program story media availability and usage-hint DTO mapping.
  - Shared story media URL, fallback-reason, and playability helpers.
  - Story content block media completion and unavailable-media callbacks.
  - Lottie ready/unavailable callbacks while preserving canvas playback.
affects: [phase-39, mini-program-story-mode, public-runtime-media]
tech-stack:
  added: []
  patterns: [typed-runtime-media-fallback, callback-based-media-events]
key-files:
  created:
    - .planning/phases/39-mini-program-story-mode-experience/39-01-SUMMARY.md
  modified:
    - packages/client/src/services/api.ts
    - packages/client/src/types/game.ts
    - packages/client/src/services/gameService.ts
    - packages/client/src/components/StoryContentBlockRenderer/index.tsx
    - packages/client/src/components/StoryContentBlockRenderer/index.scss
    - packages/client/src/components/LottieAssetPlayer/index.tsx
key-decisions:
  - Keep public client media fields limited to traveler-safe runtime metadata.
  - Treat unsupported or missing media as visible Traditional Chinese fallback UI rather than blank or crashing content.
patterns-established:
  - Resolve playable story media through gameService helpers instead of duplicating URL fallback logic in UI.
  - Emit audio/video completion only from actual ended events.
requirements-completed: [MP-01, MP-05]
duration: 40min
completed: 2026-05-03
---

# Phase 39-01: Runtime Media Foundation Summary

**Mini-program story runtime media now preserves public availability metadata, renders fallback states, and emits safe media lifecycle callbacks.**

## Performance

- **Duration:** 40 min
- **Started:** 2026-05-03T08:14:53Z
- **Completed:** 2026-05-03T09:02:00Z
- **Tasks:** 3
- **Files modified:** 6 source files plus this summary

## Accomplishments

- Added Phase 38 media availability, runtime kind, duration, file size, and usage-hint fields to public DTOs and client story media types.
- Centralized story media URL resolution, fallback reason, and playability checks in `gameService.ts`.
- Updated story content blocks to show Traditional Chinese unavailable/fallback UI and to report audio/video completion only after actual playback ended.
- Added Lottie ready/unavailable callbacks while preserving `lottie-miniprogram`, WeChat canvas setup, network JSON fetch, and destroy-on-unmount behavior.

## Task Commits

1. **Task 39-01-01 through 39-01-03:** pending plan commit after summary creation.

## Files Created/Modified

- `packages/client/src/services/api.ts` - Adds public-safe story media availability and usage-hint DTO fields.
- `packages/client/src/types/game.ts` - Adds mapped story media availability and usage-hint client types.
- `packages/client/src/services/gameService.ts` - Maps all new media fields and exports fallback/playability helpers.
- `packages/client/src/components/StoryContentBlockRenderer/index.tsx` - Adds completion and unavailable-media callbacks with fallback UI.
- `packages/client/src/components/StoryContentBlockRenderer/index.scss` - Styles fallback notice copy.
- `packages/client/src/components/LottieAssetPlayer/index.tsx` - Adds safe ready/unavailable callbacks and generic failure logging.

## Decisions Made

- Did not expose admin-only provenance, local paths, prompt/script text, provider data, costs, QA notes, or COS object keys to the mini-program.
- Kept missing media visible so story pages remain readable and debuggable when COS or generated assets are unavailable.
- Preserved Lottie rendering in the existing component so later story page work can consume callbacks without owning canvas lifecycle.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

- The first patch briefly duplicated the `StoryContentBlockRenderer` function signature; it was caught by inspection before build and corrected.

## Verification

- `npm run build:weapp` from `packages/client` passed.
- Acceptance grep confirmed required DTO/type/helper/callback/fallback strings are present.
- Banned client provenance strings were checked across the modified DTO/type/service files and were not present.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

Plan 39-02 can now wire story-mode session events to `StoryContentBlockRenderer` callbacks and rely on typed media fallback helpers instead of parsing raw runtime JSON.

---
*Phase: 39-mini-program-story-mode-experience*
*Completed: 2026-05-03*
