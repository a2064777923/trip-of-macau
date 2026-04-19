# Phase 21: Indoor Rule Acceptance and Verification Closure - Context

**Gathered:** 2026-04-18
**Status:** Ready for planning
**Mode:** Auto-discuss via `/gsd-next`

<domain>
## Phase Boundary

This phase closes the indoor-rule milestone chain by proving that the existing authoring, governance, public runtime, and mini-program indoor flows from Phases 15-17 work coherently on the live local stack.

It covers:
- milestone-grade verification of the admin authoring surface, rule workbench, and governance center
- repeatable live verification on admin `8081` and public `8080`
- WeChat DevTools acceptance for the current indoor runtime contract and auth-gated interaction behavior
- formal verification and UAT artifacts that consolidate the closure evidence for `RULE-01` through `RULE-05`

It does not cover:
- new indoor rule capabilities, new trigger categories, or new governance features
- redesigning the mini-program indoor experience beyond what is needed to validate the promised runtime contract
- broader non-indoor control-plane work or future AI expansion
</domain>

<decisions>
## Implementation Decisions

### Verification Boundary
- **D-01:** Phase 21 is a closure and acceptance phase, not a net-new feature phase. Any code changes should be limited to fixing defects that block honest verification of the promised Phase 15-17 contract.
- **D-02:** Acceptance must stay anchored to the existing canonical Lisboa showcase instead of inventing a new fixture set for verification.
- **D-03:** Verification has to cover the whole indoor chain end-to-end: authoring surfaces, governance center, public runtime payloads, and mini-program execution.

### Evidence Strategy
- **D-04:** Use layered evidence rather than one fragile proof point: deterministic fixture/seed checks, admin smoke, public runtime smoke, targeted automated tests, client build verification, and manual WeChat DevTools UAT.
- **D-05:** Phase 21 must create its own explicit closure artifacts rather than relying on scattered prior summaries as implicit proof.
- **D-06:** Verification should reuse and extend the existing Phase 15/16/17 smoke assets where possible instead of rewriting the whole harness stack from scratch.

### Runtime And Auth Coverage
- **D-07:** The closure path must prove both anonymous-safe and auth-gated indoor interactions on the current live auth model.
- **D-08:** The supported behavior witness set remains the three authored Lisboa examples:
  - schedule-gated overlay
  - dwell-triggered guarded reveal
  - path-motion guidance with bubble or popup effect
- **D-09:** Unsupported or blocked authored behaviors must fail with visible, operator-verifiable explanations such as `blockedReason`, not silent no-op behavior or crashes.

### Mini-Program Acceptance Boundary
- **D-10:** Manual WeChat DevTools acceptance is required for Phase 21 because visual rendering, floor switching, loading guards, and interaction feel cannot be closed honestly by backend smoke alone.
- **D-11:** The current milestone only verifies the existing indoor runtime contract; deeper mini-program-specific linkage logic and bespoke experience design stay deferred to a later milestone, per prior user direction.
- **D-12:** The acceptance checklist must explicitly verify multilingual `zh-Hant` quality on floor labels, node labels, and runtime prompts because this project is sensitive to encoding and copy regressions.

### The Agent's Discretion
- Whether Phase 21 uses one consolidated smoke script or composes the existing Phase 15/16/17 smokes behind a wrapper, provided the final verification path is repeatable and honest.
- Exact naming and structure of the Phase 21 verification artifacts, provided they clearly supersede the earlier incomplete milestone-close evidence.
- Whether fixture preparation is handled by a dedicated helper or by extending the existing Lisboa seed path.
</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Roadmap and requirement anchors
- `.planning/ROADMAP.md` - Phase 21 goal, success criteria, and dependency chain from Phases 15-17
- `.planning/REQUIREMENTS.md` - `RULE-01` through `RULE-05`
- `.planning/STATE.md` - current milestone state and out-of-order execution notes

### Prior indoor phase context and verification assets
- `.planning/phases/15-indoor-interaction-rule-authoring/15-CONTEXT.md` - authoring-scope decisions and canonical rule model boundary
- `.planning/phases/15-indoor-interaction-rule-authoring/15-UAT.md` - manual authoring acceptance checklist
- `.planning/phases/15-indoor-interaction-rule-authoring/15-03-SUMMARY.md` - deterministic Lisboa showcase seed and authoring smoke baseline
- `.planning/phases/16-indoor-rule-workbench-and-governance/16-CONTEXT.md` - workbench/governance boundary decisions
- `.planning/phases/16-indoor-rule-workbench-and-governance/16-UAT.md` - workbench and governance acceptance checklist
- `.planning/phases/16-indoor-rule-workbench-and-governance/16-04-SUMMARY.md` - admin smoke and local verification baseline for governance
- `.planning/phases/17-indoor-runtime-evaluation-and-mini-program-alignment/17-UAT.md` - existing mini-program/runtime acceptance checklist
- `.planning/phases/17-indoor-runtime-evaluation-and-mini-program-alignment/17-03-SUMMARY.md` - public runtime smoke and client build proof

