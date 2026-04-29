---
phase: 31
slug: interaction-task-template-library-and-governance-center
status: complete
created: 2026-04-29
---

# Phase 31 Research - Interaction/Task Template Library and Governance Center

## Existing Foundation

The codebase already contains a Phase 28/29/30 experience foundation:

- Admin APIs under `/api/admin/v1/experience` support templates, flows, steps, bindings, overrides, exploration elements, and a governance overview.
- `AdminExperienceOrchestrationServiceImpl` already validates canonical vocabularies and requires `schemaVersion` on versioned JSON fields.
- `ExperienceTemplate` currently supports `templateType`, `category`, multilingual copy, `configJson`, `schemaJson`, `riskLevel`, `status`, and usage count from flow steps.
- `ExperienceFlowStep` references `templateId`, `triggerType`, condition/effect JSON, media asset, reward rule IDs, exploration weight, completion requirement, and inherit key.
- The current governance overview is useful but shallow: it counts entities and checks missing templates, invalid story override role, replacement requirements, and exploration weight drift.

Phase 31 should deepen the existing model rather than create a second model.

## Cross-Domain Sources

The governance center should aggregate these sources:

- Experience templates, flows, steps, bindings, overrides, and exploration elements.
- Indoor node behaviors and indoor governance findings from `IndoorRuleGovernanceService`.
- Reward rules, reward rule bindings, reward presentations, and reward governance data from the reward domain.
- Storyline mode chapter overrides introduced in Phase 30.
- POI experience steps introduced in Phase 29.

The safest approach is to add an admin-only aggregation DTO and service facade. This keeps existing indoor/reward services intact while allowing Phase 31 governance to display one unified index.

## Recommended Backend Shape

Add or extend admin DTOs:

- `AdminExperienceTemplatePresetResponse`
- `AdminExperienceTemplateUsageResponse`
- `AdminExperienceGovernanceQueryRequest`
- `AdminExperienceGovernanceItemResponse`
- `AdminExperienceGovernanceDetailResponse`
- `AdminExperienceGovernanceConflictResponse`

Extend `AdminExperienceOrchestrationController` with:

- `GET /api/admin/v1/experience/templates/presets`
- `POST /api/admin/v1/experience/templates/{templateId}/clone`
- `GET /api/admin/v1/experience/templates/{templateId}/usage`
- `GET /api/admin/v1/experience/governance/items`
- `GET /api/admin/v1/experience/governance/items/{itemKey}`
- `POST /api/admin/v1/experience/governance/check`

If service size becomes too large, create `AdminExperienceGovernanceService` and let the controller delegate governance-specific calls there.

## Recommended UI Shape

The existing `ExperienceOrchestrationWorkbench` already hosts tabs for templates and governance. Phase 31 should either:

- keep that page and extract richer components, or
- introduce dedicated components under `src/pages/Experience/` for the two specialized tabs.

Do not add generic placeholders. The specialized tabs should include:

- Template category navigation.
- Preset cards with "套用為新模板".
- Template list with filters: keyword, type, category, risk, status, used/unused.
- Structured editor with template-specific presets for config/schema payloads.
- Usage drawer listing all flow steps, POI flows, story overrides, indoor behaviors, and reward rules that reference the template or matching code.
- Governance list with filters for city, sub-map, POI, indoor building, storyline, chapter, owner type, template type, trigger type, effect family, reward type, story override, high risk, and status.
- Conflict detail panel that reuses the indoor conflict panel visual pattern but is not indoor-only.

## Conflict Checks To Implement First

Implement deterministic checks that can be smoke-tested locally:

1. `overlapping_fullscreen_effect`: same owner scope has more than one published step/template with full-screen media effect and overlapping trigger family.
2. `duplicate_reward_grant`: same reward rule id or reward target appears in more than one published step/effect with overlapping owner scope.
3. `required_step_disabled`: a published override disables a step whose inherited step is required for completion and has no replacement.
4. `shared_pickup_policy_mismatch`: same pickup/collectible key appears in multiple steps with different reward/effect payload policy.
5. `high_risk_template_without_usage_guard`: high or critical risk template is published but lacks schema JSON or has no usage references.

These checks can be approximate in Phase 31. Exact geospatial overlap and gameplay runtime validation can be deferred.

## Validation Architecture

Use existing compile/build plus live smoke verification:

- Admin backend compile:
  `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml`
- Public backend compile if public DTOs are touched:
  `mvn -q -DskipTests compile -f packages/server/pom.xml`
- Admin UI build:
  `npm run build --prefix packages/admin/aoxiaoyou-admin-ui`
- Seed import:
  `mysql --host=127.0.0.1 --port=3306 --user=root --password=Abc123456 --default-character-set=utf8mb4 aoxiaoyou < scripts\local\mysql\init\42-phase-31-interaction-template-governance.sql`
- Live smoke:
  `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-31-template-governance.ps1`
- Regression smoke:
  `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-30-storyline-mode.ps1`

## Risks

- Governance can become a slow cross-domain query if it attempts to eagerly join every domain table. Start with paginated item summaries and lazy detail endpoints.
- Raw JSON can creep back into UI. All common template types need structured preset panels first, with advanced JSON collapsed.
- Conflict detection can overclaim precision. Label Phase 31 checks as deterministic authoring checks, not as full runtime simulation.
- Existing dirty worktree is broad. Execution must avoid unrelated rewrites and avoid reverting user changes.

## Recommendation

Plan Phase 31 in four waves:

1. Backend template library and preset/usage contracts.
2. Backend governance aggregation and conflict detection.
3. Admin UI template library and governance center.
4. Seeds, smoke scripts, and regression verification.
