# Phase 31: Interaction/Task Template Library and Governance Center - Context

**Gathered:** 2026-04-29
**Status:** Ready for planning
**Source:** Roadmap Phase 31 plus prior user decisions from v3.0 story experience redesign

<domain>
## Phase Boundary

Phase 31 completes the reusable interaction/task template library and the cross-domain experience governance center. It builds on:

- Phase 28 shared experience model: `experience_templates`, `experience_flows`, `experience_flow_steps`, `experience_bindings`, `experience_overrides`, `exploration_elements`.
- Phase 29 POI 地點體驗工作台, including saving POI steps as templates.
- Phase 30 story mode and chapter override workbench, including inherited POI flow plus disable/replace/append overrides.
- Existing indoor rule workbench/governance and reward rule centers.

This phase does not implement full mini-program complex gameplay runtime. It makes templates and governance authorable, reusable, searchable, conflict-aware, and visible in admin.
</domain>

<decisions>
## Implementation Decisions

### Template Library

- The template library must be the canonical reusable source for display templates, appearance/display conditions, trigger conditions, trigger effects, task gameplay, and reward presentation.
- Operators must not be forced to hand-write JSON as the primary workflow. Use structured forms, preset cards, schema-driven helpers, and an advanced JSON section only as an escape hatch.
- Template categories must include at least:
  - `presentation`
  - `display_condition`
  - `trigger_condition`
  - `trigger_effect`
  - `task_gameplay`
  - `reward_presentation`
- Concrete preset template examples must include:
  - 全屏媒體播放
  - 圖文彈窗
  - 氣泡提示
  - Lottie 動畫
  - 地圖疊加物
  - 靠近範圍
  - 停留時長
  - 點擊拾取
  - 依序點擊
  - 問答任務
  - 拍照任務
  - 點香互動
  - 發放收集物
  - 發放徽章或稱號
  - 發放遊戲內獎勵

### Governance Center

- Governance must aggregate template and rule usage across story, POI, indoor, reward, media-linked experience steps, and activities instead of replacing existing indoor/reward governance.
- Governance filters must support at least:
  - city
  - sub-map
  - POI
  - indoor building
  - storyline
  - chapter
  - owner type
  - template type
  - trigger type
  - effect/reward type
  - status
  - story override only
  - high risk only
- Governance details must show where a template or instance is used, what flow/step/reward/rule owns it, and whether it is inherited, direct, or override-created.
- Conflict checks must include:
  - same owner or nearby scope has multiple full-screen effects with overlapping trigger semantics
  - same reward or title is granted by multiple mutually exclusive or duplicate rules
  - story override disables or replaces a step that is required for completion without replacement
  - side task and main task share the same pickup or reward with different reward policy
  - high-risk template is published without any usage or schema guard

### UI

- Admin UI must remain Traditional Chinese.
- Main pages must feel like workbenches, not raw CRUD grids:
  - left side filters or template catalog
  - middle list/cards/table
  - right side usage/conflict/detail panel where useful
  - bottom or side validation/conflict result area
- Existing route entries `/content/experience/templates` and `/content/experience/governance` should become the primary specialized pages or specialized tabs, not placeholders.
- The existing Phase 31 sidebar labels already exist and should remain:
  - `互動與任務模板庫`
  - `體驗規則治理中心`

### Backend/Public Boundary

- Admin backend owns authoring and governance.
- Public backend should not expose raw admin governance internals, but public compiled runtime DTOs must keep template IDs/codes where needed for later mini-program runtime interpretation.
- Phase 31 may add public template summary fields only if they are needed to compile story/POI runtime flows safely.

### Verification

- Verification must run against local MySQL and live admin/public services where possible.
- Seed data must be UTF-8/utf8mb4 and must not be written through inline PowerShell Chinese literals.
- Smoke coverage must prove templates can be listed, created from preset shape, reused by a flow step, and surfaced in governance with at least one conflict finding.
</decisions>

<canonical_refs>
## Canonical References

### Project Rules

- `AGENTS.md` - project constraints, UTF-8 rule, admin/public ownership, GSD workflow requirement.
- `.planning/PROJECT.md` - active v3.0 state and milestone boundaries.
- `.planning/ROADMAP.md` - Phase 31 goal, success criteria, and dependencies.
- `.planning/REQUIREMENTS.md` - `OPS-01`, `OPS-03`, `LINK-01` traceability.

### Existing Experience Foundation

- `scripts/local/mysql/init/39-phase-28-experience-orchestration.sql` - existing schema for experience templates, flows, steps, bindings, overrides, exploration elements.
- `scripts/local/mysql/init/40-phase-29-poi-default-experience.sql` - POI default-flow seed and template-save acceptance data.
- `scripts/local/mysql/init/41-phase-30-storyline-mode-overrides.sql` - story override seed and runtime smoke acceptance data.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminExperienceOrchestrationController.java` - current admin template/flow/governance facade.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminExperienceOrchestrationServiceImpl.java` - current canonical vocabulary, versioned JSON validation, governance summary, and simple findings.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Experience/ExperienceOrchestrationWorkbench.tsx` - current tabs for template library, flow bindings, overrides, exploration rules, governance.
- `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts` - admin request wrappers for experience endpoints.
- `packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts` - admin DTO types.

### Existing Domain Governance To Aggregate

- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/IndoorRuleGovernanceService.java` - indoor rule governance and conflict detail patterns.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/IndoorRuleCenter.tsx` - existing indoor governance UI.
- `packages/admin/aoxiaoyou-admin-ui/src/components/indoor/IndoorRuleConflictPanel.tsx` - reusable conflict panel pattern.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminRewardGovernanceController.java` - reward governance API.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/RewardRuleCenter.tsx` - reward rule center UI.
- `packages/admin/aoxiaoyou-admin-ui/src/components/rewards/RewardDomainShared.tsx` - reward condition group UI patterns.
</canonical_refs>

<specifics>
## Specific Ideas

- Promote existing `ExperienceTemplate` into a richer operator-facing template object by adding governance-specific DTO fields rather than immediately splitting it into many tables.
- Keep schema evolution minimal for this phase unless a field is needed for filtering/usage, such as `scope_type`, `effect_family`, `trigger_family`, or `usage_summary_json`.
- Avoid a parallel governance model. Use an aggregator service that reads experience templates/steps/overrides plus indoor/reward sources and emits unified `GovernanceItem`, `UsageRef`, and `ConflictFinding` DTOs.
- Provide one seed file named `42-phase-31-interaction-template-governance.sql`.
- Provide one smoke script named `scripts/local/smoke-phase-31-template-governance.ps1`.
</specifics>

<deferred>
## Deferred Ideas

- Full mini-program runtime behavior for AR photo recognition, sound input, puzzles, and cannon placement remains future runtime work.
- Sophisticated spatial overlap detection with real GIS geometry can start with owner/scope/radius heuristics in Phase 31 and be deepened later.
- Approval workflow and publish scheduling remain Phase 34 or later unless needed for governance flags.
</deferred>

---

*Phase: 31-interaction-task-template-library-and-governance-center*
*Context gathered: 2026-04-29*
