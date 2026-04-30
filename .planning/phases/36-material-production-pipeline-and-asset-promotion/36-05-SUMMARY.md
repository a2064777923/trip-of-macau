---
phase: 36-material-production-pipeline-and-asset-promotion
plan: 36-05
subsystem: material-production
tags: [ffmpeg, video, subtitles, material-packages, cos]

requires:
  - phase: 36-01
    provides: package-scoped material import and promotion contracts
  - phase: 36-02
    provides: still-image/audio production report inputs
provides:
  - MAT-04 chapter-video job definitions
  - ffmpeg-gated still-image motion video builder
  - video import metadata contract for subtitle and poster fallback lineage
affects: [phase-36, phase-37, phase-38, material-production, story-runtime-assets]

tech-stack:
  added: []
  patterns:
    - fail-closed local production gates for missing ffmpeg subtitle support
    - job-config-driven material production with package-scoped import reuse

key-files:
  created:
    - docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-video-jobs.json
    - scripts/local/material-production/phase36-build-video.ps1
  modified: []

key-decisions:
  - "MAT-04 video generation remains blocked unless ffmpeg exists on PATH and supports the subtitles filter."
  - "Chapter videos reuse the Phase 36 package import path through phase36-import-assets.py instead of bypassing lineage and promotion."
  - "No silent or placeholder video fallback is produced when narration or subtitle prerequisites are missing."

patterns-established:
  - "Video jobs explicitly bind hero image, narration, subtitle text, COS object key, and poster fallback item key."
  - "Live publish requires explicit -ConfirmProduction, -Upload, and -Promote published flags."

requirements-completed: [MAT-04]

duration: 20 min
completed: 2026-04-30
---

# Phase 36 Plan 05: Dedicated ffmpeg-gated MAT-04 Chapter Video Jobs Summary

**Chapter-video production is now represented as concrete job config plus a fail-closed ffmpeg builder that imports only real subtitle-burned outputs.**

## Performance

- **Duration:** 20 min
- **Started:** 2026-04-30T07:26:00Z
- **Completed:** 2026-04-30T07:46:13Z
- **Tasks:** 2
- **Files modified:** 3

## Accomplishments

- Added five concrete MAT-04 video jobs for the flagship story chapters with hero image, narration, subtitle, forced COS key, output path, and poster fallback bindings.
- Added a PowerShell ffmpeg builder that checks subtitle-filter availability before work, refuses unpublished narration inputs, and keeps `-ValidateOnly` side-effect free.
- Routed video imports through the same package-scoped material import tooling with `assetKind=video`, `subtitleMetadataJson`, `audioItemKey`, and `posterFallbackItemKey` lineage.

## Task Commits

Each task was committed atomically:

1. **Task 1: Define concrete chapter video jobs against real narration and fallback assets** - `2f4f462` (feat)
2. **Task 2: Build the ffmpeg-gated chapter-video importer with strict subtitle and narration preconditions** - `db7df0f` (feat)

**Plan metadata:** pending in docs commit

## Files Created/Modified

- `docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-video-jobs.json` - Defines the five chapter video jobs and their material lineage bindings.
- `scripts/local/material-production/phase36-build-video.ps1` - Validates ffmpeg subtitle support, checks published input assets, builds zoompan MP4s, and imports promoted video assets when explicitly confirmed.
- `.planning/phases/36-material-production-pipeline-and-asset-promotion/36-05-SUMMARY.md` - Records the MAT-04 implementation and current workstation blocker.

## Decisions Made

- Video generation is not marked operational on this workstation because `ffmpeg` is not currently available on PATH.
- `-Promote` is rejected without `-Upload` so status changes remain tied to a real imported video asset.
- The script deliberately has no silent-video or placeholder-video branch; MAT-04 stays blocked until real narration and ffmpeg subtitle burn-in are available.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing Critical] Added explicit promote-with-upload guard**

- **Found during:** Task 2 (ffmpeg-gated importer implementation)
- **Issue:** The script already required `-Upload` to require `-ConfirmProduction`, but did not explicitly reject `-Promote` without `-Upload`.
- **Fix:** Added an `UPLOAD_REQUIRED` gate so promotion cannot run without an import attempt.
- **Files modified:** `scripts/local/material-production/phase36-build-video.ps1`
- **Verification:** Static contract grep passed and `-ValidateOnly` still fails at the expected ffmpeg gate on this workstation.
- **Committed in:** `db7df0f`

---

**Total deviations:** 1 auto-fixed (1 missing critical)
**Impact on plan:** Tightens live-publish safety without changing planned behavior.

## Issues Encountered

- `ffmpeg` is not available on PATH on this workstation. Running `phase36-build-video.ps1 -ValidateOnly` correctly fails with `FFMPEG_SUBTITLES_UNAVAILABLE`, so MAT-04 video output remains honestly blocked until ffmpeg with subtitle filter support is installed.

## Verification

- `CONFIG_CHECK_PASS`: `phase36-video-jobs.json` contains `video_ch01_mirror_sea_clash`, `posterFallbackItemKey`, `subtitleTextFile`, and `audio_ch01_narration`.
- `SCRIPT_CONTRACT_PASS`: `phase36-build-video.ps1` contains `zoompan`, `subtitles`, `FFMPEG_SUBTITLES_UNAVAILABLE`, `audioItemKey`, `subtitleMetadataJson`, `posterFallbackItemKey`, `/production/import`, `-ConfirmProduction`, `-Upload`, and `-Promote published`.
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/material-production/phase36-build-video.ps1 -Config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-video-jobs.json -ValidateOnly` failed as expected with `FFMPEG_SUBTITLES_UNAVAILABLE`.

## User Setup Required

Install or expose `ffmpeg` on PATH and verify `ffmpeg -filters` includes `subtitles` before running the live MAT-04 command:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/material-production/phase36-build-video.ps1 -Config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-video-jobs.json -ConfirmProduction -Upload -Promote published
```

## Next Phase Readiness

Ready for Phase 36 Plan 04 smoke/handoff. The smoke must continue to report ffmpeg as a blocker instead of claiming generated chapter videos exist.

---
*Phase: 36-material-production-pipeline-and-asset-promotion*
*Completed: 2026-04-30*
