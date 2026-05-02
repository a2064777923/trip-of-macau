---
phase: 36
status: partially_unblocked
updated: 2026-05-02
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
| Phase live smoke | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-36-material-production.ps1` | BLOCKED: missing runtime bearer token env; also reports ffmpeg subtitle blocker |
| Admin backend compile after live TTS fix | `mvn -q -DskipTests compile` in `packages/admin/aoxiaoyou-admin-backend` | PASS |
| Live TTS/COS smoke | admin API login, `POST /api/admin/v1/ai/generation-jobs`, finalize candidate, `HEAD` generated COS URL | PASS: generated `audio/mpeg`, finalized `content_assets.id=333055`, COS `HEAD 200` |
| Live Phase 36 audio batch | `python scripts/local/material-production/phase36-batch-produce.py --manifest docs/content-packages/east-west-war-and-coexistence/content-manifest.json --batch .planning/quick/260502-urn-phase-36-live-audio-generation-and-cos-u/phase36-audio-batch.json --backend http://127.0.0.1:8081 --confirm-production --upload --promote published` | PASS: 6 audio assets generated, finalized, bound, and promoted |

`smoke-phase-36-material-production.ps1` prints `Phase 36 material production smoke passed` only after live package/API/version/COS/rollback criteria pass. In this run, it did not print that success line because live dependencies are missing.

## Historical Dependency Snapshot

From the original `smoke-phase-36-material-production.ps1 -ValidateOnly` run:

- Runtime admin bearer token env was not loaded.
- OpenAI-compatible image key env was not loaded.
- Explicit COS ready env was not loaded.
- ffmpeg subtitle support was unavailable.

No secret values were printed or committed.

## Representative Item Evidence

Required representative item keys:

- `story_cover_copper_mirror`
- `hero_ch01_ama_coast`
- `pickup_ming_coastal_token`
- `title_harbour_witness_final`
- `audio_ch01_narration`
- `sfx_reward_unlock`

Expected live evidence fields for representative material checks:

- `itemKey`
- `versionId`
- `assetId`
- `promotionStatus`
- `localPath`
- `cosObjectKey`
- `canonicalUrl`

2026-05-02 live audio evidence:

| Item | Version | Asset | Status | COS check |
| --- | ---: | ---: | --- | --- |
| `audio_ch01_narration` | 64 | 333062 | `published` | `HEAD 200 audio/mpeg`, 661531 bytes |
| `audio_ch02_narration` | 65 | 333063 | `published` | `HEAD 200 audio/mpeg`, 522811 bytes |
| `audio_ch03_narration` | 66 | 333064 | `published` | `HEAD 200 audio/mpeg`, 527611 bytes |
| `audio_ch04_narration` | 67 | 333065 | `published` | `HEAD 200 audio/mpeg`, 502651 bytes |
| `audio_ch05_narration` | 68 | 333066 | `published` | `HEAD 200 audio/mpeg`, 518491 bytes |
| `sfx_reward_unlock` | 69 | 333067 | `published` | `HEAD 200 audio/mpeg`, 259291 bytes |

Additional sanity checks:

- First three generated MP3 URLs returned `GET 206 audio/mpeg` with MP3 header bytes `49 44 33 03`.
- `ai_generation_jobs.id=63..68` contain the actual chapter scripts from `audio-scripts.md`, not unresolved `{{scriptText}}` placeholders.
- `ai_generation_candidates.id=34..39` are finalized and have both provider provenance and storage URLs normalized to HTTPS.
- The report `.planning/quick/260502-urn-phase-36-live-audio-generation-and-cos-u/phase36-production-report.json` contains no provider temporary signing markers and no temporary provider download host.

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
| MAT-01 | BLOCKED | Backend/import tooling and admin UI exist; live image generation/upload still needs representative image import evidence once local PNGs are available. |
| MAT-02 | BLOCKED | Board slicing tool/config and dry-run exist; generated parent board files and live child imports are pending. |
| MAT-03 | PASS | Live 百煉 CosyVoice generation, backend download, COS storage, candidate finalization, material-package binding, and publish were verified for all 5 narration tracks plus `sfx_reward_unlock`. |
| MAT-04 | BLOCKED | `phase36-video-jobs.json` and `phase36-build-video.ps1` exist; live MP4 output is blocked by `FFMPEG_SUBTITLES_UNAVAILABLE`. |
| MAT-05 | PARTIAL | Version schema/API/tests pass; live audio publish and COS `HEAD` evidence now pass. Full requirement remains blocked for image/board/video material versions and rollback smoke across representative non-audio assets. |

## Conclusion

Phase 36 implementation artifacts are in place, and the live audio/COS slice is now proven end-to-end. The phase is still not ready to close because image board production/import and subtitle-capable video generation remain separate blockers.
