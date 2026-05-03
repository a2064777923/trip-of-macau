---
phase: 39
slug: mini-program-story-mode-experience
status: complete
researched: 2026-05-03
requirements:
  - MP-01
  - MP-02
  - MP-03
  - MP-04
  - MP-05
---

# Phase 39 — Research

## Summary

Phase 39 should extend the mini-program's existing live story runtime baseline rather than introduce a parallel story-mode implementation.

The current code already contains:

- Public runtime DTOs and API helpers in `packages/client/src/services/api.ts`.
- Runtime mapping, story cache merge, auth-aware event helpers, and session helpers in `packages/client/src/services/gameService.ts`.
- A story page that refreshes runtime data, renders chapter cards, content blocks, runtime steps, and some events in `packages/client/src/pages/story/index.tsx`.
- A content block renderer for image, gallery, audio, video, Lottie, and attachments in `packages/client/src/components/StoryContentBlockRenderer/index.tsx`.
- A `lottie-miniprogram` canvas player with fallback image behavior in `packages/client/src/components/LottieAssetPlayer/index.tsx`.
- A map page with selected POI, route summary, related-story card, and marker rendering in `packages/client/src/pages/map/index.tsx`.

The remaining Phase 39 work is therefore integration and acceptance hardening:

- Model Phase 38 asset availability and usage metadata in mini-program types.
- Add intentional story-mode session state and auth gating.
- Report actual user/media/interactive events with backend-compatible event types and stable idempotency.
- Add route/current-chapter context between story and map pages.
- Render baseline pickup/task/reward/unsupported runtime steps as actionable traveler cards.
- Add repeatable mini-program build and local story-mode smoke evidence.

## Existing Architecture Findings

### Story Page

`packages/client/src/pages/story/index.tsx` is already the correct entry point.

Observed behavior:

- It calls `refreshPublicContent()` on mount.
- It calls `refreshStorylineRuntime(activeStory.id)` for the selected live story.
- It renders story tabs, a story hero, progress, chapter cards, rule chips, runtime steps, and `StoryContentBlockRenderer`.
- It reports render-time `content_read` and `unsupported_interaction_view` events via `recordStoryRuntimeEvent`.
- It does not yet expose an intentional "start story mode" session, session exit, route/current destination panel, pickup/task/reward action states, or media-ended reporting.

Planning implication:

- Preserve the page and add stateful story-mode controls inside it.
- Avoid refactoring the entire page in one plan because it is already 600+ lines and mixes data loading, rendering, and event side effects.

### Game Service

`packages/client/src/services/gameService.ts` is the current story runtime normalization and persistence layer.

Relevant functions:

- `mapStorylineRuntime(runtime)`.
- `mapStoryMediaAsset(asset)`.
- `mergeStorylineRuntimeIntoCache(storylineId, runtime, source)`.
- `refreshStorylineRuntime(storylineId, locale)`.
- `startStorylineRuntimeSession(storylineId)`.
- `exitStorylineRuntimeSession(storylineId, sessionId)`.
- `recordStoryRuntimeEvent(input)`.

Observed gaps:

- `StoryMediaAssetItem` does not currently include Phase 38 `availability`, `unavailableReason`, `usageHint`, duration, or file size fields.
- `buildStoryRuntimeClientEventId` does not include session id or a caller-provided action sequence. This is acceptable for once-per-block render events, but too coarse for repeatable media plays or multiple pickup attempts.
- Session helpers exist but `StoryPage` does not currently start, store, or exit sessions.
- Exploration refresh exists as `api.public.getPublicUserExploration(...)`, but the story page does not display or refresh story exploration after interactions.

Planning implication:

- Add a small story runtime/session state model instead of scattering session ids across components.
- Extend event input with optional `clientEventId` or `idempotencyScope` so the UI can make stable-but-specific keys.
- Keep anonymous no-op behavior for stateful events, but show UI gating for actions that require auth.

### Public API Client

`packages/client/src/services/api.ts` already has the Phase 38 endpoint family:

- `getPublicStorylineRuntime(storylineId, locale)`.
- `startPublicStorylineSession(storylineId)`.
- `recordPublicExperienceEvent(data)`.
- `recordPublicStorylineSessionEvent(storylineId, sessionId, data)`.
- `exitPublicStorylineSession(storylineId, sessionId)`.
- `getPublicUserExploration(params)`.

Observed gaps:

- `PublicStoryMediaAssetDto` does not expose all Phase 38 fields currently expected by `39-CONTEXT.md`, depending on backend DTO shape after Phase 38. The mini-program types should be extended defensively.
- API request retry exists, but auth failures throw `AUTH_REQUIRED` and clear token. Story action UI should translate this into Traditional Chinese auth-required feedback.

