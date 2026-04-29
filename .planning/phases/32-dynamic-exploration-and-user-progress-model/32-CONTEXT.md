# Phase 32: Dynamic Exploration and User Progress Model - Context

**Gathered:** 2026-04-29
**Status:** Ready for planning
**Source:** `/gsd-next` routed to `/gsd-discuss-phase 32`; Default mode fallback used pragmatic recommended decisions because interactive questions are unavailable.

<domain>
## Phase Boundary

Phase 32 completes the dynamic exploration and traveler progress domain on top of the Phase 28-31 experience orchestration foundation.

This phase owns:

- Dynamic progress calculation from published `exploration_elements` and immutable `user_exploration_events`.
- Traveler profile inspection beyond the current simple user drawer: identity, locale/interface preferences, linked city/map/story scopes, progress, sessions, acquisitions, logs, and timeline.
- Persistent story-session visibility and separation of temporary story-mode state from permanent exploration facts.
- Explicit recompute and repair actions for derived progress, with preview, safety limits, and audit logging.
- Admin and public contract alignment so content lifecycle changes update percentages predictably without deleting completed user events.

This phase does not own:

- Full flagship story material production. That is Phase 33.
- Full mini-program story runtime UX acceptance. That is Phase 34 or later.
- Publish scheduling and cross-domain lifecycle approval workflows. Those remain Phase 34 or later unless needed as a narrow dependency for progress recalculation.

</domain>

<decisions>
## Implementation Decisions

### Dynamic Progress Semantics

- **D32-01:** `exploration_elements` is the denominator registry. Only published rows with `include_in_exploration=true` count toward current progress percentages.
- **D32-02:** `user_exploration_events` is the immutable permanent fact log. Completed events must not be deleted when content is unpublished, removed, or reweighted.
- **D32-03:** `user_exploration_state` is a rebuildable cache only. Planning should not treat it as the source of truth.
- **D32-04:** Progress is calculated by weighted completion: `completedWeight / availableWeight`, where weights use the semantic vocabulary already locked in prior phases: `tiny`, `small`, `medium`, `large`, `core`.
- **D32-05:** Content additions, status changes, or weight changes may change displayed percentages. Admin UI must make that explainable by showing current denominator elements, completed elements, retired/unpublished completed elements, and last recompute time.
- **D32-06:** A completed event for a now-unpublished or deleted element remains visible in history and audit/timeline views, but is excluded from the active percentage unless an operator uses a diagnostic "include inactive elements" comparison view.

### Scope Model

- **D32-07:** Phase 32 should support progress scopes for `global`, `city`, `sub_map`, `poi`, `indoor_building`, `indoor_floor`, `storyline`, `story_chapter`, `task`, `collectible`, `reward`, and `media`.
- **D32-08:** Existing Phase 28 support for `city`, `sub_map`, `storyline`, and `story_chapter` is a baseline, not the final scope set.
- **D32-09:** For scopes not yet directly represented on `exploration_elements`, planning should add schema or query support without duplicating parallel progress tables per domain.
- **D32-10:** Linked scope summaries should show both semantic names and IDs so operators can trace a progress percentage back to the authoritative map, POI, indoor, story, reward, or media entity.

### User Progress Admin Workbench

- **D32-11:** Replace the current simple `UserManagement` detail drawer with a real traveler progress workbench, or add a full-page detail route launched from the list.
- **D32-12:** The workbench should use Traditional Chinese copy and be organized around operator tasks rather than raw tables.
- **D32-13:** Required sections are: `身份與偏好`, `進度總覽`, `探索元素明細`, `故事模式 Session`, `互動時間線`, `收集與獎勵來源`, `修復與重算`, and `審計紀錄`.
- **D32-14:** The progress view must be drill-down capable. Operators should be able to start from global/city/story percentages and inspect the exact included elements, completed events, excluded retired elements, and source event payload.
- **D32-15:** The timeline view should merge check-ins, trigger logs, exploration events, story-session events, acquisition history, reward redemptions, and manual repair operations into one chronological stream with filters.
- **D32-16:** Existing user basics and test-account marking remain, but they should not be the main value of the page after Phase 32.

### Story Sessions

- **D32-17:** Storyline sessions need persistent server-side representation. Existing public `startStorylineSession` currently returns an ephemeral session ID; Phase 32 should add a durable session model or equivalent persisted state that admin can inspect.
- **D32-18:** Session progress is temporary and separate from permanent exploration. Exiting story mode can mark a session exited/reset, but must not remove check-ins, pickups, rewards, titles, or exploration events.
- **D32-19:** Session details should show storyline, current chapter, status, started/exited times, event count, completed temporary steps, and whether exit cleared temporary session state.
- **D32-20:** Public event reporting should continue to use idempotent client event IDs and should attach `storyline_session_id` when applicable.

### Recompute And Repair Safety

