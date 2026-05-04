# Phase 43: Traveler Progress and Reward Operations - Research

**Researched:** 2026-05-04  
**Status:** Complete  
**Scope:** Backend/admin support operations, rule trace, admin UI workflow, and smoke verification for OPS-01 through OPS-04.

## Findings

### Existing Foundation
- The admin backend already has a traveler progress read surface:
  - `AdminTravelerProgressController` exposes workbench, breakdown, and timeline endpoints under `/api/admin/v1/users/{userId}`.
  - `AdminTravelerProgressServiceImpl` aggregates identity, preferences, linked scopes, dynamic progress, legacy progress, sessions, reward redemptions, and timeline rows.
  - `AdminTravelerProgressReadMapper` already reads check-ins, trigger logs, exploration events, sessions, reward redemptions, and progress operation audits.
- The admin backend already has preview/confirm repair operations:
  - `AdminTravelerProgressOpsController` exposes recompute preview/confirm, repair preview/apply, and audit listing.
  - `AdminUserProgressRepairServiceImpl` currently supports `RECOMPUTE_SCOPE`, `LINK_ORPHAN_EVENT`, and `MARK_DUPLICATE_CLIENT_EVENT`.
  - Repair writes `UserProgressOperationAudit` plus `SysOperationLog`, and deliberately records `deletedEventRows=0`.
- The admin UI already has a user progress page:
  - Route `/users/progress/:userId` maps to `UserProgressWorkbench`.
  - The page calls the progress workbench, breakdown, timeline, audit, recompute, and repair APIs.
  - The sidebar currently labels this entry `用戶與進度工作台`; Phase 43 should clarify support intent without adding a second confusing page.
- Reward authoring structures already exist:
  - `RewardDomainShared.tsx` contains reward/rule/presentation labels and editor helpers.
  - Admin/backend reward mappers and DTOs exist for `GameReward`, `RewardRule`, `RewardRuleBinding`, and presentations.

### Gaps To Plan
- Timeline filters are incomplete for Phase 43. Current backend timeline query accepts event types, storyline id, and date range. OPS-02 also needs story/chapter, POI/map, status, event type, reward type, and time range. Some of these can be added as backend filters, while UI should expose all meaningful filters and clearly disable or label not-yet-supported dimensions.
- Backpack-like state is not a distinct backend table in the inspected code. Phase 43 should represent it as earned collectibles/game rewards/titles/redemptions derived from existing reward and event sources, with explicit source labels. Do not invent a hidden wallet/backpack ledger.
- Rule trace does not yet exist. It should be a read-only backend endpoint that assembles from existing event, exploration element, experience step/source metadata, reward rule bindings, condition groups, and grant/redeem state. If a source link cannot be resolved, the trace should return a Traditional Chinese missing-link status rather than failing the whole drawer.
- Support actions do not yet include `RESEND_REWARD` or `ANNOTATE_ISSUE`. These should extend `AdminUserProgressRepairService` with preview/apply operations and audit records. `RESEND_REWARD` must be idempotent by source rule/event/reward target; `ANNOTATE_ISSUE` should write audit only and mutate no runtime state.
- Existing UI is functional but dense. Phase 43 needs better IA: a support workbench with labeled filters, tabs/panels, full payload drawers, and clear preview/confirm modals.

## Recommended Plan Shape

### Backend Read Model and Trace
- Add DTOs for `AdminTravelerRewardStateResponse` and `AdminTravelerRewardRuleTraceResponse`.
- Add service methods and controller endpoints:
  - `GET /api/admin/v1/users/{userId}/reward-state`
  - `GET /api/admin/v1/users/{userId}/reward-rule-trace`
- Extend timeline query DTO/service to include `chapterId`, `poiId`, `mapScopeType`, `mapScopeId`, `status`, and `rewardType`.
- Use left joins and nullable trace nodes so missing data returns usable diagnostics.

### Support Actions
- Extend repair action constants with:
  - `RESEND_REWARD`
  - `ANNOTATE_ISSUE`
  - Optional alias `VOID_DUPLICATE_EVENT` mapped to the existing duplicate mark behavior.
- Extend request DTO only if needed; prefer additive nullable fields such as `rewardId`, `gameRewardId`, `ruleId`, `sourceEventId`, `annotationText`, and `issueSeverity`.
- Keep confirmation text explicit and audit-backed.
- Never hard delete traveler events in Phase 43.

### Admin UI
- Keep the existing `/users/progress` entry, but rename/display it as `旅客進度與獎勵支援`.
- Enhance `UserProgressWorkbench.tsx` rather than creating an unrelated console.
- Add panels/tabs for:
  - Overview
  - Story sessions
  - Event timeline
  - Exploration breakdown
  - Backpack / collectibles
  - Rewards and titles
  - Rule trace
  - Repair and audit
- Use Ant Design Pro patterns already present in the admin app. Long JSON and payloads belong in drawers or collapses.

### Verification
- Add `scripts/local/smoke-phase-43-traveler-ops.ps1`.
- Smoke should target local admin backend `8081` and public backend `8080`, log in to admin, pick or accept a user id, load workbench, timeline, breakdown, reward-state, trace, audits, preview a safe operation, and optionally apply annotation/recompute against local MySQL.
- Verification docs must separate automated smoke from WeChat device UAT.

## Risks

- Admin backend and public backend may use separate entity packages/mappers even though both point to MySQL. Plan implementation should reuse admin mappers and avoid directly depending on public Java classes.
- Existing `gsd-tools state record-session` can drift frontmatter metadata on this repo. State updates after planning should be inspected before commit.
- Current dirty worktree contains unrelated UI and SQL changes. Execution must stage only Phase 43 files.
- Some rule trace links may be absent in older seed data. UI should show `找不到綁定` instead of throwing.

## Validation Architecture

### Commands
- Backend compile: `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml`
- Admin UI check: `cd packages/admin/aoxiaoyou-admin-ui; npm run type-check`
- Admin UI build: `cd packages/admin/aoxiaoyou-admin-ui; npm run build`
- Phase smoke: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-43-traveler-ops.ps1`

### Required Evidence
- Workbench endpoint returns identity, progress, sessions, rewards, and exploration context for a known user.
- Timeline filters return deterministic paginated responses and include source table/id.
- Reward state endpoint returns reward/title/backpack-like summaries.
- Rule trace endpoint returns at least one trace with condition/grant status or a clear missing-link status.
- Support action preview returns a token, apply requires confirmation text, and audit listing shows the action.

## Research Complete

The phase can be planned as four dependent slices: read model/trace, support operations, admin UI workbench, and smoke/verification.
