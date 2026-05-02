---
status: complete
phase: 36-material-production-pipeline-and-asset-promotion
source:
  - 36-01-SUMMARY.md
  - 36-02-SUMMARY.md
  - 36-03-SUMMARY.md
  - 36-04-SUMMARY.md
  - 36-05-SUMMARY.md
started: 2026-04-30T07:59:47Z
updated: 2026-05-02T12:12:00Z
---

## Current Test

[testing complete]

## Tests

### 1. Phase 36 Smoke Dependency Gate
expected: Running `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-36-material-production.ps1 -ValidateOnly` should pass local contract checks, print dependency readiness without exposing secrets, and clearly report blockers for missing admin token, provider/COS readiness, and ffmpeg subtitle support. Running the live smoke without required environment should stop before mutating production data instead of printing `Phase 36 material production smoke passed`.
result: pass

### 2. Package-Scoped Material Production UI
expected: In the admin story material package page, operators should see package-scoped production controls for preflight, local import, narration generation, chapter video creation, and version history without a new global image-2 production workbench.
result: pass
verified: |
  Reproduced the admin detail failure against `/api/admin/v1/content/material-packages/1`, then applied the missing local Phase 36 migration and restarted the 8081 admin backend with the latest compiled classes. The detail API now returns code=0 with 54 package items, and Playwright verification confirms the story material package detail page opens with package-scoped production controls visible.
root_cause: |
  Local MySQL had not applied `scripts/local/mysql/init/51-phase-36-material-production-versioning.sql`, so the Phase 36 item version table and pointer columns were missing. After the migration, the old 8081 process still had stale loaded classes and threw a Lombok builder `NoSuchMethodError`, so the backend also had to be restarted.
artifacts:
  - output/playwright/phase36-material-packages-detail.png

### 3. Version History and Rollback Contract
expected: The admin backend should expose package-scoped routes for `/production/preflight`, `/production/import`, `/production/promote`, `/production/rollback`, and item `/versions`; version rows should keep provider/model/status/path/COS lineage and rollback should flip the current pointer without deleting history.
result: pass
reported: |
  不可預覽的、損毀的素材仍佔着素材包的位置；故事路線與章節覆寫、章節管理、內容積木庫、互動與任務模板庫、體驗規則治理中心的使命邊界不清；體驗流程工作台版面多處變形；媒體資源中很多內容打不開似乎丟失。
severity: major
verified_versions: |
  User reported every "查看版本" drawer displayed "無此資料". Root cause was that the Phase 36 version journal table existed after migration but no baseline rows had been created for the 54 already-seeded material package items. The migration now idempotently backfills v1 `seed_baseline` rows from `story_material_package_items` and sets `current_version_id/current_version_no/last_produced_at`.
  Direct API check for `/api/admin/v1/content/material-packages/1/items/1/versions` returns code=0 with v1 lineage including provider/model/path/COS URL. Playwright check confirms the drawer shows v1 instead of empty state.
  Follow-up UI check fixed the drawer asset column: image/icon assets now render a thumbnail preview with Ant Image preview support, video/audio assets render inline controls when a canonical URL is available, Lottie/JSON/other assets show a constrained type card, and long canonical URLs/COS paths are middle-truncated inside the asset cell with full values available through tooltip or link.
pending: |
  Rollback and live import/promote mutation smoke still need explicit operator verification before marking the whole rollback contract complete.
follow_up_fixes: |
  2026-05-02 UAT fix pass addressed the reported admin usability issues without deleting material package requirement slots. Story material package rows now distinguish usable published assets, pending production slots, missing public URLs, and unpublished versions with counters and filters. Media resources now show no-public-link / suspected-missing state and broaden preview detection beyond MIME-only metadata. Story/content IA now explains that chapter management owns chapter content, story route override owns story-mode inheritance/overrides, content blocks own reusable display content, templates own behavior specs, and governance owns cross-domain checks. Experience flow/template/governance workspaces received browser-driven layout fixes, table scroll/fixed-action improvements, and governance row-key warnings were removed.
artifacts:
  - output/playwright/phase36-material-version-drawer.png
  - output/playwright/phase36-material-version-preview.png
  - output/playwright/phase36-material-package-after.png
  - output/playwright/phase36-media-library-after.png
  - output/playwright/phase36-experience-flow-layout-after.png
  - output/playwright/phase36-experience-template-library-after-2.png
  - output/playwright/phase36-experience-governance-after-2.png
  - output/playwright/phase36-storyline-mode-after.png
  - output/playwright/phase36-chapters-after.png

### 4. Local Production Tooling
expected: The Phase 36 local tooling should support UTF-8 preflight, explicit `--confirm-production --upload --promote published` live commands, board slicing with parent/crop metadata, and manual-import or retry gating for `sfx_reward_unlock`.
result: pass

### 5. MAT-04 Video Gate
expected: `phase36-video-jobs.json` should define five concrete chapter video jobs, and `phase36-build-video.ps1 -ValidateOnly` should fail with `FFMPEG_SUBTITLES_UNAVAILABLE` on a workstation without ffmpeg subtitle support rather than producing placeholder videos.
result: pass

### 6. Requirement and Roadmap Honesty
expected: `REQUIREMENTS.md`, `STATE.md`, `36-VERIFICATION.md`, and `36-HANDOFF.md` should make it clear that Phase 36 implementation plans are executed but MAT-01 through MAT-05 remain blocked until live admin auth, provider/COS, imported asset versions, rollback smoke, and ffmpeg subtitle video generation are verified.
result: pass

## Summary

total: 6
passed: 6
issues: 0
pending: 0
skipped: 0
blocked: 0

## Gaps

- truth: "In the admin story material package page, operators should see package-scoped production controls for preflight, local import, narration generation, chapter video creation, and version history without a new global image-2 production workbench."
  status: resolved
  reason: "Applied missing Phase 36 schema migration and restarted the admin backend so the detail endpoint uses current classes."
  severity: resolved
  test: 2
  artifacts:
    - output/playwright/phase36-material-packages-detail.png
  missing: []

- truth: "Material package version history should expose usable, previewable asset lineage and should not make planned/missing/broken assets look equivalent to usable published material."
  status: partial
  reason: "Material package UI now labels and filters requirement slots by asset usability, and media resource UI surfaces missing/no-public-link assets instead of making them look openable. Live COS repair of genuinely missing files remains a data/ops follow-up."
  severity: minor
  test: 3
  artifacts:
    - output/playwright/phase36-material-package-after.png
    - output/playwright/phase36-media-library-after.png
  missing:
    - "Operator must decide whether to re-upload, backfill canonicalUrl, or remove truly orphaned content_assets records."

- truth: "Story/content admin IA should make each workspace's mission clear and avoid overlapping shell pages."
  status: resolved
  reason: "Added page-level Traditional Chinese guidance that separates chapter content editing, story-mode route/override editing, reusable content blocks, behavior templates, and governance checks."
  severity: resolved
  test: 3
  artifacts:
    - output/playwright/phase36-storyline-mode-after.png
    - output/playwright/phase36-chapters-after.png
  missing: []

- truth: "Experience flow, template, governance, and media workspaces should be visually coherent and usable in browser."
  status: resolved
  reason: "Browser-driven fixes tightened experience flow layout, moved template list to full-width display, added operator guidance, fixed governance duplicate/deprecated row keys, and verified a fresh browser session has no console errors on template/governance pages."
  severity: resolved
  test: 3
  artifacts:
    - output/playwright/phase36-experience-flow-layout-after.png
    - output/playwright/phase36-experience-template-library-after-2.png
    - output/playwright/phase36-experience-governance-after-2.png
  missing: []
