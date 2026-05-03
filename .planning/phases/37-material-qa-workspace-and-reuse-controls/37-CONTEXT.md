# Phase 37: Material QA Workspace and Reuse Controls - Context

**Gathered:** 2026-05-03
**Status:** Ready for planning
**Source:** `/gsd-next` from completed Phase 36, v3.1 roadmap, Phase 36 handoff/verification, and current admin material package code.

<domain>
## Phase Boundary

Phase 37 turns the Phase 36 production evidence into an operator-facing QA and reuse surface. The core job is to let admins inspect generated/uploaded/published story materials, identify unusable or stale assets, approve/reject/replace safely, and reuse approved assets through existing media picker flows.

This phase owns:

- A Traditional Chinese material QA workspace for story material packages, with package/item filters and clear asset health states.
- Preview and detail views for images, board-sliced child icons, Lottie JSON, audio, and videos, including provenance, dimensions, duration, size, cost, status, version, COS URL, local path, usage target, and binding targets.
- Reject, approve, replace, regenerate-request, publish, and rollback operations that preserve version lineage and do not break existing story/content/reward/runtime references.
- Manifest consistency checks comparing planned entries, local paths, COS object keys, `content_assets`, story package item rows, and version records.
- Media picker reuse alignment so approved package assets can be found and selected from content blocks, rewards, pickups, POI/story overlays, and later runtime-facing editors.

This phase does not own:

- A new general-purpose material production studio.
- Opening `image-2` generation to arbitrary admin users.
- Re-running the full Phase 36 production batch unless needed for a replacement smoke.
- Public runtime DTO exposure. That belongs to Phase 38.
- Mini-program story journey acceptance. That belongs to Phase 39.
- Full approval workflow with reviewer roles. Phase 37 may expose QA statuses and notes, but multi-step review remains future scope.

</domain>

<decisions>
## Implementation Decisions

### QA Workspace Shape

- **D37-01:** The primary route should stay close to the existing story material package surface. Prefer enhancing `/content/material-packages` into a QA-grade workspace over adding a disconnected duplicate page.
- **D37-02:** The workspace should use a three-part layout: package selector and health summary at the top, filtered material item table/grid in the center, and version/detail drawer for inspection and actions.
- **D37-03:** Table/grid filters must have visible Traditional Chinese labels. Required filters are package, item status, asset kind, chapter, usage target, provider/model, runtime exposure, and health state.
- **D37-04:** Health states should be explicit and operator-oriented: `可用`, `待生產`, `無公開連結`, `未發布`, `版本過期`, `COS 不可用`, `類型不符`, `過大`, `需重生`, `已拒絕`.
- **D37-05:** Broken/unpreviewable assets must not look equivalent to usable assets. They remain visible as demand slots or failed versions, but default QA views should separate them from approved runtime-ready material.
- **D37-06:** Long local paths, COS keys, canonical URLs, and prompt/script snippets must use ellipsis plus tooltip/detail drawer; no cell overflow into neighboring columns.

### Preview and Evidence

- **D37-07:** Preview support must cover image/icon thumbnails, video player, audio player, Lottie JSON fallback card, JSON metadata drawer, and unsupported-file icon with clear reason.
- **D37-08:** If a preview fails to load, the row should show a stable error state with retry/open-link actions, not occupy a normal preview slot silently.
- **D37-09:** Version detail must show lineage: current version, published version, parent board version, crop metadata, finalized AI candidate if any, provider/model, cost, created admin, and verification notes.
- **D37-10:** External UTF-8 captions from Phase 36 videos are accepted metadata and should be displayed clearly as captions, not mislabeled as burned-in subtitles.
- **D37-11:** QA should surface both historical basis and literary dramatization fields for story materials when available, because the user cares about credible history versus dramatized presentation.

### QA Actions and Safety

- **D37-12:** `approve` and `publish` are separate concepts in UI even if Phase 36 often promoted directly to published. Operators need to see and choose the transition.
- **D37-13:** `reject` should mark a version or item as not usable and require a note; it must not delete the content asset or version record.
- **D37-14:** `replace` should bind a chosen existing `content_assets` record or a newly uploaded asset to the material item by creating a new material version.
- **D37-15:** `regenerate` in Phase 37 should be a request/status action that points back to production tooling or AI job creation where already available; it should not become a new all-user image generation product.
- **D37-16:** `rollback` remains version-pointer based. It must update current item pointers while preserving the rejected/replaced/newer version in history.
- **D37-17:** Any operation that makes a runtime-visible asset public or swaps a public asset should require impact preview or at least show current usage targets and dependent bindings before confirmation.
- **D37-18:** Super-admin-only protections from Phase 36 publish/rollback must remain in force for high-impact publish/rollback paths.

