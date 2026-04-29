# Phase 34 UI Spec: Mini-program Story Runtime Baseline

**Created:** 2026-04-30
**Status:** Ready for planning
**Surface:** `packages/client/src/pages/story/index.tsx`

## Design Intent

Phase 34 is not a full visual redesign of the mini-program. It is a runtime-consumption upgrade for the story page. Preserve the current warm storybook-like style, then add clear runtime affordances so travelers can understand what is live, what is interactive now, and what is planned for later gameplay.

## Required User-facing States

### Story Loading

- Show a calm Traditional Chinese loading message while the selected storyline runtime is being fetched.
- Do not block the whole tab if runtime fails; keep the existing story list/fallback content visible.
- Failure copy must be traveler-safe, for example `故事資料暫時未能同步，已顯示本機快取內容。`

### Story Runtime Summary

The active story panel should show:

- story title and introduction
- chapter count
- estimated time
- current progress
- story-mode route hint
- runtime sync status such as `即時故事資料已同步` or `使用本機快取`

### Chapter Runtime Detail

For each expanded chapter:

- Keep title, summary, detail, achievement, collectible, anchor location, and content blocks.
- Add a runtime flow section named `故事互動流程`.
- Show compiled steps in order.
- Each step card should show:
  - step display category label
  - step name
  - step description
  - action label
  - required/completion marker when applicable
  - media preview if a media asset exists and is renderable through current components

### Unsupported Gameplay

Unsupported step cards must not look broken. Use a friendly disabled/preview visual:

- label: `稍後開放`
- copy: `這個互動玩法已由後台配置，將在後續小程序玩法版本中開放。`
- show the template/step type so operators can debug when needed
- do not expose raw JSON by default

### Lottie and Media

- Lottie blocks continue to use `LottieAssetPlayer`.
- If Lottie fails, show poster/fallback or `動畫暫時無法播放`.
- Audio/video/image blocks continue through `StoryContentBlockRenderer`.

### Auth-gated Actions

- Read-only story browsing remains available anonymously.
- Starting a story session, reporting completion events, or saving progress must use existing auth guard behavior.
- If anonymous, show a clear prompt and route to the profile auth wall rather than silently failing.

## Interaction Requirements

- Selecting a story fetches runtime for that story.
- Expanding a chapter reports a best-effort `chapter_open` event only when authenticated.
- Viewing a content block or unsupported step may report a best-effort event only when authenticated.
- Duplicate event submissions must use stable `clientEventId` values derived from storyline/chapter/block/step ids.

## Copy Requirements

All new visible copy must be Traditional Chinese.

Required strings:

- `故事互動流程`
- `即時故事資料已同步`
- `使用本機快取`
- `稍後開放`
- `這個互動玩法已由後台配置，將在後續小程序玩法版本中開放。`
- `故事資料暫時未能同步，已顯示本機快取內容。`

## Non-goals

- Do not implement full map route drawing in this UI spec.
- Do not implement AR/photo recognition, voice input, puzzle, cannon defense, or route coverage gameplay.
- Do not replace the global mini-program visual language.

