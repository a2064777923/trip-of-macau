# Phase 43: Traveler Progress and Reward Operations - Context

**Gathered:** 2026-05-04  
**Status:** Ready for planning

<domain>
## Phase Boundary

Phase 43 makes live traveler progress and reward outcomes supportable from `/admin` without direct database inspection. It should give operators one coherent Traditional Chinese workflow to inspect a user's story sessions, exploration timeline, pickups, backpack-like earned items, rewards, titles, rule traces, and audit-backed support actions.

This phase should not add new mini-program gameplay engines, rebuild the reward authoring domain, or claim WeChat physical-device UAT. It operates the gameplay/runtime state already produced by Phases 41-42 and prepares cleaner evidence for Phase 44 release acceptance.
</domain>

<decisions>
## Implementation Decisions

### Operator Workflow
- **D-01:** Build Phase 43 around a single `旅客進度與獎勵支援工作台`, not scattered CRUD pages. The default flow is user search/list -> traveler detail workbench -> sessions, timeline, rewards/titles/backpack, rule trace, audit/support action panels.
- **D-02:** The workbench must support filtering by user, story, chapter, POI/map, status, event type, reward type, and time range. Filters should be visible with labels and sane widths; raw JSON must stay in detail drawers or advanced sections only.
- **D-03:** Timeline entries should combine story session events, exploration events, pickups, rewards/redemptions, titles/game rewards, check-ins, and support actions into a readable sequence with source table, source id, occurred time, payload preview, and full payload detail.
- **D-04:** Traveler identity, preferences, linked scopes, dynamic progress breakdown, legacy progress snapshot, recent sessions, and reward redemption summary should remain visible together so support staff can diagnose state mismatches without opening multiple unrelated modules.

### Support Actions
- **D-05:** Every mutating support action must be preview-first, require explicit confirmation, and write an audit record containing operator, reason, request IP, preview summary, result summary, scope, and timestamp.
- **D-06:** Recompute progress should remain scope-aware and deterministic: global, city/map/sub-map/storyline, or other supported scope. The preview must show affected events/elements/state rows before confirmation.
- **D-07:** "Void duplicate event" must not physically delete event rows by default. It should mark an event ignored/voided/duplicate or create an auditable compensating state so support history remains explainable.
- **D-08:** "Resend reward" must be idempotent and trace-linked to the source rule/event/session. Re-running the same resend action should not grant duplicate rewards unless the operator explicitly chooses a distinct corrective grant path.
- **D-09:** Issue annotation is in scope as a support action: operators need to leave a reasoned note tied to the user, source event/rule/session, and audit trail even when no state mutation is required.

### Rule Trace
- **D-10:** Rule trace should explain why a traveler did or did not receive a reward/title by walking from runtime event -> experience step/exploration element -> reward rule binding -> condition groups -> grant result.
- **D-11:** Trace output should use human-readable Traditional Chinese labels first, with ids and raw payloads available for debugging. Operators should see "條件已滿足 / 未滿足 / 已發放 / 已發放過 / 被停用 / 找不到綁定" style statuses.
- **D-12:** The first useful trace can be backend-assembled from existing runtime, exploration, and reward tables; do not introduce a second rules engine in admin. If a rule cannot be fully replayed, expose the missing link explicitly.

### Consistency and Safety
- **D-13:** Admin operations must reconcile with public backend state: after repairs, `packages/server` exploration summary/session/reward endpoints should reflect the changed state, or the smoke test must flag the mismatch.
- **D-14:** Support actions should prefer additive/annotative records over destructive cleanup because user support needs explainability.
- **D-15:** Security remains admin-token protected. Future role granularity may be added later, but Phase 43 should not expose progress repair endpoints outside the existing admin auth boundary.
- **D-16:** All UI copy, seed labels, payload fixtures, and SQL/JSON used in this phase must be UTF-8/utf8mb4 and Traditional Chinese where operator-facing.

