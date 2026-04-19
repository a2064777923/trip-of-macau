# Phase 16: Indoor Rule Workbench and Governance Center - Context

**Gathered:** 2026-04-16
**Status:** Inserted into roadmap, awaiting planning
**Source:** post-implementation operator feedback on the current Phase 15 indoor rule editor, plus the v2.1 roadmap update on 2026-04-16

<domain>
## Phase Boundary

This phase starts after the Phase 15 rule model and baseline authoring work.

It covers:
- moving the densest interaction-rule editing flow out of the inline indoor marker form and into a dedicated full-screen workbench
- adding operator-defined behavior naming, ordering, validation, and apply/save control for multi-step behaviors
- embedding thumbnail-based point picking and path authoring inside that dedicated workbench instead of forcing operators to bounce between cramped inline sections
- adding a separate governance page that can search, filter, inspect, enable/disable, and conflict-check rule-bearing entities across indoor maps and related content

It does not cover:
- executing authored rules in the public backend or mini-program runtime
- final runtime support guarantees for every authored trigger/effect combination
- general AI capability-center work
- unrelated non-rule admin redesign outside the indoor rule governance boundary
</domain>

<discovery>
## Why This Became Its Own Phase

Operator testing on the current indoor authoring page showed that the interaction-rule section is no longer "just another form block".

The newly discovered scope includes:
- the need for a dedicated workbench with isolated validation before changes are applied back to the marker form
- custom naming for behaviors so operators can distinguish multiple rules without relying on "互動行為 1/2/3"
- a stronger visual-authoring loop with in-modal thumbnail point picking and route/path authoring
- a cross-entity governance view for points, rewards, interaction objects, trigger chains, overlap/conflict review, and rule lifecycle control

That combination is large enough that keeping it hidden inside Phase 15 would blur the boundary between:
1. authoring model and persistence
2. operator-scale authoring ergonomics and governance
</discovery>

<decisions>
## Implementation Decisions

### Workbench Shape
- Prefer a full-screen modal or large drawer workbench over a small modal.
- The workbench should stage edits locally, validate them, and only apply back to the parent marker form after success.

### Behavior Identity
- Every behavior must support:
  - human-readable operator name
  - stable behavior code
  - sortable order
  - lifecycle status such as draft / enabled / disabled
- Generated labels like "互動行為 1" may remain as defaults only, never as the sole identity model.

### Visual Authoring
- The workbench must include a floor thumbnail or minimap with:
  - point picking
  - route/path drawing or append-point flows
  - current behavior preview context
- Visual authoring should live inside the same editing surface as the rule logic, not in a separate distant panel.

### Governance Center
- A dedicated page must exist outside the per-marker form.
- It should support:
  - filtering by map, sub-map, indoor building, floor, POI, entity type, status, and enabled state
  - viewing trigger-chain and dependency relationships
  - spotting overlapping schedules, contradictory conditions, or suspicious trigger collisions
  - bulk enable/disable or scoped lifecycle actions where safe

### Data Contract Direction
- The governance surface should not force a second incompatible rule model.
- Phase 16 should build on the Phase 15 canonical rule/persistence contract and add summary/index data only where needed for governance and conflict analysis.
</decisions>

<canonical_refs>
## Canonical References

### Roadmap and requirements
- `.planning/ROADMAP.md` - updated v2.1 phase order and new Phase 16 scope
- `.planning/REQUIREMENTS.md` - `RULE-04` and `RULE-05`
- `.planning/STATE.md` - roadmap evolution note and downstream phase-number shift

### Current indoor authoring baseline
- `.planning/phases/15-indoor-interaction-rule-authoring/15-CONTEXT.md`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/MapTileManagement.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/components/indoor/IndoorRuleAppearanceEditor.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/components/indoor/IndoorRuleTriggerChainEditor.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/components/indoor/IndoorRuleEffectEditor.tsx`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/IndoorMarkerAuthoringService.java`

### Downstream runtime boundary
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/IndoorController.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicIndoorServiceImpl.java`
- `packages/client/src/pages/map/indoor/index.tsx`
</canonical_refs>

<specifics>
## Specific Design Targets

- Replace the current inline "互動規則編排" block with a clear entry action such as "編輯互動規則".
- Open a dedicated workbench that contains:
  - behavior list rail
  - behavior detail editor
  - validation summary
  - floor thumbnail / point-path tools
- Keep the parent marker form responsible only for the node basics and the final persisted payload handoff.
- Add a separate "規則中心" or "互動規則中心" page for global inspection and governance.
</specifics>

<deferred>
## Deferred Ideas

- Runtime execution and mini-program support still belong to the next phase.
- AI-assisted rule authoring and auto-generated trigger suggestions remain later work.
- Broad non-indoor governance across every domain can start from the indoor-rule center and expand later if needed.
</deferred>

---

*Phase: 16-indoor-rule-workbench-and-governance*
*Context gathered: 2026-04-16 from active operator feedback and the updated v2.1 roadmap*
