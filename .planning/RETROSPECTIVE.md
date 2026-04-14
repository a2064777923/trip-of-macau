# Retrospective

## Milestone: v1.0 - Live Backend Cutover

**Shipped:** 2026-04-13
**Phases:** 6 | **Plans:** 19 | **Tasks:** 11

### What Was Built

- Canonical mini-program/admin/public contract rules, shared enums, and MySQL schema foundations.
- A MySQL-backed admin control plane for cities, POIs, storylines, chapters, rewards, runtime settings, assets, tips, notifications, and stamps.
- Public read and write APIs in `packages/server` covering runtime catalogs, traveler login/state, preferences, check-ins, and reward redemption.
- Tencent COS-backed media upload and canonical asset resolution through the admin backend.
- Repeatable mock-data migration and end-to-end smoke scripts proving admin -> MySQL/COS -> public API -> mini-program integration.

### What Worked

- The phased cutover sequence kept the brownfield repo manageable: contract -> schema -> admin -> public read -> public write -> media -> migration/hardening.
- Local smoke scripts for each late phase created concrete checkpoints and reduced ambiguity about whether the stack really worked.
- Keeping MySQL as the single operational source of truth simplified the admin/public alignment and reduced mock-era drift.

### What Was Inefficient

- Milestone-level audit was skipped before archival, so closure relied on phase summaries and smoke evidence instead of a dedicated milestone audit artifact.
- Some planning artifacts remained fragile to interrupted edits, as shown by the temporary deletion of `02-CONTEXT.md`.
- Git history was not normalized into a clean release branch during the milestone, which makes release tagging unsafe in the current dirty worktree.

### Patterns Established

- Contract-first planning for multi-surface cutovers.
- Admin-first ownership for runtime content, settings, and media.
- Repeatable smoke harnesses as the minimum verification bar for brownfield backend changes.
- Canonical asset IDs and URLs shared across admin and public surfaces.

### Key Lessons

- In this codebase, "feature complete" is not enough; every integration step needs a runnable local proof path.
- Milestone completion should be paired with a dedicated audit before archival, even when all requirements are checked off.
- Future expansion work should start as a new milestone instead of being appended into the archived cutover scope.

### Cost Observations

- Execution concentrated into one high-intensity milestone pass with 19 completed plans.
- Verification effort paid off most in the later phases where backend, COS, and seeded data interacted.
- The current dirty repository state increases the cost of safe release tagging and should be addressed before the next public release milestone.

## Cross-Milestone Trends

- v1.0 established the project's baseline pattern: phased delivery, admin/public contract discipline, and smoke-first verification.

