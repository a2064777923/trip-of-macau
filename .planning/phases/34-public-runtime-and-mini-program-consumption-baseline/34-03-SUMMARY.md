---
phase: 34-public-runtime-and-mini-program-consumption-baseline
plan: 34-03
subsystem: client-ui
tags: [taro, story-page, lottie, runtime-flow, fallback]

requires:
  - phase: 34-02
    provides: Mini-program runtime helpers and story runtime mapping
provides:
  - Runtime-aware mini-program story page
  - Traditional Chinese compiled interaction flow cards
  - Unsupported gameplay degradation cards
  - Missing media placeholders for story content blocks
affects: [phase-34, mini-program-story-page, public-runtime]

tech-stack:
  added: []
  patterns:
    - Story page keeps fallback content visible while runtime sync runs or fails
    - Unsupported gameplay appears as configured-but-deferred cards instead of blank runtime gaps

key-files:
  created: []
  modified:
    - packages/client/src/pages/story/index.tsx
    - packages/client/src/pages/story/index.scss
    - packages/client/src/components/StoryContentBlockRenderer/index.tsx
    - packages/client/src/components/StoryContentBlockRenderer/index.scss

key-decisions:
  - "Phase 34 story UI shows compiled runtime steps but intentionally does not implement AR/photo/voice/puzzle/cannon gameplay."
  - "Missing media now renders a Traditional Chinese placeholder instead of disappearing."

requirements-completed:
  - LINK-02
  - VER-01

duration: 30min
completed: 2026-04-30
---

# Phase 34 Plan 34-03: Story Runtime Page Summary

**The mini-program story page now fetches public runtime data, renders compiled interaction flow cards, and degrades unsupported gameplay/media safely.**

## Performance

- **Duration:** 30 min
- **Started:** 2026-04-30T00:08:00Z
- **Completed:** 2026-04-30T00:38:00Z
- **Tasks:** 4
- **Files modified:** 4

## Accomplishments

- Added runtime sync loading/status UI with `故事資料同步中...`, `即時故事資料已同步`, `使用本機快取`, and fallback alert copy.
- Added `故事互動流程` section per expanded chapter, rendering display category, required marker, exploration weight, traveler action, media, and unsupported cards.
- Added best-effort passive story event reporting for `story_open`, `chapter_open`, `content_read`, and `unsupported_interaction_view`.
- Hardened story content block media rendering so missing image/gallery/audio/video/lottie/attachment assets show `媒體資源暫時未能載入`.

## Task Commits

1. **Tasks 34-03-01 through 34-03-04: Runtime story page and media fallback** - `1299202` (`feat`)

## Files Created/Modified

- `packages/client/src/pages/story/index.tsx` - Runtime loading, status chips, flow cards, unsupported cards, and passive event calls.
- `packages/client/src/pages/story/index.scss` - Runtime status, flow, step, unsupported, and debug card styles.
- `packages/client/src/components/StoryContentBlockRenderer/index.tsx` - Missing-media placeholders while retaining existing media support.
- `packages/client/src/components/StoryContentBlockRenderer/index.scss` - Missing-media card styles.

## Decisions Made

- Unsupported configured gameplay is visible as `稍後開放` with operator-friendly type text, not hidden.
- Passive event reporting catches/logs errors and does not block anonymous read-only browsing.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

- Sass nesting hid the literal `.story-runtime-step--unsupported` selector from string-based acceptance checks, so it was expanded to an explicit selector.

## User Setup Required

None - no external service configuration required for this plan.

## Verification

- `npm run build:weapp --prefix packages/client` exited `0`.
- Required Traditional Chinese runtime strings, event names, CSS classes, and media fallback strings were verified with `Select-String`.

## Next Phase Readiness

Plan 34-04 can smoke public runtime output and document which mini-program experiential gameplay remains deferred.

---
*Phase: 34-public-runtime-and-mini-program-consumption-baseline*
*Completed: 2026-04-30*
