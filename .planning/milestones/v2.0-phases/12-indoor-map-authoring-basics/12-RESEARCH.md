# Phase 12 Research: Indoor Map Authoring Basics

## Objective

Research how to implement Phase 12 so the admin can author indoor buildings, floors, floor tiles, and markers against the existing brownfield stack while keeping the scope intentionally smaller than the future indoor rules engine.

## Current Starting Point

- The local MySQL schema already contains legacy indoor tables: `buildings`, `indoor_floors`, `indoor_maps`, and `indoor_nodes`.
- The current admin backend only exposes building list/create/update via `AdminIndoorController` and `AdminIndoorServiceImpl`.
- The current admin UI has:
  - `IndoorBuildingManagement.tsx` - a basic building CRUD form
  - `MapTileManagement.tsx` - a placeholder tile-config page unrelated to real indoor floor ingestion
- Phase 9 already established the canonical city / sub-map / POI hierarchy that indoor authoring must bind to.
- Phase 10 already established:
  - COS-backed asset publication
  - shared admin media picker / quick upload UX
  - central `content_assets` records and `content_relation_links` for reusable attachments
- The mini-program indoor page is still hardcoded:
  - floor arrays are defined in component state
  - tile filenames are synthesized client-side
  - floor metadata is not loaded from the backend

## What Phase 12 Must Resolve

1. Expand the brownfield indoor data model from buildings-only into buildings + floors + tile manifests + markers.
2. Align indoor bindings with the canonical city / sub-map / POI model from Phase 9.
3. Move building/floor media away from raw URL-only handling to the shared asset model from Phase 10.
4. Support tile import from two operator flows without introducing manual COS path handling.
5. Support preview-first marker CSV import and direct visual editing without shipping the full rules engine.
6. Expose enough public indoor-read data for the mini-program indoor page to consume authored data instead of local mocks.

## Recommended Architecture

### 1. Extend the Existing Indoor Tables Instead of Replacing Them

Recommended decision:
- keep `buildings`, `indoor_floors`, `indoor_maps`, and `indoor_nodes` as the canonical indoor tables
- add the missing fields needed for v2.0 rather than introducing a second indoor schema

Suggested model direction:
- `buildings`
  - canonical spatial bindings: `city_id`, `sub_map_id`, `poi_id`
  - localized names / subtitles / introductions
  - normalized coordinates and source-coordinate metadata when directly map-bound
  - `cover_asset_id`
  - popup/display metadata
- `indoor_floors`
  - localized floor labels and introduction fields
  - `floor_code`, `floor_number`, `area_sqm`
  - `cover_asset_id`
  - popup/display metadata
  - zoom min / max / default values
  - relation-driven attachments
- `indoor_maps`
  - one tile-manifest record per floor map version
  - source type: `zip` or `image-sliced`
  - source asset reference
  - width / height / tile size / grid size
  - tile root URL / manifest JSON / import status
  - zoom-derivation snapshot
- `indoor_nodes`
  - marker / overlay code and type
  - localized labels / descriptions
  - relative coordinates rather than pixel-only coordinates
  - optional linked entity metadata
  - icon / animation asset references

Why:
- this preserves brownfield compatibility
- existing admin code already knows about `buildings`
- the rest of v2.0 already favors extending legacy tables into canonical models instead of starting from zero

### 2. Reuse Phase 9 Spatial Bindings and Phase 10 Media Contracts

Recommended decision:
- indoor buildings bind to real `city`, `sub_map`, and `poi` records instead of free-form `city_code`
- cover images and attachments use `content_assets` plus shared picker components
- ordered attachments use `content_relation_links` the same way other authored domains now do

Why:
- Phase 9 already proved the canonical spatial hierarchy
- Phase 10 already proved shared asset selection/upload flows and reference tracing
- inventing a separate indoor-only media or location contract would create avoidable divergence

### 3. Tile Import Should Stay Fully Server-Side and Reuse Existing COS Storage Boundaries

Recommended decision:
- add indoor-specific import services in the admin backend that sit above the existing COS storage layer
- use two import modes:
  - zip import: validate naming, unzip, normalize tile paths, upload tiles, write manifest
  - image-slice import: read a full image, slice into tiles, upload tiles, write manifest
- do not let the browser compute final COS keys or write manifests directly

Implementation guidance:
- zip import can use Java's built-in `ZipInputStream` and explicit path sanitization
- image slicing can use `ImageIO` and `BufferedImage.getSubimage(...)`
- publish generated tiles through the same backend-owned COS key generation style already used by Phase 10 uploads
- store a machine-readable manifest JSON so the mini-program can request one floor payload and then render tiles predictably

Why:
- this avoids local operator dependence on pre-arranged COS directory conventions
- it keeps validation and path safety centralized
- it stays compatible with the current Java stack without introducing native image tooling

### 4. Zoom Bounds Should Be Derived, Then Editable

Recommended decision:
- derive default zoom bounds from:
  - floor image dimensions
  - tile size / grid dimensions
  - floor area
  - configurable default target scales stored in admin/system settings
- persist the derived result on the floor / indoor-map record
- allow operators to override per floor after auto-fill

Why:
- the user explicitly asked for auto-filled upper/lower limits with default target scales
- operators still need manual override for unusual floor shapes or display goals
- keeping both the derived snapshot and the current editable values makes the result explainable

Recommended formula direction:
- compute pixels-per-meter from image dimensions and area
- translate target scale limits into usable min/max zoom values for the mobile renderer
- preserve the derived inputs in JSON for future recalculation when system defaults change

