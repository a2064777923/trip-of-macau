# Phase 10 Research: Media Asset Pipeline and Library

## Objective

Research how to implement Phase 10 so the admin gains a real media pipeline with COS-backed uploads, enforced processing policy, a searchable asset library, and shared media reuse across milestone-owned modules.

## Current Starting Point

- `content_assets` already exists as the canonical asset table and stores basic COS metadata (`asset_kind`, `bucket_name`, `object_key`, `canonical_url`, MIME, locale, checksum, image dimensions, status).
- `content_asset_links` already exists and can store ordered asset relations for entity attachments, but it is not yet the center of a unified media-library workflow.
- The current admin backend exposes `GET /api/admin/v1/content/assets` and `POST /api/admin/v1/content/assets/upload`, but upload is single-file oriented and writes directly to COS without recording processing policy, upload source, or uploader attribution.
- `CosAssetStorageService` already generates backend-owned COS object keys and stores the payload successfully when COS is enabled.
- Phase 9 added direct upload behavior inside spatial forms through `SpatialAssetPickerField`, including drag/drop, click upload, clipboard paste, preview, and auto-selection after upload.
- `/content/media` is still a placeholder route in the admin shell even though the backend already has asset list/upload endpoints.
- `StorylineManagement` and several non-spatial forms still rely on raw asset ID inputs instead of a shared media picker or library chooser.
- Admin account listing is driven by `SysAdmin`, but the current model and UI do not expose a lossless-upload permission.
- There is no typed media upload policy model in admin settings yet, and no asset row records which compression/transcoding rule was applied.

## What Phase 10 Must Resolve

1. Turn the current single-upload baseline into a canonical pipeline that supports the required ingest modes.
2. Enforce lossless-versus-compressed upload behavior on the backend based on real admin permissions and explicit policy settings.
3. Record enough audit/process metadata on each asset to make the pipeline explainable and safe to operate.
4. Replace the `/content/media` placeholder with a real central resource center.
5. Make shared media reuse work across milestone-owned forms without forcing operators to manually paste numeric asset IDs.
6. Add reference tracing and safe delete/replace behavior for shared assets used in multiple places.

## Recommended Architecture

### 1. Treat the Current Upload Endpoint as the Foundation, Then Add a Batch/Folder Contract Beside It

Recommended decision:
- keep `POST /api/admin/v1/content/assets/upload` for simple single-file uploads used by existing direct-upload components
- add a batch-aware upload contract beside it, for example `POST /api/admin/v1/content/assets/batch-upload`
- accept multipart `files[]` plus `assetKind`, optional `localeCode`, `uploadSource`, and `clientRelativePaths[]`
- make both endpoints call the same media-intake service so policy logic is not duplicated

Why:
- Phase 9 spatial forms already depend on the single upload endpoint and should not break
- folder import is naturally batch-oriented and needs to preserve relative path metadata
- a shared intake service lets the admin UI expose multiple ingest affordances without re-implementing policy decisions

### 2. Move from “Store Bytes in COS” to “Process, Audit, Then Store”

Recommended decision:
- introduce a media-intake orchestration layer in the admin backend:
  - request validation
  - asset policy resolution
  - optional processing/transcoding/compression
  - COS storage
  - canonical asset record persistence
- extend `StoredAssetMetadata` and `content_assets` so each asset records:
  - `original_filename`
  - `file_extension`
  - `upload_source`
  - `client_relative_path`
  - `uploaded_by_admin_id`
  - `uploaded_by_admin_name`
  - `processing_policy_code`
  - `processing_profile_json`
  - `processing_status`
  - `processing_note`
  - `published_at` or an equivalent publishability marker if the policy succeeds

Why:
- MEDIA-02 requires recording the applied policy before asset publication
- operators will need to explain why two assets of the same kind were handled differently
- audit metadata is also the basis for search, reference tracing, and safe operations later

### 3. Put Policy Authority in Typed Admin/System Settings, Not in Ad Hoc Constants

