# Phase 41 UI Spec: Story Runtime Diagnostics and Media Fallback

**Created:** 2026-05-04  
**Scope:** Mini-program story page and UAT-visible runtime status only

## Design Goal

Make the live story route understandable when it is running in WeChat DevTools or device testing: the traveler should see the story content normally, while testers can clearly distinguish live data, fallback data, media fallback, and unavailable media without reading console logs.

## Existing Visual Direction

- Preserve the current warm travel-story visual language in `pages/story/index.scss`.
- Preserve the existing card-based chapter and content block layout.
- Do not introduce a new design system or broad restyle in Phase 41.

## Required UI States

### Runtime Status

- Live runtime loaded: show Traditional Chinese copy indicating live story data is synced.
- Fallback runtime loaded: show Traditional Chinese warning copy that cached/fallback content is being displayed.
- Runtime loading: show a loading state that does not shift layout abruptly.
- Runtime failure: show retry/debug-friendly copy without blanking the page.

### Media Fallback

- Available media: render normally.
- Fallback media: show `已使用備用媒體播放。`.
- Unavailable media: show a visible card with title `媒體資源暫時未能載入` and a reason string.
- Lottie unavailable: fallback to poster/fallback image if present; otherwise show `動畫暫時無法播放`.

### UAT Diagnostics

- Diagnostics should be concise and non-secret.
- Allowed diagnostic values: runtime source, story id/code if already public, API base host label, `USE_MOCK`, dev-bypass enabled/disabled, media counts, fallback counts.
- Forbidden values: tokens, API keys, raw provider payloads, local filesystem paths, COS secrets.

## Acceptance Checks

- `pages/story/index.tsx` contains Traditional Chinese runtime/fallback status copy.
- `StoryContentBlockRenderer` retains fallback and missing-media notices for image/audio/video/lottie/attachment_list.
- `LottieAssetPlayer` destroys animation on unmount and shows fallback or error copy.
- Smoke/UAT report captures media availability/fallback counts separately from visual UAT status.

---

*Phase: 41-wechat-runtime-uat-harness-and-story-entry-hardening*
