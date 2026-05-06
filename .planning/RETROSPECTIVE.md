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

## Milestone: v3.0 - Admin Core Domain Completion and Control-Plane Linkage

**Shipped:** 2026-04-30
**Phases:** 8 | **Plans:** 33 | **Tasks:** 54 | **Status:** shipped with accepted future slices

### What Was Built

- Shared story-experience orchestration: templates, flows, bindings, overrides, Lottie-aware content/media, POI default experiences, storyline mode, chapter overrides, and governance.
- Dynamic traveler progress: weighted exploration elements, durable story sessions, recompute/repair operations, audit records, and a Traditional Chinese traveler progress workbench.
- The `東西方文明的戰火與共生` flagship package: five chapters, story content, rewards, exploration elements, and a traceable planned material manifest.
- Public runtime and mini-program consumption baseline: compiled traveler-safe story runtime DTOs, content block rendering, Lottie/audio/video support, and unsupported gameplay degradation.
- Operations lifecycle control: cross-domain status labels, dependency-aware preview, scheduled publish/unpublish/remove, run-due execution, audit/history, and public filtering verification.

### What Worked

- Treating story, user/progress, and operations as one linked milestone prevented another round of isolated CRUD pages.
- Live smoke scripts gave concrete proof for admin/public integration and made lifecycle/progress regressions easier to catch.
- Explicitly separating admin-authored configuration from public compiled runtime DTOs reduced mini-program complexity and kept future gameplay work bounded.

### What Was Inefficient

- Some validation artifacts lagged behind the actual verification evidence, especially around older Nyquist rows.
- Real generated materials were deferred because the manifest, prompt, slicing, upload, and asset-promotion workflow needs its own external-service phase.
- Full WeChat device acceptance remains outside the local smoke baseline and still needs a dedicated future milestone.

### Patterns Established

- Build reusable authoring substrates first, then expose dedicated workbenches over them.
- Keep public runtime responses compiled and traveler-safe rather than exposing raw admin JSON.
- Lifecycle changes should be preview-first, dependency-aware, auditable, and reversible where possible.
- Material packages should carry provenance, COS keys, local paths, and status before real generation begins.

### Key Lessons

- A truthful planned-material manifest is better than pretending AI assets were generated; generation should be costed, reviewed, uploaded, and promoted as a separate production pipeline.
- Dynamic exploration should be calculated from weighted elements, not hard-coded percentage increments.
- Milestone audits are useful but should be kept clean from summary noise so accomplishments do not include bug-fix headings.

### Cost Observations

- `v3.0` carried heavy integration cost because it connected story authoring, progress, operations, public runtime, and mini-program baseline behavior.
- Verification cost was justified: Phase 34 and 35 smoke checks found contract and lifecycle issues before closeout.
- Future material-production work will likely spend more on external APIs and asset review than on schema/API scaffolding.

## Milestone: v3.1 - Material Production and Mini-program Experience Acceptance

**Shipped:** 2026-05-04  
**Phases:** 5 | **Plans:** 18 | **Status:** shipped with accepted WeChat UAT caveat

### What Was Built

- A real flagship material-production pipeline: generated/imported images, board slices, narration/audio, videos, COS-backed assets, package versions, promotion/rollback, and production smoke evidence.
- Traditional Chinese material QA and reuse controls over the shared media library and picker.
- Public runtime generated-asset consumption with image/audio/video/Lottie DTOs, fallbacks, unsupported states, privacy guards, and idempotent event/session ingestion.
- Mini-program story-mode baseline consumption of live public runtime data, route handoff, media rendering, action cards, pickups/rewards/progress, and unsupported-gameplay placeholders.
- Release-readiness evidence with Phase 40 quick smoke, AI monitoring/cost observability, scoped AI history visibility, acceptance report, and WeChat UAT checklist.

### What Worked

- The Phase 33 manifest made material generation traceable instead of ad hoc.
- Keeping generated assets behind package item versions, status transitions, and COS-backed `content_assets` made rollback and runtime exposure auditable.
- Public runtime smoke caught missing media linkage and stale backend-process issues before they reached milestone closure.
- Separating baseline mini-program story consumption from full gameplay engines kept v3.1 shippable without pretending AR/speech/puzzle features exist.

### What Was Inefficient

- Manual WeChat DevTools/device UAT remained outside the automated flow and still needs operator execution.
- Some requirement checkboxes lagged behind traceability rows, so milestone closure needed reconciliation.
- The story page bundle-size warning remains unresolved.
- Local DevTools automation was not reliable enough to replace manual visual/device validation.

### Patterns Established

