# Phase 36: Material Production Pipeline and Asset Promotion - Context

**Gathered:** 2026-04-30
**Status:** Ready for planning
**Source:** `/gsd-discuss-phase 36`, v3.1 roadmap, Phase 33/34 handoffs, and user decisions.

<domain>
## Phase Boundary

Phase 36 turns the Phase 33 planned material manifest for `東西方文明的戰火與共生` into real reusable assets where feasible.

This phase owns:

- Generating or importing the flagship story media assets from `docs/content-packages/east-west-war-and-coexistence/content-manifest.json`.
- Producing real image assets using `image-2` through Codex/image generation when the backend cannot directly call it.
- Producing chapter pickup material boards, slicing them into child assets, and linking each child asset back to manifest items.
- Producing Mandarin narration with Bailian CosyVoice.
- Producing simple short MP4 story videos from hero images, narration, Traditional Chinese subtitles, and light pan/zoom motion.
- Uploading generated files to Tencent COS through backend-owned media/COS APIs where possible.
- Creating or updating `content_assets` and `story_material_package_items` records.
- Promoting item status through `uploaded`, `approved`, and `published` with version/candidate traceability.
- Recording reusable one-off production commands/templates for later storylines.

This phase does not own:

- A new global admin material production workbench.
- Opening `image-2` generation as a general feature for all admin users.
- Mini-program story journey acceptance, route drawing, pickups, or reward UX. Those remain Phase 39.
- Full AR, speech-input gameplay, puzzle engines, or production approval workflow.

</domain>

<decisions>
## Implementation Decisions

### Generation Strategy and Cost Control

- **D36-01:** The preferred execution model is direct production by Codex during this phase: generate the story assets the flagship story needs, save them locally, upload them to COS, create or update `content_assets`, update material package items, and complete the story configuration.
- **D36-02:** Admin UI should not become a general `image-2` production surface. If UI changes are needed, keep them inside the existing story material package page as explicit package-scoped production/import/status actions.
- **D36-03:** Real `image-2` generation is allowed in Phase 36, but it is limited to this story package and must keep cost/provenance records.
- **D36-04:** If the backend cannot directly call `image-2`, use Codex/image generation to create local files, then pass those files through the existing COS/content asset/material package promotion path.
- **D36-05:** Generation should support a batch action with an explicit pre-flight confirmation showing item count, estimated cost, target asset kinds, and risk.
- **D36-06:** Every generation attempt should preserve candidates or versions. A failed or disliked result must not destroy the previous usable asset.
- **D36-07:** Cost control uses daily/batch estimated cost ceilings. Exceeding a ceiling requires super-admin confirmation.

### Material Board and Slicing Flow

- **D36-08:** Generate one pickup material board per chapter, plus one full-line title/badge board.
- **D36-09:** Do not productize a general admin cropper in Phase 36. Slicing is a one-off execution step performed by Codex/local tooling.
- **D36-10:** Codex should visually inspect the generated board, decide which region corresponds to which manifest item, then slice the board into child assets.
- **D36-11:** Slicing should use a repeatable local command/template so later storylines can reuse the method, even though Phase 36 does not expose it as a general product feature.
- **D36-12:** Child assets must preserve parent board provenance and still map cleanly to the manifest item `itemKey`, `localPath`, and `cosObjectKey`.
- **D36-13:** After slicing, child assets should be uploaded and automatically reflected in `content_assets` plus the matching `story_material_package_items` rows.

### Audio and Video Production

- **D36-14:** Phase 36 produces Mandarin narration first. Cantonese, English, and Portuguese voice versions are deferred.
- **D36-15:** Audio generation should directly use Bailian CosyVoice and upload generated audio to COS.
- **D36-16:** If CosyVoice fails, do not silently publish substitute audio. Planning can include retry/error handling, but the locked target is real Bailian output.
- **D36-17:** Each chapter should receive a short MP4 made from the chapter hero image, Mandarin narration, Traditional Chinese subtitles, and light pan/zoom motion.
- **D36-18:** Video subtitles must be embedded in Traditional Chinese and generated from `audio-scripts.md`.

### Status Promotion and Rollback

- **D36-19:** Successful generation should upload to COS immediately and move the item to `uploaded`.
- **D36-20:** After local visual or playback verification by Codex, the item can be promoted automatically to `approved`.
- **D36-21:** Approved items should then automatically become `published` so the story can use the assets immediately.
- **D36-22:** Every item must keep candidate/version traceability. Rollback means pointing `asset_id` and package item metadata back to a previous finalized version.

