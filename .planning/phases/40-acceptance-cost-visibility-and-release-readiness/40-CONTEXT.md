# Phase 40: Acceptance, Cost Visibility, and Release Readiness - Context

**Gathered:** 2026-05-03
**Status:** Ready for planning
**Source:** `$gsd-next` routed to `/gsd-discuss-phase 40`. Interactive question UI was unavailable in Default mode, so recommended defaults were applied and recorded here for review.

<domain>
## Phase Boundary

Phase 40 closes v3.1 by turning the Phase 36-39 work into repeatable acceptance evidence, operational visibility, and release-readiness documentation.

This phase owns:

- One repeatable local smoke or smoke bundle that verifies material generation metadata, COS URL availability where possible, asset promotion state, public runtime consumption, story-mode session/event behavior, and mini-program build compatibility.
- A WeChat DevTools/device UAT checklist for the flagship story journey: route display, chapter progression, media playback, pickups, rewards, exit/restart behavior, and fallback/unsupported states.
- Admin-visible generation job/history/cost views sufficient for operators to inspect provider/model usage, status, errors, retries, and cost estimates without exposing API keys or provider secrets.
- A final v3.1 acceptance report that distinguishes automated evidence, browser/manual evidence, accepted caveats, and future deferred gameplay engines.
- Clear handoff rules for what blocks milestone closure versus what is intentionally deferred.

This phase does not own:

- New AR/photo recognition, speech-input gameplay, puzzle/minigame engines, route-coverage games, or cannon defense gameplay.
- A new material production workbench. Existing Phase 36/37 material package and QA controls should be reused.
- New AI provider onboarding beyond surfacing existing job/history/cost data already stored by the admin/backend.
- Rewriting the mini-program story runtime or backend runtime schema.

</domain>

<decisions>
## Implementation Decisions

### Acceptance Smoke Bundle

- **D40-01:** Phase 40 should consolidate existing smokes rather than replace them. The bundle should call the latest proven scripts for Phase 36 material production, Phase 37 QA where safe, Phase 38 public runtime assets, and Phase 39 mini-program story mode.
- **D40-02:** The smoke must support a safe default mode that runs without printing secrets. Live authenticated/provider/COS checks may be gated behind explicit environment variables.
- **D40-03:** The smoke result should be one concise report that records pass, skip, and blocked states per domain. A skipped live dependency is acceptable only when the report clearly states the missing env/config.
- **D40-04:** The smoke should not mutate production-like state unless the invoked phase smoke is already reversible or explicitly documented as local/dev-only.
- **D40-05:** The smoke must keep the same secret policy as Phase 36-39: no bearer tokens, API keys, COS secrets, provider secrets, prompt text, local paths, or admin-only provenance in output or planning artifacts.
- **D40-06:** Mini-program compatibility evidence must include `npm run build:weapp` and `npm run smoke:phase39:story-mode`.

### WeChat UAT And Release Readiness

- **D40-07:** The UAT checklist should be written for a real operator using WeChat DevTools or a test device, not as a vague QA note.
- **D40-08:** Checklist items must cover the flagship `東西方文明的戰火與共生` story end-to-end: open story, start story mode, see route/current chapter, inspect all five chapters, play or fallback-render media, tap runtime cards, open map, exit story mode, restart, and verify progress semantics.
- **D40-09:** UAT should explicitly mark complex gameplay placeholders as expected behavior for v3.1 when AR/photo, speech input, and puzzle/minigame engines are encountered.
- **D40-10:** Release readiness should define closure gates: automated smoke pass, no secret leakage, backend/admin/client build status, public runtime smoke pass, story-mode smoke pass, known warnings listed, and manual UAT result recorded.
- **D40-11:** The existing `pages/story/index.js (248 KiB)` build warning should be carried into release readiness as an advisory item, not a Phase 40 blocker unless it grows materially or WeChat tooling rejects the build.

### Cost, Usage, And Job History Visibility

