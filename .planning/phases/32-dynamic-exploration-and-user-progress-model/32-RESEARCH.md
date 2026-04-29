# Phase 32: Dynamic Exploration and User Progress Model - Research

**Researched:** 2026-04-29  
**Domain:** Dynamic traveler progress, exploration semantics, timeline aggregation, and admin/public contract alignment [VERIFIED: .planning/ROADMAP.md; .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md]  
**Confidence:** MEDIUM

<user_constraints>
## User Constraints (from CONTEXT.md)

Verbatim copy from `.planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md`. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md]

### Locked Decisions

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

### Claude's Discretion

- Exact Java service names, DTO names, and MyBatis mapper decomposition can follow existing admin/public backend patterns.
- The planner may choose whether the admin detail is a drawer-plus-tabs or a full route, provided it can handle deep drill-down and repair operations without becoming cramped.
- Initial recompute can be synchronous for scoped user/scope actions if it is fast locally; the schema and DTOs should leave room for queued background recompute later.
- Initial timeline aggregation can combine MySQL sources first. Mongo `user_event_logs` can be integrated if already useful, but Phase 32 should not become a Mongo logging redesign.

### Deferred Ideas (OUT OF SCOPE)

- Full publish scheduling, approval chains, and dependency-aware lifecycle operations belong to Phase 34 or later unless Phase 32 needs a narrow status hook for denominator calculations.
- Full mini-program rendering of progress dashboards and story session UI remains future runtime scope.
- A background job queue for massive all-user recomputation can be deferred if scoped recompute is reliable and the contracts leave room for async execution.
- Advanced GIS/spatial recomputation for movement traces can be deferred; Phase 32 should still expose stored traces/events that already exist.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| USER-01 | Operators can inspect a traveler profile with identity basics, locale and interface preferences, linked cities and maps, and recent activity context. [VERIFIED: .planning/REQUIREMENTS.md] | Full-page admin workbench, new admin-side preference/read-model bridge, linked scope summaries, and paginated activity endpoints in `## Architecture Patterns`, `## Common Pitfalls`, and `## Validation Architecture`. [VERIFIED: .planning/REQUIREMENTS.md; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserServiceImpl.java; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/UserPreference.java] |
| USER-02 | Operators can inspect exploration and completion progress by city, sub-map, POI, indoor map, storyline, chapter, task, collectible, and reward domain with drill-down detail. [VERIFIED: .planning/REQUIREMENTS.md] | Canonical weighted calculator, scope expansion on `exploration_elements`, active-vs-retired denominator split, and drill-down endpoints in `## Summary`, `## Standard Stack`, and `## Architecture Patterns`. [VERIFIED: .planning/REQUIREMENTS.md; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java; scripts/local/mysql/init/39-phase-28-experience-orchestration.sql] |
| USER-03 | Operators can inspect traveler interaction logs, movement or route traces, acquisition history, and major state changes through one coherent timeline. [VERIFIED: .planning/REQUIREMENTS.md] | Timeline fan-in from check-ins, trigger logs, exploration events, sessions, reward redemptions, and repair audits, with route traces treated as an explicit open question if no verified source table exists. [VERIFIED: .planning/REQUIREMENTS.md; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserServiceImpl.java; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/UserExplorationEvent.java; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/RewardRedemption.java] |
| USER-04 | Operators can trigger safe recomputation or repair actions for derived traveler progress and keep an audit trail of those manual operations. [VERIFIED: .planning/REQUIREMENTS.md] | Preview-first recompute/repair endpoints, immutable event log protection, audit persistence, and targeted backend tests in `## Architecture Patterns`, `## Don't Hand-Roll`, `## Validation Architecture`, and `## Security Domain`. [VERIFIED: .planning/REQUIREMENTS.md; .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/SysOperationLog.java] |
| LINK-03 | Derived availability and traveler progress update predictably when source content changes, without hidden manual patching or stale counters. [VERIFIED: .planning/REQUIREMENTS.md] | One canonical denominator model, rebuildable cache semantics, inactive-element diagnostics, and parity tests between admin and public calculations in `## Summary`, `## Common Pitfalls`, and `## Validation Architecture`. [VERIFIED: .planning/REQUIREMENTS.md; .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java] |
</phase_requirements>

## Summary

Phase 32 should be planned as a data-model and operator-workbench phase, not as a cosmetic extension of the current user drawer. The existing admin UI still exposes `/users/progress` through a narrow `Drawer` with width `560` and legacy snapshot fields, while the admin backend still assembles detail data from `TravelerProfile`, `TravelerProgress`, `TravelerCheckin`, and `TriggerLog` rather than from the dynamic exploration foundation. [VERIFIED: packages/admin/aoxiaoyou-admin-ui/src/pages/UserManagement/index.tsx; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserServiceImpl.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminUserDetailResponse.java]

The public backend already proves the right progress semantics for Phase 32: `PublicExperienceServiceImpl#getUserExploration` filters published `exploration_elements`, resolves semantic weights, and computes `completedWeight / availableWeight`, but it currently only filters `city`, `sub_map`, `storyline`, and `story_chapter`, and the public session DTO is still ephemeral. [VERIFIED: packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/UserExplorationResponse.java; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StorylineSessionResponse.java]