### Consistency Checks

- **D37-19:** Consistency checks should compare these sources: manifest item key/path/COS key, story package item row, current version row, published version row, linked `content_assets`, local file existence where accessible, and COS `HEAD` result where configured.
- **D37-20:** Checks should classify findings into blocking, warning, and info. Blocking examples: missing current asset for published item, wrong asset kind, missing canonical URL for runtime-facing published item, COS URL fails `HEAD`.
- **D37-21:** Oversized asset checks should use sensible first-pass thresholds by kind and expose the raw size/duration/dimensions for operator judgment.
- **D37-22:** The consistency checker should be package-scoped first. Cross-package/global checks can be deferred unless needed for media picker reuse.
- **D37-23:** The checker should output both row-level badges and a downloadable/reportable summary for smoke evidence.

### Reuse Controls

- **D37-24:** Approved/published package assets must be discoverable in existing `MediaAssetPickerField` and media library filters without requiring operators to remember asset IDs.
- **D37-25:** Media picker labels should include asset id, asset name, package code/item key when available, asset kind, and enough path context to choose correctly.
- **D37-26:** Reuse must not duplicate binary files by default. Downstream editors should bind the existing `content_assets.id` unless the operator explicitly uploads/replaces a new asset.
- **D37-27:** Package item usage targets should link out to the owning editor when possible: story route/workbench, content blocks, rewards, pickups/exploration elements, POI/story overlays, or media detail.
- **D37-28:** Rejected and draft assets should be hidden from default media picker reuse unless the operator explicitly enables an advanced filter.

### Verification

- **D37-29:** Verification must include admin backend compile, admin UI build, and a local smoke for package QA summary, version detail, consistency checks, approve/reject/replace/rollback behavior, and media picker discoverability.
- **D37-30:** Browser verification should inspect the material package page with the Phase 36 flagship package and confirm previews, long path truncation, filters, version drawer, and action affordances are visually stable.
- **D37-31:** No provider/COS/API secrets may be committed. COS checks in smoke should use runtime environment only and degrade to explicit warning if credentials are absent.

### the agent's Discretion

- The planner may choose whether the QA workspace is a new child route such as `/content/material-qa` or an enhanced mode inside `/content/material-packages`, but it must not create two confusing pages with overlapping responsibilities.
- The planner may decide whether consistency checks are computed live or persisted as the latest QA report, provided operators can see current row-level findings and smoke can assert results.
- The planner may reuse `content_relation_links` or material package usage metadata to power usage targets, as long as existing story/content/reward/runtime bindings are not broken.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project and Milestone Scope

- `AGENTS.md` - admin ownership, COS/media rules, secret handling, UTF-8/utf8mb4 rule, and verification expectations.
- `.planning/PROJECT.md` - v3.1 milestone value and admin/public ownership.
- `.planning/REQUIREMENTS.md` - `QA-01` through `QA-04` and Phase 37 acceptance criteria.
- `.planning/ROADMAP.md` - Phase 37 goal, dependencies, and later Phase 38/39 boundary.
- `.planning/STATE.md` - current status and Phase 36 completion notes.

### Phase 36 Production Evidence

- `.planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md` - locked Phase 36 material generation/promotion decisions.
- `.planning/phases/36-material-production-pipeline-and-asset-promotion/36-HANDOFF.md` - live routes, scripts, published video versions, caption caveat, and safety notes.
- `.planning/phases/36-material-production-pipeline-and-asset-promotion/36-VERIFICATION.md` - evidence for image, board-slice, audio, video, COS, version history, and rollback smoke.
- `.planning/phases/36-material-production-pipeline-and-asset-promotion/36-SECURITY.md` - publish/rollback authorization and threat closure.
- `docs/content-packages/east-west-war-and-coexistence/content-manifest.json` - canonical package manifest.
- `docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-production-report.json` - generated asset report tying image, board-slice, audio, and video evidence together.
- `docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-board-slice-report.json` - board-slice child asset evidence.
- `docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-video-jobs.json` - chapter video job definitions and caption metadata source.

### Existing Admin Backend

- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminStoryMaterialPackageController.java` - existing material package, production, promote, rollback, and versions endpoints.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminStoryMaterialPackageService.java` - package/detail service contract.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminStoryMaterialProductionService.java` - production/version/promotion service contract.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminStoryMaterialPackageServiceImpl.java` - current package/item mapping and counters.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminStoryMaterialProductionServiceImpl.java` - import, candidate bind, promote, rollback, and version implementation.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/StoryMaterialPackage.java` - package entity.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/StoryMaterialPackageItem.java` - package item entity and current asset/status fields.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/StoryMaterialPackageItemVersion.java` - version lineage entity.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/ContentAsset.java` - canonical reusable asset metadata.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/ContentAssetLink.java` - cross-domain asset usage linkage where present.