- **D40-12:** Operators need a practical admin view for recent generation jobs and costs: provider, model, capability, status, user/operator, started/completed time, asset/package linkage, retry/error message, estimated/actual cost if available.
- **D40-13:** The UI should favor readable cards/tables with filters over raw JSON. JSON can remain in a collapsible advanced section for diagnostics.
- **D40-14:** Cost and usage values may be best-effort because providers do not always return exact costs. The UI should label values as `估算` or `實際` clearly.
- **D40-15:** Admin users may view job history appropriate to their role. Super/admin can see all jobs; non-super operators should not see other operators' sensitive request details unless the backend already permits it.
- **D40-16:** API keys and encrypted provider credentials must never be rendered, exported, or written to verification artifacts.
- **D40-17:** Where prompt/script provenance exists, Phase 40 should not expose full prompt/script text broadly. Prefer summaries, asset linkage, model/capability, and status. Full provenance can stay in existing detail views only if already authorized and sanitized.

### Final Evidence And Milestone Closure

- **D40-18:** The final Phase 40 verification should be explicit about what is fully automated, what was manually accepted, what was skipped due to local environment, and what remains deferred.
- **D40-19:** The final v3.1 acceptance document should cross-reference Phase 36-39 verification files and Phase 40 smoke/UAT results instead of duplicating long logs.
- **D40-20:** Deferred gameplay engines should be listed as future requirements, not hidden gaps: full AR/photo recognition, visual positioning, speech-input NPC interactions, route-coverage minigames, puzzle/minigame engines, and `.lottie` package/sprite/sequence-frame animation pipelines.
- **D40-21:** If local services are unavailable, Phase 40 should fail closed for automated evidence rather than marking the milestone complete with unverified claims.
- **D40-22:** Completion should prepare the project for `/gsd-complete-milestone`, but should not automatically archive v3.1 unless verification is passed and the user accepts any manual UAT caveats.

### the agent's Discretion

- The planner may decide whether the consolidated smoke is a wrapper PowerShell script, a documented command matrix, or both, provided it is repeatable and safe.
- The planner may decide the exact admin placement for cost/history visibility, but it should reuse existing AI/material package pages where possible rather than adding a confusing duplicate top-level module.
- The planner may decide whether UAT evidence is Markdown-only or includes a small structured JSON checklist, provided it is readable and can be audited later.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project And Milestone Scope

- `AGENTS.md` - project constraints, UTF-8/utf8mb4 rule, media/COS/security requirements, and verification expectations.
- `.planning/PROJECT.md` - core value and v3.1 milestone intent.
- `.planning/REQUIREMENTS.md` - `ACC-01` through `ACC-04` and completed `MAT`, `QA`, `RUN`, `MP` traceability.
- `.planning/ROADMAP.md` - Phase 40 goal, dependencies, success criteria, and milestone closure context.
- `.planning/STATE.md` - current Phase 40 planning state and local runtime caveats.

### Prior Evidence To Aggregate

- `.planning/phases/36-material-production-pipeline-and-asset-promotion/36-VERIFICATION.md` - material production, COS, and generated asset evidence/caveats.
- `.planning/phases/36-material-production-pipeline-and-asset-promotion/36-04-SUMMARY.md` - smoke and dependency gate patterns.
- `.planning/phases/37-material-qa-workspace-and-reuse-controls/37-03-SUMMARY.md` - material QA workspace and reuse-control closure.
- `.planning/phases/38-public-runtime-asset-consumption/38-VERIFICATION.md` - public runtime asset, event, and privacy smoke evidence.
- `.planning/phases/39-mini-program-story-mode-experience/39-VERIFICATION.md` - mini-program build and story-mode smoke evidence.
- `.planning/phases/39-mini-program-story-mode-experience/39-04-SUMMARY.md` - Phase 39 smoke script, npm alias, and caveats.

### Smoke Scripts And Commands

