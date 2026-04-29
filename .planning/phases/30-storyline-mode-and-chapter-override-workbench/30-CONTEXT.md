---
phase: 30-storyline-mode-and-chapter-override-workbench
status: ready-for-planning
created: 2026-04-29
source:
  - .planning/ROADMAP.md
  - .planning/REQUIREMENTS.md
  - .planning/phases/28-story-and-content-control-plane-completion/28-CONTEXT.md
  - .planning/phases/28-story-and-content-control-plane-completion/28-HANDOFF.md
  - .planning/phases/29-poi-default-experience-workbench/29-HANDOFF.md
  - .planning/phases/29-poi-default-experience-workbench/29-VERIFICATION.md
---

# Phase 30: Storyline Mode and Chapter Override Workbench - Context

<domain>

## Phase Boundary

Phase 30 builds the story-mode authoring layer on top of the Phase 28 shared experience orchestration model and the Phase 29 POI default experience workbench. It must let operators compose a storyline as an independent route mode and configure chapter-level inheritance/overrides against POI, indoor, task, marker, overlay, or manual anchors.

This phase owns:

- Storyline overview and story-mode configuration: cover, world view, recommendation entry copy, map/sub-map bindings, and story-mode map strategy.
- Route arrangement: chapter order, chapter anchors, route preview metadata, branch insertion policy, and current/inactive route display rules.
- Chapter override authoring: inherit a target default flow, then per step choose `inherit`, `disable`, `replace`, or `append`.
- Chapter-specific additions: story media, Lottie/fullscreen media, overlays, pickups, hidden challenges, reward/title grants, completion effects, and content-block links.
- Admin/public contract alignment: admin-authored chapter overrides compile into the public storyline runtime DTO shape already started in Phase 28.
- Smoke verification proving an authored chapter can inherit the A-Ma Temple POI default flow and replace the default arrival media with story-specific immersive media.

This phase does not own:

- Building the global interaction/task template library and conflict governance center. That is Phase 31.
- Building user progress recomputation and traveler timeline views. That is Phase 32.
- Producing the full five-chapter flagship material package and AI-generated assets. That is Phase 33.
- Full mini-program WeChat experiential acceptance. That is Phase 34 or later.

</domain>

<decisions>

## Locked Decisions

### D30-01 Storyline Mode Is Independent Of Default POI Walk-In
- A storyline is not just a list of story pages. It is an explicit mode with its own introduction, route sequence, map display strategy, temporary session rules, and chapter current-state.
- When story mode is active, admin can configure unrelated map content to be hidden, nearby content to be conditionally revealed, the current chapter route to be highlighted, inactive route segments to be greyed or muted, and temporary story-session progress to clear on exit.
- Permanent exploration events, check-ins, pickups, rewards, titles, and collectible acquisitions must not be cleared by story-mode exit.

### D30-02 Chapter Anchors Inherit Target Experience Flows
- A chapter anchor may be `poi`, `indoor_building`, `indoor_floor`, `indoor_node`, `task`, `overlay`, or `manual`.
- If the anchor has a default experience flow, the chapter should default to inheriting that flow.
- For Phase 30 acceptance, the A-Ma Temple chapter must inherit the Phase 29 `poi_ama_default_walk_in` flow.
- Missing anchor flows must be visible as validation warnings, not silent no-ops.

### D30-03 Override Semantics Are Per-Step And Explicit
- Chapter overrides use only these operator-facing modes: `inherit`, `disable`, `replace`, and `append`.
- Operators must be able to disable a default POI effect, replace it with story-specific media/effects, or append new story-only steps without hand-writing raw JSON.
- Override payloads remain versioned JSON under the hood, with `schemaVersion: 1` required before saving.
- Advanced JSON can exist as an explicit collapsed expert fallback, but it is not the primary editing surface.

### D30-04 Chapter Workbench Uses Structured Cards
- The chapter workbench must speak in story authoring concepts: anchor, inherited flow, override action, story media, route segment, pickup, hidden challenge, reward/title grant, completion effect, and validation result.
- It should preserve the three-panel workbench pattern from Phase 28/29: left storyline/chapter tree, middle route and flow composition area, right property panel, bottom validation/conflict output.
- All admin copy must be Traditional Chinese.
- Invalid save must scroll to the first invalid card/control, focus it, and show a visible shake/error affordance.

### D30-05 Storyline Route Arrangement Is Operator-Controlled
- Operators can define chapter order and chapter-to-anchor route sequence.
- Branch recommendation is configuration-first in this phase, not algorithm-first. Supported source options should include nearby POIs, same-theme POIs, same sub-map POIs, and manually selected POIs.
- Operators can set whether branch inserts appear before a chapter, after a chapter, or between two chapters, whether they are skippable, and whether they affect storyline progress.
- This phase can store route preview metadata rather than implement full routing algorithms.