Planning implication:

- Keep API helper signatures stable.
- Extend TypeScript DTOs in-place and ensure mapping remains backward-compatible.

### Content Block Renderer

`StoryContentBlockRenderer` already supports the required block types.

Observed behavior:

- Images and galleries use `Image`.
- Audio uses `Taro.createInnerAudioContext()` and a play/pause button.
- Video uses Taro `Video`.
- Lottie delegates to `LottieAssetPlayer`.
- Missing media displays Traditional Chinese fallback blocks.

Observed gaps:

- Audio and video do not report `media_completed`.
- Audio card does not expose playback end to the story page.
- Video renderer cannot currently notify parent code on ended events.
- Missing media does not currently use backend `availability` or `unavailableReason` when present.

Planning implication:

- Add optional callbacks to `StoryContentBlockRenderer`, such as `onMediaCompleted(block, asset)` and `onUnavailableMediaViewed(block, asset)`.
- Add `onEnded` to audio context and video component.
- Keep the renderer reusable and avoid story-page-specific imports inside it.

### Lottie Player

`LottieAssetPlayer` already follows the official `lottie-miniprogram` baseline:

- Fetches network JSON.
- Uses a 2D canvas.
- Calls `lottie.setup(canvas)` and `lottie.loadAnimation(...)`.
- Destroys the animation on unmount.
- Shows poster/fallback on error.

Observed gaps:

- It does not notify when animation is loaded or fails.
- It does not expose completion events, which may be fine because looping Lottie may not have a meaningful completion.

Planning implication:

- Phase 39 can leave Lottie completion as "viewed" unless a non-looping animation has a clear end callback available.
- Do not replace `LottieAssetPlayer`.

### Map Page

`packages/client/src/pages/map/index.tsx` already has:

- `selectedPoiId` and `setSelectedPoiId`.
- `routeSummary` from `getWalkingRouteSummary`.
- Marker rendering with current user, POIs, and selected POI radius.
- `relatedStory` card linking to `/pages/story/index?storyId=...`.
- Selected POI panel with route card and action buttons.

Observed gaps:

- It does not parse story-mode route params such as `storyId`, `chapterId`, or anchor target.
- It does not draw story route segments or grey inactive segments.
- It cannot highlight the current chapter unless the anchor resolves to an existing POI.
- There is no shared story-mode route state.

Planning implication:

- Add a small story-mode navigation context that can be passed by route params and/or stored in game state.
- For Phase 39, highlight the current destination marker/card and render a story route strip/card. Only draw map polylines if safe coordinates exist.
- Avoid fake geometry. If only chapter order exists, present it as a route sequence strip.

## Public Backend Contract Findings

Phase 38 verification established:

- `GET /api/v1/storylines/{id}/runtime?locale=zh-Hant` returns the flagship runtime with five chapters, content blocks, generated media coverage, and safe fallback/unsupported states.
- Authenticated event/session smoke passes with local/dev dev-bypass.
- Banned fields such as prompts, local paths, provider secrets, and cost data are not exposed.
- Duplicate `clientEventId` handling is idempotent.

Planning implication:

- Phase 39 should not change backend runtime semantics unless a client-blocking contract gap is found.
- Client code should consume `availability`/fallback/unsupported fields from the existing runtime object.
- Smoke should reuse Phase 38 local dev-bypass convention but must not print bearer tokens.

## Recommended Implementation Slices

### Slice 1 — Runtime Type and Media Event Alignment

Purpose:

- Bring mini-program DTOs/types in line with Phase 38 runtime fields.
- Add renderer callbacks for media completion and unavailable media.

Files likely touched:

- `packages/client/src/services/api.ts`
- `packages/client/src/types/game.ts`
- `packages/client/src/services/gameService.ts`
- `packages/client/src/components/StoryContentBlockRenderer/index.tsx`
- `packages/client/src/components/StoryContentBlockRenderer/index.scss`
- `packages/client/src/components/LottieAssetPlayer/index.tsx` if load/error callback is needed

Verification:

- Type/build check.
- Static smoke or unit-like script verifies `mapStoryMediaAsset` preserves `availability` and fallback fields.

### Slice 2 — Story Mode Session and Interactive Cards

Purpose:

- Add explicit story-mode start/exit controls.
- Store active session state.
- Report chapter/content/media/pickup/task/reward/unsupported events with stable idempotency.
- Refresh exploration progress after accepted interactions.

Files likely touched:

- `packages/client/src/pages/story/index.tsx`
- `packages/client/src/pages/story/index.scss`
- `packages/client/src/services/gameService.ts`
- `packages/client/src/types/game.ts`

