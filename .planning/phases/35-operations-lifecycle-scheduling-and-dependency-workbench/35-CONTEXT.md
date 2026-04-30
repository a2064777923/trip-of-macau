# Phase 35: Operations Lifecycle Scheduling and Dependency Workbench - Context

**Gathered:** 2026-04-30
**Status:** Ready for planning
**Source:** `/gsd-plan-phase 35`; Phase 35 was added by the v3.0 milestone gap plan to close `.planning/v3.0-MILESTONE-AUDIT.md` gaps `OPS-02` and `OPS-04`.

<domain>
## Phase Boundary

Phase 35 is a focused v3.0 gap-closure phase. It owns the admin and backend operations lifecycle subsystem required to schedule publish, unpublish, and remove actions across content-bearing domains with dependency-aware preview and auditable history.

This phase owns:

- A canonical admin lifecycle operation model for immediate and scheduled publish, unpublish, and remove actions.
- A cross-domain lifecycle status vocabulary and transition contract for admin-controlled content.
- Dependency impact preview before every destructive or public-visibility-changing lifecycle action.
- Lifecycle audit/history records for immediate and scheduled operations.
- A Traditional Chinese admin workbench under the operations area for selecting targets, previewing impact, scheduling actions, applying immediate actions, and inspecting history.
- Public runtime alignment checks so traveler-facing APIs continue to filter unpublished/deleted content after lifecycle transitions.
- Local smoke verification for at least one preview -> apply/schedule -> audit/history -> public filtering flow.

This phase does not own:

- A full multi-role approval workflow. The `reviewing` state may exist as a lifecycle state, but approval chains, reviewer assignment, and review inboxes remain future scope unless needed for a safe transition guard.
- Full WeChat DevTools/device experiential UAT. That acceptance slice remains explicitly deferred beyond v3.0.
- Rebuilding existing story, POI, indoor, reward, media, AI, or progress editors.
- Physical deletion as the default removal strategy. Removal should be a lifecycle transition to `deleted` where supported, with explicit impact preview.
- Large new content creation or seeded story material production.

</domain>

<decisions>
## Implementation Decisions

### Lifecycle Scope

- **D35-01:** Phase 35 closes `OPS-02` and `OPS-04` together because scheduling, dependency preview, consistent status vocabulary, and lifecycle history are one operations lifecycle subsystem.
- **D35-02:** Supported lifecycle targets must include at least `city`, `sub_map`, `poi`, `indoor_building`, `indoor_floor`, `indoor_node`, `storyline`, `story_chapter`, `content_block`, `content_asset`, `experience_flow`, `experience_flow_step`, `experience_binding`, `experience_override`, `collectible`, `reward`, `game_reward`, `redeemable_prize`, `honor`, and `activity` where tables/entities exist.
- **D35-03:** Phase 35 must be cross-domain but pragmatic. If a target table lacks one of the canonical statuses or columns, execution may add compatibility mapping rather than blocking the whole workbench.
- **D35-04:** The lifecycle vocabulary is `editing`, `reviewing`, `published`, `unpublished`, and `deleted`. Existing `draft` maps to `editing`; existing `archived` maps to `unpublished` for operator-facing UI, while old values may remain readable for compatibility during migration.
- **D35-05:** New content created by existing editors should remain or become non-public by default. Phase 35 should not accidentally publish drafts while normalizing status labels.

### Operation Semantics

- **D35-06:** Every lifecycle action must be preview-first. The apply/schedule endpoint should either require a preview token/hash or repeat the preview calculation server-side before mutation.
- **D35-07:** Supported actions are `publish`, `unpublish`, and `remove`. `remove` means lifecycle deletion (`deleted`) by default, not physical delete.
- **D35-08:** Immediate actions and scheduled actions share the same operation record and audit model. Scheduled operations can start as `scheduled`, then become `applied`, `failed`, or `cancelled`.
- **D35-09:** A scheduled operation without a background scheduler may still be useful if stored and inspectable, but the execution plan should include a lightweight due-operation runner so local verification can apply due jobs.
- **D35-10:** Deleting or unpublishing a parent target must list inbound bindings, downstream children, public runtime effects, and exploration/progress implications before confirmation.

### Dependency Preview

- **D35-11:** `content_relation_links` is the canonical cross-domain dependency source where relation rows exist.
- **D35-12:** Preview must also include structural child relationships that are not fully expressed in `content_relation_links`, such as city -> sub-map -> POI, indoor building -> floor -> node, storyline -> chapter, experience flow -> steps, and reward condition/group links.
- **D35-13:** Dependency preview should classify impact into `blocking`, `warning`, and `info`, not just count rows.
- **D35-14:** The preview must distinguish inbound dependencies, downstream children, public runtime visibility, exploration/progress elements, reward/collectible consequences, and media references.
- **D35-15:** If a target is already public and operator requests `unpublish` or `remove`, the preview should explicitly state which traveler-facing routes or runtime DTOs will stop exposing it.