### Admin authoring and governance surfaces
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/MapTileManagement.tsx` - indoor marker form and workbench launch surface
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/IndoorRuleCenter.tsx` - rule governance center
- `packages/admin/aoxiaoyou-admin-ui/src/components/indoor/IndoorRuleWorkbench.tsx` - dedicated interaction-rule workbench
- `packages/admin/aoxiaoyou-admin-ui/src/components/indoor/IndoorRuleWorkbenchMapPanel.tsx` - minimap point and path authoring surface
- `packages/admin/aoxiaoyou-admin-ui/src/components/indoor/IndoorRuleValidationSummary.tsx` - validation UX pattern

### Admin backend and public runtime contract
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/IndoorMarkerAuthoringService.java` - canonical authoring write path
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/IndoorRuleGovernanceService.java` - rule-governance read and lifecycle surface
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/IndoorController.java` - public indoor endpoints
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicIndoorRuntimeServiceImpl.java` - runtime evaluation contract
- `packages/client/src/services/indoorRuntime.ts` - client-side runtime adapter
- `packages/client/src/pages/map/indoor/index.tsx` - mini-program indoor runtime page

### Existing smoke and fixture tooling
- `scripts/local/seed-lisboeta-indoor.ps1` - deterministic Lisboa indoor fixture promotion
- `scripts/local/smoke-phase-15-indoor-authoring.ps1` - authoring-layer smoke baseline
- `scripts/local/smoke-phase-16-indoor-rule-governance.ps1` - governance-layer smoke baseline
- `scripts/local/smoke-phase-17-indoor-runtime.ps1` - public runtime smoke baseline
</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `scripts/local/seed-lisboeta-indoor.ps1`: already prepares the canonical Lisboa building, floors, assets, and runtime-facing indoor data.
- `scripts/local/smoke-phase-15-indoor-authoring.ps1`, `scripts/local/smoke-phase-16-indoor-rule-governance.ps1`, `scripts/local/smoke-phase-17-indoor-runtime.ps1`: already cover the three layers that Phase 21 needs to close together.
- `packages/admin/aoxiaoyou-admin-ui/src/components/indoor/IndoorRuleWorkbench.tsx`: existing dedicated authoring surface that Phase 21 needs to validate, not redesign.
- `packages/client/src/services/indoorRuntime.ts` and `packages/client/src/pages/map/indoor/index.tsx`: existing client runtime integration points for supported behavior execution and auth gating.

### Established Patterns
- Indoor verification in this repo is already live-stack-first: PowerShell smoke against real ports, deterministic fixture discovery, then targeted tests and manual UAT.
- Lisboa `1F` and the Phase 15 witness behaviors already serve as the canonical showcase baseline.
- Manual UAT markdown files are used as resumable experiential checklists rather than mixing every visual check into automation.

### Integration Points
- Admin acceptance flows route through `MapTileManagement.tsx` and `IndoorRuleCenter.tsx`.
- Runtime acceptance flows route through `IndoorController.java`, `PublicIndoorRuntimeServiceImpl.java`, and the mini-program indoor page.
- Phase 21 verification artifacts should plug into the same local stack assumptions: admin backend on `8081`, public backend on `8080`, and the current real auth flow for guarded interactions.
</code_context>

<specifics>
## Specific Ideas

- Keep the witness set explicit in Phase 21 artifacts:
  - `1f-phase15-night-market-overlay`
  - `1f-phase15-royal-palace-dwell`
  - `1f-phase15-zipcity-path`
- Treat floor switching, loading masks, and stale-path cleanup as first-class acceptance checks because these were visible operator/user pain points.
- If DevTools acceptance finds a failure, record the exact failing checkpoint and live symptom in Phase 21 artifacts instead of soft-marking the whole chain as passed.
</specifics>

<deferred>
## Deferred Ideas

- Special mini-program linkage logic, bespoke gameplay orchestration, and future indoor-experience polish belong to a later milestone, not Phase 21 closure.
- New indoor behavior categories, new governance analytics, or broader cross-domain rule governance are out of scope here.
- Fully automated visual acceptance for WeChat DevTools remains out of scope; manual UAT stays required.
</deferred>

---

*Phase: 21-indoor-rule-acceptance-and-verification-closure*
*Context gathered: 2026-04-18*
