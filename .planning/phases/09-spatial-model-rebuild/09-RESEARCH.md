# Phase 9 Research: Spatial Model Rebuild

## Objective

Research how to implement Phase 9 so the admin can author cities, sub-maps, normalized coordinates, and POIs through a richer spatial hierarchy while the public backend and mini-program continue to serve live data reliably.

## Current Starting Point

- `cities` already exists, but it only models top-level regions and stores one center point plus cover/banner assets.
- There is no first-class `sub_maps` table or admin/public API for map areas beneath a city.
- `pois` currently bind to `city_id` and an optional single `storyline_id`, but they do not bind to a sub-map, do not have a dedicated map-icon asset, and do not carry popup/display configuration.
- Spatial coordinates are stored as bare `latitude` / `longitude` or `center_lat` / `center_lng` only; there is no source coordinate-system metadata and no preserved raw input.
- The mini-program runtime already assumes POI coordinates are GCJ-02-compatible:
  - `packages/client/src/types/game.ts` has `gcj02Latitude` / `gcj02Longitude`
  - `packages/client/src/services/gameService.ts` maps public `latitude` / `longitude` straight into those GCJ-02 fields
  - `packages/client/src/pages/map/index.tsx` uses `Taro.getLocation({ type: 'gcj02' })`
- The current runtime still treats `taipa` and `coloane` as top-level "cities", which conflicts with the user's desired hierarchy.
- There is no canonical attachment-relation table for spatial entities yet; cities and POIs only point at a few single asset IDs.

## What Phase 9 Must Resolve

1. Introduce a real city -> sub-map hierarchy without destabilizing current public reads.
2. Preserve source coordinate information while normalizing everything needed by AMap and the mini-program to GCJ-02.
3. Give operators a practical admin authoring flow for cities, sub-maps, and POIs instead of flat legacy forms.
4. Add spatial attachment and popup/display composition now, while avoiding overlap with Phase 10's richer upload/media-library work.
5. Re-seed and migrate the current spatial catalog so Macau, Hengqin, Hong Kong, and ECNU are top-level switchable cities, and Macau Peninsula / Taipa / Coloane are Macau sub-maps.

## Recommended Architecture

### 1. Use an Explicit `cities + sub_maps + pois` Hierarchy, Not a Generic Spatial Graph

Recommended decision:
- keep `cities` as the top-level traveler-switchable unit
- add a dedicated `sub_maps` table keyed by `city_id`
- add nullable `sub_map_id` to `pois`
- do not introduce a generic self-referential tree or polymorphic spatial node system in Phase 9

Why this is the lowest-risk brownfield move:
- current public/admin code, SQL, and runtime logic are already city-centric
- the user only requires one extra managed layer beneath cities in v2.0
- a dedicated `sub_maps` table keeps queries, validation, and authoring UX straightforward
- later indoor authoring can bind to city/sub-map/POI without Phase 9 prematurely over-generalizing the model

Recommended core `sub_maps` fields:
- `id`, `city_id`, `code`
- `name_zh`, `name_en`, `name_zht`, `name_pt`
- `subtitle_*`, `description_*`
- `cover_asset_id`
- center coordinate raw/source/normalized fields
- `bounds_json` or `viewport_json` for optional initial map framing
- `popup_config_json`, `display_config_json`
- `sort_order`, `status`, `published_at`

### 2. Preserve Legacy Normalized Columns and Add Raw/Source Metadata Beside Them

Recommended decision:
- keep existing public compatibility columns as normalized GCJ-02 values
- add new raw/source metadata columns instead of renaming or removing the existing normalized columns

Recommended pattern:
- `cities.center_lat` / `center_lng`: normalized GCJ-02 values used by current public runtime
- add `cities.source_center_lat`, `cities.source_center_lng`, `cities.source_coordinate_system`
- `pois.latitude` / `longitude`: normalized GCJ-02 values used by current public runtime
- add `pois.source_latitude`, `pois.source_longitude`, `pois.source_coordinate_system`
- add normalization metadata such as `normalization_status`, `normalization_note`, and `normalized_at`
- build `sub_maps` with the same raw/source/normalized pattern from day one

Why this matters:
- the mini-program already treats public `latitude` / `longitude` as GCJ-02 and would otherwise regress
- operators still need to see what they entered originally
- migration can backfill all current data as `source_coordinate_system = GCJ02` with no public contract break

### 3. Use a Local Coordinate-Normalization Utility for Save-Time Determinism

Recommended decision:
- normalize coordinates in backend code with a deterministic utility for `WGS84 -> GCJ02`, `BD09 -> GCJ02`, and `GCJ02 -> GCJ02`
- do not make content save success depend on a remote coordinate-conversion API

