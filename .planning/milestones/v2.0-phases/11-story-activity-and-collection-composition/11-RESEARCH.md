# Phase 11 Research: Story, Activity, and Collection Composition

## Objective

Research how to implement Phase 11 so the admin can author storylines, chapters, tasks/activities, and collection content through a coherent composition model while the public backend and mini-program consume the resulting managed data without falling back to mock logic.

## Current Starting Point

- `StorylineManagement` currently mixes storyline CRUD and chapter authoring in one page. Chapters are still edited in a drawer instead of a dedicated composition workflow.
- Storylines still persist a single `cityId` even though Phase 9 already introduced cities and sub-maps as a richer spatial hierarchy.
- Story chapters currently expose localized title/summary/detail fields, one `mediaAssetId`, and one `unlockParamJson`, but no canonical anchor type/target, prerequisite model, completion model, or reward model.
- `AdminOperationsController` only exposes `GET /api/admin/v1/operations/activities`, and `AdminOperationsServiceImpl` only maps a shallow list view from `Activity`.
- The current public discover/runtime path still synthesizes “activity” cards from unrelated content in `PublicCatalogServiceImpl` instead of from a real activity authoring model.
- Collectibles and badges only support create/list flows today, still persist raw URL-style media fields, and do not model storyline or spatial bindings.
- Rewards are still owned by `AdminSystemManagementController` instead of the collection/composition domain the user expects.
- Phase 10 already delivered the shared media picker/library, ordered `content_asset_links` usage, asset usage tracing, and delete guards that Phase 11 should reuse rather than re-implement.
- The public backend already exposes storyline and reward catalog endpoints, and the mini-program already consumes those DTOs in `packages/client/src/services/api.ts` and `gameService.ts`, but the contracts are still shaped around the older single-city and shallow chapter model.

## What Phase 11 Must Resolve

1. Replace the current single-city storyline model with a canonical multi-map and multi-sub-map binding model.
2. Separate storyline metadata editing from chapter composition so chapter authoring can become an owned product surface.
3. Add a bounded, reusable chapter-composition contract that supports anchor entities and structured gate/reward metadata without dragging the full indoor rules engine into v2.0.
4. Turn activities into a real authored content domain instead of a thin list derived from the old table.
5. Move collectibles, badges, and rewards toward a real content graph with canonical media assets and relation metadata.
6. Update public catalog contracts and mini-program consumers so phase-owned fields actually flow through live APIs.

## Recommended Architecture

### 1. Add a Reusable Relation Spine Instead of Hardcoding Every New Link

Recommended decision:
- introduce a generic relation table such as `content_relation_links`
- columns should include:
  - `owner_type`
  - `owner_id`
  - `relation_type`
  - `target_type`
  - `target_id`
  - `target_code`
  - `sort_order`
  - `metadata_json`
- use it for:
  - storyline -> city bindings
  - storyline -> sub-map bindings
  - chapter -> reward / collectible / badge links
  - collectible / badge / reward -> storyline bindings
  - collectible / badge / reward -> city / sub-map bindings

Why:
- Phase 11 needs many new cross-domain links
- Phase 13 will need the same graph for progress computation
- this avoids exploding the schema with one-off join tables for every entity pair

### 2. Keep Storyline Metadata CRUD and Chapter Composition as Two Separate Surfaces

Recommended decision:
- keep `StorylineManagement` focused on storyline metadata list/create/edit
- create a dedicated chapter-composition page and route, for example `StoryChapterManagement`
- storyline rows should navigate into that composer instead of opening an inline drawer

Why:
- `STORY-02` explicitly rejects the current reused storyline CRUD flow
- a dedicated composer page can show chapter order, anchor selection, and relation metadata without turning the storyline page into an unusable mega-form

### 3. Use a Bounded Chapter Model: Typed Metadata Plus JSON Fallback

Recommended decision:
- extend `story_chapter` with canonical fields such as:
  - `anchor_type`
  - `anchor_target_id`
  - `anchor_target_code`
  - `prerequisite_json`
  - `completion_json`
  - `reward_json`