### D30-06 Story Additions Reuse Existing Domain Models
- Story-specific media should use `content_assets`, Lottie-aware media previews, and story content blocks already delivered by Phase 28.
- Story-specific reward/title grants should link to the existing reward, badge, honor/title, and game reward domain, not create a parallel reward model.
- Story-specific pickups, hidden challenges, and overlays should be represented as flow steps/effects and relation links where possible, leaving richer task/game templates to Phase 31.
- `content_relation_links` remains the canonical cross-domain relation layer for story anchors, override targets, reward rule bindings, and exploration element bindings.

### D30-07 Public Runtime Alignment Is Mandatory
- Phase 30 must prove that admin-authored story overview, route strategy, chapter anchor, inherited flow, and override policy can be read through public storyline runtime DTOs.
- Public runtime must expose compiled flow data, not raw admin-only editor state or admin-only `status` fields.
- Existing public POI runtime must keep passing after storyline runtime changes.

### D30-08 Flagship Story Scenario Drives Acceptance But Full Content Is Later
- The acceptance scenario is the first chapter of "東西方文明的戰火與共生":
  - Storyline intro sets the user as a "濠江歷史見證者".
  - Chapter 1 "鏡海初戰：中葡首次海防對峙" anchors to 媽閣廟.
  - The chapter inherits the A-Ma Temple default flow.
  - The default arrival 50m animation is disabled or replaced with a story-specific immersive historical media step.
  - Story-only mainline overlays, pickups, hidden challenge, and title/reward grants can be added as structured cards.
- Phase 30 seeds just enough data for this acceptance slice. The complete five-chapter material package is Phase 33.

### D30-09 Out Of Scope
- Do not build a new global template library in Phase 30; reuse existing templates and save only story-specific step/template references needed for chapter authoring.
- Do not solve all conflict detection in Phase 30; provide local validation and leave cross-domain conflict governance to Phase 31.
- Do not hardcode exploration percentage increments. Use semantic weights and exploration elements where a story step contributes to exploration.
- Do not write Chinese content through inline PowerShell. SQL/JSON/CSV/content files must be UTF-8/utf8mb4 safe.

</decisions>

<canonical_refs>

## Canonical References

### Planning And Prior Phase Evidence
- `.planning/ROADMAP.md` - Phase 30 goal, success criteria, dependencies, and requirements `STORY-02`, `STORY-04`, `LINK-02`.
- `.planning/REQUIREMENTS.md` - v3.0 requirements and out-of-scope constraints.
- `.planning/phases/28-story-and-content-control-plane-completion/28-CONTEXT.md` - user-approved v3.0 story-experience redesign and shared foundation decisions.
- `.planning/phases/28-story-and-content-control-plane-completion/28-HANDOFF.md` - foundation handoff, delivered tables, DTOs, and non-claims.
- `.planning/phases/28-story-and-content-control-plane-completion/28-VERIFICATION.md` - foundation verification evidence.
- `.planning/phases/29-poi-default-experience-workbench/29-HANDOFF.md` - POI default flow contract and A-Ma Temple step vocabulary.
- `.planning/phases/29-poi-default-experience-workbench/29-VERIFICATION.md` - POI workbench verification and smoke evidence.
- `AGENTS.md` - project constraints, UTF-8 rule, dirty worktree caution, and verification expectations.

### Admin Backend Foundation
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminExperienceOrchestrationController.java` - generic admin experience APIs.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminExperienceOrchestrationServiceImpl.java` - canonical experience vocabulary, versioned JSON validation, DTO assembly, and governance counts.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminPoiExperienceController.java` - Phase 29 POI-specific facade and default flow patterns.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminPoiExperienceServiceImpl.java` - default POI flow loading/saving and template-save patterns.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminStoryLineController.java` - existing storyline admin entrypoint.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminStoryChapterController.java` - existing chapter admin entrypoint.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminStoryChapterUpsertRequest.java` - current chapter request fields, including `experienceFlowId`, `overridePolicyJson`, and `storyModeConfigJson`.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminStoryChapterResponse.java` - current chapter response fields and content block links.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminExperienceRequest.java` - shared flow, step, binding, override, and exploration request contracts.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminExperienceResponse.java` - shared response contracts to reuse for inherited flow and overrides.

### Admin UI Foundation
- `packages/admin/aoxiaoyou-admin-ui/src/pages/StorylineManagement/index.tsx` - existing storyline list/detail surface.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/StoryChapterManagement/index.tsx` - existing chapter management list surface.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryChapterWorkbench.tsx` - current chapter workbench entry to extend or replace.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Experience/ExperienceOrchestrationWorkbench.tsx` - shared three-panel orchestration pattern and card vocabulary.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/POIExperienceWorkbench/index.tsx` - Phase 29 dedicated POI workbench pattern.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryContentBlockManagement.tsx` - content block selection and reuse target.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/MediaLibraryManagement.tsx` - global media and Lottie-aware asset source.
- `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts` - admin frontend API helpers to extend.
- `packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts` - admin frontend DTOs to extend.
- `packages/admin/aoxiaoyou-admin-ui/src/App.tsx` - route registry.
- `packages/admin/aoxiaoyou-admin-ui/src/layouts/DefaultLayout.tsx` - sidebar and selected menu state.
- `packages/admin/aoxiaoyou-admin-ui/src/utils/formErrorFeedback.ts` - invalid form scroll/focus/shake helper.

