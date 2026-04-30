# Phase 35 Research: Operations Lifecycle Scheduling and Dependency Workbench

**Researched:** 2026-04-30
**Status:** Ready for planning
**Scope:** Admin lifecycle operations, dependency impact preview, scheduling, audit/history, cross-domain status consistency, and public runtime alignment.

## Executive Summary

The codebase already has many domain editors and status fields, but no canonical operations lifecycle subsystem. Current lifecycle support is fragmented:

- `ContentStatus` only defines `draft`, `published`, and `archived`.
- `ContentLifecycleStatusSupport` only allows manual `published` and `archived`.
- Public runtime filtering was hardened in Phase 34, but admin-side scheduling and dependency preview remain incomplete.
- `content_relation_links` can be used as the primary cross-domain dependency graph, but structural parent/child links still need explicit resolvers.

Phase 35 should introduce a dedicated lifecycle operation model and workbench rather than adding one-off publish buttons to each editor. The implementation should be incremental:

1. Add canonical lifecycle schema, DTOs, and status support.
2. Add service/controller logic for target catalog, preview, apply/schedule, due-runner, and history.
3. Add a Traditional Chinese admin workbench.
4. Add smoke verification and update v3.0 traceability only after live proof.

## Existing Backend Findings

### Status Support Is Too Narrow

`ContentStatus.java` currently contains:

- `DRAFT("draft")`
- `PUBLISHED("published")`
- `ARCHIVED("archived")`

`ContentLifecycleStatusSupport.java` currently allows only `PUBLISHED` and `ARCHIVED` as manually operable. This is not enough for the requested lifecycle labels:

- `editing`
- `reviewing`
- `published`
- `unpublished`
- `deleted`

Recommended approach:

- Extend `ContentStatus` with new canonical codes.
- Keep compatibility helpers for existing `draft` and `archived` values.
- Map `draft -> editing` and `archived -> unpublished` in UI/response labels.
- Avoid broad destructive migrations that rewrite every row before the service can handle both old and new values.

### Operations Domain Exists But Only Covers Activities

`AdminOperationsController.java` is mounted at `/api/admin/v1/operations`, but currently only exposes activity/task endpoints.

This makes `/api/admin/v1/operations/lifecycle` the natural route for Phase 35 because:

- it belongs to operations, not a single content editor
- it can coexist with `/activities`
- it gives the admin sidebar a direct `測試與營運管理` entry

### Relation Graph Exists

`ContentRelationLink.java` provides:

- `ownerType`
- `ownerId`
- `relationType`
- `targetType`
- `targetId`
- `targetCode`
- `metadataJson`
- `sortOrder`

Preview should query both directions:

- inbound: rows where target matches the selected target
- outbound: rows where owner matches the selected target

However, not all important dependencies are relation rows. The service must add structural resolvers for:

- city -> sub-maps -> POIs
- sub-map -> POIs
- POI -> indoor buildings where bound by existing fields/relations
- indoor building -> floors -> nodes
- storyline -> chapters
- experience flow -> steps
- reward/rule entities through reward rule bindings and relation links
- exploration elements for progress impact

### Public Runtime Already Filters Published Content

Phase 34 added traveler-facing filtering and smoke verification for the public runtime subset. Phase 35 must not weaken this.

When lifecycle operations change a story, chapter, flow, POI, or asset status:

- public runtime should exclude unpublished/deleted content
- admin preview/history should still show it
- smoke must verify at least one target disappears from public runtime after unpublish/remove and returns after publish where feasible

## Recommended Schema

### `content_lifecycle_operations`

Recommended columns:

- `id`
- `operation_code`
- `target_type`
- `target_id`
- `target_code`
- `target_name`
- `action`
- `from_status`
- `to_status`
- `requested_status`
- `operation_status`
- `scheduled_at`
- `applied_at`
- `cancelled_at`
- `failed_at`
- `requested_by`
- `requested_by_name`
- `reason`
- `preview_hash`
- `preview_json`
- `request_json`
- `result_json`
- `error_message`
- `created_at`
- `updated_at`
- `deleted`

Recommended operation statuses:

- `draft`
- `scheduled`
- `applying`
- `applied`
- `failed`
- `cancelled`

### `content_lifecycle_operation_impacts`

Recommended columns:

- `id`
- `operation_id`
- `impact_type`
- `severity`
- `source_type`
- `source_id`
- `source_code`
- `source_name`
- `relation_type`
- `target_type`
- `target_id`
- `target_code`
- `target_name`
- `impact_summary`
- `metadata_json`
- `sort_order`
- `created_at`
- `updated_at`
- `deleted`

Recommended severities:

- `blocking`
- `warning`
- `info`

Recommended impact types:

- `inbound_relation`
- `outbound_relation`
- `downstream_child`
- `public_runtime`
- `exploration_progress`
- `reward_rule`
- `media_reference`
- `status_transition`

