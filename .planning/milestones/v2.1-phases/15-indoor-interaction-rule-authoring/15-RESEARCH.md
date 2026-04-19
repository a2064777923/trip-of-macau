# Phase 15 Research: Indoor Interaction Rule Authoring

## Objective

Research how to implement Phase 15 so the current indoor map tooling evolves from static marker CRUD into a real authoring platform for indoor appearance, trigger, effect, and path-based interaction rules.

## Current Starting Point

- Phase 12 already delivered a live indoor foundation:
  - indoor building and floor CRUD
  - tile ZIP import and full-image slicing
  - marker CRUD
  - preview-first CSV import
  - minimap point picking
  - live public indoor reads
- The current indoor rule shape is still flat and JSON-heavy:
  - `IndoorNode.java` stores `tags`, `popup_config_json`, `display_config_json`, and `metadata_json`
  - `AdminIndoorMarkerUpsertRequest.java` exposes those JSON strings directly
  - `IndoorMarkerAuthoringService.java` only validates JSON syntax, not rule meaning
- `MapTileManagement.tsx` already contains helpful authoring primitives such as drafts, linked-entity selectors, minimap picking, media asset selection, and CSV preview, but it still treats behavior mainly as loose JSON fragments.
- The public backend and mini-program indoor runtime only render static markers today. They do not evaluate rule graphs yet.

## What Phase 15 Must Resolve

1. Replace flat JSON-only behavior editing with a canonical indoor interaction-rule model.
2. Support both marker and overlay authoring within the same floor workspace.
3. Make complex behaviors authorable through structured admin forms and path editors instead of freehand JSON.
4. Persist rules in a way that Phase 16 can evaluate safely without redesigning the data model.
5. Preserve current Phase 12 strengths:
  - normalized relative coordinates
  - preview-first CSV handling
  - floor-canvas point picking
  - additive, brownfield-safe schema evolution

## Baseline Gaps

### 1. Data-model gap

The current `indoor_nodes` shape can store display metadata, but it cannot represent:
- typed appearance conditions
- typed trigger chains with prerequisites
- typed effect lists
- path geometry with motion timing
- overlay geometry
- runtime-support annotations

### 2. Validation gap

The admin backend currently proves only:
- required names
- normalized coordinates
- same-city POI binding
- JSON parseability

It does not prove:
- condition type compatibility
- trigger prerequisite validity
- effect-to-trigger mapping integrity
- path point normalization
- overlay geometry shape validity

### 3. UX gap

Operators can pick points and import CSV rows, but they cannot visually compose:
- condition groups
- trigger chains
- effect stacks
- movement paths
- overlay regions

### 4. Phase-boundary gap

If Phase 15 tries to evaluate the rules now, it will duplicate Phase 16 and over-expand scope. The right outcome is an authoring-safe contract that Phase 16 can execute.

## Recommended Architecture

### 1. Keep `indoor_nodes` as the anchor entity, but stop treating it as the whole rule model

The current `indoor_nodes` table already anchors floor-relative interactive objects. Keep it as the parent record and extend it additively for authoring concepts that belong to the node itself:
- `presentation_mode` with values like `marker` and `overlay`
- `overlay_type` with values like `point`, `polyline`, `polygon`, `bubble`, `media`
- `overlay_geometry_json` for normalized geometry
- `inherit_linked_entity_rules` or equivalent inheritance toggle
- optional authoring summary fields such as `rule_status` or `runtime_support_level`

This preserves existing Phase 12 marker identity and avoids a disruptive replacement of the current indoor stack.

### 2. Store the interaction rule graph in a dedicated behavior-profile layer

For brownfield compatibility, the best fit is a dedicated indoor behavior-profile table with typed JSON segments, rather than many tiny relational tables or one giant undifferentiated JSON blob on `indoor_nodes`.

Recommended shape:
- `indoor_node_behaviors`
  - `node_id`
  - `behavior_code`
  - `behavior_name_zh`
  - `behavior_name_en`
  - `behavior_name_zht`
  - `behavior_name_pt`
  - `appearance_preset_code`
  - `trigger_template_code`
  - `effect_template_code`
  - `appearance_rules_json`
  - `trigger_rules_json`
  - `effect_rules_json`
  - `path_graph_json`
  - `inherit_mode`
  - `runtime_support_level`
  - `sort_order`
  - `status`

Why this is the right compromise:
- additive to the existing schema
- easy for MyBatis-Plus entities and DTO mapping
- expressive enough for nested chains and geometry
- future Phase 16 runtime can consume the same profile shape directly
- better than freehand JSON because the service layer can validate a typed schema

### 3. Introduce typed DTOs for authoring, even if storage stays JSON-backed

Phase 15 should create typed payload DTOs such as:
- `AdminIndoorNodeBehaviorPayload`
- `AdminIndoorAppearanceRulePayload`
- `AdminIndoorTriggerRulePayload`
- `AdminIndoorEffectRulePayload`
- `AdminIndoorPathPointPayload`

These DTOs should be the admin contract. They can serialize into JSON columns, but operators and downstream code should no longer reason in terms of arbitrary strings.

### 4. Keep controller compatibility but introduce canonical node-oriented endpoints

The existing controller is marker-oriented. Phase 15 should add canonical node-oriented endpoints while preserving compatibility where practical:
- `GET /api/admin/v1/map/indoor/floors/{floorId}/nodes`
- `POST /api/admin/v1/map/indoor/floors/{floorId}/nodes`
- `PUT /api/admin/v1/map/indoor/nodes/{nodeId}`
- `DELETE /api/admin/v1/map/indoor/nodes/{nodeId}`
- `POST /api/admin/v1/map/indoor/nodes/validate-rule-graph`

