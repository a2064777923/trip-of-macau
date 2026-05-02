---
quick_id: 260502-so0
type: quick
description: Phase 36 custom image endpoint support and local asset generation evidence
status: completed
completed_at: "2026-05-02T21:45:00+08:00"
---

# Quick Task 260502-so0 - Summary

## Outcome

Phase 36 image production now supports an OpenAI-compatible custom image endpoint through runtime environment variables without changing the tracked package manifest/provider configuration and without writing secrets to disk.

The custom endpoint was validated as:

- `POST /v1/images` returns `404`.
- `POST /v1/images/generations` is the working route.
- `image-2` is rejected by the provider as not accepted for the image endpoint.
- `gpt-image-1` succeeds and returns `data[0].b64_json`.

The generation run produced local image material for the `east-west-war-and-coexistence` package:

- 37 PNG files under `local-content/phase33/east-west-war-and-coexistence`.
- 6 generated board/source PNGs.
- 23 sliced pickup/title icon PNGs.
- 5 chapter hero PNGs.
- 1 story cover, 1 route/banner image, and 1 video poster fallback.

The generated binary files remain local working artifacts and are not promoted to tracked source as part of this quick task.

An overwrite rerun was started with the custom endpoint and stopped after the command exceeded the local tool timeout. It rewrote the cover, route/banner image, and the first four chapter hero images before being stopped to avoid uncontrolled repeated generation cost. The local validation report records the final 37 PNG files currently available for follow-up COS/import smoke.

## Files Changed

- `scripts/local/material-production/phase36-batch-produce.py`
- `docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-production-report.json`
- `docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-board-generation-report.json`
- `docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-board-slice-report.json`
- `docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-local-image-validation-report.json`

## Verification

- `python -m py_compile scripts/local/material-production/phase36-batch-produce.py`
- `python scripts/local/material-production/phase36-preflight.py --manifest docs/content-packages/east-west-war-and-coexistence/content-manifest.json --batch docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-batch.json --dry-run`
- `python scripts/local/material-production/phase36-batch-produce.py --manifest docs/content-packages/east-west-war-and-coexistence/content-manifest.json --batch docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-batch.json --confirm-production`
- `python scripts/local/material-production/phase36-slice-board.py --config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-board-slices.json`
- Local PNG header/size validation: 37 files detected, 37 PNG, 0 unknown headers.
- `phase36-local-image-validation-report.json`: 37 files, 46,521,102 bytes, dimensions bucketed as `512x512=20`, `384x384=3`, `1024x1024=4`, `1024x1536=2`, `1152x1920=1`, `1536x1024=7`.

## Still Blocked

Phase 36 remains blocked for live completion until these are verified:

- Admin bearer token is available for import/publish smoke.
- COS upload/import/publish/rollback smoke is run against local services.
- Audio generation is run against the configured provider.
- `ffmpeg` with subtitle support is available for video assembly.

## Secret Handling

No API key was committed or written to tracked files. The custom image key is consumed only through process environment variables such as `PHASE36_IMAGE_API_KEY`.