The plan should therefore center on four deliverables: a full-page traveler progress workbench in Traditional Chinese, one canonical weighted progress-calculation service reused by admin and public code paths, durable story-session persistence that remains separate from permanent exploration facts, and preview-first recompute/repair endpoints with structured audit rows. Existing user preferences and reward redemptions already live in the public backend domain, so Phase 32 also needs admin-side read models for those tables rather than a UI-only retrofit. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md; packages/admin/aoxiaoyou-admin-ui/src/App.tsx; packages/admin/aoxiaoyou-admin-ui/src/layouts/DefaultLayout.tsx; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/UserServiceImpl.java; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/UserPreference.java; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/RewardRedemption.java]

**Primary recommendation:** Build Phase 32 around a full-page admin workbench backed by a single MySQL-based progress engine that treats `user_exploration_events` as immutable truth, `user_exploration_state` as rebuildable cache, and story sessions as a new durable operator-visible state model. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md; scripts/local/mysql/init/39-phase-28-experience-orchestration.sql; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java]

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Traveler progress workbench UI | Browser / Client | Frontend Server (none in this repo) | The admin UI owns operator navigation, Traditional Chinese copy, drill-down cards, filters, and guarded repair affordances, and it already routes `/users/progress` directly in the SPA. [VERIFIED: packages/admin/aoxiaoyou-admin-ui/src/App.tsx; packages/admin/aoxiaoyou-admin-ui/src/layouts/DefaultLayout.tsx; packages/admin/aoxiaoyou-admin-ui/src/pages/UserManagement/index.tsx] |
| Operator-facing traveler detail, timeline, recompute, and repair APIs | API / Backend | Database / Storage | Phase 32 decisions explicitly assign detail, recompute, repair, audit, and timeline APIs to the admin backend, and the current admin controller/service layer is already the operator facade pattern to extend. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminUserController.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserServiceImpl.java] |
| Canonical weighted progress calculation | API / Backend | Database / Storage | The dynamic math already exists in `PublicExperienceServiceImpl`, but predictable admin/public parity requires one canonical service or shared algorithm rather than divergent duplicate calculations. [VERIFIED: packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java; .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md] |
| Exploration denominator and completion facts | Database / Storage | API / Backend | `exploration_elements` and `user_exploration_events` are already the foundational tables for denominator and immutable completion facts, while `user_exploration_state` is explicitly cache-only. [VERIFIED: scripts/local/mysql/init/39-phase-28-experience-orchestration.sql; .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md] |
| Story-session durability and inspection | Database / Storage | API / Backend | Session state must become persistent and operator-visible, but public runtime endpoints should stay limited to traveler-facing session start/exit and event recording. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StorylineSessionResponse.java] |
| Traveler-facing summary and event recording | API / Backend | Database / Storage | The public backend already owns `/api/v1/users/me/exploration` and event/session endpoints and should keep admin repair internals out of its contract. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java] |
| Auditability of manual repair operations | API / Backend | Database / Storage | Operator identity, request metadata, preview summaries, and result summaries should be persisted server-side alongside the repair execution path. Existing admin operation-log patterns already show where this belongs. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserServiceImpl.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminSystemManagementController.java] |

## Standard Stack

### Core

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Spring Boot | 3.2.4 (repo pin) | Admin and public HTTP APIs, validation, actuator, and runtime config | Both Java services already standardize on Spring Boot 3.2.4, and the project explicitly forbids stack replacement in this milestone. [VERIFIED: packages/server/pom.xml; packages/admin/aoxiaoyou-admin-backend/pom.xml; AGENTS.md] |
| MyBatis-Plus | 3.5.6 (repo pin) | Mapper-layer queries and DTO assembly | Both backends already use MyBatis-Plus mappers and query wrappers, so Phase 32 should extend existing mapper/service patterns instead of introducing a new persistence abstraction. [VERIFIED: packages/server/pom.xml; packages/admin/aoxiaoyou-admin-backend/pom.xml; AGENTS.md] |
| MySQL | 8.0.x local runtime | Operational source of truth for progress, sessions, audits, seeds, and admin/public linkage | MySQL is the locked primary operational store for this cutover, and the relevant exploration tables already live there with utf8mb4 collation. [VERIFIED: AGENTS.md; scripts/local/mysql/init/39-phase-28-experience-orchestration.sql; live runtime probes on 2026-04-29] |
| React 18 + Ant Design Pro | `react` `^18.3.1`, `antd` `^5.24.6`, `@ant-design/pro-components` `^2.8.7` (repo pins) | Admin workbench UI, `ProTable`, `PageContainer`, drawer-to-workbench replacement | The admin SPA already uses React 18, Ant Design, and Ant Design Pro patterns throughout; preserving that stack is lower risk than importing a new admin UI framework. [VERIFIED: packages/admin/aoxiaoyou-admin-ui/package.json; packages/admin/aoxiaoyou-admin-ui/src/pages/UserManagement/index.tsx; AGENTS.md] |
| React Query | `^5.71.0` (repo pin) | Admin-side data loading, mutations, and cache invalidation for detail/timeline/recompute workflows | The admin UI already carries React Query in its dependency set, which is the right cache layer for drill-down pages and post-recompute refreshes. [VERIFIED: packages/admin/aoxiaoyou-admin-ui/package.json] |