### the agent's Discretion

- The planner may choose whether to implement candidate/version traceability by adding a dedicated material item versions table, by linking to existing AI generation candidate records, or by storing version metadata on material package items, provided rollback is reliable and inspectable.
- The planner may choose the exact local production script language, but scripts must avoid inline PowerShell Chinese text and must read UTF-8 files for prompts/scripts.
- The planner may decide how to estimate cost if a provider does not return exact cost, but cost records must be visible and conservative.
- The planner may choose whether COS upload happens through existing multipart endpoints, direct service calls from backend jobs, or a controlled local import endpoint, provided secrets remain outside tracked files.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project Rules and Phase Scope

- `AGENTS.md` - stack, admin/public ownership, COS/media rules, UTF-8 rule, secret handling, and GSD workflow constraint.
- `.planning/PROJECT.md` - v3.1 milestone goals and active scope.
- `.planning/REQUIREMENTS.md` - `MAT-01` through `MAT-05` and related QA/runtime boundaries.
- `.planning/ROADMAP.md` - Phase 36 goal, dependencies, success criteria, and later phase split.
- `.planning/STATE.md` - current v3.1 state and accumulated constraints.

### Flagship Story Material Package

- `docs/content-packages/east-west-war-and-coexistence/content-manifest.json` - canonical 54 planned material items, local paths, COS keys, asset ids, usage targets, and status seeds.
- `docs/content-packages/east-west-war-and-coexistence/image-prompts.md` - image and material-board prompts.
- `docs/content-packages/east-west-war-and-coexistence/audio-scripts.md` - narration scripts, voice-language guidance, and finale/reward audio direction.
- `docs/content-packages/east-west-war-and-coexistence/lottie-design.md` - Lottie design and fallback references.
- `docs/content-packages/east-west-war-and-coexistence/historical-checklist.md` - historical basis and literary dramatization boundaries.
- `docs/content-packages/east-west-war-and-coexistence/story-script.md` - story package script and chapter content context.

### Prior Phase Decisions

- `.planning/phases/33-complete-flagship-story-content-material-package/33-CONTEXT.md` - flagship story package scope, manifest model, story codes, and UTF-8 safety decisions.
- `.planning/phases/34-public-runtime-and-mini-program-consumption-baseline/34-CONTEXT.md` - public runtime DTO and mini-program baseline boundary.
- `.planning/phases/35-operations-lifecycle-scheduling-and-dependency-workbench/35-CONTEXT.md` - lifecycle/status semantics to preserve when publishing assets.

### Admin Backend and Media Infrastructure

- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminStoryMaterialPackageController.java` - existing material package CRUD API.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminStoryMaterialPackageServiceImpl.java` - existing package/item mapping and counters.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/StoryMaterialPackage.java` - package entity.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/StoryMaterialPackageItem.java` - package item entity and current status/asset fields.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminAiController.java` - AI provider, generation job, candidate finalize, voice, and log API.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java` - existing AI job, candidate, finalize, CosyVoice/image provider, and cost/log implementation.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminAiGenerationJobCreateRequest.java` - current generation job create contract.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminAiCandidateFinalizeRequest.java` - current candidate finalize contract.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/CosAssetStorageService.java` - backend-owned COS upload service.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/ContentAsset.java` - canonical content asset metadata.

### Admin UI

- `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryMaterialPackageManagement.tsx` - existing story material package page that should receive package-scoped production/import/status actions.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryMaterialPackageManagement.scss` - styling for the material package page.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/MediaLibraryManagement.tsx` - media preview and asset management patterns.
- `packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetPreview.tsx` - reusable preview component.
- `packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetDetailDrawer.tsx` - reusable asset detail UI.
- `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts` - admin API client contract.

### Schema, Seeds, and Verification