Verification:

- Script-level smoke calls public runtime/session/events if local backend and dev-bypass are available.
- UI build check.
- Manual mini-program page check if WeChat DevTools is available.

### Slice 3 — Story Route and Map Handoff

Purpose:

- Render story route/current chapter status on the story page.
- Handoff current story/chapter/anchor to map page.
- Highlight current destination in map page where anchor target resolves to a POI.
- Show route sequence or honest fallback when geometry is unavailable.

Files likely touched:

- `packages/client/src/pages/story/index.tsx`
- `packages/client/src/pages/story/index.scss`
- `packages/client/src/pages/map/index.tsx`
- `packages/client/src/pages/map/index.scss`
- `packages/client/src/services/gameService.ts`
- `packages/client/src/types/game.ts`

Verification:

- Build check.
- Local route-context smoke where a known chapter anchor selects a POI or shows fallback destination.

### Slice 4 — Phase 39 Verification and Evidence

Purpose:

- Add repeatable smoke for Phase 39 client/runtime mapping and event behavior.
- Update verification docs and requirements traceability after execution.

Files likely touched:

- `scripts/local/smoke-phase-39-mini-program-story-mode.ps1`
- `packages/client/package.json` if adding a script alias is useful
- `.planning/phases/39-mini-program-story-mode-experience/39-VERIFICATION.md`
- `.planning/REQUIREMENTS.md`
- `.planning/STATE.md`

Verification:

- `npm run build:weapp` from `packages/client`.
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-39-mini-program-story-mode.ps1`.

## Risks and Mitigations

| Risk | Impact | Mitigation |
| --- | --- | --- |
| Story page grows larger and harder to maintain | Future changes become brittle | Keep new logic in helper functions/components where possible; avoid wholesale rewrite |
| Event IDs too coarse | Repeated media plays or taps may be swallowed as duplicates | Allow caller-provided stable ids for repeatable actions while preserving idempotency |
| Anonymous users see actionable buttons that no-op | Confusing UX | Render auth-required state and Traditional Chinese prompt for stateful actions |
| Route geometry unavailable | Map may look fake or incorrect | Render route strip/current destination first; draw map geometry only when coordinates are safe |
| Media completion events fire on render | Incorrect progress | Fire media completion only on audio/video ended callbacks |
| WeChat-only APIs are hard to test in Node | Automated coverage limited | Combine Taro build with public API smoke and manual DevTools notes |
| Backend event names differ from current client strings | Events rejected | Normalize client event names to Phase 38 allowed types during implementation |

## Validation Architecture

### Validation Dimensions

| Dimension | Requirement | Validation |
| --- | --- | --- |
| Runtime loading | MP-01 | Story runtime smoke confirms flagship runtime can be fetched and mapped into story state with five chapters and content blocks. |
| Media rendering/fallback | MP-01, MP-05 | Static mapping/build checks confirm image/audio/video/Lottie/fallback fields are represented and unavailable media has Traditional Chinese copy. |
| Route/current chapter | MP-02 | Client smoke or manual DevTools check confirms story page route strip and map handoff for a current chapter. |
| Event/session idempotency | MP-03 | Authenticated local smoke starts a session, posts duplicate events, and checks stable event acceptance. |
| Interactive objects | MP-04, MP-05 | Runtime step cards expose pickup/task/reward/unsupported actions and report the configured event types. |
| Build compatibility | MP-01 to MP-05 | `npm run build:weapp` from `packages/client` exits 0. |

### Recommended Commands

```powershell
cd D:\Archive\trip-of-macau\packages\client
npm run build:weapp
```

```powershell
cd D:\Archive\trip-of-macau
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-39-mini-program-story-mode.ps1
```

If backend auth is required for event smoke:

```powershell
$env:PHASE39_TRAVELER_DEV_IDENTITY='phase39-smoke-traveler'
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-39-mini-program-story-mode.ps1
```

### Manual Checks

- Open WeChat DevTools against `packages/client/dist`.
- Visit `pages/story/index?storyId=<flagshipId>`.
- Confirm runtime status shows live data if backend is running.
- Start story mode while authenticated.
- Expand each of the five chapters.
- Play an audio block and confirm completion reporting path.
- Play or fallback-render a video block.
- Open map from current chapter and confirm current destination/story context appears.
- Tap a pickup/task/reward/unsupported card and confirm the UI feedback is clear.

## Research Complete

This phase can be planned as four waves/plans: runtime/media alignment, session and interactive story UX, route/map handoff, and smoke/verification closure.

## RESEARCH COMPLETE
