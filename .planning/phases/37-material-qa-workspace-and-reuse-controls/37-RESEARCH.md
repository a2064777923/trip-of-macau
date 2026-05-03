---
phase: 37
slug: material-qa-workspace-and-reuse-controls
status: complete
created: 2026-05-03
---

# Phase 37 Research - Material QA Workspace and Reuse Controls

## Research Goal

Plan a safe Phase 37 implementation that builds on Phase 36 material versioning instead of creating a parallel production system. The implementation must help operators inspect, reject, approve, replace, roll back, and reuse story package assets while preserving lineage, COS evidence, and existing runtime bindings.

## Existing Foundation

- `AdminStoryMaterialPackageController` already exposes package detail, production preflight, local import, candidate binding, promote, rollback, and version history under `/api/admin/v1/content/material-packages`.
- `AdminStoryMaterialProductionServiceImpl` owns package item version creation, promotion, rollback, and super-admin confirmation gates for high-impact operations.
- `story_material_package_item_versions` is the canonical lineage table for Phase 36 assets.
- `StoryMaterialPackageManagement.tsx` already has the right admin entry point: package selector, material item table, health counters, production actions, version drawer, and preview helpers.
- `MediaAssetPreview.tsx` and `MediaAssetPickerField.tsx` already support shared image/audio/video/Lottie preview, direct upload, selected asset display, missing asset ids, and multi-asset gallery behavior.
- `content_assets` remains the canonical reusable media record; Phase 37 should prefer rebinding assets by id instead of duplicating binaries.

## Implementation Findings

### Backend QA Surface

Phase 37 should add a package-scoped QA API layer rather than broadening the production API into a global QA system.

Recommended endpoint family:

- `GET /api/admin/v1/content/material-packages/{packageId}/qa/overview`
- `GET /api/admin/v1/content/material-packages/{packageId}/qa/items`
- `GET /api/admin/v1/content/material-packages/{packageId}/qa/items/{itemId}`
- `POST /api/admin/v1/content/material-packages/{packageId}/qa/items/{itemId}/reject`
- `POST /api/admin/v1/content/material-packages/{packageId}/qa/items/{itemId}/approve`
- `POST /api/admin/v1/content/material-packages/{packageId}/qa/items/{itemId}/replace`
- `POST /api/admin/v1/content/material-packages/{packageId}/qa/consistency-check`

These endpoints should reuse existing production promotion/rollback semantics where possible. Reject and replace are the missing operations.

### Consistency Checks

Package-scoped checks are enough for this phase. The checker should compare:

- manifest item key, expected asset kind, local path, and COS key
- story package item row
- current version row
- published version row
- linked `content_assets` row
- canonical URL / COS object key
- optional COS `HEAD` result when runtime COS access is available

Finding levels:

- `blocking`: published runtime-facing asset is missing, wrong kind, missing canonical URL, or COS URL cannot be reached
- `warning`: stale version, oversized asset, preview failure, missing optional metadata, or rejected/draft asset still selected
- `info`: planned demand slot, manual import required, external captions, or non-runtime asset

### Admin UI Shape

The existing material package page should become the QA workspace. A separate page would repeat the same package and item concepts and likely recreate the confusion the user flagged earlier.

Recommended UI sections:

- package selector and QA health summary
- QA filter bar with visible labels
- material queue table with health badges and preview snippets
- detail drawer with version lineage, preview/player, QA actions, consistency findings, prompt/script/caption metadata, and usage links
- consistency report drawer
- media picker reuse section for approved/published assets

Long URLs and paths must always truncate with tooltip. Failed/unpreviewable assets must show an explicit failure state instead of occupying a normal slot.

### Reuse Controls

Approved or published material package assets should become discoverable through existing media components:

- Add package context to asset query responses where possible: `materialPackageCode`, `materialItemKey`, `materialItemStatus`, `materialVersionId`, `usageTarget`, `chapterCode`.
- Add filter support for package code, package item key, asset kind, and status.
- Hide rejected/draft assets by default in media pickers unless an advanced filter enables them.
- Keep binary reuse by `content_assets.id`; do not duplicate files.

### Security and Safety

Key risks:

- broken public references if replace or rollback mutates item pointers without showing current usage
- unauthorized publish/rollback/replacement by non-super-admins
- leaking provider prompts, scripts, local paths, or COS internals into public runtime
- SSRF or excessive latency from COS URL checks
- destructive deletion of failed versions

Mitigations:

- keep operations admin-only and reuse Phase 36 super-admin confirmation for publish/rollback and high-impact replacement
- never physically delete rejected versions
- keep QA provenance in admin APIs only
- bound consistency checks and use canonical COS metadata or backend storage checks instead of arbitrary user-provided URL probes
- require QA notes for reject/replace

## Validation Architecture

### Automated Checks

- Backend compile: `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml`
- Backend QA tests: `mvn -q -Dtest=AdminStoryMaterialQaServiceTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml`
- Admin UI build: `npm run build --prefix packages/admin/aoxiaoyou-admin-ui`
- Smoke: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-37-material-qa.ps1`

### Browser Checks

Use the existing local admin UI and the seeded/published Phase 36 flagship package:

- open the material package page
- filter by health state, kind, chapter, status, and runtime exposure
- open a version/detail drawer for image, audio, and video items
- confirm long paths do not overflow
- run consistency report
- reject a non-published test version, replace with an existing asset, approve, then rollback
- confirm the media picker can find approved assets by package/item key

## Planning Recommendations

- Plan 37-01 should implement backend QA DTOs, service, endpoints, consistency report, reject/replace/approve actions, and tests.
- Plan 37-02 should upgrade the admin workspace UI using existing components and avoid a duplicate route.
- Plan 37-03 should align media picker reuse, add smoke/browser evidence, and update documentation/state.
- Do not add a new generic production workbench in Phase 37.
