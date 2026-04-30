# Phase 35 Verification

## Prerequisites

- Local MySQL database `aoxiaoyou` is reachable and uses `utf8mb4`.
- Admin backend runs from current `packages/admin/aoxiaoyou-admin-backend` sources at `http://127.0.0.1:8081`.
- Public backend runs from current `packages/server` sources at `http://127.0.0.1:8080` for public filtering verification.
- Admin UI dependencies are installed under `packages/admin/aoxiaoyou-admin-ui`.
- Admin auth is provided through one of:
  - `PHASE35_ADMIN_BEARER_TOKEN`
  - `PHASE35_ADMIN_USERNAME` and `PHASE35_ADMIN_PASSWORD`

## Seed Import Behavior

`scripts/local/smoke-phase-35-lifecycle.ps1` imports required seed and migration SQL with MySQL `SOURCE` and `--default-character-set=utf8mb4`.

Imported files:

- `38-phase-28-story-content-and-lottie.sql`
- `39-phase-28-experience-orchestration.sql`
- `40-phase-29-poi-default-experience.sql`
- `41-phase-30-storyline-mode-overrides.sql`
- `42-phase-31-interaction-template-governance.sql`
- `43-phase-32-progress-engine.sql`
- `44-phase-32-story-sessions-and-timeline.sql`
- `45-phase-32-progress-repair-and-audit.sql`
- `47-phase-33-story-material-package-model.sql`
- `48-phase-33-flagship-material-assets.sql`
- `49-phase-33-east-west-flagship-story.sql`
- `50-phase-35-lifecycle-operations.sql`

Before importing the Phase 29 POI seed, the smoke removes known local `poi_ama_default_walk_in` seed steps so repeated local runs do not fail on `experience_flow_steps` unique keys.

## Commands Run

```powershell
mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml
npm run build --prefix packages/admin/aoxiaoyou-admin-ui
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-35-lifecycle.ps1
```

## Actual Result

- Admin backend compile exited `0`.
- Admin UI build exited `0`.
- Phase 35 lifecycle smoke exited `0`.
- Expected success line appeared: `Phase 35 lifecycle smoke passed`.
- Additional regression check: `GET /api/admin/v1/operations/lifecycle/targets?pageNum=1&pageSize=20` returned `code=0`, `total=351`, and 20 items, proving the workbench initial all-target query no longer fails when a descriptor has legacy schema differences.

## Smoke Assertions Covered

- `GET /api/admin/v1/operations/lifecycle/target-types` includes `storyline` and `story_chapter`.
- `GET /api/admin/v1/operations/lifecycle/statuses` includes `published`, `unpublished`, and `deleted`.
- `GET /api/admin/v1/operations/lifecycle/targets` finds seeded target `east_west_war_and_coexistence`.
- `POST /api/admin/v1/operations/lifecycle/preview` returns `previewHash`, `impactCounters`, and `impacts`.
- `POST /api/admin/v1/operations/lifecycle/operations` creates a scheduled unpublish operation with `confirmedImpact=true`.
- `POST /api/admin/v1/operations/lifecycle/operations/run-due` applies the due scheduled operation.
- `GET /api/admin/v1/operations/lifecycle/operations` returns the applied operation in history.
- `GET /api/admin/v1/operations/lifecycle/operations/{operationId}` returns summary, impacts, preview JSON, and request JSON.
- When public backend `8080` is reachable, the smoke verifies the unpublished story is filtered from `GET /api/v1/story-lines?locale=zh-Hant`.
- The smoke then publishes the seeded story again and verifies it returns to public listings when public backend is reachable.
- MySQL contains persisted lifecycle operation rows for the smoked target.

## Manual Admin UI Verification

1. Start admin backend on `8081`.
2. Start admin UI and open `/admin/#/ops/lifecycle`.
3. Confirm the sidebar item `生命週期與發布排程` appears under `測試與營運管理`.
4. Search for `east_west_war_and_coexistence`.
5. Click `預覽影響`.
6. Confirm the drawer shows `操作摘要`, `狀態轉換`, `依賴影響`, public runtime impact, exploration progress impact, and risk labels.
7. Confirm apply/schedule buttons stay disabled until `確認已閱讀影響` is checked.
8. Open `操作歷史` and confirm operation detail shows impacts and result/error fields.

## Deferred Checks

- No public runtime check was skipped in this run because public backend `8080` was reachable.
- full approval workflow is future scope.
- full WeChat DevTools experiential acceptance remains deferred.
- Hard-delete behavior is intentionally not part of Phase 35; `remove` sets lifecycle status to `deleted`.

## Known Local Environment Warning

Admin backend startup still logs a Mongo authentication warning on this workstation. The Phase 35 lifecycle path is MySQL-backed and the admin health endpoint plus lifecycle smoke passed despite that warning.
