---
phase: 29-poi-default-experience-workbench
status: ready-for-planning
created: 2026-04-29
source:
  - .planning/ROADMAP.md
  - .planning/REQUIREMENTS.md
  - .planning/phases/28-story-and-content-control-plane-completion/28-HANDOFF.md
  - .planning/phases/28-story-and-content-control-plane-completion/28-VERIFICATION.md
---

# Phase 29: POI Default Experience Workbench - Context

<domain>

## Phase Boundary

Phase 29 builds the dedicated POI/default-location experience workbench on top of the Phase 28 experience orchestration foundation. It does not replace `experience_flows`, `experience_flow_steps`, `experience_bindings`, `experience_templates`, `exploration_elements`, or the public compiled runtime contract.

This phase turns the existing generic experience foundation into an operator-friendly POI authoring surface:

- Select a POI.
- Create or edit its `default_poi` / `walk_in` flow.
- Bind that flow to the POI through `ownerType=poi`, `bindingRole=default_experience_flow`.
- Configure ordered steps through timeline, condition cards, effect cards, media cards, reward cards, and reusable templates.
- Save POI steps as reusable templates for later story, indoor, reward, and activity use.
- Keep the A-Ma Temple default experience fully authorable without raw JSON as the primary path.
- Prove the resulting data still compiles through public `GET /api/v1/experience/poi/{poiId}`.

</domain>

<decisions>

## Locked Decisions

### D29-01 POI Workbench Is Dedicated, Not Generic CRUD
- The Phase 28 generic `ExperienceOrchestrationWorkbench` remains the foundation console.
- Phase 29 must add a POI-specific workbench entry that speaks in POI operator concepts: natural walk-in, tap-to-explore, route guidance, proximity media, check-in tasks, pickups, hidden dwell achievement, and reward/title grant.
- Operators must not need to open the generic flow table and manually infer which `flow_id`, `owner_type`, or JSON field belongs to a POI.

### D29-02 Backend Facade Reuses Phase 28 Model
- Add a POI experience admin facade if needed for atomic loading/saving, but persist to the Phase 28 tables.
- Do not create a parallel POI-only flow schema.
- Canonical binding remains `ownerType=poi`, `bindingRole=default_experience_flow`, `flowType=default_poi`, `mode=walk_in`.

### D29-03 Structured Forms Compile To Versioned JSON
- Raw JSON is not the primary authoring path.
- Structured card forms compile to `schemaVersion: 1` JSON payloads before saving.
- Advanced JSON remains a collapsed fallback and must still pass backend versioned-object validation.

### D29-04 A-Ma Temple Is The Acceptance Scenario
- The A-Ma Temple default experience must be editable end-to-end without hand-writing JSON:
  - Intro modal with "前往探索該地".
  - Route guidance with transport, recommended storylines, nearby POIs, and waypoint POIs.
  - 50-meter proximity full-screen media with background audio.
  - Check-in task release for "大門照片" and "賽博點香".
  - Pickup side clues.
  - Hidden dwell achievement at 30 meters for 30 minutes.
  - Reward/title grant after completion.

### D29-05 Template Reuse Is First-Class
- Every POI step can be saved as a reusable `experience_template`.
- Saved templates must use the canonical Phase 28 template vocabulary, not a POI-only vocabulary.
- Template usage must remain visible through Phase 28/31 governance surfaces.

### D29-06 Public Runtime Alignment Is Mandatory
- Phase 29 acceptance must compare admin-authored POI data with public `GET /api/v1/experience/poi/{poiId}` output.
- Public runtime should expose published compiled data only, not admin-only status or raw editor state.

### D29-07 UI Standards Carry Forward
- All admin copy is Traditional Chinese.
- Workbench layout follows the Phase 28 three-panel orchestration pattern: left selector/timeline, middle visual composition/preview, right property panel, bottom validation/conflict result.
- Invalid save must scroll to the first invalid card/control, focus it, and show a visible shake/error affordance.

### D29-08 Out Of Scope
- Storyline chapter inheritance and overrides are Phase 30.
- Cross-domain template governance and conflict analysis are Phase 31.
- User progress and dynamic exploration inspection are Phase 32.
- Full flagship material generation is Phase 33.
- Mini-program runtime experiential acceptance is Phase 34 or later.

