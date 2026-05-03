# Phase 39: Mini-program Story Mode Experience - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in `39-CONTEXT.md`; this log preserves the alternatives considered.

**Date:** 2026-05-03
**Phase:** 39 - Mini-program Story Mode Experience
**Mode:** Default-mode fallback from `/gsd-next` to `/gsd-discuss-phase 39`
**Areas discussed:** Runtime consumption, session/events, route/map presentation, interactive objects, media fallback, auth, verification

## Runtime Consumption

| Option | Description | Selected |
| --- | --- | --- |
| Extend existing story page and runtime mapper | Use `StoryPage`, `gameService.ts`, `StoryContentBlockRenderer`, and `LottieAssetPlayer` as the baseline. | yes |
| Build a new parallel story runtime page | Create a separate mini-program route for the v3.1 story mode. | no |
| Keep mock/local story data as primary | Treat live backend as optional until later. | no |

**Captured decision:** Extend the existing live-runtime baseline and keep mock/local data as fallback only.

## Session And Events

| Option | Description | Selected |
| --- | --- | --- |
| Intentional story-mode session | Start sessions only when the traveler enters story mode and is authenticated. | yes |
| Start session on every read-only page open | Automatically create sessions when viewing story details. | no |
| Local-only progress until later | Do not call public session/event APIs from the mini-program. | no |

**Captured decision:** Anonymous browsing remains read-only; authenticated story mode uses public session/event endpoints with stable idempotency.

## Route And Map Presentation

| Option | Description | Selected |
| --- | --- | --- |
| Reliable route baseline | Show chapter sequence, current destination, inactive state, and map handoff. | yes |
| Fake full navigation geometry | Draw polylines even when safe route geometry is unavailable. | no |
| Defer all route UI | Keep story page read-only and leave route behavior to later. | no |

**Captured decision:** Implement a truthful route/story-mode baseline first; do not fake route geometry.

## Interactive Objects

| Option | Description | Selected |
| --- | --- | --- |
| Baseline tappable cards | Render pickup/task/reward/unsupported runtime steps as traveler-facing cards and report events. | yes |
| Full gameplay engines | Implement AR, speech, puzzle, route coverage, and cannon defense now. | no |
| Hide unsupported steps | Remove future gameplay steps from the mini-program until implemented. | no |

**Captured decision:** Show configured steps honestly; support baseline taps and explicit future-gameplay placeholders.

## Media And Fallback

| Option | Description | Selected |
| --- | --- | --- |
| Consume Phase 38 availability/fallback metadata | Render available media, poster/fallback, or unavailable reason. | yes |
| Trust every URL as playable | Assume all backend media URLs work and show empty/broken controls otherwise. | no |
| Hide unavailable blocks | Drop content blocks or steps when media is unavailable. | no |

**Captured decision:** Keep content visible and use explicit fallback/unavailable states.

## Auth

| Option | Description | Selected |
| --- | --- | --- |
| Preserve anonymous read-only browsing | Allow reading stories anonymously but gate all stateful actions. | yes |
| Auto-create synthetic guest users | Restore local guest identity creation for story mode. | no |
| Block story viewing until login | Require auth before reading any story content. | no |

**Captured decision:** Preserve the project-wide real-auth contract.

## Verification

| Option | Description | Selected |
| --- | --- | --- |
| Build plus local story-mode smoke | Verify Taro build compatibility and live public API/session/event mapping where possible. | yes |
| WeChat device UAT finalization | Complete full device acceptance and release readiness in this phase. | no |
| No client-side verification | Trust Phase 38 backend smoke only. | no |

**Captured decision:** Phase 39 must verify mini-program behavior locally, while final device/release evidence remains Phase 40.

## Deferred Ideas

- Full AR photo recognition and visual positioning.
- Speech-triggered NPC/gameplay interactions.
- Puzzle/minigame engines, route-coverage gameplay, and cannon defense.
- Phase 40 release readiness dashboard, cost visibility, and WeChat device UAT checklist.