Recommended decision:
- use a typed admin/system settings surface for upload policies rather than introducing a public runtime setting
- keep policy values admin-owned and environment-independent, for example:
  - image max long edge
  - image JPEG/WebP quality
  - whether PNG lossless is allowed
  - video target bitrate / transcode mode
  - audio target bitrate
  - max batch size
  - max file size by asset kind
- store the effective policy snapshot on the asset row so later config changes do not erase history

Why:
- the user explicitly wants these controls in the admin/system plane
- system settings are already the right home for cross-module operator controls
- future phases can reuse the same typed settings without reworking the data model

### 4. Use Real Admin Permission on `SysAdmin` for Lossless Upload Capability

Recommended decision:
- add an explicit authenticated-admin field such as `allow_lossless_upload` on `sys_admin`
- expose it through the admin account management surfaces
- treat it as the server-side gate that decides whether the resolved policy may choose a lossless path

Why:
- the authenticated admin identity already flows through `SysAdmin`-based auth
- a server-side field is the simplest brownfield-compatible source of truth
- client-side controls alone would be trivial to bypass

### 5. Generalize the Phase 9 Spatial Upload Widget into a Shared Media Workspace

Recommended decision:
- keep the good Phase 9 behaviors already added for spatial forms
- refactor them into reusable media components, such as:
  - `MediaUploadPanel`
  - `MediaPickerField`
  - `MediaLibraryDrawer`
  - `AssetUsageDrawer`
- allow the shared picker to work in two modes:
  - inline pick/upload for a single asset field
  - library drawer for search, preview, replace, and usage inspection

Why:
- Phase 9 already proved the UX direction for direct upload and preview
- Phase 10 should extend that pattern across the admin instead of creating another upload stack
- this gives Phases 11 and 12 ready-made media primitives for rebuilt story/activity/indoor pages

### 6. Replace Raw Asset-ID Inputs on Active Forms, and Prepare Future Phases to Reuse the Same Contract

Recommended decision:
- retrofit the forms that already exist in v2.0 and still expose raw asset IDs:
  - storyline and chapter-adjacent current forms
  - collectible / badge surfaces
  - indoor-building baseline forms
  - any current content forms for tips/notifications/stamps that use asset IDs directly
- keep placeholders such as campaigns/chapters as placeholders until their own composition phases, but ensure the future forms can consume the same media picker/library contract

Why:
- MEDIA-04 is specifically about attaching and ordering shared assets across milestone domains
- the biggest current usability regression is not the lack of COS, but the need to manually enter asset IDs
- a consistent picker contract lowers the cost of later composition phases

### 7. Add Reference Tracing that Understands Both Link Tables and Direct Asset Columns

Recommended decision:
- add backend asset-usage aggregation endpoints, for example `GET /api/admin/v1/content/assets/{id}/usages`
- aggregate references from:
  - `content_asset_links`
  - direct columns such as `cover_asset_id`, `banner_asset_id`, `icon_asset_id`, `map_icon_asset_id`, `audio_asset_id`, and any similar fields on current milestone entities
- block unsafe deletion when the asset is still in use unless a deliberate override flow exists

Why:
- the library is not operationally safe without reference tracing
- current brownfield modules use both shared-link records and direct asset foreign keys
- operators need a single answer to “where is this file used?”

### 8. Compression/Transcoding Should Be Pluggable Per Asset Kind

Recommended decision:
- introduce an `AssetProcessingService` boundary with kind-specific handlers:
  - image processor
  - video processor
  - audio processor
  - generic-file passthrough
- fail fast and clearly when an asset kind requires processing but no processor is configured
- do not silently skip required lossy processing for admins without the lossless permission

Why:
- image compression can be handled differently from audio/video
- the pipeline needs to remain extensible without baking all logic into COS storage code
- the user explicitly asked for quality-preserving compression behavior and permission-based bypass

### 9. Phase 10 Verification Must Include Real COS Proof and Policy Variants

