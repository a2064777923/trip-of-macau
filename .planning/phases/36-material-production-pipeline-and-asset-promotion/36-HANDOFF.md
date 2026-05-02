---
phase: 36
status: complete
updated: 2026-05-03
---

# Phase 36 Handoff — Material Production Pipeline and Asset Promotion

## What Exists

- Backend package production routes are under `/api/admin/v1/content/material-packages/{packageId}`:
  - `POST /production/preflight`
  - `POST /items/{itemId}/production/import`
  - `POST /items/{itemId}/production/bind-candidate`
  - `POST /items/{itemId}/production/promote`
  - `POST /items/{itemId}/production/rollback`
  - `GET /items/{itemId}/versions`
- Version lineage is stored in `story_material_package_item_versions`.
- Package-scoped admin controls exist in `StoryMaterialPackageManagement.tsx`.
- Local production tooling exists:
  - `phase36-preflight.py`
  - `phase36-batch-produce.py`
  - `phase36-import-assets.py`
  - `phase36-slice-board.py`
  - `phase36-build-video.ps1`
  - `smoke-phase-36-material-production.ps1`
- `phase36-build-video.ps1` resolves ffmpeg from `PHASE36_FFMPEG_PATH`, PATH, or Python `imageio-ffmpeg`; this avoids requiring a globally installed ffmpeg binary on Windows.

## Current Evidence

- Backend material production service test passed:
  - `mvn -q -Dtest=AdminStoryMaterialProductionServiceTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml`
- Admin UI build passed:
  - `npm run build --prefix packages/admin/aoxiaoyou-admin-ui`
- Local UTF-8 preflight passed:
  - `python scripts/local/material-production/phase36-preflight.py --manifest docs/content-packages/east-west-war-and-coexistence/content-manifest.json --batch docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-batch.json --dry-run`
- Board slicing dry-run passed:
  - `python scripts/local/material-production/phase36-slice-board.py --config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-board-slices.json --dry-run`
- Phase smoke validate-only passed:
  - `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-36-material-production.ps1 -ValidateOnly`
- Live image and board-slice import passed:
  - `python scripts/local/material-production/phase36-batch-produce.py --manifest docs/content-packages/east-west-war-and-coexistence/content-manifest.json --batch docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-image2-redo-batch.json --backend http://127.0.0.1:8081 --confirm-production --upload --promote published`
  - `python scripts/local/material-production/phase36-slice-board.py --config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-board-slices.json --manifest docs/content-packages/east-west-war-and-coexistence/content-manifest.json --confirm-production --upload --promote published`
- Live Phase 36 smoke passed for images, board slices, audio, videos, COS URL checks, version history, rollback, and video validate-only:
  - `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-36-material-production.ps1 -IncludeVideo`
- Live chapter video build/import/publish passed:
  - `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/material-production/phase36-build-video.ps1 -Config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-video-jobs.json -ConfirmProduction -Upload -Promote published`
- Published chapter video versions:
  - `video_ch01_mirror_sea_clash`: version 191, asset 333188, `video/mp4`, `HEAD 200`
  - `video_ch02_south_bay_boundary`: version 192, asset 333189, `video/mp4`, `HEAD 200`
  - `video_ch03_hill_watch`: version 193, asset 333190, `video/mp4`, `HEAD 200`
  - `video_ch04_fortress_fire`: version 194, asset 333191, `video/mp4`, `HEAD 200`
  - `video_ch05_coexistence_finale`: version 195, asset 333192, `video/mp4`, `HEAD 200`
- The supplied compatible image endpoint rejected model `image-2`; the same endpoint accepted `gpt-image-1`, which was used for completed image evidence.

## Live Status

- Phase 36 has no remaining live image, board-slice, audio, video, COS, or package-version blockers.
- `phase36-build-video.ps1` prepares missing local narration MP3s from published COS URLs and generates UTF-8 subtitle text from `audio-scripts.md`.
- Burned-in subtitles failed on this workstation and the builder degraded to `zoompan-external-subtitles-v1`; MP4s remain published and the UTF-8 caption metadata is stored with each package item version.
- Five chapter video package items exist in the local admin package: `video_ch01_mirror_sea_clash` through `video_ch05_coexistence_finale`.

## Required Live Commands

Load secrets/runtime auth from the local environment only, then run:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-36-material-production.ps1
```

For live generation/import:

```powershell
python scripts/local/material-production/phase36-batch-produce.py --manifest docs/content-packages/east-west-war-and-coexistence/content-manifest.json --batch docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-batch.json --confirm-production --upload --promote published
```

For the completed compatible-image endpoint rerun:

```powershell
$env:PHASE36_IMAGE_BASE_URL = 'https://api.suqis.com/v1/images'
$env:PHASE36_IMAGE_MODEL = 'gpt-image-1'
python scripts/local/material-production/phase36-batch-produce.py --manifest docs/content-packages/east-west-war-and-coexistence/content-manifest.json --batch docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-image2-redo-batch.json --backend http://127.0.0.1:8081 --confirm-production --upload --promote published
```

For live board slices:

```powershell
python scripts/local/material-production/phase36-slice-board.py --config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-board-slices.json --manifest docs/content-packages/east-west-war-and-coexistence/content-manifest.json --confirm-production --upload --promote published
```

For live chapter videos:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/material-production/phase36-build-video.ps1 -Config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-video-jobs.json -ConfirmProduction -Upload -Promote published
```

## Safety Notes

- Do not commit provider keys, COS secrets, bearer tokens, or generated runtime credentials.
- Do not treat the external-caption fallback as burned-in subtitles; if burned-in subtitles become mandatory, rerun with a subtitle path compatible with the local ffmpeg build and verify the MP4 visually.
- Do not publish substitute audio for `sfx_reward_unlock`; it must be real provider output or remain `manual_import_required` / `retry_required`.
- Keep all Chinese prompt/script/subtitle content in UTF-8 files; do not write multilingual content through inline PowerShell literals.
