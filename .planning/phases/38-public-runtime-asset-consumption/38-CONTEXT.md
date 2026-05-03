# Phase 38: Public Runtime Asset Consumption - Context

**Gathered:** 2026-05-03
**Status:** Ready for planning
**Source:** `/gsd-next` from completed Phase 37, v3.1 roadmap, Phase 34 runtime baseline, Phase 36/37 material production and QA handoff, and targeted code scout.

<domain>
## Phase Boundary

Phase 38 aligns the public backend runtime with the generated and QA-governed flagship story assets from Phases 36 and 37.

This phase owns:

- Extending traveler-safe public runtime DTOs so storylines, chapters, content blocks, experience steps, and media references can expose generated/published asset URLs, poster/fallback assets, Lottie metadata, audio/video metadata, and manifest usage hints.
- Filtering public runtime content by published/lifecycle-safe status so rejected, draft, unpublished, broken, or admin-only material does not leak into mini-program responses.
- Returning explicit fallback or unsupported-media placeholders when a configured asset is missing, unpublished, rejected, or unavailable.
- Keeping admin-only provenance private: prompts, local filesystem paths, provider names/models where not user-facing, API keys, costs, generation job internals, and raw editor JSON must not appear in public DTOs.
- Recording the baseline story events needed by Phase 39: media completion, content viewed, pickup interaction, baseline task completion, reward acquisition, chapter progression, and story session exit.
- Preserving idempotency through stable client event IDs and keeping stateful event endpoints authenticated while read-only story browsing remains public.
- Adding repeatable smoke coverage for the flagship `東西方文明的戰火與共生` asset chain, fallback behavior, lifecycle filtering, event idempotency, and session exit.

This phase does not own:

- Full mini-program story journey rendering, route highlighting, pickup UI, reward UI, or WeChat DevTools/device acceptance. Those belong to Phase 39.
- Rebuilding the admin material QA workspace or production pipeline from Phases 36 and 37.
- Implementing complex AR/photo recognition, speech input, puzzles, route-coverage games, or cannon-defense gameplay. These remain explicit runtime placeholders.
- A new public asset browser or public exposure of the story material package management model.

</domain>

<decisions>
## Implementation Decisions

### Runtime DTO Shape

- **D38-01:** Extend the existing `GET /api/v1/storylines/{storylineId}/runtime` and `GET /api/v1/experience/poi/{poiId}` DTOs instead of creating a parallel public API family.
- **D38-02:** `ExperienceRuntimeResponse.StorylineRuntime`, `StoryChapterRuntime`, `Step`, `Flow`, `StoryLineResponse`, `StoryChapterResponse`, `StoryContentBlockResponse`, and `StoryMediaAssetResponse` remain the main contract roots.
- **D38-03:** Public runtime DTOs should expose traveler-safe asset delivery fields only: asset id, kind, URL, MIME type, dimensions, duration where available, poster/fallback URLs, Lottie loop/autoplay/subtype, usage target hints, and stable item keys/codes.
- **D38-04:** Public runtime must not expose admin provenance such as prompt text, local path, COS object key when not already public, provider API keys, provider secrets, generation job payloads, cost data, QA notes, rejected-version details, or raw unsafe JSON.
- **D38-05:** Keep stable debug-safe identifiers in public DTOs: storyline code, chapter id/code/order, content block code, experience step code, element code/id, asset id, and optional manifest item key. These are allowed because they help the mini-program report events and diagnose mappings without leaking secrets.
- **D38-06:** Runtime JSON fields that originate from admin forms should be validated/normalized into structured maps or labeled placeholders. Do not ask the mini-program to interpret arbitrary editor JSON.

### Asset Filtering and Fallbacks

- **D38-07:** Public runtime may return only assets that are traveler-eligible: `content_assets.status = published` or an equivalent runtime-safe published state, and package item/version status must not be rejected/draft when material package data is consulted.
- **D38-08:** If an asset id points to an unpublished, rejected, missing, or URL-less asset, the public response should substitute poster/fallback where configured; otherwise return an explicit unsupported placeholder object with Traditional Chinese reason text.
- **D38-09:** Broken or unavailable material should not remove the whole chapter/storyline from the runtime unless the chapter itself is unpublished. Degrade the specific media block/step so the story can continue.
- **D38-10:** Lottie JSON uses `assetKind = lottie`, `animationSubtype`, `defaultLoop`, `defaultAutoplay`, `posterAssetId/posterUrl`, and `fallbackAssetId/fallbackUrl`. The mini-program can later use these fields directly with `lottie-miniprogram`.
- **D38-11:** GIF remains an image-kind asset; MP4 and audio remain standard media assets with poster/fallback support where configured.
- **D38-12:** Manifest usage hints should be public-safe and operationally useful: usage target, chapter code/order, material item key, and display role. Do not expose full production prompt/script/provenance data.

