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

## Milestone: v2.0 - Admin Control-Plane Reconstruction

**Archived:** 2026-04-15
**Phases executed:** 6 | **Plans:** 15 | **Status:** archived with accepted gaps

### What Was Built

- Traditional Chinese-first admin shell cleanup, shared branding, and the real-auth baseline across admin/public/client.
- Four-language authoring foundations and translation settings groundwork for `zh-Hant`, `zh-Hans`, `en`, and `pt`.
- Rebuilt cities, sub-maps, POIs, coordinate normalization, and a central COS-backed media library.
- Expanded storyline, chapter, activity, and collection/reward authoring with real backend/public contract alignment.
- Indoor building, floor, tile, and marker authoring basics that now flow through COS, public APIs, and the mini-program indoor runtime.

### What Worked

- The brownfield pattern still held: schema and contract upgrades were easier to land when paired with concrete smoke scripts and admin/public/runtime alignment.
- Breaking the admin rebuild into spatial, media, story, and indoor waves gave the repo enough structure to keep moving despite a very large scope.
- Reusing canonical asset IDs, relation links, and admin-owned uploads reduced cross-surface drift.

### What Was Inefficient

- Milestone automation overstated completion because planning state, archives, and actual execution had drifted apart.
- `v2.0` scope remained broad enough that some real operator-fit gaps only surfaced during late manual UAT.
- The milestone again reached archival without a dedicated audit artifact, so closure required manual judgment instead of a clean pass/fail gate.

### Patterns Established

- Archive milestones even when closure is imperfect, but record accepted gaps explicitly instead of burying them.
- Keep indoor authoring additive: authoring basics can land earlier than the full runtime rules engine.
- Treat smoke scripts plus live service checks as the minimum bar for brownfield admin/runtime changes.

### Key Lessons

- Milestone closure needs a human sanity pass; automation alone is not reliable when the planning state is already inconsistent.
- Operator feedback during UAT should immediately become named carryover requirements, not informal notes.
- If a planned phase is skipped, the next milestone should absorb it explicitly rather than pretending numbering or coverage stayed clean.

### Cost Observations

- Most engineering weight moved into admin/backend alignment, media handling, and indoor authoring rather than the shell polish work.
- Verification got more realistic, but requirement-level closeout still lagged behind implementation.
- Dirty-worktree conditions made release tagging and clean milestone commits unsafe, which raised the cost of archival hygiene.

## Milestone: v2.1 - Interactive Rules Platform and AI Capability Center

**Archived:** 2026-04-19
**Phases:** 14 | **Plans:** 44 | **Status:** ready for archival with accepted carryover

### What Was Built

- Closed the accepted `v2.0` control-plane gaps with fresh verification and operator-facing stability work.
- Built the indoor interaction-rule authoring platform, workbench UX, governance center, and public/runtime rule-evaluation chain.
- Rebuilt the AI control plane into a provider/model orchestration workspace with creative tooling, TTS voice inventory, and consolidated verification ownership.
- Split the reward domain into redeemable-prize and in-game-reward flows, then closed shared reward-trigger synchronization and acquisition-presentation verification.
- Added a milestone-close reconciliation phase so traceability, audits, and archival readiness match the real late-phase outcome.

### What Worked

- Splitting platform delivery from verification closure reduced the risk of pretending that late-phase integration work was already proven.
- Dedicated closure phases for carryover, reward synchronization, AI verification, and milestone reconciliation created a more honest finish than the earlier archive-by-assumption pattern.
- The project improved once late milestone verification became an explicit deliverable instead of an afterthought.

### What Was Inefficient

- Verification ownership arrived too late in the milestone, which forced several follow-on closure phases after the main implementation waves had already landed.
- Planning-state drift accumulated across `ROADMAP.md`, `STATE.md`, `REQUIREMENTS.md`, and milestone audit artifacts before Phase 27 finally reconciled them.
- The dirty repository state still kept release-tagging and clean archival commits out of reach.

### Patterns Established

- Use explicit closure phases when a milestone has already delivered implementation but still lacks requirement-grade verification.
- Treat accepted carryover as a first-class planning outcome only when the deferred slice is tightly bounded and named.
- Keep milestone audits, requirement traceability, and project-state documents synchronized before archival instead of trying to repair them afterward.

### Key Lessons

- `accepted carryover` is viable only when the implemented chain is already verified and the remaining gap is bounded to one clearly named experiential slice.
- `late milestone verification` needs its own ownership; otherwise the project drifts into fragmented evidence and false blocker narratives.
- `planning-state drift` becomes a real delivery risk once phase summaries, audits, and requirement maps stop describing the same truth.

### Cost Observations

- `v2.1` paid a documentation and verification tax at the end because earlier phases optimized for delivery momentum over closure discipline.
- The payoff of the late reconciliation work is high: the milestone can now archive honestly instead of carrying silent ambiguity into the next cycle.
- Future milestones should budget audit and closeout work upfront rather than adding a reconciliation phase at the very end.

## Cross-Milestone Trends

- v1.0 established the project's baseline pattern: phased delivery, admin/public contract discipline, and smoke-first verification.
- v2.0 confirmed that larger control-plane milestones need explicit carryover handling and milestone-audit discipline, not just more implementation volume.
- v2.1 established a stronger closure standard: late-phase verification, accepted-carryover discipline, and planning-state reconciliation are now part of milestone completion rather than optional cleanup.