- keep localized summary/detail-style fields for narrative copy
- reuse `content_asset_links` for ordered chapter attachments rather than introducing a separate media table
- offer structured preset editors in the admin for common prerequisite/completion/reward cases, with raw JSON only as an advanced fallback

Why:
- the user explicitly rejected JSON-only operator UX in adjacent v2.0 work
- the data still needs a flexible backend representation because chapter gates differ by story
- this supports the sample Macau storyline without requiring the full Phase 12 indoor rules engine

### 4. Support Forward-Compatible Anchor Types Without Pulling Phase 12 Forward

Recommended decision:
- chapter anchor type enum should include `poi`, `activity`, `marker`, and `overlay`
- admin picker support in Phase 11 should be full for `poi` and `activity`
- for `marker` and `overlay`, Phase 11 should expose manual ID/code entry plus validation hooks, then let Phase 12 replace those fields with real pickers

Why:
- `STORY-03` requires those anchor concepts
- Phase 12 still owns indoor authoring
- this keeps the contract stable while respecting roadmap boundaries

### 5. Reuse Phase 10 Media Primitives Instead of Building a Rich-Content DSL

Recommended decision:
- do not add a full arbitrary content-block engine in v2.0
- use localized text and HTML fields for authored narrative body
- use ordered `content_asset_links` plus `MediaAssetPickerField` for media attachments
- add small display metadata where needed, but keep composition bounded

Why:
- the user needs real multi-media composition, but not an unlimited WYSIWYG platform in this phase
- Phase 10 already solved the upload and asset-reuse problem
- a bounded model is faster to ship and easier to expose to the public backend

### 6. Treat Activities and Tasks as One Canonical Authoring Domain

Recommended decision:
- extend the existing `activity` table instead of creating multiple tables
- add a discriminator such as `activity_type` with values like:
  - `discovery_activity`
  - `official_event`
  - `private_event`
  - `global_task`
- add canonical fields for:
  - localized title / summary / description / HTML content
  - organizer name and contact
  - signup capacity, fee, and external signup data
  - publish window and signup window
  - `is_pinned`
  - canonical asset IDs plus ordered attachments

Why:
- the user groups global tasks and discover activities together conceptually
- one table keeps chapter anchors to activities straightforward
- the public runtime can filter by type without losing one canonical authoring source

### 7. Public Discover Cards Should Switch to Real Published Activities

Recommended decision:
- add a real public activity endpoint, for example `GET /api/v1/activities`
- update `PublicCatalogServiceImpl` so discover cards of type `activity` come from published activities, not tips or storyline fallbacks
- keep existing discover-card response shapes if needed, but populate them from real activity records

Why:
- the current discover activity path is not aligned to the admin’s intended control plane
- `ACT-01` is not complete if the admin can save activities but the mini-program still renders placeholder activity cards

### 8. Move Rewards into Collection Ownership While Preserving Compatibility

Recommended decision:
- create canonical collection-owned reward routes, for example under `/api/admin/v1/collectibles/rewards`
- update the admin UI so rewards live with collectibles and badges
- optionally keep `/api/admin/v1/system/rewards` as a thin compatibility facade during the migration

Why:
- the user explicitly called out the current IA as wrong
- rewards belong to the same authored content graph as collectibles and badges
- moving ownership now avoids Phase 13 having to reverse a second temporary structure

### 9. Replace URL-Only Collection Media Fields With Canonical Asset IDs

Recommended decision:
- migrate collectible and badge media handling from raw URLs to canonical asset IDs
- recommended fields:
  - `cover_asset_id`
  - `icon_asset_id`
  - `animation_asset_id`
- keep URL compatibility in responses only as derived fields if needed

Why:
- Phase 10 made asset IDs and shared library usage the canonical media model
- leaving raw URLs in collections would create a second-class workflow and weaken asset reuse and delete safety

### 10. Public Collection Catalog Support Should Start in Phase 11

Recommended decision:
- add public read endpoints for collectibles and badges if they are absent today
- extend public reward responses with relation metadata and canonical asset URLs
- keep the client adoption bounded to current reward/profile/discover surfaces, but make the live catalog contract real

Why:
- the admin cannot be authoritative if collection metadata never leaves the admin backend
- Phase 13 user-progress pages will need the same canonical collection catalog

