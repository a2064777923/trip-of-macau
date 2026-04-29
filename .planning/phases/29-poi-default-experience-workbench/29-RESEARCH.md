---
phase: 29-poi-default-experience-workbench
status: complete
created: 2026-04-29
requirements:
  - STORY-01
  - LINK-01
---

# Phase 29 Research: POI Default Experience Workbench

## Question

What does the executor need to know to plan and implement the dedicated POI default experience workbench without duplicating the Phase 28 experience orchestration model?

## Current Foundation

### Admin Backend

Phase 28 added a generic admin experience surface under `AdminExperienceOrchestrationController.java`:

- `GET/POST/PUT/DELETE /api/admin/v1/experience/templates`
- `GET/POST/PUT/DELETE /api/admin/v1/experience/flows`
- `POST/PUT/DELETE /api/admin/v1/experience/flows/{flowId}/steps`
- `GET/POST/PUT/DELETE /api/admin/v1/experience/bindings`
- `GET/POST/PUT/DELETE /api/admin/v1/experience/overrides`
- `GET/POST/PUT/DELETE /api/admin/v1/experience/exploration-elements`
- `GET /api/admin/v1/experience/governance/overview`

`AdminExperienceOrchestrationServiceImpl.java` already owns the canonical vocabulary:

- Template types: `presentation`, `effect`, `trigger_effect`, `gameplay`, `display_condition`, `trigger_condition`, `task_gameplay`, `reward_presentation`.
- Flow types: `default_poi`, `default_indoor_building`, `default_indoor_floor`, `default_indoor_node`, `default_task`, `default_marker`, `default_overlay`, `default_activity`, `story_chapter_override`, `manual_target`.
- Flow modes: `walk_in`, `story_mode`, `manual`.
- Owner types: `poi`, `indoor_building`, `indoor_floor`, `indoor_node`, `story_chapter`, `task`, `marker`, `overlay`, `activity`, `manual_target`.
- Binding roles: `default_experience_flow`, `story_override_flow`.
- Trigger types: `manual`, `tap`, `tap_action`, `proximity`, `media_finished`, `dwell`, `story_mode_enter`, `tap_sequence`, `mixed`, `compound`, `content_complete`, `task_complete`, `pickup_complete`.
- Weight levels: `tiny`, `small`, `medium`, `large`, `core`.
- Statuses: `draft`, `published`, `archived`.

The service already rejects malformed versioned JSON for core config fields. Phase 29 should keep that behavior and compile structured operator cards into versioned JSON instead of relaxing validation.

### Admin UI

`ExperienceOrchestrationWorkbench.tsx` is a broad foundation console. It already has:

- Tabs for templates, flows, bindings, overrides, exploration, and governance.
- Existing A-Ma Temple starter presets.
- Generic drawers for editing templates, flows, steps, bindings, overrides, and exploration elements.
- Traditional Chinese copy in the Phase 28-owned surfaces.

The gap is operator fit: POI editors still need to understand generic flow IDs and JSON fields. `POIManagement/index.tsx` has no dedicated "地點體驗" entry, so a POI operator cannot stay in the POI context and author the natural walk-in/tap-to-explore flow.

### Public Runtime

`ExperienceController.java`, `PublicExperienceServiceImpl.java`, and `ExperienceRuntimeResponse.java` already expose public runtime data. The existing Phase 28 smoke script proves:

- Admin bindings for owner `poi` and POI ID 9 exist.
- Public `GET /api/v1/experience/poi/9?locale=zh-Hant` returns flow code `poi_ama_default_walk_in`.
- Public runtime includes compiled step codes such as `tap_intro` and `arrival_intro_media`.
- Public runtime hides admin-only status.

Phase 29 should extend this smoke pattern to prove a POI-authored flow still compiles as expected.

## Implementation Approach

### Recommended Backend Shape

Add a POI-specific admin facade rather than forcing the UI to orchestrate every generic call:

- `AdminPoiExperienceController`
- `AdminPoiExperienceService`
- `AdminPoiExperienceServiceImpl`
- `AdminPoiExperienceRequest`
- `AdminPoiExperienceResponse`

The facade should not introduce new storage tables. It should read/write:

- `pois`
- `experience_flows`
- `experience_flow_steps`
- `experience_bindings`
- `experience_templates`
- `exploration_elements`

The facade can provide:

- Snapshot loading for a POI's default flow, steps, binding, available templates, and basic validation findings.
- Upsert of the POI default flow with canonical `flowType=default_poi`, `mode=walk_in`, and binding role `default_experience_flow`.
- Structured step upsert that compiles card values into `triggerConfigJson`, `conditionConfigJson`, `effectConfigJson`, and `rewardRuleIdsJson`.
- Save-step-as-template behavior that creates `experience_templates` and links the step to the new template.