### Event Ingestion and Sessions

- **D38-13:** Read-only runtime endpoints remain anonymous-browsable. All stateful event and session endpoints remain auth-gated through existing bearer JWT handling.
- **D38-14:** Event ingestion should accept and normalize the event classes needed by Phase 39: `story_opened`, `chapter_started`, `content_viewed`, `media_completed`, `pickup_interacted`, `task_completed`, `reward_acquired`, `unsupported_viewed`, and `story_session_exit`.
- **D38-15:** Event writes must remain idempotent by `(userId, clientEventId)`. Retried mini-program calls must return the original accepted event response rather than duplicate exploration progress.
- **D38-16:** Event payloads may carry runtime-safe references such as chapter id, block code, step code, asset id, media progress, pickup code, task code, reward code, and unsupported template type. Payload validation should reject invalid JSON and oversized/unsafe payloads.
- **D38-17:** Story session exit clears temporary story-session state but must not delete permanent exploration events, pickups, rewards, check-ins, titles, or completed media events.
- **D38-18:** Baseline reward/pickup/task events should be recorded in the dynamic exploration event stream now; richer reward inventory side effects can remain represented by existing configured effects or future Phase 39/40 acceptance if not already wired.

### Lifecycle and Privacy

- **D38-19:** Public runtime filtering must align with Phase 35 lifecycle semantics and Phase 37 QA status semantics. Admin preview may see draft/rejected/problem assets; public runtime must not.
- **D38-20:** Runtime asset sanitation should happen in the public backend service layer, not in the mini-program. The client should receive a ready-to-render object with fallback/unsupported decisions already made.
- **D38-21:** If admin/public preview behavior diverges, document the difference clearly: admin preview is editorial; public runtime is traveler-safe and lifecycle-filtered.
- **D38-22:** Do not commit or log provider/COS/API secrets while testing runtime asset URLs. Smoke scripts may use environment variables and should print only safe URLs/ids/statuses.

### Verification

- **D38-23:** Verification must include public backend compile and a smoke script for the flagship story runtime asset chain.
- **D38-24:** Smoke should assert at least: published runtime loads, five chapters are present, generated image/audio/video/Lottie-capable asset metadata is included, unpublished/rejected asset fixtures are filtered or replaced, missing assets return fallback/unsupported placeholder, and public DTO does not contain banned fields.
- **D38-25:** Authenticated smoke should cover session start, event record, duplicate `clientEventId`, media completion, pickup/task/reward event types, exploration summary, and session exit idempotency.
- **D38-26:** Phase 38 should update requirements traceability for `RUN-01` through `RUN-04` only after smoke passes locally.

### the agent's Discretion

- The planner may decide whether to enrich `StoryMediaAssetResponse` directly or add nested helper DTOs, provided existing mini-program and admin contracts remain backward-compatible.
- The planner may decide whether package item/version lookup is joined directly in public service code or backfilled into `content_assets`/relation links, provided public runtime receives safe usage hints and filtering.
- The planner may choose exact placeholder labels and unsupported-media DTO shape, as long as Traditional Chinese copy is clear and the mini-program can render it without special-case crashes.
- The planner may decide whether to extend `scripts/local/smoke-phase-34-public-runtime.ps1` or create `scripts/local/smoke-phase-38-public-runtime-assets.ps1`.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project and Milestone Scope

- `AGENTS.md` - stack, UTF-8/utf8mb4 rule, public/admin ownership, COS/media constraints, secret handling, and verification expectations.
- `.planning/PROJECT.md` - v3.1 active scope, public runtime ownership, and deferred mini-program acceptance boundary.
- `.planning/REQUIREMENTS.md` - `RUN-01` through `RUN-04`, plus the QA/material requirements Phase 38 consumes.
- `.planning/ROADMAP.md` - Phase 38 goal, dependencies, success criteria, and Phase 39/40 boundary.
- `.planning/STATE.md` - current Phase 37 completion handoff and local runtime constraints.