## Recommended Backend Contracts

Base path: `/api/admin/v1/operations/lifecycle`

Endpoints:

- `GET /target-types`
- `GET /statuses`
- `GET /targets`
- `POST /preview`
- `POST /operations`
- `POST /operations/{operationId}/apply`
- `POST /operations/{operationId}/cancel`
- `POST /operations/run-due`
- `GET /operations`
- `GET /operations/{operationId}`

Request/response DTOs:

- `AdminLifecycleTargetTypeResponse`
- `AdminLifecycleStatusResponse`
- `AdminLifecycleTargetSummaryResponse`
- `AdminLifecyclePreviewRequest`
- `AdminLifecyclePreviewResponse`
- `AdminLifecycleImpactResponse`
- `AdminLifecycleOperationRequest`
- `AdminLifecycleOperationResponse`
- `AdminLifecycleOperationDetailResponse`
- `AdminLifecycleHistoryQuery`

Implementation service:

- `AdminLifecycleOperationService`
- `AdminLifecycleOperationServiceImpl`

Support classes:

- `AdminLifecycleTargetRegistry`
- `AdminLifecycleDependencyPreviewService`
- `AdminLifecycleStatusTransitionSupport`

## Dependency Preview Strategy

Use a layered preview:

1. Resolve target metadata from a registry.
2. Validate requested action and target status transition.
3. Query `content_relation_links` inbound and outbound.
4. Query structural child relationships per target type.
5. Query `exploration_elements` where owner/scope references the target.
6. Query reward/rule references where practical.
7. Add public runtime impact messages for targets used by story/POI runtime.
8. Return counts and representative items grouped by impact type/severity.

Blocking examples:

- Removing a city that still has published sub-maps or POIs.
- Removing a storyline with published chapters unless cascading removal is explicitly confirmed.
- Removing a content asset referenced by published content or experience steps.
- Publishing a chapter whose parent storyline is not published.

Warning examples:

- Unpublishing a POI used by a published story chapter.
- Unpublishing a content block used by a published chapter.
- Removing an exploration element after users have completed it.

Info examples:

- Publishing will expose the target to public runtime after filters refresh.
- Scheduled action will run at a configured time.

## Admin UI Findings

Existing admin UI is route-based and uses:

- `src/App.tsx` for route registration
- `src/layouts/DefaultLayout.tsx` for sidebar
- `src/services/api.ts` for API wrappers
- `src/types/admin.ts` for DTO types

The new workbench should be a dedicated page rather than a tab inside activity management. Recommended route:

- `/ops/lifecycle`

Recommended layout:

- Header with title `生命週期與發布排程`
- Summary cards for pending schedules, high-impact previews, applied today, failed operations
- Target list with filters
- Preview drawer with impact groups and dependency tree
- Action panel for immediate apply or scheduled execution
- History tab/table

The UI should reuse Ant Design primitives and existing page patterns. It should not ask operators to hand-write JSON.

## Verification Strategy

Minimum automated checks:

- `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml`
- `npm run build --prefix packages/admin/aoxiaoyou-admin-ui`
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-35-lifecycle.ps1`

Recommended smoke coverage:

- import required seed SQL files with `--default-character-set=utf8mb4`
- import Phase 35 migration
- authenticate admin through env-backed credentials/token
- find `east_west_war_and_coexistence`
- preview unpublish or remove for the storyline or one published chapter
- assert preview contains dependency impact groups
- schedule an operation for near-now or create an immediate operation
- apply/run-due operation
- assert operation history and impacts are persisted
- verify public runtime filtering reflects lifecycle state
- republish or restore target before exit when smoke mutates seeded acceptance data
- print `Phase 35 lifecycle smoke passed`

## Pitfalls

- Do not physically delete rows in smoke or default UI actions. Use lifecycle `deleted` unless a future phase explicitly implements hard-delete tools.
- Do not rewrite Chinese SQL through inline PowerShell literals. Use UTF-8 `.sql` files and `SET NAMES utf8mb4`.
- Do not claim full approval workflow support if only statuses and scheduling exist.
- Do not make public runtime accept `reviewing`, `editing`, `unpublished`, or `deleted` as traveler-visible states.
- Do not only inspect `content_relation_links`; structural child relationships are required for useful previews.
- Do not block the entire phase on perfect coverage of every historical table. Provide registry entries and compatibility mapping for supported v3.0 content-bearing entities.

## Recommended Plan Split

1. Backend lifecycle schema, status support, and DTO contracts.
2. Backend lifecycle service, dependency preview, operation apply/schedule, due-runner, and history.
3. Admin Traditional Chinese lifecycle workbench UI and route/sidebar/API wiring.
4. Phase 35 smoke, verification docs, and milestone traceability updates.