Why this is preferable:

- The UI can load one POI workbench snapshot instead of racing flows, bindings, templates, and POI detail calls.
- Binding correctness can be enforced server-side.
- The generic Phase 28 console remains available for advanced operators and governance.

### Recommended UI Shape

Add a dedicated page, for example:

- `packages/admin/aoxiaoyou-admin-ui/src/pages/POIExperienceWorkbench/index.tsx`

Add protected routes:

- `/space/poi-experience`
- `/space/pois/:poiId/experience`

Add entry points:

- A "地點體驗" button in the POI table row actions.
- Optional sidebar item under "地圖與空間管理" if the route is stable enough for direct access.

Workbench layout:

- Left panel: POI selector, default flow summary, ordered timeline of steps.
- Middle panel: visual composition with grouped cards for tap-to-explore, arrival, check-in, pickups, hidden achievement, and completion reward.
- Right panel: property editor for the selected step with structured condition/effect/media/reward cards.
- Bottom strip: validation, missing media, missing reward reference, unpublished step, and public runtime alignment hints.

Advanced JSON:

- Keep a collapsed "進階 JSON" section.
- Structured fields remain the default.
- Saving from structured fields always writes `schemaVersion: 1`.

### Step Card Design

Phase 29 should cover these operator-friendly step templates:

| Step type | Purpose | Key structured fields |
| --- | --- | --- |
| `intro_modal` | Click POI and show intro modal | title/body/action label/media asset |
| `route_guidance` | Tap "前往探索該地" and show route/recommendation cards | destination POI, route card types, recommended story/nearby strategy |
| `proximity_media` | Play full-screen media within a radius | radius meters, once-per-user, video/Lottie/audio slots |
| `checkin_task` | Release check-in tasks after media or arrival | task names, task kind, required flag, completion rule |
| `pickup` | Show and collect side clues | pickup name, rarity, location hint, prerequisite step/item |
| `hidden_challenge` | Trigger dwell/hidden achievement | radius meters, dwell seconds, condition summary, challenge reward |
| `reward_grant` | Grant reward/title/badge/collectible | reward rule IDs, title summary, media presentation |

### Seed And Smoke

Add Phase 29 seed and smoke rather than mutating Phase 28 evidence:

- `scripts/local/mysql/init/40-phase-29-poi-default-experience.sql`
- `scripts/local/smoke-phase-29-poi-experience.ps1`

Smoke should:

- Authenticate admin using the same local pattern as Phase 28.
- Load A-Ma Temple POI default snapshot.
- Verify the flow is `default_poi`, `walk_in`, and bound to `ownerType=poi`.
- Upsert or assert the seven canonical step codes.
- Save one step as a reusable template and verify it appears in `GET /api/admin/v1/experience/templates`.
- Call public `GET /api/v1/experience/poi/{poiId}?locale=zh-Hant`.
- Verify public step codes include the POI-authored steps and do not expose admin-only `status`.

PowerShell smoke scripts must use explicit UTF-8 request/response handling. Do not write Chinese JSON through inline PowerShell literals when a UTF-8 file or object serialization path is safer.

## Risks And Mitigations

| Risk | Why it matters | Mitigation |
| --- | --- | --- |
| Parallel POI model drift | Future story/indoor/governance phases would not see POI workbench data | Persist only through Phase 28 experience tables and canonical owner/binding vocabulary |
| Raw JSON-first regression | User explicitly rejected difficult JSON authoring | Make structured cards the primary editor and collapse advanced JSON |
| Broken binding | Public runtime would not find POI flow | Server-side facade must enforce `ownerType=poi`, `ownerId=poiId`, `bindingRole=default_experience_flow` |
| Draft leakage | Public API must not reveal admin draft steps | Smoke public runtime after publishing and assert no `status` field |
| Overgrown UI state | Generic workbench already large; adding POI mode there may worsen lockups | Prefer a dedicated page and small helper components rather than inflating the generic workbench |

## Validation Architecture

Phase 29 validation should sample three levels:

- Admin backend compile: `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml`.
- Admin UI build: `npm run build --prefix packages/admin/aoxiaoyou-admin-ui`.
- Live admin/public smoke: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-29-poi-experience.ps1`.

The smoke script is required because Phase 29 success depends on live contract alignment, not just static UI source checks.

## Planning Recommendation

Use three implementation plans:

1. Admin backend POI experience facade and frontend API/type contract.
2. Dedicated POI default experience workbench UI and POI management entry points.
3. A-Ma Temple seed, reusable template acceptance, public runtime smoke, and handoff evidence.

Do not pull Phase 30 storyline override authoring into Phase 29.
