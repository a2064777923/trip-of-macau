# Phase 33 Research - Complete Flagship Story Content/Material Package

**Research date:** 2026-04-29
**Mode:** Local codebase research, no external agent

## Research Questions

1. What data model already exists for story content, media, experience flows, rewards, and exploration?
2. What needs to be added so the flagship story package is traceable as a reusable package instead of loose rows?
3. How should execution seed a large Chinese story package without reintroducing encoding problems?
4. What is the minimum admin surface needed to make the package inspectable without duplicating existing editors?

## Findings

### Existing Story and Media Foundation

- `content_assets` already stores uploaded/generated assets with `asset_kind`, COS metadata, processing fields, status, and publication fields.
- Phase 28 added animation metadata columns on `content_assets`: `animation_subtype`, `poster_asset_id`, `fallback_asset_id`, `default_loop`, and `default_autoplay`.
- `story_content_blocks` and `story_chapter_block_links` already support rich text, quote, image, gallery, audio, video, Lottie, and attachment-list style content composition.
- `AdminContentManagementController` exposes `/api/admin/v1/content/assets` and upload endpoints.
- `AdminStoryContentBlockController` exposes `/api/admin/v1/content/blocks`.

### Existing Experience Foundation

- `experience_templates`, `experience_flows`, `experience_flow_steps`, `experience_bindings`, and `experience_overrides` are present in `39-phase-28-experience-orchestration.sql`.
- `AdminExperienceOrchestrationController` exposes template, flow, step, binding, override, exploration element, and governance endpoints under `/api/admin/v1/experience`.
- Phase 31 seeds reusable template examples:
  - `presentation.fullscreen_media`
  - `presentation.rich_popup`
  - `presentation.lottie_overlay`
  - `presentation.map_overlay`
  - `display_condition.proximity_radius`
  - `display_condition.dwell_duration`
  - `trigger_condition.tap`
  - `trigger_condition.tap_sequence`
  - `trigger_condition.photo_checkin`
  - `task_gameplay.quiz`
  - `task_gameplay.cyber_incense`
  - `trigger_effect.grant_collectible`
  - `trigger_effect.grant_badge_title`
  - `reward_presentation.fullscreen_unlock`
- Phase 30 has only the first real flagship chapter slice. Phase 33 must expand this to the full five-chapter package.

### Existing Rewards and Exploration Foundation

- Phase 23 introduced:
  - `game_rewards`
  - `redeemable_prizes`
  - `reward_rules`
  - `reward_condition_groups`
  - `reward_conditions`
  - `reward_rule_bindings`
  - `reward_presentations`
  - `reward_presentation_steps`
- `game_rewards` supports `reward_type`, `rarity`, `cover_asset_id`, `icon_asset_id`, `animation_asset_id`, and `presentation_id`, which is sufficient for badges, honor titles, fragments, coins, and virtual items.
- Phase 32 made `exploration_elements` the canonical denominator registry. Phase 33 should seed only semantic weighted elements, never fixed percentage increments.

### Missing Package-Level Traceability

The existing tables can store assets and story rows, but they do not answer package-level operator questions:

- Which assets, prompts, scripts, local files, COS keys, and DB ids belong to the flagship package?
- Which generated material is final versus draft versus fallback?
- Which content items are historical basis versus literary dramatization?
- Which admin workbench should be used to edit the target object?

Recommendation: add a lightweight package registry:

- `story_material_packages`
- `story_material_package_items`

This should not replace `content_assets`, `story_content_blocks`, `experience_flows`, or `game_rewards`. It should aggregate and trace them.

### Seed Strategy

Use idempotent SQL and a committed manifest. Split seeds by concern:

- schema/API package registry
- material manifest and asset records
- story text, blocks, flows, rewards, and exploration rows

Avoid one huge script with mixed DDL, asset metadata, story text, and verification logic. This keeps reruns debuggable and protects UTF-8 Chinese content.

### Admin Strategy

Add `故事素材包` under the content navigation. The page should:

- list packages
- show package completion counters
- show manifest item table with type, status, local path, COS key, asset id, usage target, provenance
- link out to content blocks, media library, story mode, and experience workbench
- show historical basis/literary dramatization summaries

The page should not duplicate detailed media editing or flow editing; it is an inspection and navigation hub.

## Validation Architecture

### Automated Verification Targets

- Admin backend compile:
  - `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml`
- Admin UI build:
  - `npm run build --prefix packages/admin/aoxiaoyou-admin-ui`
- SQL import:
  - `mysql --default-character-set=utf8mb4 aoxiaoyou < scripts/local/mysql/init/47-phase-33-story-material-package-model.sql`
  - `mysql --default-character-set=utf8mb4 aoxiaoyou < scripts/local/mysql/init/48-phase-33-flagship-material-assets.sql`
  - `mysql --default-character-set=utf8mb4 aoxiaoyou < scripts/local/mysql/init/49-phase-33-east-west-flagship-story.sql`
- Live smoke:
  - `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-33-flagship-package.ps1`

### Smoke Assertions

The smoke should verify:

- `story_material_packages.code='east_west_war_and_coexistence_package'` exists.
- The package contains at least five chapter groups.
- The storyline `east_west_war_and_coexistence` has exactly five published chapters.
- Every chapter has a non-null `experience_flow_id`.
- Every chapter has at least one content block link.
- The package has at least one `lottie`, one `image`, one `audio`, and one fallback/poster item in manifest records.
- The package has at least 20 exploration elements across chapters.
- The package has reward/title rows or rule bindings for named chapter and finale rewards.
- Admin API `/api/admin/v1/content/material-packages` returns the package.
- Admin API detail returns manifest item rows and relation targets.

### Manual Verification Targets

- Open `/admin/#/content/material-packages`.
- Confirm the package page is Traditional Chinese.
- Confirm package cards show story title, status, material counts, and progress.
- Confirm item rows link to media/content/story/experience workbenches.
- Confirm no raw JSON is required to understand the package contents.

## Risks

- Large SQL files with Chinese text are prone to encoding damage if edited through non-UTF-8 shell literals.
- Asset generation may not be available in every execution environment; the plan must allow deterministic fallback metadata while still recording prompt/script provenance.
- Some POI codes may differ from the user-facing names. Execution must resolve existing codes before seeding or create a clearly named missing fixture only when safe.
- Directly committing binary image/audio/video artifacts can bloat the repo. Prefer manifest + COS + lightweight JSON fixtures unless a small local asset is intentionally needed for preview.