### Existing Admin UI

- `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryMaterialPackageManagement.tsx` - current package page, filters, production/import/narration/version drawer, preview cards, and quick actions.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryMaterialPackageManagement.scss` - current material package styling and overflow fixes.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/MediaLibraryManagement.tsx` - media library management patterns.
- `packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetPreview.tsx` - shared preview component.
- `packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetDetailDrawer.tsx` - shared asset detail drawer.
- `packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetPickerField.tsx` - existing picker/upload/AI workbench integration that Phase 37 must reuse.
- `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts` - admin API client types and material package functions.
- `packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts` - DTO types for content assets and material versions.

### Schema and Smoke

- `scripts/local/mysql/init/47-phase-33-story-material-package-model.sql` - material package/item schema.
- `scripts/local/mysql/init/48-phase-33-flagship-material-assets.sql` - flagship material package seed records.
- `scripts/local/mysql/init/51-phase-36-material-production-versioning.sql` - Phase 36 versioning schema.
- `scripts/local/smoke-phase-36-material-production.ps1` - existing package production/version/COS/rollback smoke to extend or reference.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- `StoryMaterialPackageManagement.tsx` already contains package selection, item health counters, path truncation, production actions, narration drawer, version drawer, and preview helpers. Phase 37 should strengthen this page rather than throwing it away.
- `MediaAssetPreview.tsx` already classifies image/audio/video/Lottie/unsupported assets and shows failure labels. It can become the shared preview basis for QA rows and media picker cards.
- `MediaAssetPickerField.tsx` already supports direct upload, drag/drop, paste upload, AI workbench finalization, missing selected IDs, multi-select galleries, and selected-asset preview. Phase 37 reuse controls should extend search/filter metadata rather than create another picker.
- `AdminStoryMaterialPackageController` already exposes `/production/preflight`, `/production/import`, `/production/bind-candidate`, `/production/promote`, `/production/rollback`, and `/versions` endpoints.
- `StoryMaterialPackageItemVersion` already provides a version lineage anchor for QA history and rollback.

### Established Patterns

- Admin UI uses Ant Design, `PageContainer`, `Card`, `Table`, `Drawer`, and centralized API calls in `services/api.ts`.
- Admin backend uses `/api/admin/v1/...`, `ApiResponse<T>`, `PageResponse<T>`, DTO request/response packages, and service interfaces/implementations.
- Existing material package rows are demand slots as well as assets. A missing asset can be a valid planned requirement, but it must be visually separated from runtime-ready material.
- Existing Phase 36 publish/rollback paths require super-admin role plus explicit confirmation for high-impact actions.
- Chinese content and scripts must remain UTF-8/utf8mb4; avoid inline PowerShell Chinese writes.

### Integration Points

- QA actions should call existing package production endpoints first where possible, adding new endpoints only for QA summary, reject, replace, and consistency report if missing.
- Reuse controls should enrich content asset queries with material package/item context where the backend can provide it.
- Consistency checks must join package item, version, content asset, and manifest metadata. COS `HEAD` checks must remain runtime-secret-backed.
- Browser verification should use the seeded flagship package `east_west_war_and_coexistence_package`.

</code_context>

<specifics>
## Specific Ideas

- Use the existing Phase 36 material package page as the operator's entry point, but give it a clearer QA identity: health dashboard, filtered queue, preview-rich version drawer, and consistency report drawer.
- Default view should show `可用資產` separately from `待處理問題`; operators should not have to scroll through unusable assets to find production-ready material.
- Version drawer should support direct playback for published audio/video and show external caption metadata for chapter videos.
- Rejected versions should remain in history with note, operator, and timestamp.
- Replace should allow selecting an existing approved media asset or uploading a new one, then create a new version and keep previous current/published pointers available for rollback.
- Regenerate request should be represented as a status/operation note if actual provider generation is not part of this phase.
- Consistency report should highlight the same items that caused user complaints earlier: unpreviewable assets occupying useful slots, missing media files, broken links, overly long asset URLs, and confusing overlap between story content tools.

</specifics>

<deferred>
## Deferred Ideas

- General material production workbench for all future storylines.
- Full reviewer assignment, approval chain, and notification workflow.
- Public runtime DTO asset filtering and fallback behavior, which is Phase 38.
- Mini-program playback/device journey acceptance, which is Phase 39.
- Advanced visual cropper, regeneration prompt editor, or image-2 UI product.

</deferred>

---

*Phase: 37-material-qa-workspace-and-reuse-controls*
*Context gathered: 2026-05-03*
