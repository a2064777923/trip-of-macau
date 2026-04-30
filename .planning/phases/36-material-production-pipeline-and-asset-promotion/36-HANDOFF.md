---
phase: 36
status: blocked_on_live_dependencies
updated: 2026-04-30
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

## Live Blockers

- `PHASE36_ADMIN_BEARER_TOKEN` is not set in this shell, so live backend smoke stops before `/production/preflight`, `/production/rollback`, version-history, and COS `HEAD` checks.
- `OPENAI_API_KEY` is not set in this shell, so live image generation/upload is not attempted.
- `PHASE36_COS_READY` is not set in this shell. The smoke does not print or require COS secrets directly, but it records COS readiness as false.
- `ffmpeg` is not available on PATH or does not expose `subtitles` in `ffmpeg -filters`; MAT-04 video output is blocked.
- No `phase36-production-report.json` with imported/published rows exists yet, so representative item evidence for `story_cover_copper_mirror`, `hero_ch01_ama_coast`, `pickup_ming_coastal_token`, `title_harbour_witness_final`, `audio_ch01_narration`, and `sfx_reward_unlock` remains pending.

## Required Live Commands

Load secrets/runtime auth from the local environment only, then run:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-36-material-production.ps1
```

For live generation/import:

```powershell
python scripts/local/material-production/phase36-batch-produce.py --manifest docs/content-packages/east-west-war-and-coexistence/content-manifest.json --batch docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-batch.json --confirm-production --upload --promote published
```

For live board slices:

```powershell
python scripts/local/material-production/phase36-slice-board.py --config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-board-slices.json --confirm-production --upload --promote published
```

For MAT-04 after installing ffmpeg with subtitle support:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/material-production/phase36-build-video.ps1 -Config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-video-jobs.json -ConfirmProduction -Upload -Promote published
```

## Safety Notes

- Do not commit provider keys, COS secrets, bearer tokens, or generated runtime credentials.
- Do not mark MAT-04 complete until `phase36-build-video.ps1` creates subtitle-burned MP4s and imports them as published video assets.
- Do not publish substitute audio for `sfx_reward_unlock`; it must be real provider output or remain `manual_import_required` / `retry_required`.
- Keep all Chinese prompt/script/subtitle content in UTF-8 files; do not write multilingual content through inline PowerShell literals.
