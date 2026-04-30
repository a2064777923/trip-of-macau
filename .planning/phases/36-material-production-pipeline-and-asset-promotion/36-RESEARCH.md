# Phase 36: Material Production Pipeline and Asset Promotion - Research

**Researched:** 2026-04-30
**Domain:** Material production orchestration, backend-owned asset registration, COS promotion, and versioned rollback for flagship story assets
**Confidence:** MEDIUM

<user_constraints>
## User Constraints (from CONTEXT.md)

Verbatim copy from `.planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md`. [VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md]

### Locked Decisions

#### Generation Strategy and Cost Control

- **D36-01:** The preferred execution model is direct production by Codex during this phase: generate the story assets the flagship story needs, save them locally, upload them to COS, create or update `content_assets`, update material package items, and complete the story configuration.
- **D36-02:** Admin UI should not become a general `image-2` production surface. If UI changes are needed, keep them inside the existing story material package page as explicit package-scoped production/import/status actions.
- **D36-03:** Real `image-2` generation is allowed in Phase 36, but it is limited to this story package and must keep cost/provenance records.
- **D36-04:** If the backend cannot directly call `image-2`, use Codex/image generation to create local files, then pass those files through the existing COS/content asset/material package promotion path.
- **D36-05:** Generation should support a batch action with an explicit pre-flight confirmation showing item count, estimated cost, target asset kinds, and risk.
- **D36-06:** Every generation attempt should preserve candidates or versions. A failed or disliked result must not destroy the previous usable asset.
- **D36-07:** Cost control uses daily/batch estimated cost ceilings. Exceeding a ceiling requires super-admin confirmation.

#### Material Board and Slicing Flow

- **D36-08:** Generate one pickup material board per chapter, plus one full-line title/badge board.
- **D36-09:** Do not productize a general admin cropper in Phase 36. Slicing is a one-off execution step performed by Codex/local tooling.
- **D36-10:** Codex should visually inspect the generated board, decide which region corresponds to which manifest item, then slice the board into child assets.
- **D36-11:** Slicing should use a repeatable local command/template so later storylines can reuse the method, even though Phase 36 does not expose it as a general product feature.
- **D36-12:** Child assets must preserve parent board provenance and still map cleanly to the manifest item `itemKey`, `localPath`, and `cosObjectKey`.
- **D36-13:** After slicing, child assets should be uploaded and automatically reflected in `content_assets` plus the matching `story_material_package_items` rows.

#### Audio and Video Production

- **D36-14:** Phase 36 produces Mandarin narration first. Cantonese, English, and Portuguese voice versions are deferred.
- **D36-15:** Audio generation should directly use Bailian CosyVoice and upload generated audio to COS.
- **D36-16:** If CosyVoice fails, do not silently publish substitute audio. Planning can include retry/error handling, but the locked target is real Bailian output.
- **D36-17:** Each chapter should receive a short MP4 made from the chapter hero image, Mandarin narration, Traditional Chinese subtitles, and light pan/zoom motion.
- **D36-18:** Video subtitles must be embedded in Traditional Chinese and generated from `audio-scripts.md`.

#### Status Promotion and Rollback

- **D36-19:** Successful generation should upload to COS immediately and move the item to `uploaded`.
- **D36-20:** After local visual or playback verification by Codex, the item can be promoted automatically to `approved`.
- **D36-21:** Approved items should then automatically become `published` so the story can use the assets immediately.
- **D36-22:** Every item must keep candidate/version traceability. Rollback means pointing `asset_id` and package item metadata back to a previous finalized version.

### Claude's Discretion

- The planner may choose whether to implement candidate/version traceability by adding a dedicated material item versions table, by linking to existing AI generation candidate records, or by storing version metadata on material package items, provided rollback is reliable and inspectable.
- The planner may choose the exact local production script language, but scripts must avoid inline PowerShell Chinese text and must read UTF-8 files for prompts/scripts.
- The planner may decide how to estimate cost if a provider does not return exact cost, but cost records must be visible and conservative.
- The planner may choose whether COS upload happens through existing multipart endpoints, direct service calls from backend jobs, or a controlled local import endpoint, provided secrets remain outside tracked files.

### Deferred Ideas (OUT OF SCOPE)

- General-purpose admin `image-2` generation for arbitrary users or arbitrary content.
- A full visual cropper/material-board editor inside admin.
- Cantonese, English, and Portuguese narration versions.
- Complex AI video generation beyond still-image pan/zoom MP4 assembly.
- Full WeChat mini-program story-mode acceptance and gameplay polish, which remains Phase 39.
- AR recognition, voice-input gameplay, puzzle engines, and advanced minigames.
</user_constraints>

<phase_requirements>
## Phase Requirements

Requirement definitions come from `.planning/REQUIREMENTS.md`. [VERIFIED: .planning/REQUIREMENTS.md]

