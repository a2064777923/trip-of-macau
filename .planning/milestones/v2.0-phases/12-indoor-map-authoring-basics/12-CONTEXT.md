# Phase 12: Indoor Map Authoring Basics - Context

**Gathered:** 2026-04-14
**Status:** Ready for planning
**Source:** Milestone v2.0 requirements, the user's locked indoor-authoring directives, current indoor code inspection, and upstream spatial/media phase outputs

<domain>
## Phase Boundary

Phase 12 turns the current placeholder indoor-building baseline into a real authoring and runtime chain for indoor buildings, floors, tile maps, and floor markers.

This phase owns:
- indoor-building authoring bound to city/sub-map or POI
- localized building and floor metadata in the same four-language model used by the rest of v2.0
- cover/media attachment support for buildings and floors using the shared media library from Phase 10
- floor-level tile ingestion from either prepared zip packages or a single full floor image
- backend-side tile slicing, path organization, COS publication, and tile manifest persistence
- floor zoom-bound calculation and editable overrides
- marker/overlay authoring through CSV preview validation and direct minimap-assisted editing
- the minimum public runtime contract needed for the mini-program indoor page to stop depending on local-only mock manifests

This phase does not own:
- the full conditional appearance / trigger / effect rules engine described by the user for future indoor overlays
- full AR indoor positioning accuracy work beyond preserving the current client shell and making authored floor data available to it
- system-settings UX redesign as a broad control-plane initiative, though this phase may add the specific indoor zoom-default keys it needs
- generalized campaign / mission orchestration for indoor overlays beyond storing lightweight linkage metadata for later phases
</domain>

<decisions>
## Locked Decisions

- Phase 12 builds on the existing brownfield indoor tables (`buildings`, `indoor_floors`, `indoor_maps`, `indoor_nodes`) instead of replacing them with a greenfield subsystem.
- Indoor buildings must be bindable either to a city/sub-map pair or to a POI. If a building is bound to a POI, standalone latitude/longitude becomes optional; if it is bound directly to city/sub-map, latitude/longitude remains required.
- Buildings and floors must follow the same canonical multilingual storage direction as the rest of v2.0: `zh-Hans`, `zh-Hant`, `en`, and `pt`.
- Building and floor cover images plus other media attachments must use the shared media-library / COS flow from Phase 10 rather than isolated raw URL fields as the canonical source of truth.
- Tile import must support both:
  - a prepared zip package of numbered tiles
  - a single floor image uploaded to the backend and sliced server-side into tiles
- COS object keys, tile directories, and manifest paths remain backend-generated. Operators do not control final COS layout directly.
- Floor zoom bounds must be auto-derived from floor area plus map dimensions, using configurable default target scales equivalent to a default maximum of 50 centimeters and minimum of 20 meters, while still allowing per-floor override.
- CSV marker import must be preview-first: validate, show parsed rows and errors, and only write after explicit confirmation.
- Direct marker editing must use minimap-assisted relative coordinates rather than requiring operators to hand-enter raw pixel coordinates only.
- If a marker or overlay has no custom image or GIF, the runtime fallback should remain a simple red-dot style marker.
- The future rules engine for overlay appearance, trigger conditions, and effects is deferred. Phase 12 may store lightweight relation metadata such as linked task/activity/collectible/badge IDs, but it must not try to deliver the full conditional runtime system yet.
- The mini-program indoor page should be able to read authored indoor floors, tiles, and markers from real backend data once this phase is complete; it should no longer depend only on the checked-in `cosAssetManifest` and hardcoded floor arrays.
</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Milestone and upstream phase decisions
- `.planning/ROADMAP.md` - Phase 12 goal, requirements, success criteria, and dependencies
- `.planning/REQUIREMENTS.md` - `INDO-01` through `INDO-04`
- `.planning/STATE.md` - current milestone state
- `.planning/phases/09-spatial-model-rebuild/09-CONTEXT.md` - canonical city / sub-map / POI model that indoor bindings must align to
- `.planning/phases/09-spatial-model-rebuild/09-03-SUMMARY.md` - verified spatial hierarchy and live public alignment
- `.planning/phases/10-media-asset-pipeline-and-library/10-CONTEXT.md` - shared asset-library and upload-policy decisions
- `.planning/phases/10-media-asset-pipeline-and-library/10-03-SUMMARY.md` - verified shared media picker and COS-backed admin upload flow
- `.planning/phases/11-story-activity-and-collection-composition/11-01-PLAN.md` - current composition-plan style and relation-link usage patterns

### Current indoor backend and schema baseline
- `scripts/local/mysql/init/01-init.sql` - legacy indoor and map-tile table definitions already in local MySQL
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminIndoorController.java` - current indoor admin entry surface
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminIndoorServiceImpl.java` - current building-only indoor service
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/Building.java` - current building entity baseline
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/MapTileConfig.java` - current outdoor tile config pattern that informs indoor tile metadata

### Current admin UI surfaces
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/IndoorBuildingManagement.tsx` - current indoor-building placeholder CRUD page
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/MapTileManagement.tsx` - current tile-management placeholder page
- `packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetPickerField.tsx` - Phase 10 shared media-picker primitive
- `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts` - current indoor and media API contracts
- `packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts` - admin DTO contract baseline

### Current mini-program indoor runtime
- `packages/client/src/pages/map/indoor/index.tsx` - current hardcoded indoor map runtime that must be aligned to live data
- `packages/client/src/constants/assetUrls.ts` - current asset-manifest indirection patterns

### Current media/upload implementation
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/MediaIntakeService.java` - existing upload processing and COS publication boundary
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/CosAssetStorageService.java` - existing backend-owned COS key generation/storage
</canonical_refs>

<specifics>
## Specific Ideas

- Indoor-building editing should likely expand into a master-detail flow: building list -> building drawer/page -> floor list/editor -> marker editor.
- Floor media attachments should reuse the existing content-asset relation pattern instead of inventing separate attachment tables.
- Tile ingestion should persist both the authored source asset reference and the generated tile-manifest metadata so operators can re-import cleanly later.
- Marker editing should support both a tabular data grid and a visual picker; the visual picker can remain a lightweight image-overlay editor for v2.0.
- CSV import should validate column names, coordinate ranges, marker type enums, linked entity IDs (when present), and duplicate marker codes before confirmation.
</specifics>

<deferred>
## Deferred Ideas

- full overlay appearance rules, trigger rules, reward effects, and conditional sequencing
- path-drawn movement animations for overlays
- voice / shout / drag gesture trigger configuration
- full indoor AR positioning and calibration workflows
- advanced GIS editing tools or polygon/route authoring inside floor editors
</deferred>

---

*Phase: 12-indoor-map-authoring-basics*
*Context gathered: 2026-04-14*