### 5. CSV Marker Import Should Be Preview-Then-Confirm, and Parsing Should Be Server-Validated

Recommended decision:
- implement two-step endpoints:
  - preview endpoint: upload CSV, parse rows, validate schema and linked IDs, return warnings/errors plus normalized rows
  - confirm endpoint: persist only the accepted preview batch
- use the backend as the source of truth for validation

Why:
- it prevents UI-only CSV parsing drift
- linked entities such as POIs / tasks / collectibles can be validated against the database
- the user's requirement explicitly says "show what will be added, then write only after confirmation"

Validation categories:
- required columns present
- code uniqueness in the batch and against existing floor nodes
- relative-coordinate range or pixel-coordinate convertibility
- allowed marker/overlay types
- linked entity IDs exist when provided
- icon/media references exist when provided

### 6. Direct Visual Editing Can Stay Lightweight in v2.0

Recommended decision:
- implement the direct editor as a thumbnail/minimap image with overlays and click-to-pick relative coordinates
- keep the data model normalized around relative positions (`0..1` or percentage) rather than screen pixels
- pair the visual picker with a structured side-panel form for metadata

Why:
- this satisfies the user's minimap-assisted editing requirement
- it avoids introducing a heavyweight drawing/canvas framework before it is necessary
- relative positions survive re-exported map dimensions better than absolute pixels

### 7. Public Indoor Read APIs Are Part of the Phase if the Mini-Program Is Expected to Stop Mocking

Recommended decision:
- add public read endpoints in `packages/server` for:
  - indoor building list/detail
  - floor list/detail
  - floor tile manifest
  - floor markers
- update the mini-program indoor page to fetch real building/floor/tile/marker data and treat existing local manifest assets as fallback only during rollout

Why:
- the user's global direction is to remove mock-driven runtime behavior
- Phase 12 would remain half-finished if indoor authoring could not reach the mini-program
- the current indoor page is one of the last obvious mock-driven runtime surfaces

### 8. Defer the Full Rules Engine, but Leave Clean Attachment Points

Recommended decision:
- allow markers / overlays to optionally store:
  - linked task/activity/collectible/badge IDs
  - a small `metadata_json` payload for future extensibility
- do not implement time-window triggers, user-state appearance rules, or chained effect execution in Phase 12

Why:
- the user explicitly asked for those advanced behaviors eventually
- Phase 12 goal is "authoring basics"
- storing clean linkage hooks now reduces rework later without overcommitting this phase

## Recommended Phase Output Shape

Phase 12 should deliver:
- extended indoor schema and admin CRUD for buildings and floors
- building/floor media reuse via the shared media library
- backend tile import for zip and full-image slicing
- derived and editable zoom settings
- preview-first CSV marker import plus direct visual marker editing
- public indoor read APIs and mini-program indoor runtime alignment

Phase 12 should not yet deliver:
- full indoor trigger/effect rules
- complex path animation authoring
- AR calibration pipelines
- generalized mission logic inside indoor overlays

## Key Risks and Mitigations

### Archive Traversal or Invalid Tile Pack Content

Risk:
- zip uploads can contain unexpected paths, duplicate tiles, or malformed naming

Mitigation:
- reject absolute paths and `..` traversal
- normalize entries server-side
- require a predictable tile naming contract and preview/validation result before publish

### Full-Image Slicing Causes Memory Pressure

Risk:
- large floor images can exhaust heap or cause slow import behavior

Mitigation:
- enforce input size limits
- read dimensions early
- cap supported image sizes
- slice iteratively and fail clearly on oversized imports

### Indoor Authoring Forks Away from Canonical Media/Spatial Models

Risk:
- indoor pages might keep raw `cityCode` and `coverImageUrl` patterns forever

Mitigation:
- route all new work through Phase 9 canonical bindings and Phase 10 shared media patterns

### CSV Import Writes Bad Data Blindly

Risk:
- operators could accidentally create duplicate or broken markers at scale

Mitigation:
- preview-validate-confirm flow with row-level diagnostics and idempotent confirmation tokens

### Mini-Program Stays Mock-Driven After Admin Authoring Exists

Risk:
- Phase 12 ships admin write capability but no runtime consumption

Mitigation:
- treat public indoor-read APIs and client indoor-page cutover as part of the plan, not as a stretch goal

## Validation Architecture

- Admin backend tests should cover:
  - indoor building/floor validation
  - zip import sanitization
  - image slicing and manifest generation
  - CSV preview validation and confirmation
- Admin UI verification should cover:
  - building/floor authoring flows
  - tile import UX
  - minimap coordinate picking
  - marker CSV preview and confirmation
- Public/client verification should cover:
  - real indoor detail payloads from `packages/server`
  - mini-program indoor page rendering a live floor/tile/marker payload

## Sources

### Local codebase references

- `scripts/local/mysql/init/01-init.sql`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminIndoorController.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminIndoorServiceImpl.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/Building.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/MediaIntakeService.java`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/IndoorBuildingManagement.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/MapTileManagement.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetPickerField.tsx`
- `packages/client/src/pages/map/indoor/index.tsx`
- `.planning/phases/09-spatial-model-rebuild/09-03-SUMMARY.md`
- `.planning/phases/10-media-asset-pipeline-and-library/10-03-SUMMARY.md`

- The phase needs a Wave 0-quality smoke path that proves admin write -> MySQL/COS persistence -> public indoor read -> mini-program consumption.
- The plan should keep zip/image import and CSV preview verification explicitly automated wherever possible because they are high-regression surfaces.
