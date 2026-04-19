# Phase 9: Spatial Model Rebuild - Context

**Gathered:** 2026-04-14
**Status:** Approved plan, ready for execution
**Source:** Milestone v2.0 requirements, the user's spatial-authoring brief, current brownfield admin/public/client inspection, and official AMap capability references

<domain>
## Phase Boundary

Phase 9 rebuilds the live spatial model around top-level cities, first-class sub-maps, coordinate normalization, and richer POI authoring that the admin can manage and the mini-program can consume.

This phase owns:
- top-level switchable city modeling for the traveler runtime
- first-class sub-map modeling beneath a city
- coordinate source metadata plus normalized AMap-compatible storage
- spatial-domain popup/display settings for cities, sub-maps, and POIs
- spatial-domain multi-asset attachment modeling for cities, sub-maps, and POIs
- POI redesign around city/sub-map binding, cover asset, map icon, and location metadata
- seed and migration alignment for the initial city/sub-map hierarchy
- public runtime and mini-program alignment for the rebuilt spatial hierarchy

This phase does not yet own:
- richer upload intake modes, lossless-policy UX, and media-library ergonomics beyond the current content-asset pipeline
- canonical storyline-to-POI many-to-many composition as the authoritative relationship source
- indoor building/floor/tile authoring
- exploration-progress recomputation and user-operations dashboards
</domain>

<decisions>
## Locked Decisions

- A "city" means a top-level switchable traveler region. The initial seeded city set should be `macau`, `hengqin`, `hong-kong`, and `ecnu`.
- `taipa` and `coloane` stop being top-level cities and become sub-maps under `macau`; `macau-peninsula` becomes an explicit Macau sub-map as well.
- Sub-maps are a first-class table beneath cities, not a polymorphic graph and not a reuse of indoor-map tables in Phase 9.
- City, sub-map, and POI display fields remain explicit four-locale columns in the same brownfield style established in Phase 8.
- City, sub-map, and POI each require a cover asset and may own ordered attachment collections plus popup/display configuration blocks.
- Attachment composition for spatial domains lands in Phase 9 through canonical relation records, but richer upload channels, clipboard/folder intake, compression-policy UX, and central media-library workflows remain Phase 10 work.
- Coordinate storage must preserve raw input latitude/longitude plus source coordinate-system metadata and normalized GCJ-02 values for AMap and mini-program runtime use.
- Coordinate normalization is backend-owned and deterministic. Operators may choose or confirm the source system, and the backend must never silently discard the raw input coordinates.
- Metadata suggestion for new cities and sub-maps is operator-triggered and editable. The system should use curated defaults plus map/geocoding suggestions rather than uncontrolled web scraping during save.
- POIs bind to one city and optionally one sub-map. Direct storyline ownership is no longer authored from the POI form in Phase 9; legacy storyline data remains compatibility-only until Phase 11 introduces canonical story composition.
- Public runtime responses keep backward-compatible `latitude` / `longitude` semantics by exposing normalized GCJ-02 values there, while also adding explicit source/raw metadata for richer consumers.
- The mini-program map flow must consume the new spatial hierarchy without regressing the authenticated public-data flow delivered in Phases 7 and 8.
</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Milestone and upstream decisions
- `.planning/ROADMAP.md` - Phase 9 goal, requirements, success criteria, and dependency on Phase 8
- `.planning/REQUIREMENTS.md` - `MAP-01` through `MAP-04`
- `.planning/phases/08-multilingual-authoring-foundation/08-CONTEXT.md` - four-locale storage and authoring decisions the spatial model must reuse
- `.planning/phases/08-multilingual-authoring-foundation/08-RESEARCH.md` - translation/settings patterns and explicit-column multilingual guidance
- `.planning/phases/08-multilingual-authoring-foundation/08-VALIDATION.md` - validation contract style to extend for spatial work

### Current admin backend spatial surfaces
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/City.java` - current city entity shape
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/Poi.java` - current POI entity shape
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminMapController.java` - current city-only map admin routes
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminCityServiceImpl.java` - current city CRUD/service behavior
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminPoiServiceImpl.java` - current POI CRUD/service behavior and legacy single-storyline coupling

### Current admin UI spatial surfaces
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/CityManagement.tsx` - current city form/table UX
- `packages/admin/aoxiaoyou-admin-ui/src/pages/POIManagement/index.tsx` - current POI authoring UX
- `packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts` - current admin DTO surface
- `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts` - current admin API bindings

### Public/backend runtime surfaces
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/City.java` - public city storage shape
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/Poi.java` - public POI storage shape
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/CatalogFoundationServiceImpl.java` - current published content fetch path
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java` - current city/POI public response assembly
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/CityController.java` - public city endpoint
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/PoiController.java` - public POI endpoint

### Mini-program runtime surfaces
- `packages/client/src/services/api.ts` - public DTOs currently exposed to the client
- `packages/client/src/services/gameService.ts` - current city/POI mapping, GCJ-02 assumptions, and pseudo-city runtime logic
- `packages/client/src/pages/map/index.tsx` - current traveler map UI and city switching behavior
- `packages/client/src/types/game.ts` - current runtime types already assuming GCJ-02 POI coordinates

### Schema foundations
- `scripts/local/mysql/init/02-live-backend-foundation.sql` - canonical MySQL schema for cities/POIs/content assets
- `scripts/local/mysql/init/04-admin-control-plane-alignment.sql` - admin alignment migration pattern
- `scripts/local/mysql/init/06-live-backend-mock-migration.sql` - current seed/backfill approach to extend for spatial migration

### External capability references
- `https://amap.apifox.cn/` - official AMap Web Service API overview
- `https://amap.apifox.cn/api-14546468` - official AMap geocoding API reference for operator-triggered metadata suggestions
- `https://amap.apifox.cn/api-14639804` - official AMap AOI boundary query reference for optional sub-map suggestion enrichment
</canonical_refs>

<specifics>
## Specific Ideas

- Add a reusable admin coordinate field group with source-system select, raw-vs-normalized preview, and operator-triggered "suggest metadata" actions.
- Rebuild city management into a city workspace with nested sub-map management rather than leaving sub-maps implicit or using unrelated indoor pages.
- Use a canonical asset-link model so city/sub-map/POI attachments can be ordered now and reused by later phases.
- Keep `cities.center_lat / center_lng` and `pois.latitude / longitude` as normalized GCJ-02 compatibility fields so the mini-program does not break while richer coordinate metadata is added.
- Migrate the current "Taipa" and "Coloane" runtime assumptions out of the top-level city switcher and into a Macau sub-map layer.
</specifics>

<deferred>
## Deferred Ideas

- Folder picker, drag/drop, clipboard paste, and lossless-upload policy UX in the admin
- Canonical storyline-to-POI reverse relationship authoring and chapter composition ownership
- Indoor map/floor/tile/overlay authoring
- User progress recomputation and traveler operations views
</deferred>

---

*Phase: 09-spatial-model-rebuild*
