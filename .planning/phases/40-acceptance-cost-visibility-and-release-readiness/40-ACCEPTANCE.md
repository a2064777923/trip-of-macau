# v3.1 Acceptance And Release Readiness

## Release Scope

v3.1 closes the material production and mini-program story-mode acceptance slice. The release scope covers generated and promoted story assets, material QA and reuse visibility, public runtime asset consumption, mini-program story-mode baseline consumption, AI monitoring/cost visibility, and release-readiness evidence.

This document does not claim completion of future gameplay engines. It separates automated evidence, admin evidence, manual UAT readiness, skipped local checks, accepted caveats, and deferred scope.

## Automated Evidence

| Evidence | Domain | Result | Notes |
| --- | --- | --- | --- |
| `40-SMOKE-REPORT.md` | Consolidated release readiness smoke | PASS_WITH_SKIPS_ALLOWED | Quick mode passed Phase 36 validate-only, Phase 37 read-only QA, Phase 38 runtime, Phase 39 story-mode smoke, client story smoke alias, and AI observability safe-field checks. |
| `36-VERIFICATION.md` | Material generation, asset promotion, COS publication | PASS | Images, board-sliced icons, narration, sound cue, videos, versioning, rollback and COS checks were verified in Phase 36 evidence. |
| `38-VERIFICATION.md` | Public runtime asset consumption and event APIs | PASS | Public story runtime returns safe media DTOs, fallback states, events, exploration summaries, and session exit behavior. |
| `39-VERIFICATION.md` | Mini-program story experience baseline | PASS | WeChat build, story runtime smoke, route/map handoff source checks, baseline events, pickups, rewards, and unsupported fallback metadata passed. |

## Admin Evidence

| Area | Evidence | Result |
| --- | --- | --- |
| AI monitoring and cost | `/ai/observability` rebuilt as `監控與成本`; smoke checks overview, jobs, logs, safe summaries, candidate metadata, cost labels. | PASS |
| Material QA/reuse | Phase 37 QA smoke and Phase 36 material package evidence remain the source of truth for generated asset inspection and promotion. | PASS |
| Secret safety | Default AI monitoring view uses safe summaries and collapses raw diagnostics behind `進階診斷`. Smoke and docs avoid credential or raw provider payload leakage. | PASS |

## Manual Evidence

| Evidence | Result | Notes |
| --- | --- | --- |
| `40-UAT.md` | Pending operator execution | Checklist is ready for 微信開發者工具 or test-device validation, but no human pass has been recorded in this execution. |
| WeChat DevTools/device UAT | Pending | This is not claimed as passed until the operator fills evidence and results in `40-UAT.md`. |

## Skipped Or Blocked Checks

| Check | State | Reason |
| --- | --- | --- |
| Phase 40 live provider/COS checks in quick smoke | Skipped | Quick mode keeps live mutations opt-in. Phase 36 already holds live asset/COS evidence. |
| Phase 40 full builds in quick smoke | Skipped | Quick mode skips full backend/admin/client builds; Wave 2 separately verified admin backend compile and admin UI build. |
| WeChat DevTools/device UAT | Pending | Requires operator-run visual/device evidence. |
| Blocked checks | None recorded | No current automated blocker is recorded in `40-SMOKE-REPORT.md`. |

## Accepted Caveats

| Caveat | Accepted For v3.1 | Notes |
| --- | --- | --- |
| Manual UAT remains pending | Yes, as release-readiness checklist evidence only | Milestone closure still needs user/operator acceptance of pending UAT rows. |
| Story page bundle-size warning | Yes, advisory | Phase 39 recorded the existing story page asset-size warning; no WeChat tooling rejection has been recorded. |
| Chapter video captions | Yes | Phase 36 degraded to external caption metadata on this workstation instead of burned-in captions. |
| Live checks opt-in | Yes | Live generation/COS operations are cost-bearing and mutation-capable, so the default Phase 40 smoke is safe. |

## Deferred Gameplay

- Full AR/photo recognition
- Indoor visual positioning
- Speech-input NPC interactions
- Route-coverage minigames
- Puzzle/minigame engines
- Cannon-defense gameplay
- `.lottie` package/sprite/sequence-frame animation pipelines
- Full approval workflow

## Secret And Provenance Safety

The release evidence intentionally avoids credential strings, raw provider payloads, full prompts, full scripts, and machine-specific absolute paths. Public runtime evidence references safe DTO behavior rather than admin provenance, and AI monitoring defaults to summaries instead of raw diagnostic content.

## Closure Gate

| Gate | State | Evidence |
| --- | --- | --- |
| ACC-01 | Complete | Repeatable Phase 40 smoke bundle and `40-SMOKE-REPORT.md`. |
| ACC-02 | Complete with pending execution caveat | `40-UAT.md` exists as an actionable WeChat DevTools/device checklist; rows remain Pending until operator execution. |
| ACC-03 | Complete | AI monitoring/cost/job-history UI and API smoke evidence from Wave 2. |
| ACC-04 | Complete | This report separates automated, manual, skipped, accepted, and deferred outcomes. |
| No secret leakage | Complete | Phase 40 docs and smoke report were scanned for sensitive literal patterns. |
| Manual UAT caveats accepted | Pending user/operator decision | User can accept the pending UAT caveat or fill results before `/gsd-complete-milestone`. |
