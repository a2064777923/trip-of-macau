# Debug: Mini-program empty content after Phase 44

## Symptoms
- User reports local WeChat DevTools still does not show expected mini-program content.
- Map surface only showed one map, homepage did not show backend story/catalog data.
- Console showed `GET http://127.0.0.1:8080/api/v1/user/test-mode 401`.
- Later console showed no `[TripOfMacau][public-content]` diagnostic and did show generic `Error: timeout`.

## Evidence
- Public backend health on 8080 is UP.
- `GET /api/v1/health` reports published catalog counts: cities=4, subMaps=3, storylines=6, storyChapters=24, pois=10.
- Direct public APIs all return success locally:
  - `/cities?locale=zh-Hant`: 4
  - `/sub-maps?locale=zh-Hant`: 3
  - `/pois?locale=zh-Hant`: 10
  - `/story-lines?locale=zh-Hant`: 6
  - runtime/discover, runtime/map, runtime/travel all return success.
- The observed `/user/test-mode` request is authenticated and should not run for anonymous users.

## Root Causes / Findings
1. Frontend query builder used `URLSearchParams`. WeChat mini-program JS runtime may not provide this Web API. Every public catalog call includes a query object (`locale=zh-Hant`), so those requests could fail before network dispatch.
2. `/user/test-mode` has no query string, so it could still hit the backend and return 401. This made it look like the app was connected only to auth-gated APIs while public catalog stayed empty.
3. Map page called `/user/test-mode` even for anonymous users. This was incorrect because test mode is an authenticated-user feature.
4. `refreshPublicContent()` previously awaited user-state sync before public catalog loading. Public content is anonymous-readable, so stale token or user-state failures must not block catalog hydration.
5. Taro location calls can timeout in DevTools. Location timeout should not block rendering public catalog.
6. The map cross-city restriction was not the cause and should remain scoped as originally intended.

## Fixes Applied
- Replaced `URLSearchParams` in `packages/client/src/services/api.ts` with a mini-program-safe `encodeURIComponent` query builder.
- Replaced native `Promise.allSettled` usage in `refreshPublicContent()` with a local compatibility helper.
- Added request-level console diagnostics in local/dev mode:
  - `[TripOfMacau][request:start]`
  - `[TripOfMacau][request:success]`
  - `[TripOfMacau][request:failed]`
- Public GET requests now fail fast: 8s timeout and no retries, instead of potentially blocking the page for repeated 30s timeouts.
- `refreshPublicContent()` no longer blocks on authenticated user-state sync; it triggers sync in the background only when a non-anonymous token exists.
- Stopped anonymous map page bootstrap from calling `/user/test-mode`; it now only runs when a session token exists and the user is not anonymous.
- Added short 2.5s location fallbacks for homepage assessment and map location calls.
- Restored the map page cross-city guard; cross-map switching remains handled from the homepage selector, not inside the current map page.
- Rebuilt `packages/client/dist` and regenerated root/dist project configs.

## Verification
- `npm run build:weapp`: PASS. Existing warning remains: `pages/story/index.js` exceeds recommended bundle size.
- Source no longer contains `URLSearchParams` or native `Promise.allSettled`.
- `scripts/local/smoke-phase-41-wechat-runtime-uat.ps1 -Quick`: PASS.
- `scripts/local/smoke-phase-42-gameplay-event-engine.ps1`: PASS.
- `scripts/local/smoke-phase-43-traveler-ops.ps1`: PASS from previous run after the same public-content changes.

## Manual Verification Needed
- In WeChat DevTools, close/reopen `D:\Archive\trip-of-macau\packages\client` or click Compile after this rebuild.
- Expected console after homepage loads:
  - `[TripOfMacau][request:start]` for `/cities`, `/sub-maps`, `/pois`, `/story-lines`, etc.
  - `[TripOfMacau][request:success]` with counts such as cities=4 and storylines=6.
  - `[TripOfMacau][public-content]` with counts showing cities=4, subMaps=3, storylines=6, pois=10.
- If no `[TripOfMacau]` logs appear, DevTools is not running the rebuilt `dist` or homepage hydration is not executing.
- If logs appear but fail, use the `path` and `message` fields from `[TripOfMacau][request:failed]` for the next root-cause pass.