Why this is preferable:
- save-time normalization must be available locally and in tests
- external API outages should not block operator save flows
- the user needs automatic AMap-compatible coordinates, not necessarily a remote conversion dependency

Recommended admin behavior:
- operator chooses or confirms the source coordinate system from `GCJ02`, `WGS84`, `BD09`, `UNKNOWN`
- the backend normalizes and returns both raw and GCJ-02 preview values
- if the source system is `UNKNOWN`, the backend may offer a warning plus a best-effort guess, but it must not silently overwrite without surfacing what happened

### 4. Use Operator-Triggered Metadata Suggestions, Not Uncontrolled Web Scraping

The user asked for the system to help fill country, center coordinates, and related metadata when creating new cities or sub-maps.

Recommended decision:
- implement a suggestion endpoint that uses:
  - curated built-in templates for the initial expected regions (`macau`, `hengqin`, `hong-kong`, `ecnu`)
  - AMap geocoding and optional AOI lookup for place-name/address suggestions when a key is configured
- keep operator edits authoritative and never auto-save suggestion results
- do not introduce uncontrolled generic web crawling in Phase 9

Why:
- AMap already provides structured geocoding and AOI data that are a better fit than scraping arbitrary sites
- operator-triggered suggestions keep the admin authoritative and auditable
- this stays within predictable latency and failure behavior

