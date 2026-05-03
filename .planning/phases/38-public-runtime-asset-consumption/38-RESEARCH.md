---
phase: 38
slug: public-runtime-asset-consumption
status: complete
created: 2026-05-03
---

# Phase 38 Research - Public Runtime Asset Consumption

## Research Goal

Plan a safe Phase 38 implementation that lets the mini-program public runtime consume Phase 36/37 generated and published assets without exposing admin-only material production data. The implementation must preserve the existing public runtime endpoint family, add fallback-safe asset DTOs, and prepare idempotent story events for Phase 39.

## Existing Foundation

- `ExperienceController` already exposes the public runtime endpoint family Phase 38 needs: POI experience, storyline runtime, experience events, story sessions, session events, session exit, and user exploration.
- `PublicExperienceServiceImpl` already compiles inherited POI/default flows, chapter-specific flows, overrides, unsupported gameplay labels, session updates, event idempotency, and dynamic exploration progress.
- `PublicCatalogServiceImpl` is the central published-only catalog mapper. It builds `StoryLineResponse`, `StoryChapterResponse`, `StoryContentBlockResponse`, and `StoryMediaAssetResponse`.
- `StoryMediaAssetResponse` already carries baseline fields: `assetKind`, `url`, `mimeType`, dimensions, Lottie subtype, loop/autoplay, poster/fallback ids and URLs.
- `CatalogFoundationServiceImpl.getPublishedAssetsByIds` already enforces `content_assets.status = published`, which is the correct default for public asset filtering.
- The public server currently has no `StoryMaterialPackageItem` or `StoryMaterialPackageItemVersion` entity/mapper, so public runtime cannot yet add sanitized usage hints from material package rows.
- Phase 36 introduced `story_material_package_items.current_version_id/current_version_no/last_produced_at` and `story_material_package_item_versions`.
- Existing smoke `scripts/local/smoke-phase-34-public-runtime.ps1` already covers public story runtime, story session start, idempotent event write, exploration, and session exit.

## Implementation Findings

### Public Asset DTO Enrichment

Phase 38 should enrich `StoryMediaAssetResponse` directly instead of adding a separate public asset DTO family. This avoids forcing Phase 39 to consume multiple asset shapes.

Recommended public-safe fields:

- `availability`: `available`, `fallback`, or `unsupported`
- `unavailableReason`: Traditional Chinese reason for unsupported/fallback state
- `fileSizeBytes`: copied from `content_assets.file_size_bytes`
- `durationMs`: if available from safe metadata or future asset metadata; optional in Phase 38
- `usageHint`: nested or map DTO with `materialItemKey`, `usageTarget`, `chapterCode`, `targetType`, `targetCode`, `displayRole`
- `runtimeKind`: normalized media kind for the mini-program renderer: `image`, `icon`, `audio`, `video`, `lottie`, `json`, `map_tile`, `other`
- `fallbackUsed`: boolean convenience for the client

Do not expose:

- `promptText`
- `scriptText`
- `localPath`
- `cosObjectKey`
- `providerName`
- `modelCode`
- `estimatedCost`
- `actualCost`
- `qaNote`
- version provenance JSON

### Public Material Package Read Model

The public backend needs a read-only, sanitized material usage lookup. Do not expose admin package management APIs or write paths.

Recommended read model:

- Add public server entity `StoryMaterialPackageItem` mapped to `story_material_package_items` with only fields needed for safe lookup.
- Add public server entity `StoryMaterialPackageItemVersion` mapped to `story_material_package_item_versions` with only non-secret fields needed for lifecycle filtering.
- Add mapper interfaces extending MyBatis-Plus `BaseMapper`.
- Add service helper `PublicRuntimeAssetService` or equivalent to resolve asset ids to safe `StoryMediaAssetResponse`.

Filtering rules:

