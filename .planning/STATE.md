---
gsd_state_version: 1.0
milestone: v3.1
milestone_name: Material Production and Mini-program Experience Acceptance
current_phase: 36
current_plan: 5
status: completed
stopped_at: Phase 37 context gathered
last_updated: "2026-05-03T00:32:04.612Z"
last_activity: 2026-05-03 -- Phase 36 live chapter videos generated from stills plus narration, imported through backend APIs, published to COS, and smoke verified
progress:
  total_phases: 5
  completed_phases: 1
  total_plans: 5
  completed_plans: 5
  percent: 100
---

# Project State

## Project Reference

See: `.planning/PROJECT.md` (updated 2026-04-30)

**Core value:** Admins can configure the live mini-program experience end-to-end, and the mini-program consumes that managed data reliably through real backend APIs instead of mocks.
**Current focus:** Phase 36 — material-production-pipeline-and-asset-promotion

## Current Position

Phase: 36 (material-production-pipeline-and-asset-promotion) — COMPLETE
Plan: 5 of 5
Milestone: `v3.1`
Current Phase: 36
Total Phases Planned: 5
Status: Phase 36 complete; live image/board/audio/video/COS path passed
Current Plan: 5
Last activity: 2026-05-03 -- Phase 36 live chapter videos generated from stills plus narration, imported through backend APIs, published to COS, and smoke verified
Last Activity Description: Phase 36 UAT is complete at 6/6 passed. Security audit `36-SECURITY.md` closes 25/25 threats and fixed publish/rollback authorization so both super-admin role and explicit confirmation are required. Live image, board-slice, audio, and video assets are now verified with backend COS storage, published material package versions, COS URL checks, version history, and rollback smoke. MAT-01 through MAT-05 are complete; chapter MP4s use external UTF-8 caption metadata because burned-in subtitles degraded on this workstation.

Progress: [##########] 100% of Phase 36 implementation plans executed and live-smoked

## Performance Metrics

**Completed milestones:**

- `v1.0`: 6 phases, 19 plans, live backend cutover shipped on 2026-04-13
- `v2.0`: 6 executed phases, 15 plans, archived with accepted gaps on 2026-04-15
- `v2.1`: 14 phases, 44 plans, archived with one accepted future slice on 2026-04-19
- `v3.0`: 8 phases, 33 plans, admin core domain completion and control-plane linkage shipped on 2026-04-30

## Quick Tasks Completed

| Date | Task | Summary |
| --- | --- | --- |
| 2026-04-29 | `260429-ocp` | Cleaned local verification artifacts, documented admin IA, removed confirmed dead admin entry points, and browser-verified the retained admin control plane. |
| Phase 33 P33-03 | 55 min | 2 tasks | 2 files |
| Phase 33 P33-04 | 35 min | 4 tasks | Admin story material package page, route/sidebar, smoke script, and handoff docs |
| 2026-05-02 | `260502-p19` | Clarified material asset usability states, repaired media preview/error states, and browser-fixed story/experience IA/layout issues. |
| 2026-05-02 | Phase 36 UAT/security | Completed 6/6 UAT checkpoints, created `36-SECURITY.md`, and tightened publish/rollback super-admin authorization. |
| 2026-05-02 | `260502-so0` | Added env-backed custom image endpoint support, generated 37 local PNG story assets, and recorded remaining live COS/audio/video blockers. |
| 2026-05-02 | `260502-urn` | Verified live 百煉 audio generation, backend COS upload, finalized content assets, and published material package versions for 6 Phase 36 audio items. |
| 2026-05-02 | `260502-w21` | Verified live compatible-image import, 35 board-sliced pickup/title icons, COS upload, published versions, rollback smoke, and prepared video closure. |
| 2026-05-03 | Phase 36 video closure | Generated five chapter MP4s from stills and narration with portable ffmpeg, imported them through backend production APIs, promoted them to COS, and passed live smoke with video checks. |

## Accumulated Context

### Decisions

- Keep `packages/server` as the public API surface for the mini-program.
- Keep `/admin` as the authoritative control plane for mini-program content, settings, media, and operator workflows.
- Keep MySQL as the primary source of truth for live runtime content and admin/public integration.
- Preserve explicit carryover instead of faking milestone closure.
- Treat the deferred mini-program frontend acceptance work as future scope rather than forcing it into `v3.0`.
- Complete story/content, user/progress, and operations/testing as linked domains rather than isolated CRUD pages.
- Phase 34 public runtime smoke is the v3.0 closure evidence for public-runtime linkage, not a substitute for full WeChat device journey UAT.
- Future material production can assemble video from one or several `image-2` stills with pan/zoom/motion and narration/audio stitching; keep using material boards and local slicing to save generation cost.
- v3.1 adopts that future scope as active work: real generated assets, asset QA/promotion, and mini-program story-mode acceptance.
- Phase 36 publish/rollback operations require both `SUPER_ADMIN` / `ROLE_SUPER_ADMIN` and an explicit confirmation flag; a confirmation boolean alone is not authorization.

### Pending Todos

- Keep provider and COS secrets outside tracked files while implementing real asset generation/upload.
- Verify generated assets and package publish/rollback behavior on local services before marking Phase 36 complete.

### Blockers/Concerns

- COS secrets and provider secrets must continue to stay outside tracked files.
- Phase 36 image/board/audio/video/COS is verified. Burned-in subtitles degraded to external caption metadata for chapter videos on this workstation.
- Real `image-2`, CosyVoice, and COS operations may incur external API cost and must remain explicit, logged, and reversible.
- Local Mongo still emits a warning on this workstation, though the admin HTTP stack remains healthy for verified admin flows.
- `spring-boot:run` currently needs `-Dmaven.test.skip=true` on this workstation because older server unit tests still reference stale constructor signatures, although `mvn -q -DskipTests compile -f packages/server/pom.xml` passes.

## Session Continuity

Last session: 2026-05-03T00:32:04.608Z
Stopped at: Phase 37 context gathered
Resume file: .planning/phases/37-material-qa-workspace-and-reuse-controls/37-CONTEXT.md