### Supporting

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Springdoc OpenAPI | 2.3.0 (repo pin) | Inspect and smoke-test new admin/public endpoints during execution | Use for Phase 32 endpoint verification and contract inspection once detail, timeline, and repair APIs exist. [VERIFIED: packages/server/pom.xml; packages/admin/aoxiaoyou-admin-backend/pom.xml; packages/server/src/main/resources/application.yml; packages/admin/aoxiaoyou-admin-backend/src/main/resources/application.yml] |
| `java-jwt` | 4.4.0 (repo pin) | Existing JWT auth on admin/public surfaces | Reuse existing auth handling; do not add a separate session/auth scheme for repair endpoints. [VERIFIED: packages/server/pom.xml; packages/admin/aoxiaoyou-admin-backend/pom.xml] |
| Axios request helper | repo-local | Admin UI API wrapper and token handling | Extend `src/services/api.ts` rather than adding a second client layer for progress endpoints. [VERIFIED: packages/admin/aoxiaoyou-admin-ui/src/services/api.ts; packages/admin/aoxiaoyou-admin-ui/src/utils/request.ts] |
| `SysOperationLog` pattern | repo-local | Audit trail seed for manual repair/recompute actions | Reuse the existing operation-log pattern for operator identity and request metadata, then add Phase 32 structured fields where needed. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserServiceImpl.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminSystemManagementController.java] |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Full-page progress workbench | Keep the current drawer and add tabs | The current drawer is only `560` pixels wide and already overloaded with legacy summaries, so deep drill-down, timeline filtering, and guarded repair UX will be cramped and harder to audit. [VERIFIED: packages/admin/aoxiaoyou-admin-ui/src/pages/UserManagement/index.tsx; .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md] |
| Extending `exploration_elements` scope support | Parallel per-domain progress tables | Separate progress tables would drift from the locked denominator model and make `LINK-03` harder to guarantee. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md; scripts/local/mysql/init/39-phase-28-experience-orchestration.sql] |
| Canonical shared progress logic | Independent admin and public calculators | Duplicated math would make content lifecycle changes unpredictable and create parity bugs between `/api/v1/users/me/exploration` and admin detail views. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java] |
| Dedicated durable story-session storage | Continue returning only ephemeral `sessionId` strings | The current public response only returns `storylineId`, `sessionId`, and `status`, which is insufficient for operator inspection, exit-state visibility, or event-count auditing. [VERIFIED: packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StorylineSessionResponse.java; .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md] |

**Installation:**
```bash
npm install --prefix packages/admin/aoxiaoyou-admin-ui
mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml
mvn -q -DskipTests compile -f packages/server/pom.xml
```

The research recommendation does not require adopting new frameworks; it extends the existing manifests and Maven builds already in the repo. [VERIFIED: packages/admin/aoxiaoyou-admin-ui/package.json; packages/admin/aoxiaoyou-admin-backend/pom.xml; packages/server/pom.xml]

**Version verification:** The brownfield pins remain the implementation target for Phase 32, but current npm registry heads were checked so the planner knows these are intentional non-upgrades: `antd` `6.3.7` published 2026-04-27, `@ant-design/pro-components` `2.8.10` published 2025-07-17, `@tanstack/react-query` `5.100.6` published 2026-04-28, and `react-router-dom` `7.14.2` published 2026-04-21, while the repo currently pins older compatible versions. [VERIFIED: npm registry; packages/admin/aoxiaoyou-admin-ui/package.json]

## Architecture Patterns

### System Architecture Diagram

Recommended Phase 32 flow, grounded in existing ownership boundaries and current endpoints. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md; packages/admin/aoxiaoyou-admin-ui/src/App.tsx; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java]

```text
Admin operator
  -> Admin UI workbench (/users/progress -> traveler detail route)
    -> Admin backend traveler-progress APIs
      -> Profile/preference bridge queries
      -> Canonical progress calculator
        -> exploration_elements (published + include_in_exploration)
        -> user_exploration_events (immutable fact log)
        -> user_exploration_state (rebuildable cache)
        -> durable story-session store
      -> Timeline fan-in service
        -> traveler_checkins
        -> trigger_logs
        -> user_exploration_events
        -> reward_redemptions
        -> session events
        -> repair/recompute audit rows
      -> Preview-first recompute/repair service
        -> audit persistence

Traveler app
  -> Public backend event/session APIs
    -> record immutable exploration events
    -> return traveler-facing exploration summary
    -> never expose admin repair internals
```

### Recommended Project Structure

Extend the existing package boundaries instead of creating a shared cross-service module. [VERIFIED: AGENTS.md; packages/admin/aoxiaoyou-admin-ui/src/App.tsx; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminUserController.java; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java]

```text
packages/
├── admin/aoxiaoyou-admin-ui/src/pages/UserProgress/      # Full-page traveler workbench UI
├── admin/aoxiaoyou-admin-ui/src/services/api.ts          # New detail/timeline/recompute endpoints
├── admin/aoxiaoyou-admin-ui/src/types/admin.ts           # Rich traveler progress DTOs
├── admin/aoxiaoyou-admin-backend/src/main/java/.../user  # Detail, timeline, repair controllers/services/mappers
├── server/src/main/java/.../experience                   # Shared progress semantics + public summary/session contracts
└── scripts/local/mysql/init/43-phase-32-*.sql           # Schema + seed additions for sessions/audits/scope support
```

