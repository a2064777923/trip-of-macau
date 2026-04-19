# Phase 11: Story, Activity, and Collection Composition - Context

**Gathered:** 2026-04-14
**Status:** Ready for planning
**Source:** Milestone v2.0 requirements, Phase 9-10 outputs, current story/activity/collection code inspection, and the user's v2.0 control-plane directives

<domain>
## Phase Boundary

Phase 11 rebuilds the content-composition layer that sits on top of the new spatial model and shared media pipeline.

This phase owns:
- storyline authoring that can bind one storyline to multiple cities and sub-maps instead of a single `cityId`
- dedicated chapter composition flows instead of the current storyline-page drawer
- chapter anchor, prerequisite, completion, and reward metadata that can drive authored story progression
- task/activity authoring with localized content, schedule windows, organizer and signup metadata, HTML-rich content, and pinning
- collectible, badge, and reward authoring with richer bindings to storyline and spatial entities plus canonical media assets
- admin, admin-backend, public-backend, and mini-program contract alignment for story/activity/reward/collection domains covered by this phase

This phase does not own:
- the indoor marker or overlay authoring system itself, which remains Phase 12 scope
- the full indoor trigger/effect rules engine, which remains v2.1 scope
- user-progress recomputation and deep traveler progression dashboards, which remain Phase 13 scope
- the AI capability platform, which remains v2.1 scope
</domain>

<decisions>
## Locked Decisions

- Storyline-to-space binding must move off the current single `cityId` model. Phase 11 should introduce canonical multi-binding records that can represent `city` and `sub_map` targets for a storyline.
- Storyline metadata editing and chapter composition are separate concerns. The current `StorylineManagement` page keeps storyline overview CRUD, while chapter authoring moves to a dedicated Traditional Chinese composition page and route.
- Chapters must support canonical anchor metadata. Phase 11 should support `poi`, `activity`, `marker`, and `overlay` as anchor types at the data-contract level.
- Because Phase 12 has not yet rebuilt indoor marker and overlay authoring, Phase 11 may provide full picker support for `poi` and `activity`, and forward-compatible manual code/ID entry for `marker` and `overlay`.
- Chapter gating and rewards should be authored through structured metadata fields exposed in the admin, with JSON remaining as a fallback representation rather than the primary operator UX.
- Storyline, chapter, activity, collectible, badge, and reward media must reuse the Phase 10 shared media primitives and `content_asset_links` conventions. Phase 11 must not introduce a second ad hoc upload system.
- Phase 11 should favor reusable relation records over hardcoding new one-off foreign keys for every storyline/map/sub-map/reward linkage. The relation model must be usable again by Phase 13 progress computation.
- Activity and task authoring should share one canonical activity domain with an explicit `activityType` discriminator rather than separate tables for each minor variant.
- The current operations page is not authoritative enough. Phase 11 should turn it into a real authoring surface, and the public discover/runtime flow should stop relying on placeholder activity cards derived from unrelated content.
- Rewards should no longer be treated as primarily a system-settings concern. Phase 11 should move reward authoring into the collection/composition information architecture while preserving backward-compatible backend access if needed during migration.
- Public backend and mini-program alignment is mandatory for phase-owned fields. Phase 11 should extend live public contracts and client mappers rather than leaving the mini-program on mock-only story/activity/collection assumptions.
- Phase 11 should not introduce a full arbitrary content-block DSL. For v2.0, localized intro/summary/detail/HTML fields plus ordered shared media attachments are the bounded composition model.
</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Milestone and upstream planning decisions
- `.planning/ROADMAP.md` - Phase 11 goal, success criteria, and dependency chain
- `.planning/REQUIREMENTS.md` - `STORY-01`, `STORY-02`, `STORY-03`, `ACT-01`, and `COLL-01`
- `.planning/STATE.md` - current milestone state and recent decisions
- `.planning/phases/09-spatial-model-rebuild/09-CONTEXT.md` - spatial hierarchy and coordinate-model decisions that storyline/activity/collection bindings must respect
- `.planning/phases/09-spatial-model-rebuild/09-01-SUMMARY.md` - verified city, sub-map, and POI outcomes already available to Phase 11
- `.planning/phases/10-media-asset-pipeline-and-library/10-CONTEXT.md` - shared media-library and upload-policy decisions
- `.planning/phases/10-media-asset-pipeline-and-library/10-03-SUMMARY.md` - verified shared media picker/library rollout and asset-usage tracing

### Current admin UI surfaces to replace or extend
- `packages/admin/aoxiaoyou-admin-ui/src/App.tsx` - current route ownership and phase-owned navigation entries
- `packages/admin/aoxiaoyou-admin-ui/src/pages/StorylineManagement/index.tsx` - current storyline CRUD plus inline chapter drawer that must be split
- `packages/admin/aoxiaoyou-admin-ui/src/pages/OperationsManagement/index.tsx` - current read-heavy activity page baseline
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/CollectibleManagement.tsx` - current collectible create/list baseline
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/BadgeManagement.tsx` - current badge create/list baseline
- `packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetPickerField.tsx` - canonical Phase 10 shared media picker
- `packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetDetailDrawer.tsx` - canonical shared media detail/usage drawer
- `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts` and `src/types/admin.ts` - current admin DTO and route contracts

### Current admin backend surfaces to replace or extend
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminStoryLineController.java` - current storyline CRUD endpoints
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminStoryChapterController.java` - current nested chapter CRUD endpoints
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminOperationsController.java` - current activities read endpoint only
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminCollectibleController.java` - current collectible and badge create/list endpoints
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminSystemManagementController.java` - current reward ownership and system-level reward endpoints
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminStoryLineServiceImpl.java` - current single-city storyline implementation
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminStoryChapterServiceImpl.java` - current shallow chapter model
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminOperationsServiceImpl.java` - current activity list mapper
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminCollectibleServiceImpl.java` - current collectible/badge persistence baseline
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminSystemManagementServiceImpl.java` - current reward persistence baseline

### Current public backend and mini-program consumers to align
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/StoryLineController.java` - public storyline read endpoints
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/RewardController.java` - public reward read endpoint
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java` - public story, reward, POI, and discover-card shaping
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/CatalogFoundationServiceImpl.java` - public published-content lookups
- `packages/client/src/services/api.ts` - mini-program public DTO contracts
- `packages/client/src/services/gameService.ts` - current storyline/discover/reward consumption and remaining mock-shaped assumptions
</canonical_refs>

<specifics>
## Specific Ideas

- The user’s sample Macau wartime storyline should be expressible by Phase 11 using: multi-map storyline binding, ordered POI-anchored chapters, chapter-level audio/video/image attachments, reward/collectible links, and chapter gating metadata.
- Activities should support both “discovery content shown in the discover page” and “global task or event” semantics through `activityType`, publish windows, and organizer/signup fields rather than by mixing content into unrelated modules.
- Collectibles, badges, and rewards should gain storyline and spatial relation metadata now so Phase 13 can compute progress from the same graph instead of reverse-engineering free-text fields later.
</specifics>

<deferred>
## Deferred Ideas

- Full indoor marker and overlay authoring UI, minimap picking, CSV ingest, and tile-linked authoring remain Phase 12 scope.
- Full appearance-condition, trigger-condition, effect-chain authoring remains v2.1 rules-engine scope.
- Deep traveler progress recomputation, audit-heavy user progress dashboards, and operations/testing cleanup remain Phase 13 scope.
- AI capability provider orchestration and quota/governance remain v2.1 scope.
</deferred>

---

*Phase: 11-story-activity-and-collection-composition*
*Context gathered: 2026-04-14*