### Verification
- **D-17:** Verification should include admin backend compile, admin UI type/build checks where practical, and a smoke path that logs in to admin, loads a traveler workbench, filters timeline/audits, previews a repair/recompute, and confirms at least one safe operation against local MySQL.
- **D-18:** Smoke evidence must distinguish automated backend/API/browser checks from manual WeChat device UAT. Device UAT remains Phase 44 / future evidence unless actually run.

### the agent's Discretion
- The planner may choose whether to enhance the existing `OperationsManagement` area, add a new `UserManagement` subpage, or introduce a dedicated operations route, as long as the result is one coherent support workflow and Phase 44 can polish IA around it.
- The planner may reuse existing DTOs first and add narrowly-scoped response fields rather than redesigning tables.
- Exact visual composition is flexible, but it should follow the admin system's Ant Design Pro patterns and avoid cramped columns, unlabeled filters, or JSON-first interaction.
</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### v3.2 Planning
- `.planning/PROJECT.md` - Current project constraints, admin/public ownership, UTF-8 rule, and v3.2 active scope.
- `.planning/REQUIREMENTS.md` - Requirements `OPS-01`, `OPS-02`, `OPS-03`, and `OPS-04`.
- `.planning/ROADMAP.md` - Phase 43 goal and success criteria.
- `.planning/STATE.md` - Current milestone state, known caveats, and Phase 42 completion note.

### Prior Phase Runtime Context
- `.planning/phases/41-wechat-runtime-uat-harness-and-story-entry-hardening/41-CONTEXT.md` - Story runtime UAT boundary and device-UAT caveat.
- `.planning/phases/42-traveler-gameplay-event-engine/42-CONTEXT.md` - Runtime event engine decisions, idempotency, auth/session rules, and deferred Phase 43 operator support.
- `.planning/phases/42-traveler-gameplay-event-engine/42-VERIFICATION.md` - Verified gameplay event engine evidence and pending manual UAT.
- `.planning/phases/42-traveler-gameplay-event-engine/42-UAT.md` - Phase 42 smoke evidence for session/event/duplicate/exploration behavior.

### Admin Progress Backend
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminTravelerProgressController.java` - Existing workbench, breakdown, and timeline endpoints.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminTravelerProgressOpsController.java` - Existing preview/confirm repair and audit endpoints.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminTravelerProgressService.java` - Admin traveler progress read contract.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminUserProgressRepairService.java` - Preview/apply recompute and repair service contract.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminTravelerProgressWorkbenchResponse.java` - Current workbench response shape.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminTravelerTimelineEntryResponse.java` - Current timeline entry response shape.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminUserProgressBreakdownResponse.java` - Current weighted progress breakdown response shape.

### Reward and Runtime State
- `packages/admin/aoxiaoyou-admin-ui/src/components/rewards/RewardDomainShared.tsx` - Existing reward rule/presentation UI structures and labels.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminCollectibleController.java` - Collectible/reward admin APIs.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminExperienceOrchestrationController.java` - Exploration element and experience orchestration admin endpoints.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java` - Public session/event/exploration endpoints.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java` - Public runtime compilation, event idempotency, session updates, and exploration summary logic.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/UserExplorationEvent.java` - Durable traveler exploration event records.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/UserStorylineSession.java` - Story session state.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/ExplorationElement.java` - Dynamic exploration element source of truth.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/GameReward.java` - Game reward/title style outcomes.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/Reward.java` - Redeemable reward records.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/RewardRule.java` - Reward rule source records for rule trace.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/RewardRuleBinding.java` - Rule-to-source binding records for trace.

### Admin UI Integration
- `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts` - Existing admin API wrappers for progress workbench, timeline, progress ops, audits, and rewards.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/OperationsManagement/index.tsx` - Existing operations page pattern; likely not sufficient as-is for Phase 43 support workflow.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/UserManagement` - Existing user/admin area where traveler lookup may integrate.
- `packages/admin/aoxiaoyou-admin-ui/src/App.tsx` - Route registration.
- `packages/admin/aoxiaoyou-admin-ui/src/layouts/DefaultLayout.tsx` - Sidebar/navigation integration.

