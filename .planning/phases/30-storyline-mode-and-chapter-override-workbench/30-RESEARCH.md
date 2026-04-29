---
phase: 30-storyline-mode-and-chapter-override-workbench
status: complete
researched: 2026-04-29
mode: orchestrator-fallback-after-subagent-timeout
requirements:
  - STORY-02
  - STORY-04
  - LINK-02
---

# Phase 30 Research: Storyline Mode and Chapter Override Workbench

## Research Complete

Phase 30 should build on existing Phase 28/29 code rather than introduce a parallel story runtime. The useful foundation already exists:

- `experience_flows`, `experience_flow_steps`, `experience_bindings`, and `experience_overrides` exist in both admin and public backend source trees.
- Public `GET /api/v1/storylines/{storylineId}/runtime` already exists in `ExperienceController`.
- `PublicExperienceServiceImpl` already compiles story chapter runtime by loading an inherited default flow from the chapter anchor, applying published `experience_overrides`, and appending chapter flow steps.
- `StoryChapter` already has `experienceFlowId`, `overridePolicyJson`, `storyModeConfigJson`, `anchorType`, `anchorTargetId`, and `anchorTargetCode`.
- Phase 29 added a POI-specific facade and workbench that can be copied in pattern for a story-specific facade/workbench.

## Key Implementation Seams

### Admin Backend

Use existing classes:

- `AdminStoryLineController` currently supports basic CRUD at `/api/admin/v1/storylines`.
- `AdminStoryChapterController` currently supports chapter CRUD at `/api/admin/v1/storylines/{storylineId}/chapters`.
- `AdminStoryLineServiceImpl` already handles city/sub-map/attachment relation links for storylines.
- `AdminStoryChapterServiceImpl` already validates JSON, content blocks, `experienceFlowId`, and some anchor types.
- `AdminExperienceOrchestrationServiceImpl` already contains shared vocabulary, versioned JSON validation, and experience DTO assembly.
- `AdminPoiExperienceController` and `AdminPoiExperienceServiceImpl` are the closest implementation pattern for a dedicated workbench facade.

Recommended additions:

- Add `AdminStorylineModeController` under `/api/admin/v1/storylines/{storylineId}/mode-workbench`.
- Add `AdminStorylineModeService` and implementation to assemble one snapshot containing story detail, chapters, anchors, inherited flows, chapter flow, overrides, route strategy, validation findings, and public runtime path.
- Reuse `ExperienceFlowMapper`, `ExperienceFlowStepMapper`, `ExperienceBindingMapper`, and `ExperienceOverrideMapper` rather than creating story-specific tables.
- Extend `AdminStoryChapterServiceImpl.validateAnchorTarget` to support `indoor_building`, `indoor_floor`, and `indoor_node` in addition to existing `poi`, `task`, `overlay`, and `manual`.
- Persist story-mode config through `StoryChapter.storyModeConfigJson` for phase scope, with a consistent storyline-wide config copied or exposed through the first/current primary chapter. If a new `story_lines.story_mode_config_json` column already exists or is easy to add safely, prefer storyline-level storage; otherwise avoid schema churn and use chapter-level config with explicit facade semantics.

### Public Backend

Use existing runtime:

- `ExperienceController.getStorylineRuntime` already exposes `GET /api/v1/storylines/{storylineId}/runtime`.
- `PublicExperienceServiceImpl.buildChapterRuntime` already:
  - resolves inherited flow using `findDefaultFlow(chapter.getAnchorType(), chapter.getAnchorTargetId(), chapter.getAnchorTargetCode())`;
  - loads `chapterFlow` when `chapter.getExperienceFlowId()` is set;
  - loads published `ExperienceOverride` rows with `ownerType=story_chapter` and `ownerId=chapter.id`;
  - compiles `disable`, `replace`, and `append` overrides.

Risks to address:

- Public `toStoryModeConfig` currently expects keys `nearbyRevealMeters`, `currentRouteStyle`, `exitResetsSessionProgress`; Phase 30 product language uses `nearbyRevealEnabled`, `nearbyRevealRadiusMeters`, `currentRouteHighlight`, `clearTemporaryProgressOnExit`, and `preservePermanentEvents`. The plan must either normalize admin keys to current public keys or extend public DTO parsing to support the Phase 30 canonical names.
- Public runtime only reads published overrides and published flows/steps. Seed/smoke must set status to `published`.
- `findDefaultFlow` depends on anchor type matching `experience_bindings.owner_type`. Admin chapter anchor types must use the same tokens as bindings.

### Admin UI

Use existing surfaces:

