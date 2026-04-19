# Phase 10: Media Asset Pipeline and Library - Context

**Gathered:** 2026-04-14
**Status:** Ready for planning
**Source:** Milestone v2.0 requirements, Phase 9 outputs, current media/upload code inspection, and the user's latest media-pipeline directives

<domain>
## Phase Boundary

Phase 10 turns the current COS-backed asset upload baseline into a real media pipeline and reusable media resource center for the admin.

This phase owns:
- the canonical admin upload pipeline for image, video, audio, and generic file assets
- upload-source handling for file picker, drag/drop, folder import, and clipboard paste
- server-side enforcement of lossless-upload permission versus managed compression policy
- recording the applied processing policy and upload audit metadata on each asset
- the real `/content/media` admin module with search, filter, preview, and retrieval workflows
- reusable media-picker and media-linking primitives for milestone-owned admin forms
- asset reference tracing and safe delete/replace behavior for shared assets

This phase does not own:
- the full story/chapter/task composition redesign beyond making those future pages consume the shared media primitives
- indoor tile zip ingestion or floor-image slicing, which remain Phase 12 scope
- AI tagging, OCR, automatic captioning, or semantic media analysis
- public mini-program rendering redesign beyond preserving the canonical asset contracts already used by admin/public data models
</domain>

<decisions>
## Locked Decisions

- All admin asset uploads continue to flow through backend APIs into Tencent COS. Phase 10 does not introduce direct browser-to-COS upload flows.
- The existing `content_assets` table remains the canonical asset record, and `content_asset_links` remains the canonical ordered attachment relation. Phase 10 extends them instead of introducing per-module asset tables.
- Upload entry modes must include file picker, drag/drop, clipboard paste, and folder import. Folder import must preserve client relative-path metadata for audit and organization, but server object keys remain backend-generated.
- Upload policy is enforced on the backend using the authenticated admin identity. The UI may preview the expected policy, but it is never trusted as the authority.
- Admin lossless-upload capability is an explicit permission/attribute on real admin accounts, not a hidden environment bypass and not a per-request client flag.
- Assets uploaded by admins without the lossless capability must be processed through a high-quality compression/transcoding policy before COS publication whenever the asset kind supports processing.
- Every uploaded asset must record upload source, uploader attribution, original filename, effective policy code, and a policy/result snapshot before the asset is considered publishable.
- Media upload policy defaults belong to admin/system configuration, not to public runtime settings and not to hardcoded constants inside the upload service.
- The `/content/media` route must become a real resource center, not a placeholder. It must support preview, search, filtering, and reference inspection across milestone-owned domains.
- The richer direct-upload behavior added during Phase 9 for spatial forms is treated as the starting point. Phase 10 must generalize it into a shared media primitive instead of duplicating one-off upload widgets.
- Existing raw numeric asset-ID fields on active admin forms should be replaced by shared media pickers/library interactions wherever those pages already exist in v2.0.
- COS credentials and any future processor credentials remain runtime/env-managed only and must never be hardcoded into tracked source or planning artifacts.
</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Milestone and upstream phase decisions
- `.planning/ROADMAP.md` - Phase 10 goal, success criteria, and dependency chain
- `.planning/REQUIREMENTS.md` - `MEDIA-01` through `MEDIA-04`
- `.planning/STATE.md` - current milestone state
- `.planning/phases/09-spatial-model-rebuild/09-RESEARCH.md` - why Phase 9 intentionally stopped at baseline asset linking and direct spatial upload affordances
- `.planning/phases/09-spatial-model-rebuild/09-01-SUMMARY.md` - verified Phase 9 media-related outcomes already in place

### Current backend media/upload implementation
- `scripts/local/mysql/init/02-live-backend-foundation.sql` - canonical `content_assets` and `content_asset_links` schema baseline
- `scripts/local/mysql/init/04-admin-control-plane-alignment.sql` - admin alignment migration for shared asset relations
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/ContentAsset.java` - current asset entity fields
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/ContentAssetLink.java` - current attachment relation model
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminContentManagementController.java` - current asset list/upload endpoints
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminContentManagementServiceImpl.java` - current asset CRUD/upload orchestration
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/CosAssetStorageService.java` - current COS storage boundary
- `packages/admin/aoxiaoyou-admin-backend/src/main/resources/application.yml` and `application-local.yml` - current COS and translation config patterns

### Current admin identity and permission surfaces
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/SysAdmin.java` - current authenticated admin account model
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminServiceImpl.java` - current admin login/auth identity flow
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminRbacServiceImpl.java` - current admin-account listing source
- `packages/admin/aoxiaoyou-admin-ui/src/pages/System/AdminUsersManagement.tsx` - current admin account UI baseline
- `packages/admin/aoxiaoyou-admin-ui/src/pages/SystemManagement/index.tsx` - current typed settings page baseline

### Current admin UI media consumers
- `packages/admin/aoxiaoyou-admin-ui/src/App.tsx` - current `/content/media` placeholder route and milestone route map
- `packages/admin/aoxiaoyou-admin-ui/src/components/spatial/SpatialAssetPickerField.tsx` - current Phase 9 direct upload and preview widget
- `packages/admin/aoxiaoyou-admin-ui/src/components/spatial/SpatialAttachmentListField.tsx` - current ordered-attachment baseline
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/CityManagement.tsx` - current shared-asset usage in spatial forms
- `packages/admin/aoxiaoyou-admin-ui/src/pages/POIManagement/index.tsx` - current cover/map-icon/audio usage in spatial forms
- `packages/admin/aoxiaoyou-admin-ui/src/pages/StorylineManagement/index.tsx` - current raw asset-ID usage still needing replacement
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/CollectibleManagement.tsx` - current collection media form baseline
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/BadgeManagement.tsx` - current badge media form baseline
- `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts` and `src/types/admin.ts` - current asset API and DTO contracts
</canonical_refs>

<specifics>
## Specific Ideas

- Extend the asset schema with audit and processing metadata rather than creating a second media table.
- Add explicit upload-source values such as `picker`, `drag-drop`, `clipboard`, and `folder`.
- Expose a real asset-usage endpoint so operators can see where a media item is used before deleting or replacing it.
- Normalize the Phase 9 spatial asset picker into a reusable media picker/library drawer used by multiple modules.
- Use typed admin/system settings for media policy defaults while storing the effective policy snapshot on each asset row for traceability.
</specifics>

<deferred>
## Deferred Ideas

- AI-generated captions, tags, OCR, or content moderation workflows
- Background transcoding queues beyond the synchronous policy path needed for v2.0
- CDN image-style orchestration or signed URL workflows
- Indoor floor-tile packaging/slicing and any other large-binary authoring pipelines outside the shared media library contract
</deferred>

---

*Phase: 10-media-asset-pipeline-and-library*
*Context gathered: 2026-04-14*