### Codebase Conventions and Verification
- `.planning/codebase/ARCHITECTURE.md` - Admin/public backend layering and frontend routing patterns.
- `.planning/codebase/CONVENTIONS.md` - Admin UI, backend, auth, and UTF-8 editing conventions.
- `.planning/codebase/TESTING.md` - Low automated coverage and practical verification commands.
</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `AdminTravelerProgressController` already exposes `/api/admin/v1/users/{userId}/progress-workbench`, `/progress-breakdown`, and `/timeline`; Phase 43 should extend or compose these instead of starting from scratch.
- `AdminTravelerProgressOpsController` already provides recompute preview/confirm, repair preview/apply, and audit listing with explicit confirmation text and preview tokens.
- `AdminTravelerProgressWorkbenchResponse` already aggregates identity, preferences, linked scopes, dynamic progress, legacy snapshots, story sessions, reward redemptions, and exploration context.
- `AdminTravelerTimelineEntryResponse` already has source table/id, story/POI linkage, payload preview/raw payload, and occurred time fields suitable for a unified timeline.
- `AdminUserProgressBreakdownResponse` already models weighted progress elements, retired elements, completed state, source event id, and event timestamp.
- `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts` already contains API wrapper functions for progress workbench, timeline, breakdown, progress ops, audits, and reward-domain data.
- `RewardDomainShared.tsx` can provide consistent labels and structures for reward/rule/presentation detail panels.

### Established Patterns
- Admin controllers return `ApiResponse<T>` and paginated endpoints return `PageResponse<T>`.
- Admin UI uses Ant Design Pro, React Query/ahooks-style data fetching, and explicit route registration in `App.tsx`.
- Existing support operations are confirmation-token based, which matches the user's repeated requirement for safe admin actions.
- Public runtime events are idempotent by `clientEventId`; admin repair/resend behavior must preserve that property.
- The project has low automated coverage, so Phase 43 needs targeted smoke scripts and truthful manual verification notes.

### Integration Points
- Backend reads and support actions connect through `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminTravelerProgressController.java`, `AdminTravelerProgressOpsController.java`, and their services.
- Public-state consistency checks connect through `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java` and `PublicExperienceServiceImpl.java`.
- Admin UI can integrate under user/progress or operations navigation, but the final IA should leave one clear entry named around traveler progress/reward support.
- Smoke verification should use admin auth, local admin backend `8081`, local public backend `8080`, and existing seeded Phase 42 story/user data where possible.
</code_context>

<specifics>
## Specific Ideas

- The primary page should feel like a support console: search a traveler, open a workbench, scan state, trace a missing reward/title, preview a repair, then confirm with a reason.
- Useful tabs/panels: `總覽`, `故事 Session`, `事件時間線`, `探索度明細`, `背包 / 收集物`, `獎勵與稱號`, `規則追蹤`, `修復與審計`.
- Rule trace should answer common support questions directly: "為什麼沒有拿到稱號？", "這個線索是否已拾取？", "是否重複點擊但沒有重複發放？", "哪條規則發放了這個獎勵？"
- Keep destructive behavior out of the happy path: prefer `標記為重複 / 忽略 / 補發 / 重新計算 / 留註記` over delete.
- If a data source is not yet available, show "尚未接入" with the missing table/link, not an empty unexplained panel.
</specifics>

<deferred>
## Deferred Ideas

- Phase 44 owns broader management-system IA cleanup, final visual polish, and release acceptance reporting.
- Manual WeChat DevTools/physical-device UAT remains Phase 44 or future evidence unless actually executed in Phase 43.
- Production-grade AR/photo recognition, speech gameplay, puzzle/cannon-defense, route coverage, and indoor positioning engines remain future gameplay phases.
- A full approval workflow for support actions and content changes remains future release-platform scope.
</deferred>

---

*Phase: 43-traveler-progress-and-reward-operations*  
*Context gathered: 2026-05-04*