</decisions>

<canonical_refs>

## Canonical References

### Planning And Handoff
- `.planning/ROADMAP.md` - Phase 29 goal, dependencies, and success criteria.
- `.planning/REQUIREMENTS.md` - STORY-01 and LINK-01 requirement definitions.
- `.planning/phases/28-story-and-content-control-plane-completion/28-HANDOFF.md` - Phase 28 decisions D-01, D-07, D-08, D-13, D-14, D-15, and D-16.
- `.planning/phases/28-story-and-content-control-plane-completion/28-VERIFICATION.md` - What Phase 28 already proved and what Phase 29 must not re-claim.

### Admin Backend Foundation
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminExperienceOrchestrationController.java` - Generic Phase 28 experience admin endpoints.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminExperienceOrchestrationServiceImpl.java` - Canonical vocabularies, versioned JSON validation, DTO mapping, governance counts.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminExperienceRequest.java` - Current admin request contract for flows, steps, bindings, overrides, exploration elements.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminExperienceResponse.java` - Current admin response contract and operator guidance vocabulary.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminPoiController.java` - Existing POI admin CRUD endpoint and POI detail access.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminPoiServiceImpl.java` - Existing POI read/write patterns.

### Admin UI Foundation
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Experience/ExperienceOrchestrationWorkbench.tsx` - Generic Phase 28 workbench and current presets.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/POIManagement/index.tsx` - Existing POI management list/editing entry point.
- `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts` - Admin API transport layer.
- `packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts` - Admin frontend DTO types.
- `packages/admin/aoxiaoyou-admin-ui/src/App.tsx` - Route registry.
- `packages/admin/aoxiaoyou-admin-ui/src/layouts/DefaultLayout.tsx` - Protected admin sidebar navigation.
- `packages/admin/aoxiaoyou-admin-ui/src/utils/formErrorFeedback.ts` - Existing invalid form scroll/focus/shake helper.

### Public Runtime Foundation
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java` - Public POI experience runtime endpoint.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java` - Compiled public flow runtime and exploration event handling.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/ExperienceRuntimeResponse.java` - Public runtime DTO.

### Seeds And Smoke
- `scripts/local/mysql/init/39-phase-28-experience-orchestration.sql` - Current A-Ma Temple default flow seed and experience schema.
- `scripts/local/smoke-phase-28-experience.ps1` - Existing admin/public runtime smoke pattern using explicit UTF-8 HTTP handling.

</canonical_refs>

<specifics>

## Concrete Acceptance Shape

The Phase 29 executor should be able to configure the following canonical POI step codes for A-Ma Temple:

- `tap_intro`
- `start_route_guidance`
- `arrival_intro_media`
- `release_checkin_tasks`
- `pickup_side_clues`
- `hidden_dwell_achievement`
- `completion_reward_title`

The UI should present structured controls for these step categories:

- `intro_modal`
- `route_guidance`
- `proximity_media`
- `checkin_task`
- `pickup`
- `hidden_challenge`
- `reward_grant`

The admin facade, if added, should preserve these path intentions:

- `GET /api/admin/v1/pois/{poiId}/experience/default`
- `PUT /api/admin/v1/pois/{poiId}/experience/default-flow`
- `POST /api/admin/v1/pois/{poiId}/experience/steps`
- `PUT /api/admin/v1/pois/{poiId}/experience/steps/{stepId}`
- `DELETE /api/admin/v1/pois/{poiId}/experience/steps/{stepId}`
- `POST /api/admin/v1/pois/{poiId}/experience/steps/{stepId}/save-template`

</specifics>

<deferred>

## Deferred Ideas

- Chapter-level inherit/disable/replace/append authoring belongs to Phase 30.
- Full conflict governance across indoor/reward/story/POI belongs to Phase 31.
- Dynamic denominator management UI belongs to Phase 32, though Phase 29 should still save semantic weights.
- Full five-chapter story content package and generated media belongs to Phase 33.
- Mini-program runtime UX validation belongs to Phase 34 or later.

</deferred>

---

*Phase: 29-poi-default-experience-workbench*
*Context gathered: 2026-04-29*