### Pattern 1: Canonical Weighted Progress Read Model

**What:** Use one weighted progress calculator for both admin inspection and public summary generation, with `exploration_elements` as the active denominator registry and `user_exploration_events` as immutable completion facts. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java; scripts/local/mysql/init/39-phase-28-experience-orchestration.sql]

**When to use:** Any endpoint or repair preview that reports city, sub-map, POI, indoor, storyline, chapter, task, collectible, reward, media, or global progress should call the same canonical logic rather than re-deriving counts separately. [VERIFIED: .planning/REQUIREMENTS.md; .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md]

**Example:**
```java
// Source pattern verified in:
// packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java
List<ExplorationElement> elements = selectPublishedExplorationElements(scopeType, scopeId);
List<ElementProgress> elementProgress = elements.stream()
    .map(element -> {
        int weight = resolveExplorationWeightValue(element.getWeightLevel(), element.getWeightValue());
        boolean completed = completedIds.contains(element.getId()) || completedCodes.contains(element.getElementCode());
        return new ElementProgress(element.getId(), element.getElementCode(), weight, completed);
    })
    .toList();

int availableWeight = elementProgress.stream().mapToInt(ElementProgress::getWeightValue).sum();
int completedWeight = elementProgress.stream()
    .filter(ElementProgress::isCompleted)
    .mapToInt(ElementProgress::getWeightValue)
    .sum();
double progressPercent = availableWeight == 0 ? 0 : Math.round((completedWeight * 10000.0 / availableWeight)) / 100.0;
```

### Pattern 2: Timeline Fan-In With Paginated Typed Events

**What:** Build a server-side timeline aggregator that normalizes heterogeneous rows into one paginated event stream with a `type`, `occurredAt`, `scope`, `summary`, and `detail` payload instead of sending one giant detail DTO with embedded raw lists. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserServiceImpl.java]

**When to use:** Use this pattern for `USER-03`, for workbench filters, and for operator debugging where a traveler has many exploration events or multiple story sessions. [VERIFIED: .planning/REQUIREMENTS.md; .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md]

**Example:** Start with MySQL sources already verified in the codebase: `traveler_checkins`, `trigger_logs`, `user_exploration_events`, `reward_redemptions`, durable session rows, and repair/recompute audit rows; add Mongo-backed route-trace sources only if a concrete Phase 32 data source is confirmed. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserServiceImpl.java; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/UserExplorationEvent.java; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/RewardRedemption.java; packages/admin/aoxiaoyou-admin-backend/src/main/resources/application.yml]

### Anti-Patterns to Avoid

- **Parallel progress math paths:** Do not keep one percentage formula in admin and another in public; planner tasks should force a single canonical calculator or a shared algorithm contract. [VERIFIED: packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java; .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md]
- **UI-only repair semantics:** Do not let the UI recompute percentages client-side or infer repair results from stale detail payloads; all preview and confirm actions belong on the admin backend. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md]
- **One giant traveler response:** Do not return every check-in, trigger log, exploration event, reward redemption, and session row in the initial page load; Phase 32 decisions explicitly call for paginated timeline and drill-down endpoints. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md]
- **Continuing Simplified Chinese user-progress copy:** The current drawer title still shows `用户详情`; touched screens must move to Traditional Chinese in Phase 32. [VERIFIED: packages/admin/aoxiaoyou-admin-ui/src/pages/UserManagement/index.tsx; AGENTS.md]

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Dynamic progress denominators | Separate hardcoded `+10%` counters or per-domain percentage tables | `exploration_elements` + semantic weights + `user_exploration_events` + rebuildable `user_exploration_state` | Phase 28 already created the right abstraction, and Phase 32 decisions explicitly lock this model in. [VERIFIED: scripts/local/mysql/init/39-phase-28-experience-orchestration.sql; .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md] |
| Event de-duplication | Client-side “best effort” retry tracking only | Database idempotency on `(user_id, client_event_id)` | The schema already has a unique key, which is safer than trusting client behavior for permanent exploration facts. [VERIFIED: scripts/local/mysql/init/39-phase-28-experience-orchestration.sql] |
| Manual repair audit | Ad hoc console prints or free-text admin notes | Structured audit persistence following `SysOperationLog` patterns, with Phase 32 target/scope/result fields | Repair/recompute operations are high-impact and must be attributable and reviewable. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserServiceImpl.java; .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md] |
| New preferences/rewards shadow models | Frontend-only derived objects detached from database truth | Admin-side mappers/entities/DTOs that read existing `user_preferences` and `reward_redemptions` tables | Those facts already exist in the public backend domain and should be surfaced, not recopied into mock state. [VERIFIED: packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/UserServiceImpl.java; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/UserPreference.java; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/RewardRedemption.java] |
| Session durability | Reusing only the `storyline_session_id` string attached to events | A dedicated durable story-session model plus session-event linkage | The current public session response has no persisted timestamps, chapter pointer, or exit-state metadata. [VERIFIED: packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StorylineSessionResponse.java; .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md] |

