---
phase: 38-public-runtime-asset-consumption
plan: 01
subsystem: api
tags: [spring-boot, mybatis-plus, public-runtime, media-assets]
requires:
  - phase: 36-material-production-pipeline-and-asset-promotion
    provides: generated and promoted material package assets
  - phase: 37-material-qa-workspace-and-reuse-controls
    provides: material lifecycle and QA status semantics
provides:
  - Traveler-safe public runtime asset DTO enrichment
  - Public-only material package read mappings
  - Centralized public asset availability, fallback, and usage-hint sanitation
affects: [phase-39-mini-program-story-consumption, public-runtime, story-media]
tech-stack:
  added: []
  patterns: [centralized-public-asset-sanitizer, public-read-only-material-readmodel]
key-files:
  created:
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/StoryMaterialPackageItem.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/StoryMaterialPackageItemVersion.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/mapper/StoryMaterialPackageItemMapper.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/mapper/StoryMaterialPackageItemVersionMapper.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/PublicRuntimeAssetService.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicRuntimeAssetServiceImpl.java
    - packages/server/src/test/java/com/aoxiaoyou/tripofmacau/PublicRuntimeAssetServiceTest.java
  modified:
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StoryMediaAssetResponse.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/CatalogFoundationService.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/CatalogFoundationServiceImpl.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java
key-decisions:
  - "Public runtime asset sanitation is centralized in PublicRuntimeAssetService instead of duplicated across catalog and experience mappers."
  - "Public material package entities intentionally map only safe lookup fields and exclude prompts, local paths, COS keys, provider data, costs, and QA provenance."
  - "Catalog story media loading can read non-published direct asset rows only so the sanitizer can resolve configured fallback/poster assets while still hiding unavailable direct media."
patterns-established:
  - "Public DTOs carry availability/fallback state so the mini-program can render degraded media without deleting story content."
  - "Public usage hints expose only stable material item context and never production provenance."
requirements-completed: [RUN-01, RUN-02]
duration: 55 min
completed: 2026-05-03
---

# Phase 38 Plan 01: Public Runtime Asset Sanitation Summary

**Traveler-safe story media DTOs now expose availability, fallback, runtime kind, and sanitized material usage hints.**

## Performance

- **Duration:** 55 min
- **Started:** 2026-05-03T05:30:00Z
- **Completed:** 2026-05-03T06:25:00Z
- **Tasks:** 4
- **Files modified:** 17

## Accomplishments

- Added public read-only material package item/version entities and mappers with only safe lookup fields.
- Extended `StoryMediaAssetResponse` with availability, fallback, runtime kind, file size, duration placeholder, and nested `UsageHint`.
- Added `PublicRuntimeAssetServiceImpl` to decide available/fallback/unsupported media and sanitize usage hints from material package rows.
- Wired story catalog and experience runtime media mapping through the sanitizer, preserving blocks/steps even when media is unsupported.
- Added focused tests for available, fallback, unsupported, Lottie metadata, safe usage hints, and rejected material filtering.

## Task Commits

1. **Runtime sanitizer implementation** - `0dd26de` (`feat`)
2. **Focused sanitizer tests** - `bcd50df` (`test`)

## Files Created/Modified

- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/StoryMaterialPackageItem.java` - public-only material item read model.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/StoryMaterialPackageItemVersion.java` - public-only material version read model.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicRuntimeAssetServiceImpl.java` - central availability/fallback/usage-hint sanitizer.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StoryMediaAssetResponse.java` - enriched public media DTO.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java` - story/chapter/block media now use the sanitizer.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java` - flow step media now use the sanitizer.
- `packages/server/src/test/java/com/aoxiaoyou/tripofmacau/PublicRuntimeAssetServiceTest.java` - focused asset privacy/fallback tests.

## Decisions Made

- Availability is computed server-side from asset status and URL, not trusted from client or admin raw data.
- Unsupported media returns a Traditional Chinese reason and an empty URL instead of removing the surrounding content block/step.
- `CatalogFoundationService.getAssetsByIds` was added so direct unpublished asset rows can still reveal configured fallback/poster ids to the sanitizer without making the direct asset public.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Updated stale unit test constructors**
- **Found during:** Task 38-01-04
- **Issue:** Maven test compilation failed because existing tests instantiated `CatalogFoundationServiceImpl`, `PublicCatalogServiceImpl`, and `PublicExperienceServiceImpl` with stale constructor arguments.
- **Fix:** Added missing mapper/service mocks and constructor arguments in the affected tests.
- **Files modified:** `CatalogFoundationServiceImplTest.java`, `PublicCatalogServiceImplCarryoverTest.java`, `PublicRewardDomainServiceTest.java`, `PublicExperienceServiceImplTest.java`, `StorylineSessionPersistenceTest.java`
- **Verification:** `mvn -q -Dtest=PublicRuntimeAssetServiceTest test -f packages/server/pom.xml`
- **Committed in:** `bcd50df`

---

**Total deviations:** 1 auto-fixed (1 blocking).
**Impact on plan:** Required only to make focused Phase 38 tests compile; no runtime scope creep.

## Issues Encountered

- `rg` is unavailable in this Codex desktop runtime due to a WindowsApps access error, so PowerShell and `git grep` were used for acceptance checks.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

Plan 38-02 can now harden event ingestion and session lifecycle responses using the enriched runtime asset DTOs as the read-side foundation.

---
*Phase: 38-public-runtime-asset-consumption*
*Completed: 2026-05-03*