- `StorylineManagement/index.tsx` is the story list/detail surface.
- `StoryChapterManagement/index.tsx` is the chapter list surface.
- `Content/StoryChapterWorkbench.tsx` exists and should be extended or replaced with the Phase 30 three-panel workbench.
- `POIExperienceWorkbench/index.tsx` provides the latest dedicated workbench pattern: selector/timeline, structured cards, validation, public runtime preview, and template interactions.
- `Experience/ExperienceOrchestrationWorkbench.tsx` provides shared card vocabulary and generic experience management behavior.
- `services/api.ts` and `types/admin.ts` already contain many story, chapter, experience, and POI DTO types.
- `formErrorFeedback.ts` should be reused for invalid-save scroll/focus/shake.

Recommended UI layout:

- Left panel: storyline selector, chapter tree, chapter order controls, anchor summary, publish status.
- Middle panel: route arrangement and inherited flow composition. Show inherited step list with override badges: `繼承`, `停用`, `替換`, `追加`.
- Right panel: property editor for selected chapter/step. Use structured cards for anchor, story-mode strategy, branch strategy, media/effect, pickup, hidden challenge, reward/title grant.
- Bottom panel: validation findings, public runtime path, and compiled preview summary.

### Database And Seed

Existing scripts:

- `38-phase-28-story-content-and-lottie.sql` created story content/media/Lottie foundation.
- `39-phase-28-experience-orchestration.sql` created shared experience orchestration and public runtime foundation.
- `40-phase-29-poi-default-experience.sql` seeded A-Ma Temple default POI experience with canonical step codes.

Phase 30 should add a new UTF-8/utf8mb4 script, likely `41-phase-30-storyline-mode-overrides.sql`, that:

- Ensures a story line exists for `東西方文明的戰火與共生`.
- Ensures chapter 1 exists as `鏡海初戰：中葡首次海防對峙`.
- Anchors chapter 1 to the A-Ma Temple POI id used by Phase 29 smoke when present.
- Sets `override_policy_json` with `schemaVersion: 1`.
- Sets `story_mode_config_json` with `schemaVersion: 1`, `hideUnrelatedContent`, `nearbyRevealEnabled`, `nearbyRevealRadiusMeters`, `currentRouteHighlight`, `inactiveRouteStyle`, `clearTemporaryProgressOnExit`, and `preservePermanentEvents`.
- Creates a story-specific chapter flow and replacement/append steps for:
  - replacing or disabling `arrival_intro_media`;
  - appending mainline overlays;
  - appending pickups;
  - appending hidden challenge;
  - appending story reward/title grant.
- Creates published `experience_overrides` with `owner_type='story_chapter'`, `target_step_code='arrival_intro_media'`, and `override_mode='replace'` or `disable`, plus append overrides for story-only steps.

## Validation Architecture

Phase 30 needs both source-level and live-stack validation:

- Admin backend compile: `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml`.
- Public backend compile: `mvn -q -DskipTests compile -f packages/server/pom.xml`.
- Admin UI build: `npm run build --prefix packages/admin/aoxiaoyou-admin-ui`.
- Smoke script: `scripts/local/smoke-phase-30-storyline-mode.ps1`.

Smoke should verify:

- Admin auth works through existing local token handling.
- `GET /api/admin/v1/storylines/{storylineId}/mode-workbench` returns story, chapters, inherited flow, overrides, validation findings, and public runtime path.
- Chapter 1 anchor type is `poi` and anchor label resolves to 媽閣廟.
- Inherited flow contains Phase 29 step codes including `arrival_intro_media`.
- Override list contains `targetStepCode=arrival_intro_media` and `overrideMode=replace` or `disable`.
- Public `GET /api/v1/storylines/{storylineId}/runtime?locale=zh-Hant` returns compiled chapter runtime.
- Public compiled steps do not contain admin-only `status`.
- Public compiled steps reflect the replacement/disable behavior for `arrival_intro_media` and include story-only append steps.
- Public POI runtime smoke from Phase 29 still passes after Phase 30.

## Planning Recommendations

Split implementation into four plans:

1. Backend facade and seed: add admin story mode snapshot/save endpoints, anchor validation support, UTF-8 seed, and smoke scaffold.
2. Runtime contract alignment: extend public DTO/config normalization and ensure compiled runtime exposes Phase 30 canonical story-mode fields without admin-only leakage.
3. Admin UI workbench: add/extend the story route/override workbench with Traditional Chinese three-panel UI, structured cards, validation, runtime preview, and route/sidebar entry.
4. Verification and integration hardening: source checks, live smoke, Phase 29 regression smoke, documentation/handoff.

## Key Risks

- Dirty worktree is large; implementation must not revert unrelated phase work.
- Anchor vocabulary drift can break inherited default flow lookup. Use exact tokens and smoke against A-Ma Temple.
- Public runtime only reads published rows, so seeds and admin facade must use status correctly.
- Story-mode config key drift can make the UI look correct while public runtime drops fields. Normalize keys explicitly.
- Chinese seed content must be written through UTF-8 files only.