### Prior Phase Decisions

- `.planning/phases/34-public-runtime-and-mini-program-consumption-baseline/34-CONTEXT.md` - existing public runtime endpoint family, traveler-safe DTO decisions, anonymous read/auth-gated write policy, and mini-program baseline boundary.
- `.planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md` - generated material production strategy, real image/audio/video assets, COS upload, and package promotion semantics.
- `.planning/phases/36-material-production-pipeline-and-asset-promotion/36-HANDOFF.md` - Phase 36 live routes, scripts, generated/published asset evidence, and caption caveat.
- `.planning/phases/36-material-production-pipeline-and-asset-promotion/36-VERIFICATION.md` - image, board-slice, audio, video, COS, version history, and rollback evidence.
- `.planning/phases/37-material-qa-workspace-and-reuse-controls/37-CONTEXT.md` - QA status semantics, reuse controls, hidden rejected/draft defaults, preview/fallback expectations, and consistency checks.
- `.planning/phases/37-material-qa-workspace-and-reuse-controls/37-VERIFICATION.md` - Phase 37 smoke results and known evidence, if present.

### Flagship Story Package and Material Evidence

- `docs/content-packages/east-west-war-and-coexistence/content-manifest.json` - canonical material item keys, usage targets, local paths, COS target keys, and story material references.
- `docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-production-report.json` - generated asset evidence for images/audio/video.
- `docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-board-slice-report.json` - child icon/board slicing evidence.
- `docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-video-jobs.json` - MP4 job and caption metadata used by chapter videos.
- `docs/content-packages/east-west-war-and-coexistence/story-script.md` - flagship story structure and chapter expectations.
- `docs/content-packages/east-west-war-and-coexistence/audio-scripts.md` - narration scripts that explain runtime audio intent.

### Public Backend Runtime

- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java` - public POI runtime, storyline runtime, event, session, exit, and exploration endpoints.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/PublicExperienceService.java` - public runtime service contract.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java` - compiled runtime assembly, event idempotency, session handling, exploration progress, and current asset mapping.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/ExperienceRuntimeResponse.java` - current runtime DTO roots to extend.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/request/ExperienceEventRequest.java` - current event request contract.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/ExperienceEventResponse.java` - event response contract.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StoryMediaAssetResponse.java` - public media asset DTO requiring richer safe metadata/fallback support.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StoryLineResponse.java` - public story detail DTO containing cover/banner/attachments/chapters.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StoryChapterResponse.java` - public chapter DTO containing primary media, content blocks, and structured rule/effect fields.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StoryContentBlockResponse.java` - public content block DTO containing block-level media.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java` - public story/content asset mapping and published-story filtering.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/StoryLineController.java` - existing public classic story list/detail endpoint to keep aligned.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/ContentAsset.java` - canonical asset fields available to public backend.

### Schema and Seeds

- `scripts/local/mysql/init/47-phase-33-story-material-package-model.sql` - story material package/item schema.
- `scripts/local/mysql/init/48-phase-33-flagship-material-assets.sql` - baseline material asset/package item seed.
- `scripts/local/mysql/init/49-phase-33-east-west-flagship-story.sql` - flagship story, chapter, content block, flow, reward, and relation seed.
- `scripts/local/mysql/init/51-phase-36-material-production-versioning.sql` - material package versioning schema and backfill.
- `scripts/local/mysql/init/52-phase-37-material-qa.sql` - Phase 37 QA schema/seed if present after Phase 37 execution.

### Mini-program Future Consumer

- `packages/client/src/services/api.ts` - public API helper/types that Phase 39 will consume.
- `packages/client/src/services/gameService.ts` - current runtime refresh and fallback mapping.
- `packages/client/src/types/game.ts` - story/media/runtime types expected by story UI.
- `packages/client/src/pages/story/index.tsx` - Phase 39 story page consumer.
- `packages/client/src/components/StoryContentBlockRenderer/index.tsx` - rendering behavior for block/media types.
- `packages/client/src/components/LottieAssetPlayer/index.tsx` - Lottie JSON canvas/fallback baseline.

### Smoke Patterns

- `scripts/local/smoke-phase-34-public-runtime.ps1` - existing public runtime/event/session smoke pattern.
- `scripts/local/smoke-phase-36-material-production.ps1` - production/COS/version smoke pattern.
- `scripts/local/smoke-phase-37-material-qa.ps1` - QA/reuse smoke pattern.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- `ExperienceController` already exposes the endpoint family Phase 38 needs: POI runtime, storyline runtime, event recording, story session start/event/exit, and user exploration.
- `PublicExperienceServiceImpl` already compiles inherited flows, chapter flows, overrides, templates, unsupported gameplay labels, event idempotency, session updates, and weighted exploration progress.
- `ExperienceRuntimeResponse.Step` already carries `mediaAsset`, `eventType`, `elementCode`, `elementId`, `rewardRuleIds`, `explorationWeightLevel`, `unsupported`, and `travelerActionLabel`, making it the right place to add safe usage/fallback metadata.
- `StoryMediaAssetResponse` already includes `assetKind`, URL, MIME, dimensions, Lottie subtype, loop/autoplay, poster, and fallback fields, but it lacks duration/file-size/usage-hint/availability status fields.
- `PublicCatalogServiceImpl` centralizes public story/chapter/content-block mapping and current `ContentAsset` to `StoryMediaAssetResponse` conversion.
- `StoryLineServiceImpl` delegates public story list/detail to `PublicCatalogServiceImpl`, so classic story endpoints and runtime endpoint can share filtering behavior if mapper updates are centralized.
- `ExperienceEventRequest` is small and already supports `clientEventId`, `storylineSessionId`, `payloadJson`, and `occurredAt`; planning should extend validation/normalization rather than inventing a new event endpoint.

### Established Patterns

- Public backend controllers return `ApiResponse<T>` under `/api/v1`.
- State-changing public endpoints manually require bearer JWT via `JwtUtil` in `ExperienceController`; read-only runtime endpoints stay public.
- Public story detail filtering currently depends on `PublicCatalogServiceImpl` published filters.
- Runtime asset mapping currently loads direct `mediaAssetId` references from flow steps and story/content block relations; generated Phase 36 material may require package item/version context or backfilled asset bindings.
- Smoke scripts are PowerShell-based and should use env-backed tokens/secrets, but Chinese text should be read from UTF-8 files or avoided in inline shell literals.

### Integration Points

- Add runtime-safe asset sanitation close to `PublicCatalogServiceImpl.toStoryMediaAssetResponse(...)` and `PublicExperienceServiceImpl.toStoryMediaAsset(...)`.
- If Phase 36/37 package item metadata is needed publicly, expose only a sanitized `usageHint` or equivalent DTO assembled from `story_material_package_items` / version rows or relation links.
- Expand event type handling in `PublicExperienceServiceImpl.recordEvent(...)` and session update logic without breaking existing idempotency.
- Extend or create a Phase 38 smoke script from `smoke-phase-34-public-runtime.ps1` so the existing auth/session/runtime conventions stay consistent.

</code_context>

<specifics>
## Specific Ideas

- Use the flagship `east_west_war_and_coexistence` story runtime as the primary acceptance fixture.
- Smoke should verify that public runtime includes the generated Phase 36 chapter hero images, Mandarin narration audio, chapter MP4 video assets, fallback posters, and Lottie-capable metadata where configured.
- A sanitized public asset object should have a user-facing `availability` state such as `available`, `fallback`, or `unsupported`, plus Traditional Chinese `unavailableReason` when applicable.
- Banned public DTO keys should be asserted by smoke. Examples: `promptText`, `scriptText`, `localPath`, `providerApiKey`, `apiKey`, `secret`, `cost`, `qaNote`, `objectKey` if not intentionally public.
- Event smoke should post duplicate media completion and pickup/task/reward events with the same `clientEventId` and assert the same event id is returned.
- If a configured asset is unavailable, the runtime should keep the related content block/step visible with a placeholder card rather than deleting it from the response.

</specifics>

<deferred>
## Deferred Ideas

- Full mini-program route rendering, current chapter highlighting, pickup/reward UI, and WeChat DevTools/device UAT belong to Phase 39.
- Cost/history visibility and final release readiness belong to Phase 40.
- Complex gameplay engines for AR recognition, speech input, puzzles, route coverage, and cannon defense remain future scope beyond v3.1 unless explicitly pulled forward.
- Public CDN reachability or COS `HEAD` checking at request time is not required for Phase 38; runtime should rely on stored status/metadata and smoke can separately verify URL availability where env allows.

</deferred>

---

*Phase: 38-public-runtime-asset-consumption*
*Context gathered: 2026-05-03*
