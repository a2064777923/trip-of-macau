---
quick_id: 260502-urn
type: quick
description: Phase 36 live audio generation and COS upload smoke
status: planned
---

# Quick Task 260502-urn - Phase 36 Live Audio/COS Smoke

## Goal

Complete the live portions still blocking Phase 36 where local credentials and services allow it: generate audio assets, import image/audio assets through the admin backend, upload them to COS, promote them, and record smoke evidence.

## Constraints

- Do not write provider keys, admin bearer tokens, COS secrets, or generated signed URLs with secrets to tracked files.
- Use existing local services and admin APIs; do not bypass backend production/import validation.
- Preserve existing package/batch configuration unless a small script fix is required for live execution.
- Generated binary material stays in ignored local working directories unless explicitly imported through the backend.
- If a secret or credential is genuinely unavailable from runtime configuration, record the exact blocker rather than fabricating completion.

## Tasks

1. Discover live runtime conditions.
   - Confirm admin backend at `8081`.
   - Obtain an admin bearer token via the local login flow if credentials are available from seed/config.
   - Confirm COS runtime configuration and public canonical URL behavior.
   - Confirm audio provider configuration and callable API route.

2. Generate or import audio assets.
   - Use the existing Phase 36 audio work items and UTF-8 script files.
   - Prefer backend AI/job routes if already wired to the configured provider.
   - If backend route lacks a direct production path, generate local audio with the configured provider and import via production/import.

3. Upload and promote material package versions.
   - Use `phase36-batch-produce.py --confirm-production --upload --promote published` where viable.
   - Use `phase36-slice-board.py --confirm-production --upload --promote published` for sliced board children.
   - Capture import/promotion version ids and canonical URLs.

4. Verify.
   - Check material package version history for required sample items.
   - Check at least a few canonical URLs with `HEAD` or `GET`.
   - Exercise publish/rollback smoke if a safe current version exists.
   - Update Phase 36 evidence without marking completion if any live dependency remains blocked.
