# Phase 16 Research: Indoor Rule Workbench and Governance Center

## Objective

Research how to turn the current inline indoor rule editor into a dedicated operator workbench and how to add a separate governance surface without breaking the Phase 15 canonical rule model.

## Current Starting Point

- Phase 15 already established the indoor rule write model around:
  - `indoor_nodes`
  - `indoor_node_behaviors`
  - typed `AdminIndoorBehaviorProfile` payloads
  - backend `validate-rule-graph`
- The current admin UI still concentrates too much responsibility in one file:
  - `MapTileManagement.tsx` owns floor selection, tile import, floor settings, CSV preview, canvas picking, draft recovery, marker form, and inline behavior editing
  - this creates high rerender pressure and poor operator focus for the densest rule-authoring workflow
- The current data contract is already rich enough to support a workbench:
  - behavior names in four languages
  - ordered behavior profiles
  - status
  - appearance / trigger / effect arrays
  - path graph
  - overlay geometry
- What does not exist yet is the operator-scale read model:
  - no dedicated rule overview endpoint
  - no conflict analysis endpoint
  - no cross-entity governance page

## What Phase 16 Must Resolve

1. Split the crowded inline authoring page into:
   - base node form
   - dedicated full-screen rule workbench
2. Give each behavior a true operator identity:
   - name
   - code
   - order
   - status
3. Keep point and path tooling inside the workbench so operators are not forced back into the inline page.
4. Add a rule governance center that works across indoor nodes and linked entities.
5. Avoid creating a second canonical rule system.

## Key Architectural Decision

### Keep Phase 15 as the canonical write model

Phase 16 should not redesign the persistence model again.

Recommended rule:
- continue using `AdminIndoorNodeUpsertRequest` and `AdminIndoorBehaviorProfile` as the write contract
- continue storing behavior segments through the Phase 15 backend model
- add read-oriented governance DTOs and service projections on top of the existing tables

Why:
- avoids churn before Phase 17 runtime work
- keeps workbench apply/save simple
- prevents the governance center from drifting into a separate schema

## Recommended Workbench Architecture

### 1. Isolated editor state

The current inline editor uses the parent `markerForm` as the live source of truth. That is the right source of truth for final submission, but the wrong place for every intermediate rule-editing interaction.

Recommended pattern:
- parent node form keeps the persisted payload
- `IndoorRuleWorkbench` receives a cloned `behaviors` array plus node presentation context
- workbench owns temporary `draftBehaviors`
- operator presses `套用至此標記`
- only then sync back into `markerForm`

This sharply reduces noisy parent-form rerenders and gives the workbench freedom to manage validation, dirty state, and cancel/apply semantics.

### 2. Full-screen shell instead of small modal

The workbench needs three simultaneous contexts:
- behavior rail
- detailed rule editor
- map-assisted geometry tools

A full-screen modal or drawer is the right fit. A normal modal is not.

### 3. Reuse the current floor canvas, but move it into the workbench

The current `FloorCanvas` already solves:
- floor-image or tile preview
- node previews
- current draft path preview
- current overlay preview
- point picking

The right move is reuse, not replacement:
- lift or extract the canvas and minimap controls into a workbench-side panel
- keep the same normalized coordinate model
- keep loading guards around floor switching

### 4. Workbench validation contract

Recommended validation stack:
- local validation for missing behavior name/code, impossible ordering, empty effect chains, missing path graph when `path_motion` exists
- backend validation via existing `/nodes/validate-rule-graph`
- show warnings and blocking errors in a validation summary panel

The workbench should refuse `套用` on blocking validation failures.

## Recommended Governance Architecture

### 1. Build a read-oriented governance service, not a second write model

The governance center needs fast filtering and conflict summaries. It does not need to become the main write path.

Recommended backend layer:
- new admin service dedicated to governance queries
- projection DTOs built from:
  - `indoor_nodes`
  - `indoor_node_behaviors`
  - building/floor references
  - linked entities already present in admin tables

