---
phase: 36
slug: material-production-pipeline-and-asset-promotion
status: complete
nyquist_compliant: true
wave_0_complete: true
created: 2026-04-30
---

# Phase 36 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.
>
> Closure note: Wave 0 artifacts now exist, local image/board/audio/video/COS smoke passed, and chapter MP4s use external UTF-8 caption metadata because burned-in subtitle rendering degraded on this workstation.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit/Spring Boot, PowerShell smoke scripts, Python CLI checks |
| **Config file** | `packages/admin/aoxiaoyou-admin-backend/pom.xml`; local smoke scripts under `scripts/local/` |
| **Quick run command** | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` |
| **Full suite command** | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml; npm run build --prefix packages/admin/aoxiaoyou-admin-ui; powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-36-material-production.ps1 -IncludeVideo` |
| **Estimated runtime** | ~180-420 seconds, depending on local services and COS availability |

---

## Sampling Rate

- **After every backend/schema task commit:** Run `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml`
- **After every admin UI task commit:** Run `npm run build --prefix packages/admin/aoxiaoyou-admin-ui`
- **After local tooling task commits:** Run the relevant Python script with `--help` or dry-run/preflight input.
- **After every plan wave:** Run the full suite command above; video branch is allowed after portable `ffmpeg` availability and caption fallback behavior are confirmed.
- **Before `/gsd-verify-work`:** Full suite plus live local smoke must be green or documented as blocked by missing external provider/COS credentials.
- **Max feedback latency:** 420 seconds for automated checks, excluding real provider generation latency.

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 36-01-01 | 01 | 1 | MAT-05 | T36-01/T36-02 | Package production and rollback APIs are admin-only and preserve immutable lineage | compile/unit | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | ✅ | ✅ green |
| 36-01-02 | 01 | 1 | MAT-01/MAT-05 | T36-03 | Import and promotion create content asset links without overwriting prior versions | unit/integration | `mvn -q -Dtest=AdminStoryMaterialProductionServiceTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | ✅ | ✅ green |
| 36-02-01 | 02 | 2 | MAT-01/MAT-03 | T36-06/T36-08 | Local production reads UTF-8 prompt/manifest files, orchestrates narration and `sfx_reward_unlock`, and leaves failed CosyVoice output unbound/unpublished | CLI dry-run | `python scripts/local/material-production/phase36-preflight.py --manifest docs/content-packages/east-west-war-and-coexistence/content-manifest.json --batch docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-batch.json --dry-run` | ✅ | ✅ green |
| 36-02-02 | 02 | 2 | MAT-02 | T36-07/T36-10 | Board slicing preserves parent asset and crop metadata | CLI dry-run | `python scripts/local/material-production/phase36-slice-board.py --config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-board-slices.json --dry-run` | ✅ | ✅ green |
| 36-03-01 | 03 | 2 | MAT-01/MAT-05 | T36-11/T36-13 | Package page exposes only package-scoped production/status actions | UI build | `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` | ✅ | ✅ green |
| 36-05-01 | 05 | 3 | MAT-04 | T36-25 | Video job definitions reference bound narration, subtitle inputs, and poster fallbacks before any build runs | config check | `powershell -NoProfile -Command "$raw = Get-Content -Raw 'docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-video-jobs.json'; if ($raw -match 'video_ch01_mirror_sea_clash' -and $raw -match 'posterFallbackItemKey' -and $raw -match 'subtitleTextFile' -and $raw -match 'audio_ch01_narration') { exit 0 } else { exit 1 }"` | ✅ | ✅ green |
| 36-05-02 | 05 | 3 | MAT-04 | T36-21/T36-22/T36-24 | Video build runs only after ffmpeg availability and published narration checks pass; captions degrade to external UTF-8 metadata when burned-in rendering is unsafe | CLI smoke | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/material-production/phase36-build-video.ps1 -Config docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-video-jobs.json -ValidateOnly` | ✅ | ✅ green |
| 36-04-01 | 04 | 4 | MAT-01..MAT-05 | T36-16..T36-20 | Live smoke proves COS-backed asset binding, status promotion, rollback, video branch coverage, and no unsafe inline Chinese writes | smoke | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-36-material-production.ps1 -IncludeVideo` | ✅ | ✅ green |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [x] `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminStoryMaterialProductionServiceTest.java` — unit/integration tests for material version creation, promotion, rollback, and package item pointer updates.
- [x] `scripts/local/material-production/phase36-preflight.py` — manifest/cost/provider preflight using UTF-8 file reads.
- [x] `scripts/local/material-production/phase36-slice-board.py` — deterministic board crop/export helper with parent provenance metadata.
- [x] `scripts/local/material-production/phase36-build-video.ps1` — guarded ffmpeg still-image pan/zoom video builder, created by Plan `36-05`.
- [x] `scripts/local/smoke-phase-36-material-production.ps1` — local smoke for import, bind, promote, rollback, and optional video branch.
- [x] `ffmpeg` binary plus safe caption fallback path — MAT-04 completed with external UTF-8 caption metadata after burned-in subtitle rendering degraded locally.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Visual quality of generated images and material-board crops | MAT-01, MAT-02 | AI image quality and semantic crop matching cannot be fully asserted by tests | Open generated image/crop outputs, verify every manifest item has the intended visual region, and save a provenance report. |
| Audio intelligibility and tone | MAT-03 | CosyVoice output quality needs playback review | Play each generated MP3, confirm Mandarin narration is understandable, and record pass/fail in production run metadata. |
| Video pacing and caption metadata | MAT-04 | Pan/zoom speed and caption/audio judgments need playback review | Play each generated MP4 locally, confirm narration, pacing, and external Traditional Chinese caption metadata are present, then promote. |
| External provider/COS availability | MAT-01..MAT-04 | Depends on runtime credentials and network services | Run preflight, provider test, and COS upload smoke with local env secrets loaded but not committed. |

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies.
- [x] Sampling continuity: no 3 consecutive tasks without automated verify.
- [x] Wave 0 covers all MISSING references.
- [x] No watch-mode flags.
- [x] Feedback latency < 420s for non-provider checks.
- [x] `nyquist_compliant: true` and `wave_0_complete: true` set in frontmatter after Wave 0 artifacts, local video tooling, COS upload, publish, rollback, and IncludeVideo smoke passed.

**Approval:** complete. See `36-VERIFICATION.md`, `36-HANDOFF.md`, and `docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-production-report.json`.
