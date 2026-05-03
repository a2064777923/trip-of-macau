---
phase: 39-mini-program-story-mode-experience
plan: 39-03
subsystem: mini-program
tags: [taro, story-mode, route-context, map-handoff]
requires:
  - phase: 39-mini-program-story-mode-experience
    provides: 39-02 authenticated story-mode session state and event helpers.
provides:
  - Safe local story route context for tab handoff from story page to map page.
  - Current chapter, next stop, completed/inactive/locked route strip on StoryPage.
  - MapPage story-mode route panel, current destination highlight, and route clearing.
  - Honest no-geometry fallback copy when runtime anchors cannot resolve to a POI.
affects: [phase-39, story-page, map-page, public-runtime-route-ux]
tech-stack:
  added: []
  patterns: [validated-local-route-context, no-fake-route-geometry, temporary-story-route-emphasis]
key-files:
  created:
    - .planning/phases/39-mini-program-story-mode-experience/39-03-SUMMARY.md
  modified:
    - packages/client/src/types/game.ts
    - packages/client/src/services/gameService.ts
    - packages/client/src/pages/story/index.tsx
    - packages/client/src/pages/story/index.scss
    - packages/client/src/pages/map/index.tsx
    - packages/client/src/pages/map/index.scss
key-decisions:
  - Use local storage only for safe public route labels, ids, codes, and chapter states.
  - Resolve current destination by POI id first, then POI code, and show fallback copy when unresolved.
  - Clearing story map route state removes only temporary emphasis, not permanent exploration or reward state.
patterns-established:
  - Story route context is built and sanitized in `gameService.ts` rather than parsed ad hoc in pages.
  - Story and map route strips use the same status vocabulary: current, completed, inactive, locked.
requirements-completed: [MP-02, MP-03, MP-04, MP-05]
duration: 45min
completed: 2026-05-03
---

# Phase 39-03: Story Route and Map Handoff Summary

**Story mode now has a visible current-chapter route strip and a safe handoff into the map page without inventing fake route geometry.**

## Performance

- **Duration:** 45 min
- **Started:** 2026-05-03T08:43:00Z
- **Completed:** 2026-05-03T08:43:31Z
- **Tasks:** 3
- **Files modified:** 6 source files plus this summary

## Accomplishments

- Added `StoryModeRouteChapter` and `StoryModeRouteContext` types.
- Added route-context helpers in `gameService.ts`: build, save, load, clear, sanitize, and destination resolve.
- Added a StoryPage `主線路線` panel with `目前章節`, `下一站`, `支線稍後開放`, `已完成章節`, current destination, anchor type, and destination code.
- Rewired `前往地圖` to save route context before switching to the map tab.
- Added a MapPage `故事模式地圖` panel, route strip, return-to-story action, and `退出故事路線`.
- Highlighted the resolved POI card/detail panel as `目前故事目的地`.

## Task Commits

1. **Task 39-03-01 through 39-03-03:** pending plan commit after summary creation.

## Files Created/Modified

- `packages/client/src/types/game.ts` - Adds safe route context and POI code typing.
- `packages/client/src/services/gameService.ts` - Adds validated route-context storage and POI destination resolver.
- `packages/client/src/pages/story/index.tsx` - Adds story route strip and map handoff.
- `packages/client/src/pages/story/index.scss` - Styles story route panel and chapter status states.
- `packages/client/src/pages/map/index.tsx` - Consumes route context and highlights resolved destination.
- `packages/client/src/pages/map/index.scss` - Styles map story route panel and destination highlight.

## Decisions Made

- No fake `polyline` or derived map geometry was added. Route UI stays as a chapter sequence and POI destination highlight.
- Route context stores only public ids, codes, titles, labels, and status flags.
- Malformed or stale route context is ignored safely.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## Verification

- `npm run build:weapp` from `packages/client` passed.
- Acceptance grep confirmed required route-context helpers, story/map labels, map handoff copy, destination highlight classes, and `Number.isFinite` validation.
- Confirmed no `polyline` string was introduced in `gameService.ts` or `MapPage`.

## User Setup Required

None.

## Next Phase Readiness

Plan 39-04 can now add repeatable smoke and verification evidence across live runtime loading, media/event behavior, and mini-program build compatibility.

---
*Phase: 39-mini-program-story-mode-experience*
*Completed: 2026-05-03*