## Recommended Phase Output Shape

Phase 11 should deliver:
- a reusable relation model for storyline, chapter, activity, and collection bindings
- dedicated storyline and chapter authoring surfaces
- a real authored activity/task module
- collection, badge, and reward authoring aligned to the shared media library and content graph
- public backend and mini-program DTO alignment for the phase-owned content graph

Phase 11 should not deliver:
- indoor map marker and overlay authoring UI
- the full trigger/effect rules engine
- user-progress recomputation dashboards
- AI provider governance features

## Key Risks and Mitigations

### Story/Collection Links Become Another Ad Hoc JSON Dump

Risk:
- every module could add its own raw JSON relation field

Mitigation:
- standardize cross-entity bindings through one relation table and typed service methods

### Chapter Composer Overreaches Into the Indoor Rules Engine

Risk:
- implementing full overlay logic would silently pull Phase 12 or v2.1 work into Phase 11

Mitigation:
- keep anchor contracts forward-compatible, but only fully support `poi` and `activity` pickers in Phase 11

### Public Runtime Stays on Placeholder Discover Cards

Risk:
- admin work appears complete, but the mini-program still renders synthetic activities

Mitigation:
- make public activity reads and discover-card alignment an explicit Phase 11 task

### Collection Media Remains Split Between Asset IDs and Raw URLs

Risk:
- operators get inconsistent workflows and asset cleanup becomes unsafe

Mitigation:
- move collection media to canonical asset IDs and derive URLs in responses only

## Recommendation Summary

- Add a reusable relation spine early and let storylines, chapters, activities, collectibles, badges, and rewards build on it.
- Split storyline metadata editing from chapter composition.
- Treat activities/tasks as a first-class authored content domain and wire the public discover flow to real activity data.
- Move rewards into collection ownership and upgrade collectibles/badges to canonical asset-backed authoring.
- Extend public and client contracts only for phase-owned fields so the live chain stays coherent without dragging in deferred scope.

## Validation Architecture

- Admin backend tests should cover:
  - relation-link persistence and fetch behavior
  - storyline multi-map binding and chapter composition DTOs
  - activity CRUD, publish-window filtering, and pinning
  - collectible, badge, and reward relation metadata plus canonical asset IDs
- Public backend tests should cover:
  - storyline detail shaping with multi-binding and chapter metadata
  - published activity listing and discover-card sourcing
  - reward and collection catalog response shaping
- Admin UI verification should cover:
  - dedicated chapter composition route ownership
  - real activity authoring forms and schedule/HTML inputs
  - collection/reward authoring surfaces using the shared media picker
- Mini-program verification should cover:
  - storyline and reward DTO compatibility after the contract extension
  - discover activity cards coming from the real activity catalog
  - client build success without reintroducing mock-only fields
- Phase 11 should add a smoke script that proves admin writes can be read back through public APIs for at least one storyline, one activity, and one reward/collection relation

## Sources

### Local codebase references

- `packages/admin/aoxiaoyou-admin-ui/src/pages/StorylineManagement/index.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/OperationsManagement/index.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/CollectibleManagement.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/BadgeManagement.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetPickerField.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts`
- `packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminStoryLineController.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminStoryChapterController.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminOperationsController.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminCollectibleController.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminSystemManagementController.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminStoryLineServiceImpl.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminStoryChapterServiceImpl.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminOperationsServiceImpl.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminCollectibleServiceImpl.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminSystemManagementServiceImpl.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/StoryLineController.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/RewardController.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/CatalogFoundationServiceImpl.java`
- `packages/client/src/services/api.ts`
- `packages/client/src/services/gameService.ts`

### Upstream planning references

- `.planning/ROADMAP.md`
- `.planning/REQUIREMENTS.md`
- `.planning/phases/09-spatial-model-rebuild/09-CONTEXT.md`
- `.planning/phases/09-spatial-model-rebuild/09-01-SUMMARY.md`
- `.planning/phases/10-media-asset-pipeline-and-library/10-CONTEXT.md`
- `.planning/phases/10-media-asset-pipeline-and-library/10-03-SUMMARY.md`