- **D32-21:** Manual recompute must be explicit and safe: preview affected users/scopes first, then require a deliberate confirm action.
- **D32-22:** Recompute should rebuild derived `user_exploration_state` from source elements and events. It must not mutate or delete `user_exploration_events`.
- **D32-23:** Repair actions can correct derived cache rows, link orphaned events to elements, or mark duplicate client-event submissions, but each action must write an admin audit log.
- **D32-24:** Repair actions should be scoped narrowly by user, scope type/id, storyline, or time window. Avoid broad all-user repair unless the UI labels it as high impact and records why it was run.
- **D32-25:** Every recompute or repair audit row should include operator id/name, target user/scope, action type, preview summary, result summary, reason, request IP, and timestamp.

### Backend And Public Contract

- **D32-26:** Admin backend owns operator-facing detail, recompute, repair, audit, and timeline APIs.
- **D32-27:** Public backend should expose traveler-facing exploration summaries and event recording, not admin repair internals.
- **D32-28:** Public `GET /api/v1/users/me/exploration` should evolve toward the same dynamic calculation semantics used by admin, with scope filters and element-level completion summaries.
- **D32-29:** Admin APIs should use paginated timeline and drill-down endpoints to avoid loading every event for active users in one response.
- **D32-30:** DTOs should distinguish current active progress from historical/retired completions so admin screens can explain why percentages changed after content edits.

### Data And Migration

- **D32-31:** Existing legacy `traveler_progress`, `traveler_checkins`, `trigger_logs`, reward redemption, and test-account data should be bridged into the new admin view rather than discarded.
- **D32-32:** Existing fixed `progress_percent` fields are compatibility data only. New progress displays should prefer dynamic weighted calculation.
- **D32-33:** Seed data should include at least one traveler with completed A-Ma Temple/Chapter 1 elements, one active story session, one exited session, reward/acquisition history, and a recompute audit record.
- **D32-34:** All seed SQL and JSON must remain UTF-8/utf8mb4 safe and must not be written through inline PowerShell Chinese literals.

### UI Quality

- **D32-35:** The admin UI should remain a workbench: strong hierarchy, segmented filters, cards for progress summaries, timeline grouping, and clear empty states.
- **D32-36:** Long payloads and event JSON should be collapsible with copy/view-detail actions, not dumped directly into dense tables.
- **D32-37:** Dangerous repair actions should use warning affordances, impact preview, and typed/explicit confirmation where feasible.
- **D32-38:** Phase 32 should fix shell-level Simplified Chinese/English wording surfaced in the touched user-progress screens and use Traditional Chinese.

### the agent's Discretion

- Exact Java service names, DTO names, and MyBatis mapper decomposition can follow existing admin/public backend patterns.
- The planner may choose whether the admin detail is a drawer-plus-tabs or a full route, provided it can handle deep drill-down and repair operations without becoming cramped.
- Initial recompute can be synchronous for scoped user/scope actions if it is fast locally; the schema and DTOs should leave room for queued background recompute later.
- Initial timeline aggregation can combine MySQL sources first. Mongo `user_event_logs` can be integrated if already useful, but Phase 32 should not become a Mongo logging redesign.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project Rules And Phase Scope

- `AGENTS.md` - project constraints, GSD workflow requirement, UTF-8/utf8mb4 rule, verification expectations, and dirty worktree caution.
- `.planning/PROJECT.md` - v3.0 milestone boundary and admin/public ownership.
- `.planning/REQUIREMENTS.md` - `USER-01`, `USER-02`, `USER-03`, `USER-04`, and `LINK-03`.
- `.planning/ROADMAP.md` - Phase 32 goal, dependencies, and success criteria.

### Prior Phase Decisions

- `.planning/phases/28-story-and-content-control-plane-completion/28-CONTEXT.md` - dynamic exploration semantics, permanent event vs temporary session separation, and Phase 28 foundation tables.
- `.planning/phases/29-poi-default-experience-workbench/29-CONTEXT.md` - POI default flow and semantic exploration weight expectations.
- `.planning/phases/30-storyline-mode-and-chapter-override-workbench/30-CONTEXT.md` - storyline session, chapter inheritance, and exit/reset policy decisions.
- `.planning/phases/31-interaction-task-template-library-and-governance-center/31-CONTEXT.md` - template/governance linkage and conflict visibility expectations.
- `.planning/phases/31-interaction-task-template-library-and-governance-center/31-HANDOFF.md` - Phase 31 delivered code and verification handoff.
- `.planning/phases/31-interaction-task-template-library-and-governance-center/31-VERIFICATION.md` - Phase 31 smoke and regression evidence to preserve.

### Admin User And Progress Baseline

