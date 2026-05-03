# Phase 40 Research: Acceptance, Cost Visibility, and Release Readiness

**Created:** 2026-05-03
**Status:** Complete

## Research Question

What must Phase 40 plan so v3.1 can close with repeatable evidence, practical cost visibility, and a truthful release-readiness handoff without expanding into new gameplay or provider work?

## Phase Boundary

Phase 40 is a closure phase. It should aggregate and harden evidence from Phases 36-39 rather than introducing new runtime schemas or new production workflows.

In scope:

- Repeatable smoke bundle for material production metadata, QA/package state, COS URL availability where configured, public runtime asset consumption, story session/event behavior, and mini-program build compatibility.
- WeChat DevTools/device UAT checklist for the flagship `east_west_war_and_coexistence` journey.
- Admin-visible generation job, request log, provider/model, status, error, asset linkage, and cost estimate visibility.
- Final v3.1 acceptance report separating automated evidence, manual evidence, skipped checks, accepted caveats, and deferred complex gameplay.

Out of scope:

- New AR/photo recognition, speech input, puzzle/minigame engines, route-coverage gameplay, cannon defense gameplay.
- New material production workbench.
- New AI provider onboarding.
- Public runtime or admin schema rewrite.

## Existing Evidence To Reuse

Phase 36:

- `scripts/local/smoke-phase-36-material-production.ps1` already supports validation-only, admin authenticated checks, package versioning, COS HEAD checks, rollback checks, and optional video branch.
- `36-VERIFICATION.md` records generated image/board/audio/video/COS evidence and caveats.

Phase 37:

- `scripts/local/smoke-phase-37-material-qa.ps1` checks material package QA overview, kind filters, usable health filter, item details, consistency check, content asset metadata, and optional QA actions.
- `37-03-SUMMARY.md` is the latest material QA closure summary.

Phase 38:

- `scripts/local/smoke-phase-38-public-runtime-assets.ps1` verifies public runtime DTO safety, runtime media/fallback fields, event endpoints, idempotency, exploration summary, and session exit.
- `38-VERIFICATION.md` includes backend compile/test evidence and privacy checks.

Phase 39:

- `scripts/local/smoke-phase-39-mini-program-story-mode.ps1` verifies anonymous story runtime and optional authenticated story session/event behavior.
- `packages/client/package.json` already has `smoke:phase39:story-mode`.
- `39-VERIFICATION.md` records `npm run build:weapp`, anonymous smoke, authenticated smoke, and known build advisory for `pages/story/index.js (248 KiB)`.

## Smoke Bundle Design

The consolidated smoke should be a wrapper under `scripts/local/` with safe defaults:

- Default mode should run non-secret local checks only.
- Live admin/COS/auth/provider checks should be opt-in through environment variables.
- Output should be a concise summary table with `PASS`, `SKIP`, or `FAIL/BLOCKED`.
- The script should not print bearer tokens, API keys, COS secrets, provider secrets, full prompt text, full script text, local file paths, or admin-only provenance.
- Existing phase scripts should remain the source of truth; Phase 40 wrapper should compose them and normalize status.

Recommended file:

- `scripts/local/smoke-phase-40-release-readiness.ps1`

Recommended npm alias:

- `packages/client/package.json`: `smoke:phase40:release-readiness`

## Admin Cost And History Research

Actual AI admin UI paths:

- `packages/admin/aoxiaoyou-admin-ui/src/pages/AiCapabilityCenter/ObservabilityPage.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/AiCapabilityCenter/catalog.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts`

Older context references to `packages/admin/aoxiaoyou-admin-ui/src/pages/AI/AIManagement.tsx` are stale.

Existing backend endpoints:

- `GET /api/admin/v1/ai/overview`
- `GET /api/admin/v1/ai/logs`
- `GET /api/admin/v1/ai/generation-jobs`
- `GET /api/admin/v1/ai/generation-jobs/{jobId}`

Existing backend DTO fields:

- `AdminAiLogResponse.costUsd`
- `AdminAiLogResponse.tokensUsed`
- `AdminAiLogResponse.latencyMs`
- `AdminAiLogResponse.adminOwnerName`
- `AdminAiGenerationJobResponse.ownerAdminName`
- `AdminAiGenerationJobResponse.providerName`
- `AdminAiGenerationJobResponse.inventoryCode`
- `AdminAiGenerationJobResponse.jobStatus`
- `AdminAiGenerationJobResponse.resultSummary`
- `AdminAiGenerationJobResponse.errorMessage`
- `AdminAiGenerationJobResponse.candidates`

Security-sensitive DTO fields already present in generation job responses:

- `promptText`
- `promptVariablesJson`
- `requestPayloadJson`
- `providerRequestId`

Phase 40 should not remove authorized diagnostic APIs blindly because existing creative workbench may use them. Instead, add or surface safe summaries where possible and keep full diagnostics behind role-aware detail drawers with clear labels.

## UAT Checklist Design

The checklist must be executable by a real operator using WeChat DevTools or a test device. It should not claim automated proof for visual/device behavior.

Required journey:

- Open the flagship story.
- Start story mode.
- Inspect route display and current chapter highlight.
- Inspect five chapters.
- Play or fallback-render image, Lottie, audio, video, unavailable media, and unsupported gameplay placeholders.
- Tap runtime cards for pickups/tasks/rewards.
- Open map handoff.
- Exit story mode.
- Restart story mode and verify temporary story session semantics versus permanent exploration/reward semantics.

Recommended artifacts:

- `.planning/phases/40-acceptance-cost-visibility-and-release-readiness/40-UAT.md`
- `.planning/phases/40-acceptance-cost-visibility-and-release-readiness/40-ACCEPTANCE.md`

## Validation Architecture

Phase 40 validation is mixed:

- Automated validation: PowerShell smoke wrapper, Java backend compile, admin UI build, client WeChat build, Phase 38/39 runtime smokes.
- Semi-automated validation: admin API smoke for AI logs/generation jobs and sanitized detail fields.
- Manual validation: WeChat DevTools/device journey checklist.
- Documentation validation: requirements traceability for `ACC-01` through `ACC-04`, acceptance report with pass/skip/deferred tables.

Execution should fail closed for automated evidence if local services are unavailable. Manual/device UAT may be recorded as pending or skipped, but milestone closure must not claim it passed unless an operator records the result.

## Security Notes

Trust boundaries:

- Local smoke scripts call public/admin backends and may use env-provided tokens.
- Admin UI displays AI request/job data that may contain sensitive prompt or provider details.
- Acceptance artifacts are committed to the repo and must not contain secrets or local-only provenance.

Mitigations:

- Normalize smoke output and ban secret-bearing field names.
- Store bearer tokens in memory only.
- Keep prompt/script/full payload fields out of default tables.
- Use role-aware existing backend job access controls.
- Label cost values as estimated, actual, or provider-unavailable.

## Planning Recommendation

Use three plans:

1. `40-01`: Consolidated release-readiness smoke and command aliases.
2. `40-02`: Admin monitoring/cost/job-history visibility polish and safe detail behavior.
3. `40-03`: WeChat UAT checklist, final acceptance report, requirements/state closure.

This order makes automated evidence available before UI polish and final acceptance writing.

## RESEARCH COMPLETE
