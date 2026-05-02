---
phase: 36
status: complete
updated: 2026-05-03
---

# Phase 36 Verification — Material Production Pipeline and Asset Promotion

## Automated Checks

| Check | Command | Result |
| --- | --- | --- |
| Backend production service tests | `mvn -q -Dtest=AdminStoryMaterialProductionServiceTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | PASS |
| Admin UI build | `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` | PASS |
| UTF-8 manifest/batch preflight | `python scripts/local/material-production/phase36-preflight.py --manifest docs/content-packages/east-west-war-and-coexistence/content-manifest.json --batch docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-batch.json --dry-run` | PASS |
| Board slicing dry-run | `python scripts/local/material-production/phase36-slice-board.py --config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-board-slices.json --dry-run` | PASS |
| Python tooling compile | `python -m py_compile scripts/local/material-production/phase36-import-assets.py scripts/local/material-production/phase36-batch-produce.py scripts/local/material-production/phase36-slice-board.py` | PASS |
| Video builder validate-only | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/material-production/phase36-build-video.ps1 -Config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-video-jobs.json -ValidateOnly` | PASS |
| Live chapter video build/import | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/material-production/phase36-build-video.ps1 -Config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-video-jobs.json -ConfirmProduction -Upload -Promote published` | PASS |
| Phase live smoke with videos | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-36-material-production.ps1 -IncludeVideo` | PASS: printed `Phase 36 material production smoke passed` |
| Admin backend compile after live TTS fix | `mvn -q -DskipTests compile` in `packages/admin/aoxiaoyou-admin-backend` | PASS |
| Live TTS/COS smoke | admin API login, `POST /api/admin/v1/ai/generation-jobs`, finalize candidate, `HEAD` generated COS URL | PASS |
| Live Phase 36 audio batch | `python scripts/local/material-production/phase36-batch-produce.py --manifest docs/content-packages/east-west-war-and-coexistence/content-manifest.json --batch .planning/quick/260502-urn-phase-36-live-audio-generation-and-cos-u/phase36-audio-batch.json --backend http://127.0.0.1:8081 --confirm-production --upload --promote published` | PASS |
| Live image and board import | `python scripts/local/material-production/phase36-batch-produce.py --manifest docs/content-packages/east-west-war-and-coexistence/content-manifest.json --batch docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-image2-redo-batch.json --backend http://127.0.0.1:8081 --confirm-production --upload --promote published`; then `python scripts/local/material-production/phase36-slice-board.py --config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-board-slices.json --manifest docs/content-packages/east-west-war-and-coexistence/content-manifest.json --confirm-production --upload --promote published` | PASS |
| Image endpoint probe | Live `POST https://api.suqis.com/v1/images/generations` with model `image-2` | Provider returned HTTP 400 `images endpoint requires an image model`; same base URL accepted `gpt-image-1`, which was used for completed image evidence |

`smoke-phase-36-material-production.ps1 -IncludeVideo` passes only after live package/API/version/COS/rollback criteria pass and video builder validate-only succeeds.

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

## Live Audio Evidence

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
- `ai_generation_candidates.id=34..39` are finalized and have provider provenance plus storage URLs normalized to HTTPS.
- The docs report `docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-production-report.json` merges the verified image, board-slice, audio, and video rows.

## Live Still-Image Evidence

| Item | Version | Asset | Status | COS check |
| --- | ---: | ---: | --- | --- |
| `story_cover_copper_mirror` | 70 | 333068 | `published` | `HEAD 200 image/png`, 3162013 bytes |
| `story_banner_route_map` | 71 | 333069 | `published` | `HEAD 200 image/png`, 2638731 bytes |
| `hero_ch01_ama_coast` | 72 | 333070 | `published` | `HEAD 200 image/png`, 3209692 bytes |
| `hero_ch02_lilau_boundary` | 73 | 333071 | `published` | `HEAD 200 image/png`, 3181370 bytes |
| `hero_ch03_hill_watch` | 74 | 333072 | `published` | `HEAD 200 image/png`, 3120856 bytes |
| `hero_ch04_monte_fort` | 75 | 333073 | `published` | `HEAD 200 image/png`, 3080153 bytes |
| `hero_ch05_senado_coexistence` | 76 | 333074 | `published` | `HEAD 200 image/png`, 3275555 bytes |
| `poster_video_fallback` | 77 | 333075 | `published` | `HEAD 200 image/png`, 2531682 bytes |