### Backend Design

- **D35-16:** Add a dedicated admin lifecycle controller/service rather than extending one domain controller at a time.
- **D35-17:** Preferred endpoint base is `/api/admin/v1/operations/lifecycle`.
- **D35-18:** Preferred tables are `content_lifecycle_operations` and `content_lifecycle_operation_impacts`.
- **D35-19:** Operation payloads and impacts must be JSON-versioned with `schemaVersion: 1` where structured JSON is stored.
- **D35-20:** New backend DTOs should use request/response packages and follow existing `ApiResponse<T>` / `PageResponse<T>` patterns.
- **D35-21:** Any admin-authored reason/note fields must be length-bounded and escaped through normal JSON serialization. No raw SQL string concatenation.

### Admin UI Design

- **D35-22:** The admin workbench route should be `/ops/lifecycle`, with sidebar label `生命週期與發布排程`.
- **D35-23:** UI copy must be Traditional Chinese.
- **D35-24:** The workbench should feel like an operations console, not a CRUD table dump: target selector, impact preview drawer, schedule/apply panel, dependency tree/cards, and history tab.
- **D35-25:** Filters must have visible labels. Key filters: 主體類型, 目前狀態, 操作, 排程狀態, 城市, 子地圖, 故事線, 關鍵字.
- **D35-26:** Long codes, object keys, route paths, and binding names must use ellipsis plus tooltip/title instead of overflowing cards.
- **D35-27:** Destructive operations must show impact preview and confirmation copy before apply/schedule. The UI must not offer blind one-click delete.

### Verification

- **D35-28:** Verification must include admin backend compile, admin UI build, and a repeatable local smoke script.
- **D35-29:** Smoke should import required Phase 28-33 seed SQL plus Phase 35 schema/seed with `--default-character-set=utf8mb4`.
- **D35-30:** Smoke should cover at least one target from the seeded flagship story, preferably `east_west_war_and_coexistence`, because it has story, content, experience, rewards, media, and exploration dependencies.
- **D35-31:** Phase 35 may mark `OPS-02` and `OPS-04` complete only after preview, lifecycle mutation/scheduling, audit/history, and public filtering checks are verified.

### the agent's Discretion

- The executor may decide whether to implement scheduled execution through a Spring scheduled bean, an explicit admin endpoint, or both, provided local smoke can deterministically apply due operations.
- The exact dependency preview depth can be capped for performance, but counts and representative records must be returned.
- If not every older table can be migrated to the full status set in one pass, the executor may implement compatibility mapping while exposing canonical labels in the workbench.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project And Gap Scope

- `AGENTS.md` - project constraints, UTF-8/utf8mb4 rule, verification expectations, and GSD workflow requirement.
- `.planning/PROJECT.md` - v3.0 milestone boundary and admin/public ownership.
- `.planning/REQUIREMENTS.md` - `OPS-02` and `OPS-04` pending requirements.
- `.planning/ROADMAP.md` - Phase 35 goal, success criteria, and dependencies.
- `.planning/STATE.md` - current milestone state and deferred WeChat acceptance note.
- `.planning/v3.0-MILESTONE-AUDIT.md` - source audit gaps `OPS-02`, `OPS-04`, and `OPS-LIFECYCLE-01`.

### Prior v3.0 Domains

- `.planning/phases/28-story-and-content-control-plane-completion/28-CONTEXT.md` - shared story/experience/content/media foundation.
- `.planning/phases/29-poi-default-experience-workbench/29-CONTEXT.md` - POI default experience workbench and A-Ma Temple acceptance pattern.
- `.planning/phases/30-storyline-mode-and-chapter-override-workbench/30-CONTEXT.md` - story chapter inheritance and override semantics.
- `.planning/phases/31-interaction-task-template-library-and-governance-center/31-CONTEXT.md` - governance and conflict concepts for experience rules.
- `.planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md` - dynamic exploration elements and user progress state.
- `.planning/phases/33-complete-flagship-story-content-material-package/33-CONTEXT.md` - flagship story and material package fixture.
- `.planning/phases/34-public-runtime-and-mini-program-consumption-baseline/34-CONTEXT.md` - public runtime filtering and deferred full WeChat acceptance.

### Admin Backend

- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/enums/ContentStatus.java` - current status enum requiring lifecycle expansion/compatibility.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/content/ContentLifecycleStatusSupport.java` - current limited manual status helper.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminOperationsController.java` - existing operations controller route base.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminOperationsService.java` - current operations service contract for activities.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminOperationsServiceImpl.java` - current operations implementation patterns.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/ContentRelationLink.java` - cross-domain relation source.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/ExplorationElement.java` - exploration/progress impact source.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/StoryLine.java` - story lifecycle target.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/StoryChapter.java` - chapter lifecycle target.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/Poi.java` - POI lifecycle target.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/SubMap.java` - sub-map lifecycle target.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/IndoorFloor.java` - indoor floor lifecycle target.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/IndoorNode.java` - indoor node lifecycle target.
- `scripts/local/mysql/init/39-phase-28-experience-orchestration.sql` - experience foundation schema and statuses.
- `scripts/local/mysql/init/43-phase-32-progress-engine.sql` - exploration element schema and status filtering.
- `scripts/local/mysql/init/49-phase-33-east-west-flagship-story.sql` - flagship fixture used for lifecycle smoke.

### Admin UI

- `packages/admin/aoxiaoyou-admin-ui/src/App.tsx` - route registry.
- `packages/admin/aoxiaoyou-admin-ui/src/layouts/DefaultLayout.tsx` - sidebar registry and Traditional Chinese IA.
- `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts` - admin API wrappers.
- `packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts` - admin DTO types.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/OperationsManagement/index.tsx` - current operations page pattern.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Experience/ExperienceGovernanceCenter.tsx` - existing governance filter/detail UI pattern.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryMaterialPackageManagement.tsx` - recent polished admin inspection page pattern.

### Public Runtime

- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java` - public runtime published filtering to verify after lifecycle transitions.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/StoryLineServiceImpl.java` - public story list/detail published filtering.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java` - public storyline runtime endpoint.
- `scripts/local/smoke-phase-34-public-runtime.ps1` - public runtime smoke pattern to extend or reuse.

</canonical_refs>

<code_context>
## Existing Code Insights

- Current `ContentStatus` only has `draft`, `published`, and `archived`, which is insufficient for the requested lifecycle vocabulary.
- `ContentLifecycleStatusSupport` currently only treats `published` and `archived` as manually operable, so Phase 35 must replace or extend this rather than layering UI on top.
- The admin operations controller currently only manages activities/tasks under `/api/admin/v1/operations`; lifecycle should be added under the same operations domain without breaking existing activity endpoints.
- `content_relation_links` already stores owner/target/relation triples and can power inbound dependency previews.
- v3.0 introduced many cross-domain relation-backed models: experience flows, bindings, overrides, exploration elements, rewards, content blocks, assets, and material package items.
- The admin UI already has an operations group with test console, activities, and sandbox. Phase 35 should add a fourth concrete item instead of replacing existing operations pages.
- Public runtime filtering was hardened in Phase 34, but admin-side lifecycle operations remain missing.

</code_context>

<specifics>
## Specific Ideas

- Workbench label: `生命週期與發布排程`.
- Endpoint base: `/api/admin/v1/operations/lifecycle`.
- Migration file: `scripts/local/mysql/init/50-phase-35-lifecycle-operations.sql`.
- Smoke script: `scripts/local/smoke-phase-35-lifecycle.ps1`.
- Main backend service: `AdminLifecycleOperationService`.
- Main frontend page: `packages/admin/aoxiaoyou-admin-ui/src/pages/OperationsLifecycle/OperationsLifecycleWorkbench.tsx`.
- Preferred target status labels:
  - `editing` -> `編輯中`
  - `reviewing` -> `審批中`
  - `published` -> `已發布`
  - `unpublished` -> `未發布`
  - `deleted` -> `已刪除`
- Preferred operation labels:
  - `publish` -> `發布`
  - `unpublish` -> `下線`
  - `remove` -> `移除`
- Preferred impact categories:
  - `inbound_relation` -> `被其他內容綁定`
  - `downstream_child` -> `下游子內容`
  - `public_runtime` -> `小程序公開內容`
  - `exploration_progress` -> `探索度與用戶進度`
  - `reward_rule` -> `獎勵與收集物規則`
  - `media_reference` -> `媒體與附件引用`

</specifics>

<deferred>
## Deferred Ideas

- Full approval workflow with reviewers, comments, SLA, and notification.
- Full WeChat DevTools/device journey UAT.
- Public mini-program UI controls for operator lifecycle history.
- Version rollback/diff UI beyond audit/history records.
- Background distributed job scheduling with clustering/locks beyond a local-safe due-operation runner.

</deferred>

---

*Phase: 35-operations-lifecycle-scheduling-and-dependency-workbench*
*Context gathered: 2026-04-30*
