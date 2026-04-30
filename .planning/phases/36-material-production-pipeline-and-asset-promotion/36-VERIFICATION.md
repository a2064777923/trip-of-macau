---
phase: 36
status: blocked
updated: 2026-04-30
---

# Phase 36 Verification — Material Production Pipeline and Asset Promotion

## Automated Checks

| Check | Command | Result |
| --- | --- | --- |
| Backend production service tests | `mvn -q -Dtest=AdminStoryMaterialProductionServiceTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | PASS |
| Admin UI build | `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` | PASS |
| UTF-8 manifest/batch preflight | `python scripts/local/material-production/phase36-preflight.py --manifest docs/content-packages/east-west-war-and-coexistence/content-manifest.json --batch docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-batch.json --dry-run` | PASS |
| Board slicing dry-run | `python scripts/local/material-production/phase36-slice-board.py --config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-board-slices.json --dry-run` | PASS |
| MAT-04 video builder validate-only | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/material-production/phase36-build-video.ps1 -Config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-video-jobs.json -ValidateOnly` | BLOCKED: `FFMPEG_SUBTITLES_UNAVAILABLE` |
| Phase smoke validate-only | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-36-material-production.ps1 -ValidateOnly` | PASS |
| Phase live smoke | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-36-material-production.ps1` | BLOCKED: missing `PHASE36_ADMIN_BEARER_TOKEN`; also reports ffmpeg subtitle blocker |

`smoke-phase-36-material-production.ps1` prints `Phase 36 material production smoke passed` only after live package/API/version/COS/rollback criteria pass. In this run, it did not print that success line because live dependencies are missing.

## Dependency Snapshot

From `smoke-phase-36-material-production.ps1 -ValidateOnly`:

- `PHASE36_ADMIN_BEARER_TOKEN`: false
- `OPENAI_API_KEY`: false
- `PHASE36_COS_READY`: false
- `ffmpegSubtitles`: false

No secret values were printed or committed.

## Representative Item Evidence

Required representative item keys:

- `story_cover_copper_mirror`
- `hero_ch01_ama_coast`
- `pickup_ming_coastal_token`
- `title_harbour_witness_final`
- `audio_ch01_narration`
- `sfx_reward_unlock`

Current live item/version evidence is pending because live smoke could not authenticate without `PHASE36_ADMIN_BEARER_TOKEN`.

Expected live evidence fields once auth/COS/provider dependencies are loaded:

- `itemKey`
- `versionId`
- `assetId`
- `promotionStatus`
- `localPath`
- `cosObjectKey`
- `canonicalUrl`

## Board-Slice Evidence

Dry-run board slicing writes `phase36-board-slice-report.json` and proves all configured slice definitions are readable. Current rows include child targets such as:

- `pickup_ming_coastal_token`
- `pickup_fisher_net_fragment`
- `pickup_tax_contract_page`
- `title_harbour_witness_final`

The current dry-run report has `status: board_missing` for source boards because generated board images have not been produced/imported in this shell. Therefore `parentItemKey`, `parentVersionId`, and `cropRect` configuration is present, but live child imports remain pending.

## Requirement Status

| Requirement | Status | Evidence |
| --- | --- | --- |
| MAT-01 | BLOCKED | Backend/import tooling and admin UI exist; live image generation/upload is blocked by missing `OPENAI_API_KEY`, `PHASE36_ADMIN_BEARER_TOKEN`, and COS readiness. |
| MAT-02 | BLOCKED | Board slicing tool/config and dry-run exist; generated parent board files and live child imports are pending. |
| MAT-03 | BLOCKED | Audio orchestration exists; `audio_ch01_narration` and `sfx_reward_unlock` are not proven as real provider outputs in this shell. `sfx_reward_unlock` must remain `manual_import_required` / `retry_required` unless a real file is imported. |
| MAT-04 | BLOCKED | `phase36-video-jobs.json` and `phase36-build-video.ps1` exist; live MP4 output is blocked by `FFMPEG_SUBTITLES_UNAVAILABLE`. |
| MAT-05 | BLOCKED | Version schema/API/tests pass; live publish/rollback and COS `HEAD` evidence is blocked by missing `PHASE36_ADMIN_BEARER_TOKEN` and live imported versions. |

## Conclusion

Phase 36 implementation artifacts are in place, but the phase is not ready to close. Live external dependencies must be loaded and re-smoked before `REQUIREMENTS.md` or `ROADMAP.md` marks MAT-01 through MAT-05 complete.