- External-provider and COS mutation checks should remain explicit, opt-in, and logged.
- Public DTOs should expose safe availability/fallback metadata while hiding prompts, local paths, provider payloads, and admin provenance.
- Story material packages should be versioned and QA-governed before becoming runtime-facing.
- Mini-program gameplay acceptance should distinguish baseline consumption from true device/gameplay engines.

### Key Lessons

- Runtime smoke must target the correct flagship story (`東西方文明的戰火與共生`), not legacy storylines that remain incomplete.
- If a milestone closes with manual UAT pending, the caveat must be visible in roadmap, requirements archive, acceptance docs, and retrospective.
- Generated media work costs more in verification and QA than in schema creation; future AI/material milestones need enough time for review and rollback.

### Cost Observations

- v3.1 incurred external-service and media-processing cost through image/audio/video generation and COS promotion.
- The verification cost was justified because it found stale service processes and missing runtime media links.
- Future full-gameplay milestones will likely cost more in device UAT and interaction debugging than backend DTO work.

## Milestone: v3.2 - Traveler Gameplay Runtime and Operations Acceptance

**Shipped:** 2026-05-06
**Phases:** 5 | **Plans:** 16 | **Status:** shipped with accepted WeChat DevTools/physical-device UAT caveat

### What Was Built

- WeChat runtime entry hardening and story baseline smoke coverage for the live flagship story route.
- A traveler gameplay event engine for compiled runtime steps, pickups, tasks, rewards, titles, sessions, and unsupported-feature fallbacks.
- Admin traveler progress/reward operations with support workbench views, timeline filters, safe annotations, idempotent reward resend, and rule trace evidence.
- Admin IA and release-readiness polish for story/gameplay operations, media/material previews, helper copy, and truthful acceptance reporting.
- Safe storyline lifecycle controls that archive dependency-bearing duplicates and make the mini-program choose the intended `east_west_war_and_coexistence` runtime.

### What Worked

- Keeping UAT evidence truthful prevented the milestone from falsely claiming device acceptance.
- Local public/admin smoke checks caught stale story selection and verified the new flagship runtime still has five chapters.
- Impact-aware lifecycle handling avoided orphaning chapters, content links, exploration events, sessions, and progress rows.
- Operator support workflows became much more concrete once traveler events, rewards, and rule traces were connected.

### What Was Inefficient

- WeChat DevTools automation and local device evidence still lagged behind backend/admin implementation.
- Several mini-program visual/runtime polish issues had to be captured as debug notes before they could be systematically closed.
- The story page bundle-size warning remains unresolved and will need a dedicated split/lazy-load pass.

### Patterns Established

- Storyline deletion must be preview-first, dependency-aware, and archive-by-default.
- Public runtime selection should prefer exact canonical codes and reject legacy duplicates explicitly.
- Release UAT documents should keep automated, browser/admin, DevTools, physical-device, caveat, and future-scope rows separate.
- Traveler support operations need both preview actions and audit evidence, not direct database fixes.

### Key Lessons

- Mini-program acceptance cannot be inferred from backend/API smoke; DevTools or physical-device evidence must be captured directly.
- Duplicate content cleanup is a lifecycle problem, not a delete-button problem.
- Keeping old story records archived is safer than deleting them when traveler progress or exploration events exist.
- Gameplay runtime milestones should budget time for actual simulator/device observation, not just build success.

### Cost Observations

- v3.2 spent more effort on integration and verification than on new schema, which was the right tradeoff for live gameplay acceptance.
- The remaining cost is mostly in manual device validation, bundle splitting, and advanced gameplay engines.
- Future milestones should reserve explicit capacity for WeChat DevTools MCP/manual UAT before closeout.

## Cross-Milestone Trends

- v1.0 established the project's baseline pattern: phased delivery, admin/public contract discipline, and smoke-first verification.
- v2.0 confirmed that larger control-plane milestones need explicit carryover handling and milestone-audit discipline, not just more implementation volume.
- v2.1 established a stronger closure standard: late-phase verification, accepted-carryover discipline, and planning-state reconciliation are now part of milestone completion rather than optional cleanup.
- v3.0 confirmed that the admin platform works best when domain workbenches sit on shared runtime models, and when deferred experiential/material work is named explicitly instead of hidden inside "complete" claims.
- v3.1 confirmed that generated assets and mini-program runtime acceptance need production-style QA, safe public DTOs, and explicit manual-UAT boundaries.
- v3.2 confirmed that mini-program gameplay acceptance needs direct simulator/device evidence, and that lifecycle safety matters once real traveler progress references content.