- `scripts/local/mysql/init/47-phase-33-story-material-package-model.sql` - material package and item schema.
- `scripts/local/mysql/init/48-phase-33-flagship-material-assets.sql` - seeded material package item and content asset records.
- `scripts/local/seed-phase-33-material-assets.ps1` - existing seed helper for material assets.
- `scripts/local/smoke-phase-33-flagship-package.ps1` - material package smoke pattern.
- `scripts/local/smoke-phase-34-public-runtime.ps1` - runtime smoke pattern that later phases will depend on.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- `AdminStoryMaterialPackageController` already provides package and item CRUD, but it lacks production/import, candidate/version, status promotion, and manifest consistency actions.
- `AdminStoryMaterialPackageServiceImpl` already validates package/item uniqueness, validates content asset ids, recomputes counters, and maps package details for the UI.
- `StoryMaterialPackageItem` already has `assetId`, `localPath`, `cosObjectKey`, `canonicalUrl`, `status`, `promptText`, `scriptText`, historical basis, literary dramatization, and fallback fields. These are enough for first-pass promotion, but not enough by themselves for robust version history.
- `AdminAiController` and `AdminAiServiceImpl` already support generation jobs, candidates, candidate finalization, voice preview/clone/sync, provider inventory, logs, and platform settings.
- `AdminAiServiceImpl` can persist binary candidates through `CosAssetStorageService` and then finalize candidates into `ContentAsset`.
- `CosAssetStorageService` already accepts `StoredAssetPayload`, computes checksum/dimensions, uploads to COS, and returns canonical metadata.
- `StoryMaterialPackageManagement.tsx` already renders package summaries, item tables, status tags, COS paths, and quick navigation to media/content/experience/reward pages. This is the right place for package-scoped production actions.

### Established Patterns

- Admin backend controllers return `ApiResponse<T>` and are rooted under `/api/admin/v1`.
- Admin UI uses Ant Design components, `PageContainer`, and central API helpers from `services/api.ts`.
- AI generation records are owner-aware: super admins can see broader history, non-super admins see their own jobs.
- COS uploads must be backend-owned and secrets must come from runtime configuration.
- Local scripts must avoid inline PowerShell Chinese literals. Prompt/script bodies should be read from UTF-8 files.
- Material status labels already include `planned`, `generated`, `uploaded`, and `published`, but the locked Phase 36 flow skips a long-lived `generated` item state by uploading immediately.

### Integration Points

- Package-scoped actions should attach generated assets back to `story_material_package_items` by `packageId` and `itemKey`.
- If using existing AI candidates, planner must connect `AiGenerationCandidate.finalizedAssetId` with package item `assetId`.
- If using local Codex-generated files, planner needs a safe import endpoint or script that uploads files and writes `ContentAsset` plus package item updates.
- Version rollback needs an inspectable mapping from package item to prior candidate/content asset/version.
- The Phase 33 manifest remains the source of intended paths and COS keys. Execution should not invent a parallel naming system.

</code_context>

<specifics>
## Specific Ideas

- Use the existing 54-item manifest as the work queue.
- Produce real image files for story cover/banner, chapter heroes, chapter pickup boards, title/badge board, poster fallback, and sliced icons.
- Use `image-prompts.md` as the source prompt file and record the exact prompt used for each generation.
- Chapter pickup boards:
  - Chapter 1 board: four pickup/fragment icons.
  - Chapter 2 board: four pickup/fragment icons.
  - Chapter 3 board: four pickup/fragment icons.
  - Chapter 4 board: five pickup/fragment icons.
  - Chapter 5 board: three finale pickup/reward icons.
  - Full-line title/badge board: all title and badge-style icons.
- Codex/local tooling should visually inspect boards and slice regions into manifest-mapped child assets.
- Mandarin narration should be generated for `audio_ch01_narration` through `audio_ch05_narration`, plus reward SFX if feasible through the same provider path.
- Each chapter should receive a short MP4 using its hero image, generated Mandarin narration, Traditional Chinese subtitles, and subtle pan/zoom.
- Generated video can use still-image motion, not complex animation. This aligns with the user's note that story video can be assembled from one or more `image-2` stills plus motion and audio.
- Approved assets should be published immediately for this story package so later phases can validate runtime consumption.
- The implementation should leave a reusable production runbook/template under docs or scripts, but not expose a general-purpose `image-2` admin product.

</specifics>

<deferred>
## Deferred Ideas

- General-purpose admin `image-2` generation for arbitrary users or arbitrary content.
- A full visual cropper/material-board editor inside admin.
- Cantonese, English, and Portuguese narration versions.
- Complex AI video generation beyond still-image pan/zoom MP4 assembly.
- Full WeChat mini-program story-mode acceptance and gameplay polish, which remains Phase 39.
- AR recognition, voice-input gameplay, puzzle engines, and advanced minigames.

</deferred>

---

*Phase: 36-material-production-pipeline-and-asset-promotion*
*Context gathered: 2026-04-30*
