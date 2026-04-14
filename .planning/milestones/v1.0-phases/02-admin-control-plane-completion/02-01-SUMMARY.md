---
phase: 02-admin-control-plane-completion
plan: 01
subsystem: admin-backend
tags: [schema-alignment, cities, pois, storylines, rewards]
requires: [01]
provides:
  - canonical admin entity and service mappings for city, POI, storyline, chapter, and reward domains
  - local brownfield SQL alignment for legacy city, POI, storyline, chapter, and reward tables
  - backfilled canonical data visible to live admin APIs instead of empty or broken result sets
affects: [phase-03-public-read-apis-cutover, phase-06-migration-cutover-and-hardening]
tech-stack:
  added:
    - scripts/local/mysql/init/04-admin-control-plane-alignment.sql
    - scripts/local/mysql/init/05-admin-domain-alignment.sql
  patterns:
    - compatibility-first schema alignment without deleting legacy columns
    - canonical backfill from legacy brownfield tables before public API cutover
key-files:
  created:
    - scripts/local/mysql/init/04-admin-control-plane-alignment.sql
    - scripts/local/mysql/init/05-admin-domain-alignment.sql
  modified:
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/City.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/Poi.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/StoryLine.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/StoryChapter.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/Reward.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminCityServiceImpl.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminPoiServiceImpl.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminStoryLineServiceImpl.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminStoryChapterServiceImpl.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminSystemManagementServiceImpl.java
key-decisions:
  - "Keep legacy brownfield columns in place locally while adding the canonical Phase 1 columns required by the admin APIs."
  - "Backfill canonical `storylines` rows from legacy `story_lines` using preserved IDs so POI and chapter relationships remain stable."
  - "Align reward, city, POI, and chapter tables in MySQL instead of teaching new admin services to speak legacy schemas."
requirements-completed: [ADM-01]
completed: 2026-04-12
---

# Phase 2: Plan 01 Summary

**Canonicalized the existing admin map, story, chapter, POI, and reward stack against the live MySQL schema and repaired the local brownfield database so the APIs can run for real.**

## Accomplishments

- Updated the admin backend entity, DTO, and service mappings for city, POI, storyline, chapter, and reward domains to use the canonical Phase 1 column contract.
- Added local SQL alignment scripts that upgrade legacy `rewards`, `cities`, `pois`, and `story_chapters` tables in place and backfill canonical values without deleting the old brownfield columns.
- Migrated legacy `story_lines` data into canonical `storylines`, preserving IDs so cross-domain references stay coherent.
- Restored live admin reads for map/story/reward surfaces: local smoke now returns 3 cities, 4 POIs, 2 storylines, 0 existing chapters, and 3 rewards instead of SQL errors or empty canonical tables.

## Files Created/Modified

- `scripts/local/mysql/init/04-admin-control-plane-alignment.sql` - local reward-table canonicalization and reward data backfill.
- `scripts/local/mysql/init/05-admin-domain-alignment.sql` - local city/storyline/POI/chapter alignment and legacy-to-canonical backfill.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/City.java` - canonical city field mapping.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/Poi.java` - canonical POI field mapping.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/StoryLine.java` - canonical storyline field mapping.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/StoryChapter.java` - canonical chapter field mapping.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/Reward.java` - canonical reward field mapping.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminCityServiceImpl.java` - city CRUD and publish semantics against canonical fields.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminPoiServiceImpl.java` - canonical POI list/detail/create/update behavior.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminStoryLineServiceImpl.java` - canonical storyline CRUD and chapter counting.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminStoryChapterServiceImpl.java` - canonical chapter CRUD.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminSystemManagementServiceImpl.java` - canonical reward behavior after local schema alignment.

## Issues Encountered

- The local reward and POI tables were still on older schemas, which caused real `/api/admin/v1/system/rewards` and `/api/admin/v1/pois` requests to fail with unknown-column errors.
- The canonical `storylines` table existed but was empty locally because older content still lived in `story_lines`.

## Resolution

- Repaired the local database with migration-safe alignment scripts instead of reverting the code back to legacy fields.
- Backfilled canonical rows from the old tables so the admin backend now reads from the same schema family that later public API phases will consume.

## Verification

All plan-level checks passed after the SQL alignment work.

- `GET /api/admin/v1/map/cities?pageNum=1&pageSize=5` -> `code=0`, `total=3`
- `GET /api/admin/v1/pois?pageNum=1&pageSize=5` -> `code=0`, `total=4`
- `GET /api/admin/v1/storylines?pageNum=1&pageSize=5` -> `code=0`, `total=2`
- `GET /api/admin/v1/system/rewards?pageNum=1&pageSize=5` -> `code=0`, `total=3`

## Next Phase Readiness

- Phase 2 Plan 02 could extend the now-working canonical control plane to the missing runtime/content domains.
- Phase 3 can read real city, POI, storyline, chapter, and reward rows from the canonical MySQL side instead of legacy-only tables.

---
*Phase: 02-admin-control-plane-completion*
*Completed: 2026-04-12*
