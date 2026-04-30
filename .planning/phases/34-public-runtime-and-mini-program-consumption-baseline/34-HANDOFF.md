# Phase 34 Handoff

## Verified Public Endpoints

- `GET /api/v1/story-lines?locale=zh-Hant` returned the seeded story `east_west_war_and_coexistence`.
- `GET /api/v1/storylines/{id}/runtime?locale=zh-Hant` returned `runtimeVersion=v1`, `source=public_runtime`, five published chapters, compiled steps, story-mode config, image/gallery content, audio content, and Lottie content.
- `POST /api/v1/storylines/{id}/sessions/start`, `POST /api/v1/storylines/{id}/sessions/{sessionId}/events`, `POST /api/v1/storylines/{id}/sessions/{sessionId}/exit`, and `GET /api/v1/users/me/exploration` are covered by the smoke script when `PHASE34_TRAVELER_BEARER_TOKEN` or local dev-bypass credentials are provided.

## Story Fixture

- Canonical acceptance story code: `east_west_war_and_coexistence`.
- The Phase 33 seed now includes chapter narration audio content blocks linked to the five flagship chapters, so the Phase 34 public runtime proves image, audio, and Lottie content consumption.
- The public runtime keeps unsupported configured gameplay visible through `unsupported`, `unsupportedReason`, `displayCategoryLabel`, and `travelerActionLabel`.

## Client Files Touched

- `packages/client/src/services/api.ts` - Public runtime, session, event, and exploration DTO/API helpers.
- `packages/client/src/services/gameService.ts` - Runtime sync, mapping, fallback, and anonymous-safe event helpers.
- `packages/client/src/types/game.ts` - Story runtime and session types.
- `packages/client/src/pages/story/index.tsx` - Story runtime loading, status, compiled flow cards, and passive event reporting.
- `packages/client/src/pages/story/index.scss` - Runtime status and compiled-flow styling.
- `packages/client/src/components/StoryContentBlockRenderer/index.tsx` - Media fallback states.
- `packages/client/src/components/StoryContentBlockRenderer/index.scss` - Missing-media fallback styling.

## Smoke Environment

- `PHASE34_PUBLIC_BASE_URL` defaults to `http://127.0.0.1:8080`.
- MySQL defaults are read from local app config; overrides are `PHASE34_MYSQL_EXE`, `PHASE34_MYSQL_HOST`, `PHASE34_MYSQL_PORT`, `PHASE34_MYSQL_DATABASE`, `PHASE34_MYSQL_USER`, and `PHASE34_MYSQL_PASSWORD`.
- Stateful checks require `PHASE34_TRAVELER_BEARER_TOKEN`.
- Optional local/dev fallback uses `PHASE34_TRAVELER_DEV_IDENTITY` only when `WECHAT_DEV_BYPASS_ENABLED=true`.
- No bearer token, WeChat secret, COS secret, or provider key is stored in the smoke script or planning docs.

## Verification Result

- `mvn -q -DskipTests compile -f packages/server/pom.xml` exited `0`.
- `npm run build:weapp --prefix packages/client` exited `0`.
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-34-public-runtime.ps1` exited `0`.
- The authenticated section was skipped in this run with `Skipping authenticated Phase 34 checks because PHASE34_TRAVELER_BEARER_TOKEN is not set`.

## Deferred Scope

- full WeChat DevTools experiential acceptance remains deferred.
- AR/photo/voice/puzzle/cannon gameplay remains future scope; Phase 34 exposes these as configured runtime metadata and degraded UI cards only.
- Full route drawing, near-range reveal, current chapter map highlighting, and device journey polish remain future mini-program milestone work.

## Material Pipeline Note

- Future video resources can be produced from one or several `image-2` still images by adding pan, zoom, enlargement, and lateral movement, then stitching with narration, music, or sound effects.
- Continue preferring material-board generation where one `image-2` output contains multiple aligned UI, pickup, badge, icon, and overlay assets that can be sliced locally to reduce generation cost.
- This note is recorded for the Phase 33/34 material production pipeline; Phase 34 did not expand runtime implementation scope around generated video production.

## Operational Notes

- The first live verification attempt hit an old JVM on port `8080`; after restarting the public backend from current `packages/server` classes, runtime metadata appeared correctly.
- The smoke script hard-deletes only known local Phase 29 A-Ma default-flow seed steps before re-importing that seed, making the local smoke rerunnable without changing production semantics.