### Public Runtime Foundation
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java` - public experience endpoints, including POI and storyline runtime baseline.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java` - public compiled runtime flow behavior.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/ExperienceRuntimeResponse.java` - public compiled runtime DTO.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/StoryLineController.java` - public storyline API entrypoint.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/StoryLineServiceImpl.java` - public storyline list/detail behavior.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StoryLineResponse.java` - public storyline DTO to extend if needed.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StoryChapterResponse.java` - public chapter DTO to extend if needed.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StorylineSessionResponse.java` - story session response shape.

### Database, Seeds, And Smoke
- `scripts/local/mysql/init/38-phase-28-story-content-and-lottie.sql` - content block and Lottie/media foundation.
- `scripts/local/mysql/init/39-phase-28-experience-orchestration.sql` - experience orchestration, override, and exploration foundation.
- `scripts/local/mysql/init/40-phase-29-poi-default-experience.sql` - A-Ma Temple POI default flow seed and canonical step codes.
- `scripts/local/smoke-phase-28-experience.ps1` - admin/public experience smoke pattern.
- `scripts/local/smoke-phase-29-poi-experience.ps1` - POI default experience smoke pattern and auth handling.

</canonical_refs>

<specifics>

## Concrete Phase 30 Acceptance Shape

The plan should make these values concrete enough for execution:

- Canonical storyline runtime endpoint: `GET /api/v1/storylines/{id}/runtime`.
- Admin storyline mode path candidates:
  - `GET /api/admin/v1/storylines/{storylineId}/mode-workbench`
  - `PUT /api/admin/v1/storylines/{storylineId}/mode-config`
  - `POST /api/admin/v1/storylines/{storylineId}/chapters`
  - `PUT /api/admin/v1/storylines/{storylineId}/chapters/{chapterId}/anchor`
  - `PUT /api/admin/v1/storylines/{storylineId}/chapters/{chapterId}/overrides`
  - `POST /api/admin/v1/storylines/{storylineId}/chapters/{chapterId}/override-steps`
  - `DELETE /api/admin/v1/storylines/{storylineId}/chapters/{chapterId}/override-steps/{overrideId}`
  - `GET /api/admin/v1/storylines/{storylineId}/runtime-preview`
- Required override modes: `inherit`, `disable`, `replace`, `append`.
- Required anchor types: `poi`, `indoor_building`, `indoor_floor`, `indoor_node`, `task`, `overlay`, `manual`.
- Required story-mode map strategy fields:
  - `hideUnrelatedContent`
  - `nearbyRevealEnabled`
  - `nearbyRevealRadiusMeters`
  - `currentRouteHighlight`
  - `inactiveRouteStyle`
  - `clearTemporaryProgressOnExit`
  - `preservePermanentEvents`
- Required branch strategy fields:
  - `sourceType`: `nearby_poi`, `same_theme_poi`, `same_sub_map_poi`, `manual`
  - `insertPosition`: `before_chapter`, `after_chapter`, `between_chapters`
  - `skippable`
  - `affectsStoryProgress`
- Required acceptance story data:
  - Storyline title: `東西方文明的戰火與共生`
  - Chapter title: `鏡海初戰：中葡首次海防對峙`
  - Anchor POI: 媽閣廟, using the same POI id as Phase 29 smoke when available.
  - Inherited default step codes include `tap_intro`, `start_route_guidance`, `arrival_intro_media`, `release_checkin_tasks`, `pickup_side_clues`, `hidden_dwell_achievement`, `completion_reward_title`.
  - Chapter override replaces or disables `arrival_intro_media`.
  - Chapter appends story-only steps for mainline overlays, pickups, hidden challenge, and story reward/title grants.

</specifics>

<deferred>

## Deferred Ideas

- Complete five-chapter flagship content, actual generated image/audio/Lottie assets, manifest, and COS upload belong to Phase 33.
- Cross-domain conflict detection across all templates, indoor nodes, story chapters, rewards, and POI flows belongs to Phase 31.
- Dynamic traveler progress, recomputation, and user timeline views belong to Phase 32.
- Full mini-program story-mode route UI, AR/photo recognition, voice input, puzzle, cannon defense, and WeChat DevTools experiential acceptance remain future runtime scope.

</deferred>

---

*Phase: 30-storyline-mode-and-chapter-override-workbench*
*Context gathered: 2026-04-29*