- `scripts/local/smoke-phase-36-material-production.ps1` - material package production/preflight/promotion smoke.
- `scripts/local/smoke-phase-37-material-qa.ps1` - material QA workspace smoke where applicable.
- `scripts/local/smoke-phase-38-public-runtime-assets.ps1` - public runtime asset/session/event smoke.
- `scripts/local/smoke-phase-39-mini-program-story-mode.ps1` - mini-program story-mode public runtime/session smoke.
- `packages/client/package.json` - `build:weapp` and `smoke:phase39:story-mode`.
- `packages/admin/aoxiaoyou-admin-ui/package.json` - admin UI build command.
- `packages/server/pom.xml` - public backend compile/test command.
- `packages/admin/aoxiaoyou-admin-backend/pom.xml` - admin backend compile/test command.

### Admin Cost And History Surfaces

- `packages/admin/aoxiaoyou-admin-ui/src/components/ai/AiCreativeWorkbenchModal.tsx` - existing AI generation request/history UI patterns.
- `packages/admin/aoxiaoyou-admin-ui/src/components/ai/AiTtsWorkbenchFields.tsx` - TTS capability fields and user-facing generation states.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/AI/AIManagement.tsx` - AI capability center routing and configuration surface.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminAiController.java` - admin AI endpoints, recent requests, recent jobs, provider/model controls, and detail contracts.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java` - backend AI request/job history mapping and sanitization.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/material/MaterialProductionService.java` - material production package/job linkage.

### Mini-program UAT Surfaces

- `packages/client/src/pages/story/index.tsx` - story mode journey surface.
- `packages/client/src/pages/map/index.tsx` - map handoff/current destination surface.
- `packages/client/src/components/StoryContentBlockRenderer/index.tsx` - story media rendering/fallback behavior.
- `packages/client/src/components/LottieAssetPlayer/index.tsx` - Lottie playback/fallback behavior.
- `packages/client/src/services/gameService.ts` - story runtime mapping, session events, route context, and state persistence.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- Existing smoke scripts already cover most v3.1 domains. Phase 40 should compose them and normalize output rather than duplicating all checks.
- `AdminAiController` / `AdminAiServiceImpl` already expose request/job history concepts that can be expanded or surfaced more clearly for ACC-03.
- `AiCreativeWorkbenchModal` already shows recent requests/jobs and can guide the UI language for cost/history cards.
- Phase 39 added `smoke:phase39:story-mode`, giving Phase 40 a direct client package command for story-mode verification.

### Established Patterns

- Local smokes should be PowerShell, UTF-8 safe, and explicit about env-gated live checks.
- Verification docs should record exact commands and pass/skip/block states, not raw logs or secrets.
- Admin UI copy must remain Traditional Chinese and should avoid raw JSON-first workflows.
- Public/runtime verification must reject secret-bearing fields and admin-only provenance.

### Integration Points

- A Phase 40 smoke wrapper can live under `scripts/local/` and call existing phase scripts with opt-in env gates.
- Phase 40 UAT artifacts should live under `.planning/phases/40-acceptance-cost-visibility-and-release-readiness/`.
- Cost/history UI work, if needed, should stay within existing AI/admin surfaces instead of creating a new unrelated module.
- Release readiness should update `.planning/REQUIREMENTS.md`, `.planning/STATE.md`, and final Phase 40 verification artifacts only after evidence exists.

</code_context>

<specifics>
## Specific Ideas

- Treat Phase 40 as a closure phase: make evidence easy to rerun and audit.
- The flagship story remains the acceptance fixture for traveler UAT.
- UAT checklist should be practical enough for the user to open WeChat DevTools and tick through steps.
- Cost/history visibility should answer: who generated what, with which provider/model, for which package/story item, did it succeed, what did it cost or estimate, and where is the resulting asset.
- Keep accepted caveats explicit rather than hiding them as incomplete work.

</specifics>

<deferred>
## Deferred Ideas

- Production-grade AR/photo recognition and indoor visual positioning.
- Speech input gameplay and NPC voice interaction runtime.
- Puzzle/minigame engines, route-coverage validation, and cannon-defense gameplay.
- `.lottie` package upload, sprite sheets, and sequence-frame animation pipelines.
- Full approval workflow with multi-step reviewer roles.

</deferred>

---

*Phase: 40-acceptance-cost-visibility-and-release-readiness*
*Context gathered: 2026-05-03*