## Board-Slice Evidence

Live board slicing writes `phase36-board-slice-report.json` and proves all configured slice definitions are readable and importable through backend package-scoped COS upload. Current rows include child targets such as:

- `pickup_ming_coastal_token`
- `pickup_fisher_net_fragment`
- `pickup_tax_contract_page`
- `title_harbour_witness_final`
- `title_history_restoration_master`
- `title_harbour_history_grandmaster`

The live report contains 35 rows with `status: sliced`, `promotionStatus: published`, concrete `versionId`, `assetId`, `cropRect`, and `canonicalUrl`. The title badge board covers all 15 configured title icons.

Representative rows:

| Item | Version | Asset | Crop | Status |
| --- | ---: | ---: | --- | --- |
| `pickup_ming_coastal_token` | 150 | 333146 | `0,0,512,512` | `published` |
| `pickup_complete_copper_mirror` | 169 | 333165 | `0,512,512,512` | `published` |
| `title_harbour_witness_final` | 182 | 333178 | `0,1536,384,384` | `published` |
| `title_history_restoration_master` | 183 | 333179 | `384,1536,384,384` | `published` |
| `title_harbour_history_grandmaster` | 184 | 333180 | `768,1536,384,384` | `published` |

## Live Video Evidence

Five chapter videos were built from the published hero stills plus narration audio and imported through the backend package production API. All were promoted to `published` and verified with COS `HEAD 200 video/mp4`.

| Item | Version | Asset | Duration | COS check |
| --- | ---: | ---: | ---: | --- |
| `video_ch01_mirror_sea_clash` | 191 | 333188 | 33.07s | `HEAD 200 video/mp4`, 1327567 bytes |
| `video_ch02_south_bay_boundary` | 192 | 333189 | 26.14s | `HEAD 200 video/mp4`, 995305 bytes |
| `video_ch03_hill_watch` | 193 | 333190 | 26.38s | `HEAD 200 video/mp4`, 938115 bytes |
| `video_ch04_fortress_fire` | 194 | 333191 | 25.13s | `HEAD 200 video/mp4`, 1023301 bytes |
| `video_ch05_coexistence_finale` | 195 | 333192 | 25.92s | `HEAD 200 video/mp4`, 998049 bytes |

The workstation ffmpeg build failed to burn subtitles into the MP4 path, so the builder degraded to `zoompan-external-subtitles-v1`. Each video version keeps UTF-8 subtitle metadata with `subtitleTextFile`, `audioItemKey`, `heroImageItemKey`, and `buildMode`. This satisfies Phase 36's "subtitles or captions where needed" acceptance, but it is not evidence of burned-in subtitles.

## Requirement Status

| Requirement | Status | Evidence |
| --- | --- | --- |
| MAT-01 | PASS | Live still images were generated/imported through the supplied compatible image endpoint and backend COS import, with `HEAD 200 image/png` evidence for cover, banner, 5 chapter heroes, and fallback poster. The provider rejected `image-2` as a model name, so the accepted `gpt-image-1` model was used on the same endpoint. |
| MAT-02 | PASS | 35 board-sliced pickup/title child assets were imported to COS and promoted, including all 15 title icons from the title badge board. |
| MAT-03 | PASS | Live 百煉 CosyVoice generation, backend download, COS storage, candidate finalization, material-package binding, and publish were verified for all 5 narration tracks plus `sfx_reward_unlock`. |
| MAT-04 | PASS | Five chapter MP4s were built from stills plus narration, imported through backend production APIs, promoted to COS, and verified as `video/mp4`. Captions are external metadata because burned-in subtitles degraded locally. |
| MAT-05 | PASS | Version schema/API/tests, image import, board-slice import, audio candidate binding, video import, publish, COS `HEAD`, version history, and rollback smoke pass. |

## Conclusion

Phase 36 is complete for v3.1 material production and promotion. Real images, sliced icons, audio, and video assets exist in COS and are tied back to material package versions. The only caveat is that chapter-video captions are external metadata rather than burned into the MP4 files on this workstation.
