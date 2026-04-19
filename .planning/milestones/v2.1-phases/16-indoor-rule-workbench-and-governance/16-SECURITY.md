---
phase: 16
slug: indoor-rule-workbench-and-governance
status: verified
threats_open: 0
asvs_level: 1
created: 2026-04-17
---

# Phase 16 - Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Indoor behavior persistence into governance projections | Canonical indoor node and behavior rows are projected into governance overview/detail responses for operators. | behavior names, codes, statuses, rule counts, linked entity references, runtime support, conflict counts |
| Behavior status mutations back into canonical storage | Governance actions update a single behavior while preserving parent node integrity and returning actionable warnings. | behaviorId, requested status, parent node status, runtime support level, warning messages |
| Parent node form into staged workbench state | Authoring opens a cloned draft that must not mutate the parent form until the operator explicitly applies changes. | behavior arrays, naming fields, status, ordering, path graph, overlay geometry, validation results |
| Visual map tools inside the workbench | Minimap picking and path editing normalize relative coordinates and overlay geometry before apply. | relative x/y, path points, overlay points, pick mode, floor preview, draft validation state |
| Governance center filters into backend rule projections | Filter state from the governance page scopes backend result sets for triage and status actions. | keyword, cityId, buildingId, floorId, relatedPoiId, linked entity type/id, status, runtime support, conflict-only flags |
| Governance actions back into authoring routes | Operators deep-link from governance rows into the exact building and floor authoring context. | behaviorId, nodeId, buildingId, floorId, route query params, selected behavior context |
| Live smoke execution against the local admin runtime | The smoke harness authenticates to the local admin backend on port 8081 and exercises governance endpoints end to end. | admin auth token, building and floor identifiers, behavior payloads, governance overview rows, conflict detail, status mutations |
| Manual operator validation of staged workbench behavior | UAT proves staged editing, naming, ordering, minimap editing, conflict inspection, and deep-linking remain understandable to operators. | staged edits, behavior names, sort order, apply/cancel transitions, conflict summaries, navigation context |

---

## Threat Register

Phase 16 plan files reused `T-16-01` and `T-16-02` across backend and frontend slices. This register collapses them into unique phase-level threats.

| Threat ID | Category | Component | Disposition | Mitigation | Status |
|-----------|----------|-----------|-------------|------------|--------|
| T-16-01 | Projection Drift | governance overview and page rendering | mitigate | `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/IndoorRuleGovernanceService.java` builds overview/detail DTOs directly from canonical node and behavior tables, `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminIndoorController.java` exposes dedicated governance endpoints, and the admin UI consumes typed governance DTOs instead of ad hoc joins. Verified by `IndoorRuleGovernanceServiceTest.listOverviewIncludesRuntimeSupportAndConflictCount`, local `mvn -q "-Dtest=IndoorRuleGovernanceServiceTest,IndoorRuleAuthoringServiceTest" test`, and `scripts/local/smoke-phase-16-indoor-rule-governance.ps1` asserting `behaviorNameZht`, `runtimeSupportLevel`, and `conflictCount` against `lisboeta_macau`. | closed |
| T-16-02 | Conflict Misclassification / Operator Blind Spot | conflict analysis and triage | mitigate | `IndoorRuleGovernanceService` classifies conflicts with deterministic codes `MISSING_PREREQUISITE`, `SCHEDULE_OVERLAP`, `ENTITY_COLLISION`, and `STATUS_MISMATCH`, while `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/IndoorRuleCenter.tsx` and `packages/admin/aoxiaoyou-admin-ui/src/components/indoor/IndoorRuleConflictPanel.tsx` surface conflict counts and detail drawers for operators. Verified by `IndoorRuleGovernanceServiceTest` assertions for all four codes, the local Maven test run above, and the phase 16 governance smoke exercising overview and conflict detail on port 8081. | closed |
| T-16-03 | Unsafe Toggle | behavior status mutation | mitigate | `AdminIndoorController` restricts status mutation to `PATCH /rules/behaviors/{behaviorId}/status`, and `IndoorRuleGovernanceService.buildStatusWarnings(...)` returns `parentNodeStatus` plus blocking warnings for unpublished or storage-only parent state instead of allowing a silent enable. Verified by `IndoorRuleGovernanceServiceTest.updateBehaviorStatusReturnsWarningsWhenEnableIsBlocked`, the local Maven test run, and the phase 16 smoke script's live status mutation checks. | closed |
| T-16-04 | State Loss | staged editing | mitigate | `packages/admin/aoxiaoyou-admin-ui/src/components/indoor/IndoorRuleWorkbench.tsx` clones rule data into isolated draft state, validates before apply, and uses explicit `onApply` and `onClose` flows with `destroyOnClose` so parent form state is not mutated on every interaction. Verified by `16-UAT.md` coverage for naming, ordering, apply/cancel, and behavior persistence. | closed |
| T-16-05 | Usability Failure | map-assisted rule editing | mitigate | `IndoorRuleWorkbench.tsx`, `packages/admin/aoxiaoyou-admin-ui/src/components/indoor/IndoorRuleWorkbenchMapPanel.tsx`, `packages/admin/aoxiaoyou-admin-ui/src/components/indoor/IndoorRuleValidationSummary.tsx`, and `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/MapTileManagement.tsx` keep minimap pick modes, validation feedback, and floor-loading guards inside the workbench flow. Verified by local admin UI build in the smoke run and `16-UAT.md` checks for minimap editing, clear-and-repick, and scoped behavior display. | closed |
| T-16-06 | False Proof | phase verification | mitigate | `scripts/local/smoke-phase-16-indoor-rule-governance.ps1` authenticates to the live admin stack on `http://127.0.0.1:8081`, exercises governance overview/detail/conflict/status flows, and asserts exact behavior fields instead of page-load-only checks. Re-verified during this security pass: `Invoke-WebRequest http://127.0.0.1:8081/swagger-ui.html` returned `200`, and the smoke script completed successfully on 2026-04-17. | closed |
| T-16-07 | Fixture Drift | smoke data selection | mitigate | The smoke harness explicitly searches for building code `lisboeta_macau` and fails fast if the deterministic indoor fixture is missing, preventing silent success on the wrong city or floor. Verified by `scripts/local/smoke-phase-16-indoor-rule-governance.ps1` fixture assertions and the successful live smoke run against the Lisboa indoor records. | closed |
| T-16-08 | Usability Blind Spot | manual acceptance | mitigate | `D:\Archive\trip-of-macau\.planning\phases\16-indoor-rule-workbench-and-governance\16-UAT.md` provides a resumable operator checklist covering staged editing clarity, naming, ordering, minimap editing, conflict inspection, status switching, and governance deep-linking. The recorded result is `passed: 7`, `issues: 0`, confirming manual acceptance coverage exists and was completed. | closed |

*Status: open or closed*
*Disposition: mitigate (implementation required) or accept (documented risk) or transfer (third-party)*

---

## Accepted Risks Log

No accepted risks.

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-04-17 | 8 | 8 | 0 | Codex (`/gsd-secure-phase 16`) |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-04-17