Recommended endpoints:
- `GET /api/admin/v1/map/indoor/rules/overview`
- `GET /api/admin/v1/map/indoor/rules/conflicts`
- `GET /api/admin/v1/map/indoor/rules/behaviors/{behaviorId}`
- `PATCH /api/admin/v1/map/indoor/rules/behaviors/{behaviorId}/status`

### 2. Start with on-demand conflict analysis

For this phase, a persisted conflict index is not necessary yet. On-demand server-side analysis is enough if the filters are scoped.

Recommended initial conflict classes:
- `MISSING_PREREQUISITE`
- `SCHEDULE_OVERLAP`
- `ENTITY_COLLISION`
- `STATUS_MISMATCH`

That is enough to make the governance page meaningful without inventing a full rules engine before Phase 17.

### 3. Scope the governance page to indoor rules plus linked entities

The user wants to inspect points, rewards, interactive objects, and trigger chains together. The most honest first cut is:
- govern indoor node behaviors directly
- surface linked entity references for activity, collectible, badge, chapter, event, or task
- allow filtering by linked entity type and ID

This gives cross-entity visibility without pretending every domain has been fully normalized into one meta-rule registry.

## UI / Information Architecture Recommendation

### Workbench

- Entry point lives in the parent indoor page as a summary card + `編輯互動規則`
- Left rail:
  - list of behaviors
  - add / duplicate / reorder / enable / disable
  - clear naming hierarchy
- Center:
  - name and code
  - tabs for appearance / triggers / effects
  - status and runtime support
- Right rail:
  - minimap
  - point/path tools
  - validation summary

### Governance center

- new route under the map/space cluster
- filter bar with:
  - keyword
  - city
  - building
  - floor
  - POI
  - linked entity type
  - status
  - runtime support
  - conflict only
  - enabled only
- results table with:
  - behavior name
  - code
  - parent node / marker
  - floor/building
  - linked entity
  - status
  - conflict count
- side panel or drawer with:
  - appearance summary
  - trigger chain summary
  - effect summary
  - path/overlay presence
  - conflict list

## Risks and Mitigations

### Risk: workbench and inline page drift apart

Mitigation:
- parent page owns only summary + final payload handoff
- workbench writes back the same `behaviors` structure used by existing save endpoints

### Risk: governance center becomes slow or noisy

Mitigation:
- keep filters server-side
- scope conflict analysis to filtered result sets
- start with four deterministic conflict classes

### Risk: operator loses context when moving to full-screen editor

Mitigation:
- keep node basics summary visible in the workbench header
- keep minimap and current floor name visible at all times
- preserve unsaved-change confirmation and draft behavior

### Risk: Phase 16 leaks into Phase 17 runtime evaluation

Mitigation:
- do not add runtime execution logic here
- keep all outputs focused on editing, read models, and governance visibility

## Recommendation Summary

- Preserve the Phase 15 canonical write model.
- Move dense rule editing into a staged full-screen workbench.
- Reuse `FloorCanvas` and the existing normalized coordinate model inside the workbench.
- Add backend governance projections and on-demand conflict analysis instead of a new write schema.
- Add a dedicated `互動規則中心` route for filtering, conflict review, and enable/disable actions.

## Validation Architecture

- Use JUnit 5 plus Spring Boot Test in `packages/admin/aoxiaoyou-admin-backend` for governance DTO, conflict classification, and status-toggle behavior.
- Use `npm run build` in `packages/admin/aoxiaoyou-admin-ui` as the baseline frontend regression gate.
- Add a dedicated Phase 16 smoke script for admin `8081` to assert:
  - rule overview returns deterministic Lisboa behaviors
  - conflict endpoint returns classified issues
  - behavior status toggles persist
  - parent indoor page still round-trips workbench-applied behaviors
- Keep manual UAT for:
  - workbench cancel/apply behavior
  - naming and ordering usability
  - minimap-assisted path editing inside the workbench
  - governance filtering and conflict triage

## Phase 16 Output Implications

Phase 16 plans should therefore include:
- backend governance projection endpoints and conflict classification
- a dedicated full-screen rule workbench integrated into the current indoor page
- a separate governance center route and page
- smoke and UAT artifacts for workbench/governance behavior

They should not include:
- public runtime rule evaluation
- mini-program trigger execution
- AI capability-center work