| ID | Description | Research Support |
|----|-------------|------------------|
| MAT-01 | Operators can generate or import real flagship story still images from the Phase 33 manifest, including story covers, chapter hero art, pickup icons, honor/title icons, and fallback posters, while preserving prompt provenance and UTF-8 metadata. | Hybrid local-generation plus backend upload/import flow, immutable version journal, UTF-8 file-based prompts, and package-scoped import/status APIs support this directly. [VERIFIED: docs/content-packages/east-west-war-and-coexistence/content-manifest.json; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminContentManagementController.java; scripts/local/seed-phase-33-material-assets.ps1; AGENTS.md] |
| MAT-02 | Operators can use a material-board workflow where one generated image can contain multiple aligned assets, then crop/slice selected regions into reusable child assets with local files, COS object keys, and `content_assets` records. | Pillow-backed local crop/export plus backend upload/import and parent-child provenance metadata cover this with lower risk than a new admin cropper UI. [CITED: https://pillow.readthedocs.io/en/stable/reference/Image.html][VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/MediaIntakeService.java] |
| MAT-03 | Operators can synthesize narration, short sound cues, and chapter audio from approved scripts through configured providers such as CosyVoice, with language selection, voice selection, task history, preview, retry, and COS upload. | Existing admin AI jobs/candidates/voice endpoints already provide CosyVoice-oriented job, preview, refresh, and upload primitives; Phase 36 must add package binding and status promotion on top. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminAiController.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java][CITED: https://www.alibabacloud.com/help/en/model-studio/text-to-speech; https://www.alibabacloud.com/help/en/model-studio/cosyvoice-voice-list] |
| MAT-04 | Operators can assemble simple story videos from one or more still images using pan/zoom/motion, narration/audio, subtitles or captions where needed, and export them as COS-backed video assets. | `ffmpeg` `zoompan` and `subtitles` are the practical path for still-image MP4 assembly, but `ffmpeg` is missing locally and must be treated as an explicit dependency gate. [CITED: https://ffmpeg.org/ffmpeg-filters.html; https://ffmpeg.org/download.html][VERIFIED: workstation env audit 2026-04-30] |
| MAT-05 | Operators can promote manifest materials from `planned` to `generated`, `uploaded`, `approved`, or `published`, with rollback to previous asset versions and clear links back to the Phase 33 story material package. | Current package schema stores only a single live `asset_id` on each item, so reliable rollback requires immutable version records and explicit promotion APIs rather than overwriting the item row in place. [VERIFIED: scripts/local/mysql/init/47-phase-33-story-material-package-model.sql; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminStoryMaterialPackageServiceImpl.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java] |
</phase_requirements>

## Summary

Phase 36 is primarily an orchestration and lineage phase, not a greenfield media stack phase. The repo already contains a real story material package registry, backend-owned COS/media intake, and AI generation job/candidate storage, but those pieces are not yet connected for package-scoped production, explicit promotion, or rollback. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminStoryMaterialPackageController.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminStoryMaterialPackageServiceImpl.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminContentManagementController.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java]

The safest implementation path is hybrid. Use local UTF-8 file-driven tooling for Codex/OpenAI still-image production, material-board slicing, and MP4 assembly; use the existing admin AI path for CosyVoice narration where the repo already has provider routing, voice endpoints, candidate storage, and request logs; and force every finalized binary back through backend-owned upload/finalization APIs so `content_assets` remains canonical. [VERIFIED: scripts/local/seed-phase-33-material-assets.ps1; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/MediaIntakeService.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/ai/provider/AiProviderTemplateRegistry.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java][CITED: https://platform.openai.com/docs/guides/images/image-generation; https://www.alibabacloud.com/help/en/model-studio/text-to-speech]

The planner should split Phase 36 into five execution plans: `P1` backend schema/orchestration/versioning, `P2` local still-image/audio production plus board-slicing tooling, `P3` package-scoped admin UI actions, `P4` smoke/dependency verification, and `P5` a dedicated ffmpeg-gated MAT-04 video slice. This split matches the actual dependency order in the codebase and prevents MAT-04 from blocking MAT-01/02/03/05. [VERIFIED: .planning/ROADMAP.md; workstation env audit 2026-04-30]

**Primary recommendation:** Implement a package-scoped production pipeline that records immutable material versions, uploads all binaries through existing backend asset APIs, and treats local image/video tooling plus `ffmpeg` readiness as first-class Phase 36 planning inputs. [VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/MediaIntakeService.java; workstation env audit 2026-04-30]

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Manifest-driven preflight, batch selection, cost estimate preview | Operator Workstation / Local Tooling | API / Backend | The locked flow starts from local prompt/script/manifest files and requires explicit preflight before any generation/upload. [VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md; docs/content-packages/east-west-war-and-coexistence/content-manifest.json] |
| Still-image generation and material-board slicing | Operator Workstation / Local Tooling | Database / Storage | Codex/local tooling is the preferred path when backend-native OpenAI image generation is not wired, and slicing is explicitly a one-off local step. [VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java][CITED: https://pillow.readthedocs.io/en/stable/reference/Image.html] |
| CosyVoice narration generation and candidate storage | API / Backend | Database / Storage | Existing admin AI endpoints, jobs, candidates, and request logs already own provider calls and binary candidate persistence for TTS. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminAiController.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java][CITED: https://www.alibabacloud.com/help/en/model-studio/text-to-speech] |
| COS upload, `content_assets` registration, and media metadata | API / Backend | Database / Storage | Uploads already pass through `MediaIntakeService` and `CosAssetStorageService`, which populate canonical asset rows and processing metadata. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/MediaIntakeService.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/CosAssetStorageService.java] |
| Status promotion, rollback, and current-item pointer updates | API / Backend | Database / Storage | Promotion and rollback mutate business state and must update package items atomically against version history. [VERIFIED: scripts/local/mysql/init/47-phase-33-story-material-package-model.sql; .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md] |
| Published asset delivery to runtime | CDN / Static | Database / Storage | Once `content_assets` is published, COS canonical URLs become the delivery surface while package/database state decides exposure. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/ContentAsset.java; .planning/ROADMAP.md] |

## Standard Stack

Version verification for the recommended stack. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/pom.xml; packages/admin/aoxiaoyou-admin-backend/src/main/resources/application.yml; python --version; python -m pip index versions Pillow; python import audit; workstation env audit 2026-04-30][CITED: https://ffmpeg.org/download.html]

- `Spring Boot 3.2.4`, `MyBatis-Plus 3.5.6`, and `cos_api 5.6.246` are already pinned in the admin backend and should remain the backend implementation surface for Phase 36. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/pom.xml]
- `Python 3.12.4` is available locally and is the most practical local-tooling runtime for deterministic slicing/import helpers in this repo. [VERIFIED: python --version]
- `Pillow` `10.3.0` is installed locally, while `12.2.0` is the latest available version on PyPI; the installed version is sufficient for `Image.crop` and `Image.save`, so the planner does not need to force an upgrade unless execution reveals a compatibility bug. [VERIFIED: python import audit; python -m pip index versions Pillow][CITED: https://pillow.readthedocs.io/en/stable/reference/Image.html]
- `requests 2.32.5` is installed locally and is enough for authenticated import/status scripts without adding npm dependencies to the monorepo. [VERIFIED: python import audit]
- `FFmpeg 8.1` is the current stable upstream release as of 2026-03-16, but `ffmpeg` is not installed on this workstation, so MAT-04 is currently blocked until the binary is installed and verified with subtitle support. [CITED: https://ffmpeg.org/download.html][VERIFIED: workstation env audit 2026-04-30]

### Core

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Admin backend stack | Spring Boot `3.2.4` + MyBatis-Plus `3.5.6` + `cos_api 5.6.246` [VERIFIED: packages/admin/aoxiaoyou-admin-backend/pom.xml] | Schema migration, orchestration APIs, status promotion, rollback, and backend-owned upload registration. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/pom.xml; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminContentManagementController.java] | This is already the authoritative admin write surface and already owns `content_assets`, AI jobs/candidates, and COS upload services. [VERIFIED: AGENTS.md; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminAiController.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/MediaIntakeService.java] |
| Existing admin AI subsystem | Current repo schema/endpoints (`ai_generation_jobs`, `ai_generation_candidates`) [VERIFIED: scripts/local/mysql/init/31-phase-18-ai-capability-center-foundation.sql; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminAiController.java] | CosyVoice generation history, candidate storage, provider logs, and optional linkage from package versions to generated artifacts. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java] | Reusing the existing AI subsystem preserves provider logs, owner checks, and candidate metadata instead of inventing a second job system. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java; packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminAiServiceImplTest.java] |
| Local Python tooling | Python `3.12.4` + Pillow `10.3.0` + `requests 2.32.5` [VERIFIED: python --version; python import audit] | Board slicing, provenance JSON export, authenticated local import helpers, and UTF-8 file-driven preflight. [VERIFIED: python import audit][CITED: https://pillow.readthedocs.io/en/stable/reference/Image.html] | Python is already installed, avoids adding repo packages, and gives deterministic crop/save behavior with a lightweight HTTP client. [VERIFIED: python --version; python import audit] |

### Supporting

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `ffmpeg` CLI | Stable upstream `8.1`; not installed locally [CITED: https://ffmpeg.org/download.html][VERIFIED: workstation env audit 2026-04-30] | Still-image motion MP4 assembly, audio muxing, and subtitle burn-in. [CITED: https://ffmpeg.org/ffmpeg-filters.html] | Required for MAT-04 after workstation install and a post-install filter check such as `ffmpeg -filters | findstr subtitles`. [CITED: https://ffmpeg.org/ffmpeg-filters.html][VERIFIED: workstation env audit 2026-04-30] |
| Existing admin UI stack | React `18.3.1` + Ant Design `5.24.6` + Vite `6.2.3` [VERIFIED: packages/admin/aoxiaoyou-admin-ui/package.json] | Package-scoped production/import/status actions inside the existing story material package page. [VERIFIED: packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryMaterialPackageManagement.tsx] | Use only for explicit package actions and preview reuse; do not build a new global generation workbench in this phase. [VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md] |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Existing backend upload endpoints | Direct local COS writes | Faster to script, but it bypasses media policy enforcement, audit metadata, and the existing `content_assets` write path. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/MediaIntakeService.java; AGENTS.md] |
| Dedicated package-scoped actions in `StoryMaterialPackageManagement` | Reuse only the generic AI creative workbench modal | The generic modal already finalizes candidates, but it publishes directly and does not bind package items or enforce package-specific status/rollback semantics. [VERIFIED: packages/admin/aoxiaoyou-admin-ui/src/components/ai/AiCreativeWorkbenchModal.tsx; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java; .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md] |
| Immutable material version journal | Overwrite `story_material_package_items.asset_id` and stash history in free-form JSON on the live row | The current item schema only holds one live pointer; ad hoc JSON history is harder to query, audit, and roll back safely. [VERIFIED: scripts/local/mysql/init/47-phase-33-story-material-package-model.sql] |

**Installation:**
```bash
python -m pip install Pillow requests
```

No new npm packages are required for the recommended path. `ffmpeg` must be installed through the workstation package manager or a trusted Windows build before MAT-04 work starts. [VERIFIED: packages/admin/aoxiaoyou-admin-ui/package.json; workstation env audit 2026-04-30][CITED: https://ffmpeg.org/download.html]

## Architecture Patterns

### System Architecture Diagram

The recommended Phase 36 data flow below is derived from locked phase decisions plus the currently implemented upload/AI/material-package surfaces. [VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/MediaIntakeService.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java]

```text
UTF-8 manifest + prompt/script files
        |
        v
Local preflight (item count, estimated cost, target kinds, risk)
        |
        +----------------------------+----------------------------+
        |                            |                            |
        v                            v                            v
Codex/OpenAI still images     Admin AI CosyVoice TTS       Local ffmpeg MP4 assembly
and material boards           (jobs/candidates/logs)       (hero image + audio + SRT)
        |                            |                            |
        v                            v                            v
Local board slicing (Pillow)  Candidate finalize/import     Local MP4 output
        |                            |                            |
        +-------------+--------------+----------------------------+
                      |
                      v
Backend upload/import API (`/content/assets/upload` or controlled import action)
                      |
                      v
`content_assets` row + processing metadata + COS object
                      |
                      v
Immutable material version record
(`package_item_id`, `asset_id`, provenance, parent board, crop box, status)
                      |
                      v
Current `story_material_package_items` pointer update
(`asset_id`, `local_path`, `cos_object_key`, `canonical_url`, live status)
                      |
                      v
Local verification -> `uploaded` -> `approved` -> `published`
                      |
                      v
Rollback = point current item back to a previous immutable version
```

### Recommended Project Structure

This structure follows existing `scripts/local`, `docs/content-packages`, and package-scoped admin patterns in the repo. [VERIFIED: scripts/local/seed-phase-33-material-assets.ps1; docs/content-packages/east-west-war-and-coexistence/content-manifest.json; packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryMaterialPackageManagement.tsx]

```text
scripts/
├── local/
│   ├── material-production/
│   │   ├── phase36-preflight.ps1
│   │   ├── phase36-import-assets.py
│   │   ├── phase36-slice-board.py
│   │   ├── phase36-build-video.ps1
│   │   └── smoke-phase-36-material-production.ps1
docs/
├── content-packages/
│   └── east-west-war-and-coexistence/
│       ├── generated/          # local image/audio/video outputs
│       ├── crops/              # sliced child assets
│       ├── subtitles/          # UTF-8 .srt files
│       └── production-runs/    # preflight and provenance reports
packages/admin/aoxiaoyou-admin-backend/
└── src/main/java/.../material/ # package production, status, rollback APIs
packages/admin/aoxiaoyou-admin-ui/
└── src/pages/Content/          # package-scoped actions on existing page
```

### Pattern 1: Immutable Material Version Journal

**What:** Add an immutable version layer per package item, then keep `story_material_package_items` as the mutable “current pointer” row. The version row should carry at minimum `package_item_id`, `version_no`, `asset_id`, `source_type`, `source_job_id`, `source_candidate_id`, `parent_version_id`, `parent_asset_id`, `parent_object_key`, `crop_box_json`, `prompt_ref`, `script_ref`, `status`, `created_by`, and `created_at`. This is the cleanest way to make rollback inspectable without abusing the live item row. [VERIFIED: scripts/local/mysql/init/47-phase-33-story-material-package-model.sql; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java]

**When to use:** Use for every generated or imported binary that can replace a planned package item, including child assets sliced from a parent board and MP4s derived from hero images. [VERIFIED: docs/content-packages/east-west-war-and-coexistence/content-manifest.json; .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md]

**Example:**
```sql
-- Recommended Phase 36 schema pattern; derived from the current single-pointer item model.
CREATE TABLE story_material_package_item_versions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  package_item_id BIGINT NOT NULL,
  version_no INT NOT NULL,
  asset_id BIGINT NOT NULL,
  source_type VARCHAR(32) NOT NULL,
  source_job_id BIGINT NULL,
  source_candidate_id BIGINT NULL,
  parent_version_id BIGINT NULL,
  parent_asset_id BIGINT NULL,
  crop_box_json JSON NULL,
  status VARCHAR(32) NOT NULL,
  metadata_json JSON NULL,
  created_by_admin_id BIGINT NULL,
  created_by_admin_name VARCHAR(128) NOT NULL DEFAULT '',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### Pattern 2: Hybrid Production Orchestrator

**What:** Split production by the tier that already owns the hardest part: local tooling owns still-image generation, board slicing, and MP4 assembly; backend owns media registration, status changes, rollback, and CosyVoice API calls. [VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/MediaIntakeService.java]

**When to use:** Use for this phase because backend-native OpenAI image execution is not the current repo path, while local import through existing backend media endpoints is already demonstrated. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/ai/provider/AiProviderTemplateRegistry.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java; scripts/local/seed-phase-33-material-assets.ps1]

**Example:**
```typescript
// Source pattern: existing Admin AI + upload endpoint split
// Verified by local code inspection of AdminAiController/AdminContentManagementController.
type MaterialRun = {
  itemKey: string;
  localFile: string;
  assetKind: 'image' | 'audio' | 'video' | 'icon';
  source: 'local-codex' | 'dashscope-tts' | 'local-ffmpeg';
};

// Phase 36 recommendation:
// 1. Produce local file or backend TTS candidate.
// 2. Upload/import through backend asset API.
// 3. Insert immutable version row.
// 4. Promote current package item pointer + status.
```

### Pattern 3: Board-Slice Import Pipeline

**What:** Treat each board as a parent artifact, then produce child slices through deterministic crop coordinates stored as metadata and promoted as first-class assets. [VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md; docs/content-packages/east-west-war-and-coexistence/content-manifest.json][CITED: https://pillow.readthedocs.io/en/stable/reference/Image.html]

**When to use:** Use for pickup boards and the title/badge board, because Phase 36 explicitly forbids a general cropper UI but still requires repeatable slicing. [VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md]

**Example:**
```python
# Source: https://pillow.readthedocs.io/en/stable/reference/Image.html
from PIL import Image

with Image.open("chapter-board.png") as board:
    child = board.crop((left, upper, right, lower))
    child.save("icons/pickups/ch01-token.png")
```

### Anti-Patterns to Avoid

- **Overwriting the live package item row as the only history:** The current schema has one `asset_id` pointer per item, so overwriting it without an immutable version row makes rollback opaque and brittle. [VERIFIED: scripts/local/mysql/init/47-phase-33-story-material-package-model.sql]
- **Treating generic AI finalize as package completion:** `finalizeCandidate()` creates or reuses `content_assets`, but it does not update `story_material_package_items`. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java]
- **Direct-to-COS local uploads:** This bypasses backend-owned processing metadata, upload policy, and audit attribution. [VERIFIED: AGENTS.md; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/MediaIntakeService.java]
- **Inline PowerShell Chinese prompt bodies:** Project rules explicitly forbid pushing Chinese content through unsafe inline PowerShell literals. [VERIFIED: AGENTS.md]
- **Starting MAT-04 before `ffmpeg` is installed and validated with subtitle support:** The workstation currently lacks `ffmpeg`, and the subtitle filter requires a build with `libass`. [VERIFIED: workstation env audit 2026-04-30][CITED: https://ffmpeg.org/ffmpeg-filters.html]

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| COS upload + asset metadata registration | A bespoke local COS uploader that writes directly to object storage | Existing `/api/admin/v1/content/assets/upload` and `MediaIntakeService` path. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminContentManagementController.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/MediaIntakeService.java] | The backend path already computes metadata, applies policy, records uploader attribution, and inserts `content_assets`. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/MediaIntakeService.java] |
| TTS provider orchestration | A new Phase 36-only CosyVoice client stack | Existing admin AI service, voice list/preview/sync endpoints, and request logs. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminAiController.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java] | Reusing the AI subsystem keeps provider auth, owner checks, logs, and candidate history in one place. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java] |
| Board slicing math and PNG export | Manual pixel arithmetic in ad hoc shell code | Pillow `Image.crop` and `Image.save`. [CITED: https://pillow.readthedocs.io/en/stable/reference/Image.html] | Pillow already provides deterministic rectangular crop/save primitives and is installed locally. [VERIFIED: python import audit] |
| Motion video compositor | A custom Python/OpenCV or canvas renderer for pan/zoom + subtitles | `ffmpeg` `zoompan` plus `subtitles`. [CITED: https://ffmpeg.org/ffmpeg-filters.html] | `ffmpeg` already solves zoom, timing, muxing, and subtitle burn-in more robustly than a one-off renderer. [CITED: https://ffmpeg.org/ffmpeg-filters.html] |
| Rollback history hidden in one mutable row | Free-form JSON blobs on `story_material_package_items` | An immutable version journal with optional links back to AI job/candidate IDs. [VERIFIED: scripts/local/mysql/init/47-phase-33-story-material-package-model.sql; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java] | Queryable version rows make promotion, rollback, and QA diffing auditable. [VERIFIED: .planning/REQUIREMENTS.md] |

**Key insight:** The repo already has three critical building blocks: authoritative package items, authoritative asset rows, and authoritative AI candidate history. Phase 36 should join them with a thin orchestration layer, not replace them with a new media subsystem. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminStoryMaterialPackageServiceImpl.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/MediaIntakeService.java]

## Common Pitfalls

### Pitfall 1: Assuming the current package schema is already rollback-safe

**What goes wrong:** A new asset overwrites the only live `asset_id` on the package item, so the old version is no longer inspectable without scavenging external logs or object keys. [VERIFIED: scripts/local/mysql/init/47-phase-33-story-material-package-model.sql]

**Why it happens:** `story_material_package_items` carries one current pointer, not a built-in immutable version chain. [VERIFIED: scripts/local/mysql/init/47-phase-33-story-material-package-model.sql]

**How to avoid:** Add a dedicated immutable version record and make rollback a pointer flip back to an earlier version row. [VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md]

**Warning signs:** Package items keep changing `asset_id` and `canonical_url`, but there is no structured record of prior versions. [VERIFIED: scripts/local/mysql/init/47-phase-33-story-material-package-model.sql]

### Pitfall 2: Assuming AI finalize already binds package items

**What goes wrong:** A finalized AI candidate becomes a `content_assets` row, but the material package still points to the old or null asset. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java]

**Why it happens:** `finalizeCandidate()` updates candidate/job state only; it does not update `story_material_package_items`. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java]

**How to avoid:** Add a package-scoped bind/promote action that runs after finalize/import and updates both version history and the live item row in one transaction. [VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md]

**Warning signs:** AI job history shows a finalized asset ID, but the package page still shows `planned`, `generated`, or a stale `assetId`. [VERIFIED: packages/admin/aoxiaoyou-admin-ui/src/components/ai/AiCreativeWorkbenchModal.tsx; packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryMaterialPackageManagement.tsx]

### Pitfall 3: Treating provider inventory seeds as proof of backend-native OpenAI image support

**What goes wrong:** The planner assumes backend-native `gpt-image-*` generation already works and spends Phase 36 on UI wiring instead of the real import/orchestration gap. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/ai/provider/AiProviderTemplateRegistry.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java]

**Why it happens:** The provider template registry advertises OpenAI-compatible inventory seeds, but the current execution service calls only the DashScope gateway for image and TTS work. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/ai/provider/AiProviderTemplateRegistry.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java]

**How to avoid:** Keep the locked hybrid path: local Codex/OpenAI still-image generation, then backend-owned import/upload until backend-native OpenAI execution is explicitly implemented and verified. [VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md]

**Warning signs:** New UI work appears before any backend package-binding API or local import runbook exists. [VERIFIED: packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryMaterialPackageManagement.tsx; .planning/ROADMAP.md]

### Pitfall 4: Hiding video dependency risk until late execution

**What goes wrong:** MAT-04 becomes the phase blocker after image/audio work is already in progress. [VERIFIED: .planning/ROADMAP.md; workstation env audit 2026-04-30]

**Why it happens:** `ffmpeg` is not installed locally, and the subtitle burn-in path depends on the `subtitles` filter with `libass` support. [VERIFIED: workstation env audit 2026-04-30][CITED: https://ffmpeg.org/ffmpeg-filters.html]

**How to avoid:** Put `ffmpeg` install and `ffmpeg -filters | findstr subtitles` verification in the first execution plan or gate MAT-04 behind a dependency check. [CITED: https://ffmpeg.org/ffmpeg-filters.html][VERIFIED: workstation env audit 2026-04-30]

**Warning signs:** The plan mentions MP4 assembly but contains no workstation dependency step or smoke command. [VERIFIED: workstation env audit 2026-04-30]

### Pitfall 5: Violating the UTF-8 rule in local scripts

**What goes wrong:** Chinese prompt/script text gets corrupted, making generation results and subtitles inconsistent with the Phase 33 source package. [VERIFIED: AGENTS.md; docs/content-packages/east-west-war-and-coexistence/image-prompts.md; docs/content-packages/east-west-war-and-coexistence/audio-scripts.md]

**Why it happens:** Inline PowerShell literals and mismatched codepages are fragile for multilingual payloads on Windows. [VERIFIED: AGENTS.md; scripts/local/seed-phase-33-material-assets.ps1]

**How to avoid:** Read prompts, scripts, subtitles, and SQL from UTF-8 files only; keep PowerShell as a transport/orchestration shell, not the source of Chinese strings. [VERIFIED: AGENTS.md; scripts/local/seed-phase-33-material-assets.ps1]

**Warning signs:** Local commands start embedding long Traditional Chinese prompt bodies directly in shell arguments. [VERIFIED: AGENTS.md]

## Code Examples

Verified patterns from official sources and repo-compatible workflows.

### Crop and save a child asset from a board

```python
# Source: https://pillow.readthedocs.io/en/stable/reference/Image.html
from PIL import Image

with Image.open("chapter-board.png") as board:
    child = board.crop((left, upper, right, lower))
    child.save("icons/pickups/ch01-token.png")
```

### Generate an image through the official OpenAI Images API

```python
# Source: https://platform.openai.com/docs/api-reference/images/create
from openai import OpenAI

client = OpenAI()
result = client.images.generate(
    model="gpt-image-1",
    prompt="A Macau harbor story cover in cinematic copper-and-deep-blue tones",
    size="1536x1024",
)
```

### Assemble a still-image motion MP4 with subtitles

```bash
# Source: https://ffmpeg.org/ffmpeg-filters.html
ffmpeg -loop 1 -i hero.png -i narration.mp3 \
  -vf "zoompan=z='min(max(zoom,pzoom)+0.0015,1.5)':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)',subtitles=chapter01.srt:charenc=UTF-8" \
  -c:v libx264 -pix_fmt yuv420p -shortest chapter01.mp4
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Treat the story material package page as a read-only overview. [VERIFIED: packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryMaterialPackageManagement.tsx] | Use the same page for explicit package-scoped production/import/status actions, not a new global workbench. [VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md] | Locked on 2026-04-30 in Phase 36 context. [VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md] | Planning should extend the existing page instead of creating a separate admin AI product surface. [VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md] |
| Assume image generation must run inside the backend. [VERIFIED: phase scope tension between local generation and current backend code paths] | Use a hybrid flow: local Codex/OpenAI still-image generation is acceptable, while backend remains authoritative for upload, asset registration, and package promotion. [VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java] | Locked on 2026-04-30 in Phase 36 context. [VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md] | The planner should not block Phase 36 on backend-native OpenAI image execution. [VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md] |
| Use only the old Images API mindset for OpenAI image work. [CITED: https://platform.openai.com/docs/guides/images/image-generation] | Current official docs support both the Image API and the Responses API image tool for GPT Image models. [CITED: https://platform.openai.com/docs/guides/images/image-generation; https://platform.openai.com/docs/guides/tools-image-generation/] | Current docs crawled in 2026. [CITED: https://platform.openai.com/docs/guides/images/image-generation] | Local helper scripts can use either API shape, but the repo still needs only local-file outputs plus backend import for this phase. [CITED: https://platform.openai.com/docs/guides/images/image-generation][VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md] |

**Deprecated/outdated:**

- Using the generic AI workbench’s direct finalize-to-`published` flow as the sole workflow for package materials is outdated for Phase 36 because it bypasses package binding and explicit `uploaded -> approved -> published` promotion. [VERIFIED: packages/admin/aoxiaoyou-admin-ui/src/components/ai/AiCreativeWorkbenchModal.tsx; .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md]

## Assumptions Log

All factual claims in this research were verified in the repo/session or cited from current official documentation. No `[ASSUMED]` claims are being passed to the planner. [VERIFIED: this research artifact]

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| None | No unverified factual claims remain. [VERIFIED: this research artifact] | Assumptions Log | Low |

## Open Questions (RESOLVED)

1. **Should locally generated still images also be mirrored into `ai_generation_jobs` and `ai_generation_candidates`, or is a dedicated material version journal enough?** [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java; scripts/local/mysql/init/31-phase-18-ai-capability-center-foundation.sql; scripts/local/mysql/init/47-phase-33-story-material-package-model.sql]
What we know: The AI subsystem already stores candidates, request logs, and finalized asset IDs, but local image import does not naturally produce a provider-backed job record. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java; scripts/local/seed-phase-33-material-assets.ps1]
RESOLVED outcome: Locally generated still images do not need mirrored `ai_generation_jobs` or `ai_generation_candidates` rows. The dedicated material version journal is the non-negotiable source for rollback and lineage, and versions may optionally link `ai_job_id` / `ai_candidate_id` only when the output actually came through backend AI or CosyVoice flows. [VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md]

2. **Can `sfx_reward_unlock` legitimately be produced through CosyVoice, or should it be treated as a manual import asset in this phase?** [VERIFIED: docs/content-packages/east-west-war-and-coexistence/content-manifest.json; docs/content-packages/east-west-war-and-coexistence/audio-scripts.md][CITED: https://www.alibabacloud.com/help/en/model-studio/text-to-speech]
What we know: Official CosyVoice docs position the service as text-to-speech, voice cloning, and voice design rather than general Foley/SFX synthesis. [CITED: https://www.alibabacloud.com/help/en/model-studio/text-to-speech; https://www.alibabacloud.com/help/en/model-studio/cosyvoice-voice-list]
RESOLVED outcome: `sfx_reward_unlock` is required for MAT-03. It should use the same real CosyVoice or approved backend audio-provider path when the output is feasible, or else go through an explicit manual-import branch that leaves the item unbound and unpublished until a real file is imported. It must never silently publish substitute audio, and the plans must provide a concrete path for both the provider attempt and the explicit manual-import branch. [VERIFIED: .planning/REQUIREMENTS.md; .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md]

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Java | Admin backend build/test | ✓ [VERIFIED: workstation env audit 2026-04-30] | `17.0.12` [VERIFIED: workstation env audit 2026-04-30] | — |
| Maven | Admin backend build/test | ✓ [VERIFIED: workstation env audit 2026-04-30] | `3.8.8` [VERIFIED: workstation env audit 2026-04-30] | — |
| Node.js | Admin UI type-check/build | ✓ [VERIFIED: workstation env audit 2026-04-30] | `v25.9.0` [VERIFIED: workstation env audit 2026-04-30] | — |
| npm | Admin UI type-check/build | ✓ [VERIFIED: workstation env audit 2026-04-30] | `11.13.0` [VERIFIED: workstation env audit 2026-04-30] | — |
| Python | Local slicing/import tooling | ✓ [VERIFIED: workstation env audit 2026-04-30] | `3.12.4` [VERIFIED: workstation env audit 2026-04-30] | — |
| Pillow | Local board slicing/export | ✓ [VERIFIED: python import audit] | `10.3.0` installed, `12.2.0` latest [VERIFIED: python import audit; python -m pip index versions Pillow] | — |
| requests | Local authenticated upload/import scripts | ✓ [VERIFIED: python import audit] | `2.32.5` [VERIFIED: python import audit] | PowerShell `System.Net.Http` as a thin transport fallback. [VERIFIED: scripts/local/seed-phase-33-material-assets.ps1] |
| MySQL CLI | Smoke and schema verification | ✓ [VERIFIED: workstation env audit 2026-04-30] | `8.0.41` [VERIFIED: workstation env audit 2026-04-30] | — |
| Docker | Local MySQL/Mongo bootstrap | ✓ [VERIFIED: workstation env audit 2026-04-30] | `27.1.1` [VERIFIED: workstation env audit 2026-04-30] | — |
| `ffmpeg` | MAT-04 still-image motion videos + subtitles | ✗ [VERIFIED: workstation env audit 2026-04-30] | — | No practical fallback for the required MP4 slice. [CITED: https://ffmpeg.org/ffmpeg-filters.html] |
| ImageMagick / `magick` | Optional image manipulation | ✗ [VERIFIED: workstation env audit 2026-04-30] | — | Use Pillow; no need to block the phase on ImageMagick. [VERIFIED: python import audit][CITED: https://pillow.readthedocs.io/en/stable/reference/Image.html] |

**Missing dependencies with no fallback:**

- `ffmpeg` is a Phase 36 blocker for MAT-04 unless the plan explicitly defers or gates the video slice. [VERIFIED: workstation env audit 2026-04-30][CITED: https://ffmpeg.org/ffmpeg-filters.html]

**Missing dependencies with fallback:**

- `magick` is absent, but Pillow is already installed and sufficient for the required crop/export workflow. [VERIFIED: workstation env audit 2026-04-30; python import audit][CITED: https://pillow.readthedocs.io/en/stable/reference/Image.html]

**Secret preflight note:** This research intentionally did not print or inspect provider/COS secrets. The plan should include a non-echoing runtime preflight that validates required env/config presence without exposing values. [VERIFIED: AGENTS.md; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/ai/config/AiSecretCryptoService.java]

## Validation Architecture

### Test Framework

| Property | Value |
|----------|-------|
| Framework | JUnit 5 via `spring-boot-starter-test` for backend, plus TypeScript compiler/type-check for admin UI. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/pom.xml; packages/admin/aoxiaoyou-admin-ui/package.json] |
| Config file | None dedicated; Maven default test discovery for backend and `package.json` scripts for UI. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/pom.xml; packages/admin/aoxiaoyou-admin-ui/package.json] |
| Quick run command | `mvn -q -Dtest=AdminAiServiceImplTest,MediaIntakeServiceTest,AdminContentManagementServiceTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminAiServiceImplTest.java; packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/MediaIntakeServiceTest.java; packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminContentManagementServiceTest.java] |
| Full suite command | `mvn test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` and `npm run type-check --prefix packages/admin/aoxiaoyou-admin-ui` [VERIFIED: packages/admin/aoxiaoyou-admin-backend/pom.xml; packages/admin/aoxiaoyou-admin-ui/package.json] |

### Phase Requirements → Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| MAT-01 | Local import binds uploaded still images to package items with prompt/UTF-8 provenance and immutable version records. [VERIFIED: .planning/REQUIREMENTS.md] | backend service/integration | `mvn -q -Dtest=AdminStoryMaterialProductionServiceTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | ❌ Wave 0 |
| MAT-02 | Board parent asset can be sliced into multiple child assets while preserving crop metadata and parent provenance. [VERIFIED: .planning/REQUIREMENTS.md] | local tooling + backend integration | `python scripts/local/material-production/phase36-slice-board.py --config docs/content-packages/east-west-war-and-coexistence/production-runs/ch01-board.json` | ❌ Wave 0 |
| MAT-03 | CosyVoice narration jobs upload assets, preserve history, and bind approved audio to package items. [VERIFIED: .planning/REQUIREMENTS.md] | backend service/unit | `mvn -q -Dtest=AdminAiServiceImplTest,AdminStoryMaterialProductionServiceTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | ⚠️ Partial existing coverage |
| MAT-04 | Still-image MP4 builder emits subtitle-burned COS-backed videos and binds them to the package. [VERIFIED: .planning/REQUIREMENTS.md] | smoke/manual + local tooling | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-36-material-production.ps1 -IncludeVideo` | ❌ Wave 0 |
| MAT-05 | Status promotion and rollback move items through `planned -> uploaded -> approved -> published` and back to prior versions safely. [VERIFIED: .planning/REQUIREMENTS.md] | backend service/integration | `mvn -q -Dtest=AdminStoryMaterialProductionServiceTest,AdminStoryMaterialPackageServiceTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | ❌ Wave 0 |

### Sampling Rate

- **Per task commit:** `mvn -q -Dtest=AdminAiServiceImplTest,MediaIntakeServiceTest,AdminContentManagementServiceTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` [VERIFIED: existing test files]
- **Per wave merge:** `mvn test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` and `npm run type-check --prefix packages/admin/aoxiaoyou-admin-ui` [VERIFIED: packages/admin/aoxiaoyou-admin-backend/pom.xml; packages/admin/aoxiaoyou-admin-ui/package.json]
- **Phase gate:** Run a new `scripts/local/smoke-phase-36-material-production.ps1` against live local services, with the video branch enabled only after `ffmpeg` is installed and verified. [VERIFIED: scripts/local/smoke-phase-33-flagship-package.ps1; scripts/local/smoke-phase-34-public-runtime.ps1; workstation env audit 2026-04-30]

### Wave 0 Gaps

- [ ] `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminStoryMaterialProductionServiceTest.java` — covers MAT-01, MAT-03, MAT-05. [VERIFIED: missing by current test inventory]
- [ ] `scripts/local/material-production/phase36-slice-board.py` — deterministic crop/export and metadata emission for MAT-02. [VERIFIED: missing by current script inventory]
- [ ] `scripts/local/smoke-phase-36-material-production.ps1` — end-to-end local smoke for import/promotion/video gating. [VERIFIED: missing by current script inventory]
- [ ] `ffmpeg` install + `subtitles` filter verification — required before MAT-04 execution. [VERIFIED: workstation env audit 2026-04-30][CITED: https://ffmpeg.org/ffmpeg-filters.html]
- [ ] Package-scoped admin UI action coverage — no dedicated UI test harness exists today, so Phase 36 should at minimum rely on type-check plus smoke validation. [VERIFIED: packages/admin/aoxiaoyou-admin-ui/package.json]

## Security Domain

Security enforcement is enabled in `.planning/config.json`, so Phase 36 planning must include security controls explicitly. [VERIFIED: .planning/config.json]

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | yes [VERIFIED: .planning/config.json; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/config/AdminAuthInterceptor.java] | Admin bearer-token auth on existing admin endpoints; do not add anonymous production/import routes. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/config/AdminAuthInterceptor.java] |
| V3 Session Management | yes [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/resources/application.yml] | Reuse existing JWT access/refresh token handling and avoid side-channel secret transport in scripts. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/resources/application.yml; AGENTS.md] |
| V4 Access Control | yes [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java] | Keep package production, promotion, and rollback endpoints admin-only, with owner/super-admin checks where job history is reused. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java] |
| V5 Input Validation | yes [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminAiGenerationJobCreateRequest.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminAiCandidateFinalizeRequest.java] | Continue using validated request DTOs and UTF-8 file-driven payload ingestion; do not trust local tool JSON blindly. [VERIFIED: AGENTS.md; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminAiGenerationJobCreateRequest.java] |
| V6 Cryptography | yes [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/ai/config/AiSecretCryptoService.java] | Keep provider secrets in env/runtime config and encrypted at rest through `AiSecretCryptoService`; never hardcode keys in scripts or docs. [VERIFIED: AGENTS.md; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/ai/config/AiSecretCryptoService.java] |

### Known Threat Patterns for this stack

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Provider/COS secret leakage in local tooling or docs | Information Disclosure | Load secrets from environment/runtime config only; validate presence without echoing values; keep tracked scripts key-free. [VERIFIED: AGENTS.md; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/ai/config/AiSecretCryptoService.java] |
| SSRF through provider base URLs or voice-clone source URLs | Tampering / Information Disclosure | Reuse `AiOutboundUrlGuard` for provider URLs and any public source URL ingestion. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/ai/provider/AiOutboundUrlGuard.java] |
| Unauthorized rollback or publish actions | Elevation of Privilege | Put promotion/rollback behind authenticated admin endpoints and preserve actor attribution in version records. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/config/AdminAuthInterceptor.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/MediaIntakeService.java] |
| Encoding corruption in subtitles/prompts/scripts | Tampering / Denial of Service | Use UTF-8 files end-to-end and avoid inline PowerShell Chinese literals. [VERIFIED: AGENTS.md; scripts/local/seed-phase-33-material-assets.ps1] |
| Asset lineage loss during replacement | Repudiation / Tampering | Make every replacement create a new immutable material version and keep rollback as a pointer flip. [VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md; scripts/local/mysql/init/47-phase-33-story-material-package-model.sql] |

## Sources

### Primary (HIGH confidence)

- Local repo inspection of Phase 36 scope, requirements, and roadmap: `.planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md`, `.planning/REQUIREMENTS.md`, `.planning/STATE.md`, `.planning/ROADMAP.md`, `.planning/config.json`
- Local repo inspection of material package, AI, media, and UI code paths: `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminStoryMaterialPackageController.java`, `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminStoryMaterialPackageServiceImpl.java`, `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminAiController.java`, `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java`, `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/MediaIntakeService.java`, `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/CosAssetStorageService.java`, `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryMaterialPackageManagement.tsx`, `packages/admin/aoxiaoyou-admin-ui/src/components/ai/AiCreativeWorkbenchModal.tsx`
- Local repo inspection of schema and scripts: `scripts/local/mysql/init/31-phase-18-ai-capability-center-foundation.sql`, `scripts/local/mysql/init/47-phase-33-story-material-package-model.sql`, `scripts/local/seed-phase-33-material-assets.ps1`, `scripts/local/smoke-phase-33-flagship-package.ps1`
- OpenAI official docs: `https://platform.openai.com/docs/guides/images/image-generation`, `https://platform.openai.com/docs/guides/tools-image-generation/`, `https://platform.openai.com/docs/api-reference/images/create`
- FFmpeg official docs: `https://ffmpeg.org/ffmpeg-filters.html`, `https://ffmpeg.org/download.html`
- Pillow official docs: `https://pillow.readthedocs.io/en/stable/reference/Image.html`
- Alibaba Cloud Model Studio official docs: `https://www.alibabacloud.com/help/en/model-studio/text-to-speech`, `https://www.alibabacloud.com/help/en/model-studio/cosyvoice-voice-list`, `https://www.alibabacloud.com/help/en/model-studio/getting-started/models`

### Secondary (MEDIUM confidence)

- Context7 `openai-node` docs for `client.images.generate` usage. [VERIFIED: `npx --yes ctx7@latest docs /openai/openai-node "image generation"`]
- Context7 `pillow` docs for `Image.crop` and `Image.save` patterns. [VERIFIED: `npx --yes ctx7@latest docs /python-pillow/pillow "Image.crop Image.save"`]

### Tertiary (LOW confidence)

- None.

## Metadata

**Confidence breakdown:**

- Standard stack: MEDIUM - Repo-backed backend/media/UI recommendations are high confidence, but the MAT-04 toolchain is gated by a missing local `ffmpeg` dependency. [VERIFIED: packages/admin/aoxiaoyou-admin-backend/pom.xml; packages/admin/aoxiaoyou-admin-ui/package.json; workstation env audit 2026-04-30]
- Architecture: HIGH - The recommended flow is directly constrained by verified code paths and locked Phase 36 decisions. [VERIFIED: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-CONTEXT.md; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/MediaIntakeService.java]
- Pitfalls: HIGH - The most important risks are directly observable in the current schema, UI, and service implementations. [VERIFIED: scripts/local/mysql/init/47-phase-33-story-material-package-model.sql; packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java; packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryMaterialPackageManagement.tsx]

**Research date:** 2026-04-30
**Valid until:** 2026-05-14 for provider/tooling specifics, 2026-05-30 for repo-structure findings