**Key insight:** Phase 32 already has most of its primitives in the codebase, so the risk is not missing infrastructure but semantic drift between legacy snapshot code, partial public weighted logic, and new operator expectations. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserServiceImpl.java; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java; .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md]

## Common Pitfalls

### Pitfall 1: Legacy Snapshot Drift

**What goes wrong:** The UI shows `traveler_progress.progress_percent`-style snapshots while public APIs and content lifecycle changes follow dynamic exploration math, so percentages disagree. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserServiceImpl.java; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java]

**Why it happens:** The current admin service still counts published cities, sub-maps, collectibles, badges, and rewards separately and computes percentages locally, while the public service uses weighted exploration elements. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserServiceImpl.java; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java]

**How to avoid:** Force every progress display, preview, and repair path through one canonical calculator and mark legacy snapshot fields as compatibility-only in DTOs and seeds. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md]

**Warning signs:** The same traveler shows different percentages between admin detail and `GET /api/v1/users/me/exploration`, or a content unpublish changes one screen but not the other. [VERIFIED: packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminUserController.java]

### Pitfall 2: Treating `user_exploration_state` As Truth

**What goes wrong:** Repair tools start editing cache rows directly, or worse, deleting completion evidence to “fix” percentages. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md; scripts/local/mysql/init/39-phase-28-experience-orchestration.sql]

**Why it happens:** The cache table looks like the easiest place to patch percentages, but Phase 32 decisions explicitly define it as rebuildable and non-authoritative. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md]

**How to avoid:** Design preview/confirm flows that rebuild cache rows from events and elements, never the inverse, and make destructive event edits unavailable in the admin UX. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md]

**Warning signs:** A repair action can “succeed” without reading `user_exploration_events`, or the audit trail cannot explain which source rows drove the recompute result. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md]

### Pitfall 3: Under-Modeling New Scopes

**What goes wrong:** City and storyline progress work, but POI, indoor, task, collectible, reward, or media drill-downs degrade into incompatible one-off queries or extra progress tables. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md; scripts/local/mysql/init/39-phase-28-experience-orchestration.sql]

**Why it happens:** The current exploration schema only exposes direct scope columns for `city_id`, `sub_map_id`, `storyline_id`, and `story_chapter_id`, and the current public filter logic only uses those four columns. [VERIFIED: scripts/local/mysql/init/39-phase-28-experience-orchestration.sql; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java]

**How to avoid:** Plan a deliberate schema/query extension for the missing scopes before UI work begins, and keep the denominator model centralized instead of adding per-domain fallbacks. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md]

**Warning signs:** Planner tasks mention “temporary reward progress table”, “media-only completion cache”, or other scope-specific counters. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md]

### Pitfall 4: Timeline Over-Fetching

**What goes wrong:** The first detail request becomes slow or unreadable because it loads every event source in one response. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md]

**Why it happens:** The current admin detail endpoint already bundles all snapshot sections into one DTO, and it is tempting to keep appending more arrays. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminUserDetailResponse.java]

**How to avoid:** Keep the landing detail payload summary-oriented and add paginated secondary endpoints for timeline, exploration-element drill-down, reward history, and session history. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md]

**Warning signs:** DTOs add unbounded `List<?>` fields for all raw events, or UI loading states block the entire page while fetching large payloads. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminUserDetailResponse.java; packages/admin/aoxiaoyou-admin-ui/src/pages/UserManagement/index.tsx]

### Pitfall 5: Missing Admin-Side Bridges For Existing User Facts

**What goes wrong:** The workbench ships without interface preferences or reward history even though the requirement promises them. [VERIFIED: .planning/REQUIREMENTS.md]

**Why it happens:** The public backend already owns `UserPreference` and `RewardRedemption`, but the admin backend entity list contains neither model today. [VERIFIED: packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/UserPreference.java; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/RewardRedemption.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity] 

**How to avoid:** Add admin-side read entities/mappers/DTOs for the existing tables or a tightly scoped admin integration service; do not fake the data in the UI. [VERIFIED: packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/UserServiceImpl.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity] 

**Warning signs:** UI mocks for preferences or rewards appear without matching admin backend endpoints or mappers. [VERIFIED: packages/admin/aoxiaoyou-admin-ui/src/services/api.ts; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminUserController.java]

## Code Examples

Verified patterns from the current codebase:

### Weighted Exploration Calculation
```java
// Source: packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java
int availableWeight = elementProgress.stream().mapToInt(UserExplorationResponse.ElementProgress::getWeightValue).sum();
int completedWeight = elementProgress.stream()
    .filter(UserExplorationResponse.ElementProgress::isCompleted)
    .mapToInt(UserExplorationResponse.ElementProgress::getWeightValue)
    .sum();
double percent = availableWeight == 0 ? 0 : Math.round((completedWeight * 10000.0 / availableWeight)) / 100.0;
```

### Immutable Event Idempotency
```sql
-- Source: scripts/local/mysql/init/39-phase-28-experience-orchestration.sql
CREATE TABLE IF NOT EXISTS `user_exploration_events` (
  `user_id` BIGINT NOT NULL,
  `client_event_id` VARCHAR(128) NOT NULL DEFAULT '',
  `storyline_session_id` VARCHAR(96) NOT NULL DEFAULT '',
  UNIQUE KEY `uk_user_exploration_events_client` (`user_id`, `client_event_id`)
);
```