Relevant official AMap references:
- API overview: [高德 Web 服务 API 概述](https://amap.apifox.cn/)
- geocoding: [地理编码](https://amap.apifox.cn/api-14546468)
- AOI lookup: [AOI 边界查询](https://amap.apifox.cn/api-14639804)

### 5. Introduce Canonical Spatial Asset Links Now, Then Let Phase 10 Expand the Upload Experience

Phase 9 requirements already demand multi-asset attachments on cities, sub-maps, and POIs. Phase 10 later expands upload UX and media-library operations.

Recommended decision:
- add one canonical relation table now, for example `content_asset_links`, with fields like:
  - `entity_type`
  - `entity_id`
  - `usage_type`
  - `asset_id`
  - `sort_order`
  - optional localized captions/descriptions
  - `display_config_json`
  - `status`
- use it for city/sub-map/POI attachment ordering in Phase 9
- keep upload entry modes and processing-policy UX out of this phase

Why this is the right boundary:
- MAP requirements cannot wait for isolated raw URL fields only
- Phase 10 can reuse the same link model instead of inventing another attachment structure later
- admin forms can stop relying on a few hardcoded single asset IDs for everything except required hero/map-icon fields

### 6. Rebuild Admin UX Around Hierarchy-Aware Authoring

Current issues:
- city authoring is a flat table/form with no sub-map layer
- POI authoring still exposes a direct storyline select and raw numeric asset IDs
- there is no coordinate-source selection or normalized preview

Recommended admin UX shape:
- city workspace:
  - city table/cards
  - expandable sub-map list beneath each city
  - city form with localized fields, cover asset, metadata suggestion, coordinate preview, popup/display settings, and ordered attachments
- sub-map form:
  - localized fields, cover asset, coordinate preview, popup/display settings, ordered attachments
- POI form:
  - bind to city and optional sub-map
  - localized fields
  - dedicated map-icon asset
  - cover asset plus ordered attachments
  - popup/display settings
  - coordinate source/raw/normalized panel
  - remove direct storyline selection from the primary authoring flow

This does not require Phase 10's full drag/drop intake to still be a major usability improvement over numeric asset IDs.

### 7. Public Runtime and Mini-Program Alignment Must Happen in the Same Phase

Recommended decision:
- add public `sub-map` responses and filtering support now
- extend public city/POI responses with sub-map and coordinate metadata
- keep `latitude` / `longitude` normalized GCJ-02 for client compatibility

Recommended public-contract shape:
- `GET /api/v1/cities` remains, still lightweight
- add `GET /api/v1/sub-maps?cityCode=...`
- extend `GET /api/v1/pois` with `subMapCode` filter and add `subMapId`, `subMapCode`, `subMapName`, `mapIconUrl`, `sourceCoordinateSystem`, `sourceLatitude`, `sourceLongitude`
- retain normalized `latitude` / `longitude` for existing map logic

Recommended client/runtime work:
- `gameService` should load sub-maps as a first-class catalog
- top-level city switcher should stop showing `taipa` and `coloane` as cities
- Macau sub-map selection can become a secondary filter/switch under the chosen city
- legacy local state using `taipa` / `coloane` as `currentCityId` should migrate to `macau` with a derived current sub-map hint if possible

### 8. Seed and Migration Need to Reshape Existing Spatial Data, Not Just Append New Rows

Recommended initial seed target:
- top-level cities:
  - `macau`
  - `hengqin`
  - `hong-kong`
  - `ecnu`
- Macau sub-maps:
  - `macau-peninsula`
  - `taipa`
  - `coloane`

Recommended migration behavior:
- current `taipa` / `coloane` top-level city rows are migrated into sub-map rows under `macau`
- current POIs are assigned `sub_map_id` where deterministically known
- current user state pointing at legacy pseudo-city codes is remapped safely
- current public runtime keeps working even if some POIs temporarily have `sub_map_id = null`

## Recommended Phase Output Shape

Phase 9 should deliver:
- canonical `sub_maps` data model and admin/public CRUD/read flow
- raw/source + normalized GCJ-02 coordinate persistence for city/sub-map/POI
- a coordinate normalization utility and operator-triggered metadata suggestion flow
- canonical spatial asset-linking for city/sub-map/POI attachments
- rebuilt admin city/sub-map/POI authoring UX
- public/client spatial hierarchy alignment plus seed/backfill proof

Phase 9 should not yet deliver:
- drag/drop/folder/clipboard upload intake
- full media-library search ergonomics beyond what already exists
- storyline composition redesign
- indoor floor/tile/overlay authoring

## Key Risks and Mitigations

### Migration Drift Between Legacy Cities and New Sub-Maps

Risk:
- current data and client state still treat `taipa` / `coloane` as cities

Mitigation:
- explicit migration mapping
- compatibility fallback to `macau`
- smoke proof covering legacy state migration plus public reads

### Coordinate-System Ambiguity

Risk:
- operators may not know whether their input is GCJ-02, WGS84, or BD-09

Mitigation:
- explicit source-system field
- preview raw vs normalized values
- warnings on `UNKNOWN`
- preserve raw values even when normalization succeeds

### Asset-Link Model Overlap With Phase 10

Risk:
- Phase 9 could duplicate Phase 10's attachment/media design

Mitigation:
- Phase 9 introduces only the canonical relation structure needed for spatial domains
- Phase 10 focuses on upload channels, permissions, compression policy UX, and global library behavior

### Public Runtime Breakage

Risk:
- changing city hierarchy or coordinate semantics could break the map page

Mitigation:
- keep normalized `latitude` / `longitude` in public DTOs
- add sub-map support without removing existing city filters
- run client build plus end-to-end smoke before sign-off

## Recommendation Summary

- Rebuild the spatial model with an explicit `cities -> sub_maps -> pois` hierarchy.
- Keep existing public coordinate fields as normalized GCJ-02 values, and add raw/source metadata beside them.
- Normalize coordinates locally in backend code; use AMap APIs for suggestions, not for mandatory save-time conversion.
- Introduce canonical spatial asset links now so city/sub-map/POI attachments become real data instead of ad hoc fields.
- Reseed the top-level traveler regions to `macau`, `hengqin`, `hong-kong`, and `ecnu`, with Taipa/Coloane/Macau Peninsula under Macau.

## Validation Architecture

- Backend verification should add targeted tests for:
  - city/sub-map/POI schema mapping and raw-vs-normalized coordinate persistence
  - coordinate normalization for `GCJ02`, `WGS84`, and `BD09`
  - admin sub-map CRUD and asset-link validation
  - public city/sub-map/POI read behavior and filtering
- Admin UI verification should include build-level proof and targeted behavior checks for:
  - city/sub-map hierarchy rendering
  - coordinate preview behavior
  - POI sub-map binding and map-icon selection
- End-to-end smoke for this phase should prove:
  - operator creates or edits a city and sub-map
  - operator saves a POI with raw/source coordinates and sees normalized output
  - public APIs return the normalized hierarchy correctly
  - mini-program build and runtime mapping still work with the new city/sub-map structure

## Sources

### Local codebase references

- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/City.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/Poi.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminCityServiceImpl.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminPoiServiceImpl.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/CatalogFoundationServiceImpl.java`
- `packages/client/src/services/api.ts`
- `packages/client/src/services/gameService.ts`
- `packages/client/src/pages/map/index.tsx`
- `scripts/local/mysql/init/02-live-backend-foundation.sql`
- `scripts/local/mysql/init/06-live-backend-mock-migration.sql`

### External references

- [高德 Web 服务 API 概述](https://amap.apifox.cn/)
- [高德地理编码 API](https://amap.apifox.cn/api-14546468)
- [高德 AOI 边界查询 API](https://amap.apifox.cn/api-14639804)
