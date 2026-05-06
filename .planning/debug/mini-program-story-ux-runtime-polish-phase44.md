# Mini-program story UX/runtime polish debug

## Symptoms
- Story/map content finally loads after all services are restarted in one visible PowerShell window.
- Map page shows cross-city chips inside the map page; user expects large-map/city switching to happen only on the homepage selector.
- Traveler-facing story page exposes admin/operator concepts such as binding map, anchor type, chapter workbench, and interaction-flow configuration details.
- Story runtime currently reads like a configuration summary rather than a playable experience.
- Long story names overflow cards.
- Seeded covers/materials/media/animations are often missing or not playable.
- Mini-program cache grows until the DevTools/runtime crashes.

## Initial hypothesis
- Traveler UI is rendering admin/runtime DTO fields directly instead of compiling them into user-facing copy.
- Map page still renders all unlocked cities despite guarding cross-city switching.
- Public content cache stores the full catalog including large nested story runtime/content/media payloads; repeated refreshes may inflate WeChat storage usage.
- Lottie/media components need stricter availability/fallback handling and lifecycle cleanup.

## Fix strategy
- Hide cross-city switch chips in map page; retain only current city label and sub-map chips.
- Rename story UI sections to user-facing concepts and remove admin-only metadata from normal traveler UI.
- Render runtime steps as immersive action cards and avoid exposing template/step/config terms.
- Add text clamping/ellipsis to long story names.
- Slim public content persisted cache to prevent large nested runtime/media payloads from filling local storage.
- Build `weapp` and run targeted static checks.

## Fixes applied 2026-05-05
- Map page no longer renders cross-city chips; it shows the current large map and tells travelers to switch cities from the homepage selector.
- Story page hides admin/operator terms and replaces them with traveler-facing route, chapter, action, media, and progress copy.
- Story runtime cards now render as action cards with playable labels instead of configuration summaries.
- Long story names/descriptions are clamped to avoid overflowing cards.
- Missing media messages no longer expose asset ids or backend wording.
- Public content persistence now stores a slim cache; full story runtime/content/media stays in memory after fetch to reduce WeChat storage growth.
- Content viewed events now include an idempotency scope to reduce repeated event writes.
- Lottie player now uses a stable React id and destroys animations defensively on unmount.

## Verification
- `npm run build:weapp` passed after changes.
- Public backend health remained UP on `127.0.0.1:8080` with catalog counts: cities 4, subMaps 3, POIs 10, storylines 6.

## Remaining risks
- WeChat DevTools MCP is not available as a callable tool in this Codex session. The referenced MCP package requires local MCP client/server configuration and DevTools service port access before it can be used from the agent.
- Story page bundle is still over the recommended 244 KiB threshold; future work should split heavy story runtime/rendering components.

## Follow-up 2026-05-06
- User reported the automation tool should be on port `9420`.
- Confirmed MCP/DevTools parameter distinction:
  - MCP `wechat_automator`, `wechat_navigate`, `wechat_screenshot`, and `wechat_inspector` use `auto_port` for the miniprogram-automator WebSocket.
  - MCP `wechat_ide` / `wechat_build` use `port` for the WeChat DevTools IDE HTTP server.
  - Passing only `port` to automator-like tools leaves `auto_port` at its default `9420`.
- Confirmed a misleading tool state:
  - Windows showed `9420` listening, but the owning process was a WeChat DevTools renderer, not a confirmed miniprogram-automator WebSocket.
  - MCP `start` therefore reported `tcp_ready=true` but `ws_ready=false`.
  - `pageStack` WebSocket health check is the correct readiness signal, not TCP listen alone.
- Found the map blank-page root cause during DevTools navigation:
  - CDP exposed `TypeError: SystemError (exparserScriptError) Cannot read property 'lat' of undefined`.
  - The error appeared immediately after navigating to `pages/map/index`, before Taro `root` was written into page data.
  - This is consistent with the native mini-program `map` component receiving an invalid marker/circle/polyline point.
- Applied a narrow client-side fix in `packages/client/src/pages/map/index.tsx`:
  - Added `hasValidCoordinate`.
  - Filtered story route POIs and normal POI markers before passing them to the native `Map`.
  - Guarded `centerCoord`, selected POI circle, and route polyline inputs.
  - Kept city / large-map switching out of the map page.
- Verification:
  - `NODE_ENV=development npm run build:weapp` passed.
  - `wechat_build` compile passed against `D:/Archive/trip-of-macau/packages/client/dist`.
  - Full MCP navigation verification is still blocked by `auto_port` WebSocket readiness: `tcp_ready=true`, `ws_ready=false`.
- Documentation check:
  - The official mini-program `map` component expects valid coordinate-bearing data for map overlays; invalid native component input can surface as exparser runtime errors rather than React exceptions.
  - `wechat-miniprogram/lottie-miniprogram` requires a stable canvas and network `path`, and animations must be destroyed on page/component teardown. The current placeholder/fallback `LottieAssetPlayer` avoids dynamic canvas creation for now, but real Lottie playback still needs a dedicated canvas implementation later.