Recommended decision:
- create a dedicated media smoke script that proves:
  - upload with a lossless-allowed admin
  - upload with a compression-only admin
  - resulting `content_assets` rows record different effective policies
  - the media library can search/filter those uploaded assets
  - a linked asset reports usages and blocks unsafe delete

Why:
- MEDIA-01 to MEDIA-04 are operational capabilities, not just DTO changes
- Phase 10 has to prove the pipeline works locally against real COS, not only through build success

## Recommended Phase Output Shape

Phase 10 should deliver:
- richer `content_assets` audit/process metadata
- server-enforced upload policy and lossless-permission handling
- shared single/batch upload contracts for picker, drag/drop, clipboard, and folder import
- a real `/content/media` resource center
- reusable media-picker/library components for current milestone forms
- reference tracing and safe delete/replace behavior
- end-to-end COS upload smoke verification with policy differentiation

Phase 10 should not yet deliver:
- the full story/task/activity redesign
- floor-tile packaging/slicing workflows
- AI-assisted media enrichment
- CDN-side signed URL or private-delivery platform work

## Key Risks and Mitigations

### Existing Direct Upload UX Gets Forked Instead of Unified

Risk:
- Phase 9 already added one-off upload logic to spatial forms

Mitigation:
- explicitly refactor the spatial widget into a shared media primitive and migrate other forms onto it

### Lossless Permission Exists in UI Only

Risk:
- operators or scripts could bypass UI-only permission checks

Mitigation:
- enforce permission resolution entirely on the backend from authenticated `SysAdmin` data

### Asset Delete Becomes Unsafe Because References Are Split Across Multiple Patterns

Risk:
- brownfield modules use both `content_asset_links` and direct asset foreign keys

Mitigation:
- build one aggregated usage service that reads both relation styles before allowing delete

### Video/Audio Processing Environment Drift

Risk:
- media processing requirements may differ between local and deployed environments

Mitigation:
- isolate processing behind pluggable handlers, make health/config explicit, and fail clearly when required processors are unavailable

## Recommendation Summary

- Extend the current COS upload baseline into an audited media-intake service with explicit policy resolution and uploader attribution.
- Store policy defaults in typed admin/system settings, and store the effective policy snapshot on each asset row.
- Add a real admin permission on `SysAdmin` for lossless upload.
- Promote the Phase 9 spatial upload widget into a shared media picker/library workspace.
- Make `/content/media` a real searchable resource center and add asset-usage tracing so operators can safely reuse, replace, and delete media.

## Validation Architecture

- Admin backend tests should cover:
  - policy resolution from admin permission plus system settings
  - asset record audit/process fields
  - single-file and batch/folder upload request handling
  - usage tracing and delete protection
- Admin UI verification should cover:
  - real `/content/media` route
  - batch upload affordances for click, drag/drop, folder, and clipboard
  - asset search/filter/preview and usage inspection
  - lossless permission and policy settings surfaces
- Smoke proof should verify:
  - real COS upload succeeds
  - policy metadata differs between lossless-enabled and lossless-disabled admins
  - linked assets are traceable and not silently deletable while in use

## Sources

### Local codebase references

- `scripts/local/mysql/init/02-live-backend-foundation.sql`
- `scripts/local/mysql/init/04-admin-control-plane-alignment.sql`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/ContentAsset.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/ContentAssetLink.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/SysAdmin.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminContentManagementController.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminContentManagementServiceImpl.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/CosAssetStorageService.java`
- `packages/admin/aoxiaoyou-admin-ui/src/components/spatial/SpatialAssetPickerField.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/System/AdminUsersManagement.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/SystemManagement/index.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/StorylineManagement/index.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/App.tsx`

### Upstream planning references

- `.planning/ROADMAP.md`
- `.planning/REQUIREMENTS.md`
- `.planning/phases/09-spatial-model-rebuild/09-RESEARCH.md`
- `.planning/phases/09-spatial-model-rebuild/09-01-SUMMARY.md`