### Admin Operation Audit Pattern To Reuse
```java
// Source: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserServiceImpl.java
log.setAdminUsername(operatorName);
log.setModule("USER");
log.setOperation(target ? "MARK_TEST_ACCOUNT" : "UNMARK_TEST_ACCOUNT");
log.setRequestMethod("POST");
log.setRequestUrl("/api/admin/v1/users/" + userId + "/test-flag");
sysOperationLogMapper.insert(log);
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Fixed or count-based progress snapshots in `traveler_progress` | Weighted dynamic completion from `exploration_elements` and `user_exploration_events` | Phase 28 created the foundation; Phase 32 is where it becomes the operator-facing truth. [VERIFIED: scripts/local/mysql/init/39-phase-28-experience-orchestration.sql; .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md] | Percentages can change predictably when content is published, retired, or reweighted without deleting completion history. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md] |
| Simple drawer with legacy summaries | Full traveler workbench with drill-down, timeline, session visibility, and guarded repairs | Phase 32 locked decision. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md; packages/admin/aoxiaoyou-admin-ui/src/pages/UserManagement/index.tsx] | Operators can answer “why is this user at this percentage?” instead of only seeing coarse totals. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md] |
| Ephemeral story session response only | Durable session state plus admin-visible lifecycle details | Phase 30 established the temporary-vs-permanent rule; Phase 32 must persist and expose it. [VERIFIED: .planning/phases/30-storyline-mode-and-chapter-override-workbench/30-CONTEXT.md; .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StorylineSessionResponse.java] | Operators can inspect active/exited sessions and correlate temporary session state with permanent exploration events. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md] |

**Deprecated/outdated:**
- `AdminUserDetailResponse` as the sole traveler-progress contract is outdated for Phase 32 because it only carries basic info, five coarse progress snapshots, active storylines, recent check-ins, and recent trigger logs. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminUserDetailResponse.java]
- The current `Drawer`-based `UserManagement` detail is outdated for Phase 32 because it still shows Simplified Chinese title text and does not expose preferences, exploration events, reward history, sessions, recompute, or audits. [VERIFIED: packages/admin/aoxiaoyou-admin-ui/src/pages/UserManagement/index.tsx]

## Assumptions Log

All factual claims in this research were verified against repo files, prior phase artifacts, local runtime probes, or npm registry metadata in this session. No user-confirmation assumptions were required for the research conclusions.

## Open Questions (RESOLVED)

1. **Where do Phase 32 “movement or route traces” come from in the current stack?**
   - Resolution: No concrete route-trace storage model was verified in the current Phase 32 code paths. The only direct hits for route-trace terminology in this phase were planning artifacts, while the verified implementation-side user-progress sources remain `traveler_checkins`, `trigger_logs`, `user_exploration_events`, public reward redemptions, and durable story sessions added in this phase. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserServiceImpl.java; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/UserExplorationEvent.java; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/RewardRedemption.java; .planning/phases/32-dynamic-exploration-and-user-progress-model/32-02-PLAN.md]
   - Planning decision: Build the timeline around those verified MySQL sources and add a `RouteTraceSourceAdapter` plus `EmptyRouteTraceSourceAdapter` contract that reports `sourceStatus='unavailable'` until a real storage model is verified. Do not invent fake route traces or silently imply trace completeness. [VERIFIED: .planning/REQUIREMENTS.md; .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md]
   - Execution consequence: Phase 32 smoke and UI plans must show explicit unavailable status for route traces, while keeping the design open for a future concrete adapter if a real trace store appears later. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-05-PLAN.md; .planning/phases/32-dynamic-exploration-and-user-progress-model/32-04-PLAN.md]

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| MySQL | Exploration tables, sessions, audits, seeds | ✓ | `mysql 8.0.41`, port `3306` listening | — [VERIFIED: live runtime probes on 2026-04-29] |
| Public backend | Canonical exploration semantics and traveler event APIs | ✓ | Spring Boot app on `http://127.0.0.1:8080`; `/actuator/health` reports `UP` | — [VERIFIED: packages/server/src/main/resources/application.yml; live runtime probes on 2026-04-29] |
| Admin backend | Workbench APIs, repair endpoints, audit integration | ✓ | Spring Boot app on `http://127.0.0.1:8081`; `/swagger-ui.html` returned `200` | — [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/resources/application.yml; live runtime probes on 2026-04-29] |
| Admin UI | Workbench route verification | ✓ | Vite dev server on `http://127.0.0.1:5173/admin/`; HTTP `200` | — [VERIFIED: packages/admin/aoxiaoyou-admin-ui/package.json; live runtime probes on 2026-04-29] |
| MongoDB | Optional existing log/trace extension for admin timelines | ✓ | Container `trip-of-macau-mongo` healthy on `27017` | Use MySQL-first timeline if direct Mongo integration is not needed. [VERIFIED: docker ps on 2026-04-29; packages/admin/aoxiaoyou-admin-backend/src/main/resources/application.yml] |
| `mongosh` CLI | Direct local Mongo inspection | ✗ | — | Use `docker exec` or defer to MySQL-first sources; not blocking Phase 32 planning. [VERIFIED: local CLI probe on 2026-04-29; docker ps on 2026-04-29] |