- `content_assets.status` must be `published` for an asset to be `available`.
- Material item `status` must not be `draft`, `rejected`, `retry_required`, or `manual_import_required` for public usage hints.
- Material version `promotion_status` should be `published` for generated package material to be public runtime eligible.
- If material package data is missing, the asset can still be `available` if `content_assets.status = published`; it just has no usage hint.
- If an asset id exists but is filtered out, return a fallback or unsupported object instead of leaking unpublished data.

### Fallback Behavior

Backend service should decide fallbacks before the response reaches the mini-program.

Fallback order:

1. Return direct asset if `content_assets.status = published` and `canonical_url` is non-empty.
2. If direct asset is unavailable and `fallback_asset_id` points to a published asset with URL, return a response with fallback URL and `availability = fallback`.
3. If fallback is unavailable but `poster_asset_id` points to a published asset with URL, return poster URL and `availability = fallback`.
4. If no usable delivery URL exists, return a response with `availability = unsupported`, `url = ""`, and `unavailableReason = "此媒體暫時未能播放，請稍後再試。"`

The parent content block or step should remain in the runtime payload unless the chapter/flow itself is unpublished.

### Event Ingestion

`ExperienceEventRequest` is already sufficient for Phase 38 if validation is tightened:

- `eventType` should be normalized and checked against a public allowlist.
- `payloadJson` should be valid JSON if present.
- payload length should be bounded to avoid oversized writes.
- `clientEventId` should remain the idempotency key.
- `eventSource` should default to `mini_program` if missing.

Recommended allowlist:

- `story_opened`
- `chapter_started`
- `content_viewed`
- `media_completed`
- `pickup_interacted`
- `task_completed`
- `reward_acquired`
- `unsupported_viewed`
- `story_session_exit`
- Keep existing `chapter_open` accepted for backward compatibility with Phase 34 smoke.

`ExperienceEventResponse` should add:

- `duplicate`
- `acceptedAt`
- `currentChapterId`

These fields are safe and useful for client retry behavior.

### Smoke Strategy

Create a Phase 38 smoke script rather than overloading Phase 34 smoke. It can reuse Phase 34 helper patterns but should assert Phase 38-specific concerns:

- flagship story runtime loads
- at least five chapters are present
- runtime contains `StoryMediaAssetResponse` objects with `availability`
- at least one available image/audio/video asset is present after Phase 36 promotion
- Lottie-capable metadata remains present where configured
- direct public JSON does not contain banned key names
- fallback/unsupported behavior can be exercised through a safe database fixture or by checking a known placeholder item if available
- authenticated event writes for `media_completed`, `pickup_interacted`, `task_completed`, and `reward_acquired` are accepted and idempotent
- session exit remains idempotent-safe and clears temporary state without deleting permanent events

## Validation Architecture

### Automated Checks

- Public backend compile: `mvn -q -DskipTests compile -f packages/server/pom.xml`
- Focused tests after Plan 38-01: `mvn -q -Dtest=PublicRuntimeAssetServiceTest test -f packages/server/pom.xml`
- Focused tests after Plan 38-02: `mvn -q -Dtest=PublicExperienceEventServiceTest test -f packages/server/pom.xml`
- Full public runtime smoke: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-38-public-runtime-assets.ps1`

### Manual Checks

- Inspect the raw JSON from `/api/v1/storylines/{id}/runtime?locale=zh-Hant` and confirm it includes safe asset metadata but no banned admin fields.
- If local COS URLs are reachable, open at least one generated image, one audio, and one video URL from the runtime response.
- Confirm unauthenticated read-only runtime still works while unauthenticated event writes return `4010`.

## Planning Recommendations

- Plan 38-01 should implement public runtime asset sanitation, DTO enrichment, public read-only material item/version entities, and tests.
- Plan 38-02 should implement event allowlist/validation, response enrichment, session exit idempotency behavior, and tests.
- Plan 38-03 should implement smoke verification, runtime banned-field checks, requirements traceability updates, and final documentation.
- Do not add mini-program UI work to Phase 38. Phase 39 will consume the DTOs.
