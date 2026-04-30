# Phase 34 Verification

## Prerequisites

- Local MySQL database `aoxiaoyou` is reachable and uses `utf8mb4`.
- Public backend is running from current `packages/server` sources at `http://127.0.0.1:8080`.
- Java 17, Maven, Node, and the mini-program dependencies are installed.
- Optional stateful smoke requires `PHASE34_TRAVELER_BEARER_TOKEN`.
- Optional local/dev stateful smoke may use `PHASE34_TRAVELER_DEV_IDENTITY` only when `WECHAT_DEV_BYPASS_ENABLED=true`.

## Seed Import Behavior

`scripts/local/smoke-phase-34-public-runtime.ps1` imports the required Phase 28-33 SQL files using MySQL with `--default-character-set=utf8mb4` and `SOURCE`.

The smoke imports:

- `38-phase-28-story-content-and-lottie.sql`
- `39-phase-28-experience-orchestration.sql`
- `40-phase-29-poi-default-experience.sql`
- `41-phase-30-storyline-mode-overrides.sql`
- `43-phase-32-progress-engine.sql`
- `44-phase-32-story-sessions-and-timeline.sql`
- `45-phase-32-progress-repair-and-audit.sql`
- `47-phase-33-story-material-package-model.sql`
- `48-phase-33-flagship-material-assets.sql`
- `49-phase-33-east-west-flagship-story.sql`

Before importing the Phase 29 POI seed, the smoke removes known local seed rows for `poi_ama_default_walk_in` so repeated local runs do not fail on `experience_flow_steps` unique keys.

## Commands Run

```powershell
mvn -q -DskipTests compile -f packages/server/pom.xml
npm run build:weapp --prefix packages/client
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-34-public-runtime.ps1
```

## Actual Result

- Backend compile exited `0`.
- Mini-program WeApp build exited `0`.
- Phase 34 public runtime smoke exited `0`.
- Expected success line appeared: `Phase 34 public runtime smoke passed`.
- Authenticated section was skipped because `PHASE34_TRAVELER_BEARER_TOKEN` was not set.
- Exact skip line: `Skipping authenticated Phase 34 checks because PHASE34_TRAVELER_BEARER_TOKEN is not set`.

## Runtime Assertions Covered

- `GET /api/v1/story-lines?locale=zh-Hant` includes `east_west_war_and_coexistence`.
- `GET /api/v1/storylines/{id}/runtime?locale=zh-Hant` returns `runtimeVersion=v1`.
- Runtime `source` is `public_runtime`.
- Published chapter count is at least five.
- Runtime chapters include compiled steps.
- Runtime steps expose `displayCategoryLabel`, `travelerActionLabel`, and boolean `unsupported`.
- Runtime includes story-mode config.
- Runtime content blocks include at least one Lottie block, one audio block, and one image or gallery block.

## Manual Mini-program Check

1. Start the public backend on `8080`.
2. Build or open the WeChat mini-program from `packages/client/dist`.
3. Navigate to the story page.
4. Confirm the story list contains `東西方文明的戰火與共生`.
5. Open the story and expand chapters.
6. Confirm `故事互動流程` cards show display category labels, traveler action labels, required markers, exploration weight, unsupported cards, and media fallback states.
7. Confirm missing or unsupported complex gameplay displays a Traditional Chinese degraded state rather than a blank area.

## Deferred Acceptance

full WeChat DevTools experiential acceptance remains deferred.

Complex AR/photo recognition, voice input, puzzle, cannon defense, and route coverage gameplay are not implemented in Phase 34. They are represented through runtime DTO metadata and degraded mini-program UI only.