**Missing dependencies with no fallback:**
- None identified for planning. [VERIFIED: live runtime probes on 2026-04-29]

**Missing dependencies with fallback:**
- `mongosh` is not on `PATH`, but Mongo-backed inspection is optional for this phase and the service itself is running. [VERIFIED: local CLI probe on 2026-04-29; docker ps on 2026-04-29]

## Validation Architecture

Validation architecture is required because `.planning/config.json` enables `workflow.nyquist_validation`. [VERIFIED: .planning/config.json]

### Test Framework

| Property | Value |
|----------|-------|
| Framework | Backend: JUnit 5 via `spring-boot-starter-test` and Mockito-style service tests; Admin UI: no dedicated test harness detected, so build plus browser/smoke verification remains required. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/pom.xml; packages/server/pom.xml; packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminUserServiceImplTest.java; packages/admin/aoxiaoyou-admin-ui/package.json] |
| Config file | None; Maven Surefire defaults for both Java services, and no `vitest`/`jest` config was found in the admin UI. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/pom.xml; packages/server/pom.xml; local file probes on 2026-04-29] |
| Quick run command | `mvn -q -Dtest=AdminUserServiceImplTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` for current admin harness, plus new targeted Phase 32 backend test classes once created. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminUserServiceImplTest.java] |
| Full suite command | `mvn -q test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` and `mvn -q test -f packages/server/pom.xml`, followed by `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` and a Phase 32 smoke script. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/pom.xml; packages/server/pom.xml; packages/admin/aoxiaoyou-admin-ui/package.json; .planning/phases/31-interaction-task-template-library-and-governance-center/31-VERIFICATION.md] |

### Phase Requirements → Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| USER-01 | Traveler detail returns identity basics, preferences, linked scopes, and recent activity context | service/integration | `mvn -q -Dtest=AdminUserServiceImplTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | ✅ expand existing [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminUserServiceImplTest.java] |
| USER-02 | Weighted progress drill-down works across required scopes and exposes active vs retired completions | service/unit | `mvn -q -Dtest=PublicExperienceServiceImplTest test -f packages/server/pom.xml` | ❌ Wave 0 [VERIFIED: packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java; packages/server/src/test/java] |
| USER-03 | Timeline aggregation merges verified event sources into a filtered chronological stream | service/unit | `mvn -q -Dtest=AdminUserTimelineServiceTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | ❌ Wave 0 [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/test/java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserServiceImpl.java] |
| USER-04 | Recompute/repair preview, confirm, and audit writes are safe and explicit | service/integration | `mvn -q -Dtest=AdminUserProgressRepairServiceTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | ❌ Wave 0 [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md; packages/admin/aoxiaoyou-admin-backend/src/test/java] |
| LINK-03 | Content lifecycle and weight changes update derived percentages predictably without deleting completion facts | service/unit | `mvn -q -Dtest=PublicExperienceServiceImplTest test -f packages/server/pom.xml` | ❌ Wave 0 [VERIFIED: packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java; scripts/local/mysql/init/39-phase-28-experience-orchestration.sql] |

### Sampling Rate

- **Per task commit:** Run the targeted backend test class for the touched service and `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` whenever the admin workbench UI changes. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/pom.xml; packages/server/pom.xml; packages/admin/aoxiaoyou-admin-ui/package.json]
- **Per wave merge:** Run both backend Maven test suites and the admin UI build, then execute the Phase 32 live smoke script against local services. [VERIFIED: .planning/phases/31-interaction-task-template-library-and-governance-center/31-VERIFICATION.md]
- **Phase gate:** Full suite green plus live MySQL/admin/public smoke before `/gsd-verify-work`. [VERIFIED: AGENTS.md; .planning/config.json]

### Wave 0 Gaps

- [ ] `packages/server/src/test/java/com/aoxiaoyou/tripofmacau/PublicExperienceServiceImplTest.java` — weighted-scope parity, inactive-element handling, and lifecycle recalculation coverage for `USER-02` and `LINK-03`. [VERIFIED: packages/server/src/test/java]
- [ ] `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminUserTimelineServiceTest.java` — timeline fan-in coverage for `USER-03`. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/test/java]
- [ ] `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminUserProgressRepairServiceTest.java` — preview/confirm/audit safety coverage for `USER-04`. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/test/java]
- [ ] `scripts/local/smoke-phase-32-user-progress.ps1` — local end-to-end smoke for admin detail route, public exploration parity, recompute preview, and repair audit visibility. [VERIFIED: .planning/phases/31-interaction-task-template-library-and-governance-center/31-VERIFICATION.md]
- [ ] Admin UI route/browser verification artifact — no dedicated frontend test framework is present, so plan a browser smoke or Playwright check for the new full-page workbench. [VERIFIED: packages/admin/aoxiaoyou-admin-ui/package.json; local file probes on 2026-04-29]

## Security Domain

