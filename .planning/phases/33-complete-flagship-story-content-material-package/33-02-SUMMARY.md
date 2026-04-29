---
phase: 33-complete-flagship-story-content-material-package
plan: 33-02
subsystem: content-seed
tags: [mysql, manifest, content-assets, cos, provenance]
requires:
  - phase: 33-01
    provides: Story material package schema and admin API contract
provides:
  - Canonical flagship story material manifest
  - Historical basis and literary dramatization production docs
  - Deterministic planned content asset and material package item seeds
  - Optional local upload helper for generated materials
affects: [phase-33, material-package-ui, story-seed, media-library]
tech-stack:
  added: []
  patterns:
    - UTF-8 manifest-first material traceability
    - Planned-only upload helper that skips missing local binaries
key-files:
  created:
    - docs/content-packages/east-west-war-and-coexistence/content-manifest.json
    - scripts/local/mysql/init/48-phase-33-flagship-material-assets.sql
    - scripts/local/seed-phase-33-material-assets.ps1
  modified: []
key-decisions:
  - "The committed manifest is the canonical source for planned item keys, local paths, COS keys, content asset ids, usage targets, and provenance refs."
  - "Large generated binaries remain optional in this phase; deterministic planned asset rows unblock admin/package verification without committing binary files."
patterns-established:
  - "Long-form historical and script content lives in UTF-8 docs; SQL stores refs and concise provenance to reduce encoding and maintenance risk."
  - "Upload helper only requires admin bearer when actual local files exist, so planned-only verification is deterministic on clean workstations."
requirements-completed: [STORY-03, VER-02]
duration: 45 min
completed: 2026-04-29
---

# Phase 33 Plan 33-02: Material Manifest and Asset Seed Summary

**Traceable five-chapter material manifest with 54 planned assets, COS keys, provenance docs, and deterministic MySQL seed rows**

## Performance

- **Duration:** 45 min
- **Started:** 2026-04-29T23:00:00+08:00
- **Completed:** 2026-04-29T23:45:00+08:00
- **Tasks:** 3
- **Files modified:** 7

## Accomplishments

- Created `content-manifest.json` for `east_west_war_and_coexistence_package` with five chapter codes and 54 material entries covering cover/banner, Lottie, audio, video placeholder, hero art, pickup icons, and honor title icons.
- Added production documentation for historical basis, literary dramatization boundaries, image generation prompts, audio scripts, and JSON-only Lottie fallback behavior.
- Seeded deterministic `content_assets` ids `333001` through `333054` and matching `story_material_package_items` rows with COS object keys, local paths, provenance refs, prompt/script refs, and package counters.
- Added an optional PowerShell upload helper that reads the manifest, uploads existing files through `/api/admin/v1/content/assets/upload`, and skips missing files as `planned-only`.

## Task Commits

1. **Task 33-02-01: Create content package manifest and production docs** - `7abb6c5` (docs)
2. **Task 33-02-02: Seed material assets and package item records** - `aa6e526` (feat)
3. **Task 33-02-03: Add optional upload helper for generated materials** - `2704a48` (chore)

## Files Created/Modified

- `docs/content-packages/east-west-war-and-coexistence/content-manifest.json` - canonical material manifest with 54 item records.
- `docs/content-packages/east-west-war-and-coexistence/historical-checklist.md` - separated evidence, dramatization, and unsafe-history wording.
- `docs/content-packages/east-west-war-and-coexistence/image-prompts.md` - image prompt set for cover, heroes, pickups, titles, and fallback posters.
- `docs/content-packages/east-west-war-and-coexistence/audio-scripts.md` - Traditional Chinese narration scripts and TTS notes.
- `docs/content-packages/east-west-war-and-coexistence/lottie-design.md` - Lottie JSON fixture design and fallback rules.
- `scripts/local/mysql/init/48-phase-33-flagship-material-assets.sql` - idempotent content asset and package item seed.
- `scripts/local/seed-phase-33-material-assets.ps1` - optional uploader for real generated local files.

## Decisions Made

- Used deterministic planned asset rows rather than forcing image/audio generation in this phase, keeping the local stack verifiable without external generation dependencies.
- Stored full source prose and prompt details in committed UTF-8 docs, while SQL rows store stable refs and concise provenance fields.
- Kept upload helper optional and non-secret-printing; it never recursively uploads directories and does not require auth when all files are missing.

## Deviations from Plan

None - plan executed exactly as written.

---

**Total deviations:** 0 auto-fixed.
**Impact on plan:** No scope changes.

## Issues Encountered

- An initial ad-hoc MySQL count query used brittle nested shell quoting and printed MySQL help; reran the query directly through PowerShell invocation successfully.
- `mysql` emitted the standard command-line password warning during local import and count checks; imports and checks exited `0`.

## Verification

- Manifest JSON parsed successfully with Node and reported 5 chapters, 54 materials, and `lottie_final_mirror_synthesis`.
- `48-phase-33-flagship-material-assets.sql` imported through `mysql --default-character-set=utf8mb4` with exit `0`.
- Database count check returned package counters `54 54 52` and 54 active package item rows.
- `scripts/local/seed-phase-33-material-assets.ps1` ran in planned-only mode and skipped missing local binaries without requiring or printing secrets.
- `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` exited `0`.

## User Setup Required

None - no external service configuration required for the deterministic seed. Real generated files can later be placed under `local-content/phase33/east-west-war-and-coexistence` and uploaded with `PHASE33_ADMIN_BEARER`.

## Next Phase Readiness

Wave 3 can now seed the five-chapter story package while referencing deterministic material ids, item keys, prompt/script refs, fallback assets, and historical/literary separation.

---

*Phase: 33-complete-flagship-story-content-material-package*
*Completed: 2026-04-29*
