# Phase 35 Handoff

## Delivered Scope

Phase 35 closes the v3.0 operations lifecycle gap with a centralized admin subsystem for dependency-aware lifecycle operations.

Delivered admin route:

- `/admin/#/ops/lifecycle`

Delivered admin API base:

- `/api/admin/v1/operations/lifecycle`

## Endpoints Delivered

- `GET /api/admin/v1/operations/lifecycle/target-types`
- `GET /api/admin/v1/operations/lifecycle/statuses`
- `GET /api/admin/v1/operations/lifecycle/targets`
- `POST /api/admin/v1/operations/lifecycle/preview`
- `POST /api/admin/v1/operations/lifecycle/operations`
- `POST /api/admin/v1/operations/lifecycle/operations/{operationId}/apply`
- `POST /api/admin/v1/operations/lifecycle/operations/{operationId}/cancel`
- `POST /api/admin/v1/operations/lifecycle/operations/run-due`
- `GET /api/admin/v1/operations/lifecycle/operations`
- `GET /api/admin/v1/operations/lifecycle/operations/{operationId}`

## Supported Target Types

- `city`
- `sub_map`
- `poi`
- `indoor_building`
- `indoor_floor`
- `indoor_node`
- `storyline`
- `story_chapter`
- `content_block`
- `content_asset`
- `experience_flow`
- `experience_flow_step`
- `experience_binding`
- `experience_override`
- `collectible`
- `reward`
- `game_reward`
- `redeemable_prize`
- `honor`
- `activity`

## Supported Actions and Statuses

Actions:

- `publish` -> `published`
- `unpublish` -> `unpublished`
- `remove` -> `deleted`

Canonical statuses:

- `editing` / `編輯中`
- `reviewing` / `審批中`
- `published` / `已發布`
- `unpublished` / `未發布`
- `deleted` / `已刪除`

Compatibility:

- Existing `draft` rows canonicalize to `editing`.
- Existing `archived` rows canonicalize to `unpublished`.

## Smoke Prerequisites

Run after admin backend is running on `8081`.

Environment variables:

- `PHASE35_ADMIN_BASE_URL`, default `http://127.0.0.1:8081`
- `PHASE35_PUBLIC_BASE_URL`, default `http://127.0.0.1:8080`
- `PHASE35_ADMIN_BEARER_TOKEN`
- `PHASE35_ADMIN_USERNAME`
- `PHASE35_ADMIN_PASSWORD`
- `PHASE35_MYSQL_EXE`
- `PHASE35_MYSQL_HOST`
- `PHASE35_MYSQL_PORT`
- `PHASE35_MYSQL_DATABASE`
- `PHASE35_MYSQL_USER`
- `PHASE35_MYSQL_PASSWORD`

Auth rule:

- Use `PHASE35_ADMIN_BEARER_TOKEN`, or provide `PHASE35_ADMIN_USERNAME` and `PHASE35_ADMIN_PASSWORD`.
- No bearer token, password, WeChat secret, COS secret, or provider API key is stored in the script or docs.

Smoke command:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-35-lifecycle.ps1
```

Expected success line:

```text
Phase 35 lifecycle smoke passed
```

## Verification Result

- Backend compile passed.
- Admin UI build passed.
- Phase 35 lifecycle smoke passed against local admin backend `8081`.
- Public backend `8080` was reachable during verification, so public runtime filtering was checked by temporarily unpublishing and restoring `east_west_war_and_coexistence`.

## Deferred Scope

- full approval workflow is future scope.
- full WeChat DevTools experiential acceptance remains deferred.
- Hard-delete tooling is not implemented; `remove` is a lifecycle transition to `deleted`.
- Rich reviewer assignment, approval inbox, notification routing, and multi-role sign-off should be planned separately if needed.

## Operational Notes

- The smoke imports Phase 28-35 SQL with `--default-character-set=utf8mb4`.
- Before importing the Phase 29 POI default experience seed, the smoke removes known local `poi_ama_default_walk_in` seed steps to keep repeated local runs stable.
- The smoke restores the flagship story to `published` before exit.
- This phase uses a registry whitelist for target tables and columns; the UI never sends raw table names that are executed directly.