Security domain is required because `.planning/config.json` keeps `security_enforcement` enabled. [VERIFIED: .planning/config.json]

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | yes | Keep admin/public JWT auth as the gate for all traveler detail and repair endpoints; both services already depend on `java-jwt`, and admin auth is enforced through interceptor-driven patterns. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/pom.xml; packages/server/pom.xml; AGENTS.md] |
| V3 Session Management | yes | Keep authentication stateless, and model story sessions as domain data rather than auth state so exit/reset logic cannot affect login/session security semantics. [VERIFIED: .planning/phases/30-storyline-mode-and-chapter-override-workbench/30-CONTEXT.md; .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md] |
| V4 Access Control | yes | Repair, recompute, audit, and traveler-detail APIs remain admin-backend-only, with operator identity captured for every manual action. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminUserController.java] |
| V5 Input Validation | yes | Use Spring `@Valid` DTO validation, explicit scope-type whitelists, and preview-before-confirm request shapes for high-impact operations. [VERIFIED: packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java; packages/admin/aoxiaoyou-admin-backend/pom.xml; .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md] |
| V6 Cryptography | yes | Reuse existing JWT signing and existing secret-loading conventions; never hardcode secrets or invent custom crypto for audit/session integrity. [VERIFIED: AGENTS.md; packages/server/src/main/resources/application.yml; packages/admin/aoxiaoyou-admin-backend/src/main/resources/application.yml] |

### Known Threat Patterns for This Stack

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Unauthorized repair or recompute execution | Elevation of Privilege | Expose repair/recompute only on admin endpoints, require authenticated operator context, and persist operator identity with each action. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserServiceImpl.java] |
| Scope or user-id tampering in repair requests | Tampering | Validate scope enums, existence, and target user ownership server-side, and show preview counts before confirm. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md] |
| Duplicate exploration event submissions | Repudiation / Tampering | Keep `(user_id, client_event_id)` idempotency in the database and never rely on client-side dedup alone. [VERIFIED: scripts/local/mysql/init/39-phase-28-experience-orchestration.sql] |
| Sensitive payload leakage in operator UI | Information Disclosure | Collapse large JSON payloads, mask or omit sensitive profile fields where possible, and avoid dumping raw request bodies into dense tables. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md; AGENTS.md] |
| Direct cache-row edits that bypass source facts | Tampering | Rebuild `user_exploration_state` from source elements/events and prohibit mutation of immutable exploration events through normal repair flows. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md] |

## Sources

### Primary (HIGH confidence)

- `.planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md` - locked decisions, scope list, admin/public ownership, recompute safety, and deferred items.
- `.planning/REQUIREMENTS.md` - requirement wording for `USER-01` through `USER-04` and `LINK-03`.
- `.planning/ROADMAP.md` - Phase 32 goal, dependencies, and success criteria.
- `.planning/phases/31-interaction-task-template-library-and-governance-center/31-HANDOFF.md` and `31-VERIFICATION.md` - live local runtime URLs and regression verification pattern.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/UserManagement/index.tsx` - current drawer-based UI baseline.
- `packages/admin/aoxiaoyou-admin-ui/src/App.tsx`, `src/layouts/DefaultLayout.tsx`, `src/services/api.ts`, `src/types/admin.ts` - current route/API/DTO/admin ownership patterns.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminUserController.java`, `service/impl/AdminUserServiceImpl.java`, and `dto/response/AdminUserDetailResponse.java` - current admin user detail behavior and gaps.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java`, `service/impl/PublicExperienceServiceImpl.java`, `dto/response/UserExplorationResponse.java`, `dto/response/StorylineSessionResponse.java`, `service/impl/UserServiceImpl.java`, `entity/UserPreference.java`, `entity/RewardRedemption.java` - current public exploration/session/preference/reward foundation.
- `scripts/local/mysql/init/39-phase-28-experience-orchestration.sql` - exploration tables, immutable-event uniqueness, cache semantics, and current scope columns.
- `packages/server/pom.xml`, `packages/admin/aoxiaoyou-admin-backend/pom.xml`, `packages/admin/aoxiaoyou-admin-ui/package.json`, `.planning/config.json`, `AGENTS.md` - stack, validation, and project constraints.
- Live runtime probes run on 2026-04-29 - verified listeners, HTTP reachability, Docker status, and local tool availability.

### Secondary (MEDIUM confidence)

- npm registry metadata checked on 2026-04-29 for `antd`, `@ant-design/pro-components`, `@tanstack/react-query`, and `react-router-dom` - current latest versions and publish dates.

### Tertiary (LOW confidence)

- None.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - the brownfield stack, versions, and active local runtime are directly verified in manifests, config, and live probes. [VERIFIED: packages/server/pom.xml; packages/admin/aoxiaoyou-admin-backend/pom.xml; packages/admin/aoxiaoyou-admin-ui/package.json; live runtime probes on 2026-04-29]
- Architecture: MEDIUM - the current admin/public split and weighted foundation are verified, but final schema shape for the missing scope columns and route-trace source integration still needs planning decisions. [VERIFIED: .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md; packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java; scripts/local/mysql/init/39-phase-28-experience-orchestration.sql]
- Pitfalls: HIGH - the main failure modes are directly visible in the legacy admin detail implementation, the partial public calculator, and the locked Phase 32 semantics. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserServiceImpl.java; packages/admin/aoxiaoyou-admin-ui/src/pages/UserManagement/index.tsx; .planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md]

**Research date:** 2026-04-29  
**Valid until:** 2026-05-29 for repo-shape findings; recheck npm registry and local runtime status sooner if Phase 32 planning is delayed. [VERIFIED: live runtime probes on 2026-04-29; npm registry]
