---
gsd_state_version: 1.0
milestone: v2.0
milestone_name: 後台管理系統的改進與完善
status: active
stopped_at: roadmap created, ready for phase 7 planning
last_updated: "2026-04-13T19:05:00+08:00"
last_activity: 2026-04-13
progress:
  total_phases: 7
  completed_phases: 0
  total_plans: 0
  completed_plans: 0
  percent: 0
---

# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-04-13)

**Core value:** Admins can configure the live mini-program experience end-to-end, and the mini-program consumes that managed data reliably through real backend APIs instead of mocks.
**Current focus:** Phase 7 roadmap kickoff for the admin shell and real-auth rebuild

## Current Position

Phase: 7 - Admin Shell and Real Auth Alignment
Plan: -
Status: Roadmap created, ready for phase planning
Last activity: 2026-04-13 - Milestone v2.0 roadmap created

Progress: [----------] 0%

## Performance Metrics

**Velocity:**

- Total plans completed: 19
- Phase 1 has the only explicit per-plan timing data so far
- The full six-phase backend cutover and hardening milestone completed in one execution day

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 1 | 4/4 | 49 min | 12 min |
| 2 | 3/3 | brownfield pass | n/a |
| 3 | 3/3 | integration pass | n/a |
| 4 | 3/3 | brownfield pass | n/a |
| 5 | 3/3 | brownfield pass | n/a |
| 6 | 3/3 | brownfield pass | n/a |

**Recent Trend:**

- Last 5 plans: 05-02, 05-03, 06-01, 06-02, 06-03
- Trend: Completed the mock-to-live cutover with repeatable seed migration, operator health visibility, and full-stack smoke proof

## Accumulated Context

### Decisions

Decisions are logged in PROJECT.md Key Decisions table.
Recent decisions affecting current work:

- [Phase 0]: Use `packages/server` as the public API surface for the mini-program
- [Phase 0]: Treat `/admin` as the authoritative control plane for mini-program content/settings
- [Phase 0]: Use MySQL as the primary source of truth for the live backend cutover
- [Phase 1]: Lock shared publish-state, locale, and asset semantics through mirrored public/admin enums and a canonical contract matrix
- [Phase 1]: Keep Phase 1 runtime seed data deterministic and ASCII-safe until full localized content migration lands in Phase 6
- [Phase 1]: Support host-managed MySQL on port `3306` in local smoke verification instead of assuming compose owns the port
- [Phase 2]: Align local brownfield MySQL tables forward to the canonical schema instead of teaching new admin APIs to read legacy-only columns
- [Phase 2]: Allow Mongo bootstrap to fail open during local admin startup so optional document-store drift does not block MySQL-backed control-plane verification
- [Phase 5]: Keep COS uploads in the admin backend and reuse `content_assets.canonical_url` as the shared admin/public asset contract
- [Phase 6]: Treat MySQL tables plus admin-managed runtime settings as the canonical replacement for former client mock content
- [Phase 6]: Cut dashboard and traveler admin views over to `user_profiles`, `user_progress`, and `user_checkins` so admin sees real public writes
- [Milestone v2.0]: Split the expansion into `v2.0` admin/control-plane reconstruction and `v2.1` platform-heavy interaction/AI work
- [Milestone v2.0]: Treat Traditional Chinese-first admin UX, four-language authoring, and real-login alignment as first-class milestone scope

### Pending Todos

None yet.

### Blockers/Concerns

- No phase-blocking gaps remain from `v1.0`, but the new milestone dramatically expands admin/data-model complexity and will need careful scope control.
- COS secrets must continue to remain outside tracked files during future environment rollout work.
- Automated regression coverage is still lighter than the integration smoke harness, so future changes should keep rerunning the Phase 6 smoke until deeper tests land.
- `v2.1` already has known deferred items (full indoor interaction rules and AI provider platformization); roadmap work must avoid silently pulling them back into `v2.0`.

## Session Continuity

Last session: 2026-04-13 19:05
Stopped at: roadmap created, ready for phase 7 planning
Resume file: None