Legacy `markers` endpoints can remain as alias or marker-filtered compatibility routes so the existing Phase 12 surface does not break mid-upgrade.

### 5. Build the admin authoring UI around structured editors and visual geometry tools

The current `MapTileManagement.tsx` already owns the right workspace. Phase 15 should evolve it, not replace it.

Recommended UI additions:
- node basics panel
  - presentation mode
  - overlay type
  - linked entity and inheritance mode
  - status and runtime-support badge
- appearance editor
  - schedule window
  - recurring days or calendar pattern
  - progress predicate builder
  - dwell/proximity toggles
  - preset selector
- trigger-chain editor
  - add step
  - select trigger type
  - configure prerequisite trigger
  - configure enable/disable order
- effect editor
  - popup
  - bubble
  - media playback
  - reward/task/account mutation hooks
  - path-motion binding
- path and geometry editor
  - click-to-add points
  - reorder points
  - remove points
  - set duration, easing, loop, and hold time
  - toggle point / polyline / polygon overlay preview

Raw JSON should move behind an explicit advanced toggle or diagnostic drawer.

### 6. Extend CSV and draft workflows without forcing path authoring into CSV

CSV remains useful for bulk point-node creation. The right Phase 15 extension is:
- allow optional preset columns such as `presentationMode`, `appearancePresetCode`, `triggerTemplateCode`, `effectTemplateCode`, and `inheritMode`
- continue preview-before-confirm validation
- keep path geometry visual/manual only unless a later requirement needs path CSV import

This preserves the strong Phase 12 import pattern without overcomplicating bulk authoring.

### 7. Add authoring-safe runtime support annotations now

Some trigger categories in the user's vision, such as voice or shout interaction, may not be executable immediately in Phase 16. The authoring contract should carry a machine-readable support state such as:
- `runtimeSupportLevel = authoring_only`
- `runtimeSupportLevel = planned_phase_16`
- `runtimeSupportLevel = ready`

This avoids false promises in the admin UI while preserving the richer rule graph.

## Recommended Verification Strategy

### Backend
- add rule-graph validation tests in `packages/admin/aoxiaoyou-admin-backend`
- verify invalid prerequisite references, invalid geometry, and incompatible condition/effect combinations are rejected
- verify legacy marker reads still work for plain marker nodes

### Admin UI
- verify the admin UI builds after the structured editor refactor
- verify floor switching does not reintroduce the flicker loop the user already reported
- verify draft recovery and selector filtering still work with node/overlay authoring

### Smoke
- create a Phase 15 smoke script that:
  - logs into admin `8081`
  - fetches a Lisboa floor
  - creates or updates a node behavior profile with structured rules
  - reads the same record back
  - confirms the persisted rule graph and geometry match the authored payload

### Manual proof
- prepare a Phase 15 UAT checklist for:
  - authoring a schedule-gated overlay
  - authoring a prerequisite trigger chain
  - drawing and editing a motion path
  - restoring a saved draft

## Risks and Mitigations

### Risk: Repeating the JSON-only pattern

Mitigation:
- typed DTOs and structured UI must become the main contract, even if persistence uses JSON columns behind the scenes

### Risk: Rule model too rigid for future runtime

Mitigation:
- use typed JSON segments plus enumerated categories, not a hardcoded fixed number of SQL columns per condition type

### Risk: Overlay support becomes a second system

Mitigation:
- keep marker and overlay authoring on the same node model and the same behavior-profile layer

### Risk: Phase 15 leaks into runtime evaluation

Mitigation:
- keep public/backend runtime execution work out of scope and annotate support states explicitly

## Recommendation Summary

- Keep the Phase 12 indoor foundation intact.
- Extend `indoor_nodes` additively for presentation and overlay metadata.
- Add a dedicated behavior-profile table with structured JSON segments for appearance, triggers, effects, and path graph.
- Expose typed admin DTOs and node-oriented authoring endpoints.
- Refactor `MapTileManagement.tsx` into a structured indoor interaction-rule workspace.
- Verify authoring persistence with Lisboa showcase data and an admin smoke harness.

## Validation Architecture

- Use JUnit 5 plus Spring Boot Test in `packages/admin/aoxiaoyou-admin-backend` as the primary automated verification baseline for rule-schema validation and authoring persistence.
- Use `npm run build` in `packages/admin/aoxiaoyou-admin-ui` as the main frontend regression gate for the indoor authoring workspace refactor.
- Add a PowerShell smoke script for admin `8081` that round-trips a structured indoor behavior payload against the live local stack.
- Keep Phase 15 validation authoring-focused:
  - quick: targeted admin backend tests and admin UI build
  - full: admin backend tests, admin UI build, and Phase 15 smoke script
- Manual UAT remains necessary for path-editing usability and trigger-chain clarity, but automated smoke must still prove persistence and readback.

## Phase 15 Output Implications

Phase 15 plans should therefore include:
- additive indoor rule schema and behavior-profile persistence
- typed admin DTOs, validation, and controller endpoints
- a structured indoor authoring workspace in the admin UI
- Lisboa showcase rule seeds
- authoring persistence smoke and UAT scaffolding

They should not try to finish:
- public runtime delivery of executable rule graphs
- mini-program evaluation logic
- final support for every rich trigger type in production
