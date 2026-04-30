# Phase 36-02 Summary — Local Material Production Tooling

## Outcome

Implemented UTF-8-safe local tooling and production-run configuration for the flagship story material package:

- `phase36-preflight.py` reads manifest/prompt/script files as UTF-8 and writes cost/risk preflight evidence.
- `phase36-batch-produce.py` provides the explicit live command path with `--confirm-production --upload --promote published`, OpenAI image generation support through `OPENAI_API_KEY`, backend AI narration orchestration, and `sfx_reward_unlock` failure gating.
- `phase36-import-assets.py` imports already-generated local files through package-scoped backend `/production/import` and optional promote.
- `phase36-slice-board.py` slices generated pickup/title boards with Pillow and preserves `parentVersionId`/`cropRect` metadata for child imports.
- `production-runs/phase36-batch.json` and `production-runs/phase36-board-slices.json` define the flagship production queue and board slicing map.

## Verification

- `python scripts/local/material-production/phase36-preflight.py --manifest docs/content-packages/east-west-war-and-coexistence/content-manifest.json --batch docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-batch.json --dry-run`
- `python scripts/local/material-production/phase36-slice-board.py --config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-board-slices.json --dry-run`

Both commands passed locally.

## Live Production Blockers

- Live production still requires `OPENAI_API_KEY` and `PHASE36_ADMIN_BEARER_TOKEN`.
- Board slicing live import requires generated board images at the configured local paths before `--confirm-production --upload --promote published`.
- Audio publish remains blocked unless CosyVoice returns a finalized binary candidate; failures are recorded as `retry_required` or `manual_import_required` and are not auto-published.
