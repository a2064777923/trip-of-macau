# Phase 36 Production Runs

All commands read manifest, prompt, and script text from UTF-8 files. Do not paste Chinese prompts or scripts into inline PowerShell commands.

## Dry-Run Preflight

```powershell
python scripts/local/material-production/phase36-preflight.py --manifest docs/content-packages/east-west-war-and-coexistence/content-manifest.json --batch docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-batch.json --dry-run
```

## Live Still/Audio Production

Live production is blocked unless the operator explicitly supplies confirmation and runtime secrets through environment variables.

Required environment:

- `OPENAI_API_KEY` for OpenAI image generation when the script is generating still images directly.
- `PHASE36_ADMIN_BEARER_TOKEN` for package import, AI job orchestration, candidate bind, and promote calls.
- `PHASE36_ADMIN_BASE_URL`, optional, defaults to `http://localhost:8081`.

```powershell
python scripts/local/material-production/phase36-batch-produce.py --manifest docs/content-packages/east-west-war-and-coexistence/content-manifest.json --batch docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-batch.json --confirm-production --upload --promote published
```

## Board Slicing

The board slice config records `parentVersionId`, `parentItemKey`, and `cropRect` for each child asset. Update crop rectangles after visual inspection of the generated board image.

```powershell
python scripts/local/material-production/phase36-slice-board.py --config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-board-slices.json --dry-run
```

Live import:

```powershell
python scripts/local/material-production/phase36-slice-board.py --config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-board-slices.json --confirm-production --upload --promote published
```

## Audio Failure Gate

- CosyVoice success: finalized candidate -> package `bind-candidate` -> local playback verification -> promote to `published`.
- CosyVoice failure or unacceptable output: write `audioGenerationStatus=retry_required` or `audioGenerationStatus=manual_import_required`, do not bind, do not publish.
- `sfx_reward_unlock` follows the same rule. There is no stock audio, silent placeholder, or automatic substitute path.