- `packages/admin/aoxiaoyou-admin-ui/src/pages/UserManagement/index.tsx` - current user list/detail drawer to upgrade or replace.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminUserController.java` - existing admin user endpoint group.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserServiceImpl.java` - current user detail aggregation, fixed-count progress snapshots, check-ins, and trigger logs.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminUserDetailResponse.java` - current admin user detail DTO to expand or supersede.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminTestConsoleController.java` - existing progress reset/test-account operation patterns.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminSystemManagementController.java` - audit log endpoint patterns.

### Experience And Exploration Foundation

- `scripts/local/mysql/init/39-phase-28-experience-orchestration.sql` - `exploration_elements`, `user_exploration_events`, `user_exploration_state`, and seed examples.
- `scripts/local/mysql/init/40-phase-29-poi-default-experience.sql` - POI default-flow seed data to include in progress examples.
- `scripts/local/mysql/init/41-phase-30-storyline-mode-overrides.sql` - story mode and chapter override seed data to include in session/progress examples.
- `scripts/local/mysql/init/42-phase-31-interaction-template-governance.sql` - template/governance seed data to preserve.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/ExplorationElement.java` - admin exploration element entity.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminExperienceOrchestrationController.java` - current admin exploration element CRUD endpoint group.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminExperienceOrchestrationServiceImpl.java` - current exploration element admin service and vocabulary handling.

### Public Runtime Baseline

- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java` - public event reporting, story sessions, and user exploration endpoints.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java` - current dynamic calculation and event idempotency behavior.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/UserExplorationResponse.java` - public exploration response shape to align with admin semantics.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/UserExplorationEvent.java` - immutable public exploration event entity.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/UserServiceImpl.java` - legacy user progress/check-in/reward state that Phase 32 must bridge instead of silently bypassing.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- `AdminUserController` and `AdminUserServiceImpl`: already provide list/detail/test-account flag paths and should become the admin progress workbench facade or be split into a dedicated progress service.
- `UserManagement/index.tsx`: already has a ProTable user list and detail entry point, but the drawer is too narrow for Phase 32 drill-down and currently contains Simplified Chinese copy.
- `AdminUserDetailResponse`: current detail DTO already groups basic info, progress snapshots, recent check-ins, and trigger logs; it can be expanded or replaced by richer workbench DTOs.
- `PublicExperienceServiceImpl#getUserExploration`: already calculates weighted progress from published exploration elements and user events for some scopes.
- `PublicExperienceServiceImpl#recordEvent`: already has `client_event_id` idempotency and JSON payload validation.
- `exploration_elements`, `user_exploration_events`, and `user_exploration_state`: Phase 28 created the right conceptual tables but Phase 32 must make them operational for admin inspection and repair.
- `SysOperationLog` patterns in test-account marking and system management can be reused for recompute/repair audit records.

### Established Patterns

- Admin backend uses `ApiResponse<T>`, `PageResponse<T>`, controller/service/mapper layering, and MyBatis-Plus query wrappers.
- Admin UI uses Ant Design Pro, `PageContainer`, `ProTable`, explicit routes in `App.tsx`, request helpers in `services/api.ts`, and DTOs in `types/admin.ts`.
- Public backend and admin backend maintain parallel entities/mappers rather than a shared Java library, so Phase 32 planning should update both sides where runtime/admin alignment requires it.
- Verification is smoke-script and live-service oriented; the plan should include backend compile, admin UI build, SQL seed import, API smoke, and browser route checks where UI changes are made.

### Integration Points

- User/progress management route should remain under the admin control plane and become the authoritative operator surface for traveler progress.
- Dynamic progress must integrate with Phase 28-31 experience, POI, story, and governance tables rather than rebuilding a separate isolated user-progress model.
- Public event/reporting contracts must stay compatible with Phase 34 mini-program consumption, but full mini-program UI acceptance remains deferred.
- Recompute/repair audit should connect to existing system audit logs and admin identity from `AdminAuthInterceptor`.

</code_context>

<specifics>
## Specific Ideas

- A traveler detail should answer "why is this user 42.86% complete in this city?" by showing the included exploration elements, their weights, which are completed, which event completed them, and which content changes affected the denominator.
- Story session detail should show active/exited state and make clear that exit cleared only temporary session state, not permanent acquisitions or exploration events.
- Repair UX should feel closer to a guarded operations panel than a normal edit form: preview impact first, then run, then show audit/result.
- Retired completed elements should not disappear. They should appear as historical achievements with a label such as `已退役，不計入目前百分比`.
- Use meaningful seeded test data so the page is not empty: one active traveler, one test traveler, A-Ma Chapter 1 completions, pickups, reward/title acquisitions, active/exited sessions, and a recompute audit example.

</specifics>

<deferred>
## Deferred Ideas

- Full publish scheduling, approval chains, and dependency-aware lifecycle operations belong to Phase 34 or later unless Phase 32 needs a narrow status hook for denominator calculations.
- Full mini-program rendering of progress dashboards and story session UI remains future runtime scope.
- A background job queue for massive all-user recomputation can be deferred if scoped recompute is reliable and the contracts leave room for async execution.
- Advanced GIS/spatial recomputation for movement traces can be deferred; Phase 32 should still expose stored traces/events that already exist.

</deferred>

---

*Phase: 32-dynamic-exploration-and-user-progress-model*
*Context gathered: 2026-04-29*
