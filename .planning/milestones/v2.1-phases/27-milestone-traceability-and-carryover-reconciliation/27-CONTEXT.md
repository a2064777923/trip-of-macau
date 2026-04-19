# Phase 27: milestone traceability and carryover reconciliation - Context

**Gathered:** 2026-04-19
**Status:** Ready for planning

<domain>
## Phase Boundary

Phase 27 is a milestone-close reconciliation phase. It does not add new mini-program or admin product capability. Its job is to resolve the remaining `RULE-03` carryover decision honestly and refresh milestone-level closure artifacts so `v2.1` can be archived without stale or contradictory planning state.

</domain>

<decisions>
## Implementation Decisions

### Carryover Disposition
- **D-01:** `RULE-03` should be closed in `v2.1` as accepted carryover rather than reopened for fresh implementation work inside this milestone.
- **D-02:** The deferred slice is specifically the real WeChat DevTools experiential acceptance for the mini-program indoor runtime, not the admin/public/backend contract, which Phase 21 already verified.

### Milestone Reconciliation Scope
- **D-03:** Phase 27 should reconcile milestone artifacts, audits, traceability, and archival readiness only; it should not reopen AI, reward, or indoor platform implementation unless a current artifact still makes a false claim.
- **D-04:** Stale milestone findings that were already closed by later phases, especially the old AI and reward gaps in the audit trail, should be refreshed to match the current verified truth instead of being carried forward as phantom blockers.

### Closure Standard
- **D-05:** The target outcome of Phase 27 is to make `v2.1` ready for `/gsd-complete-milestone` if the only remaining open item is the explicitly deferred mini-program acceptance slice.
- **D-06:** If honest archival still requires a missing closure artifact, Phase 27 may backfill or replace milestone-close documentation, but it should prefer the smallest truthful reconciliation over reopening prior feature phases.

### the agent's Discretion
- The exact set of milestone-close documents to refresh, replace, or supersede.
- Whether any missing late-phase verification artifacts need lightweight backfill during reconciliation, or whether milestone-level reconciliation artifacts are sufficient.
- The cleanest end-state for `ROADMAP.md`, `STATE.md`, milestone audit files, and milestone summary files so downstream archival commands do not see stale blockers.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Milestone Truth
- `.planning/PROJECT.md` - current project constraints, milestone framing, and the archived-carryover model
- `.planning/ROADMAP.md` - Phase 27 goal, milestone sequencing, and current active milestone structure
- `.planning/REQUIREMENTS.md` - final remaining requirement ownership, especially `RULE-03`
- `.planning/STATE.md` - current GSD state and next-step routing
- `.planning/v2.1-MILESTONE-AUDIT.md` - stale milestone-close audit that now needs reconciliation against later phase evidence

### Indoor Carryover Evidence
- `.planning/phases/21-indoor-rule-acceptance-and-verification-closure/21-VERIFICATION.md` - canonical proof that indoor admin/public/runtime closure passed with accepted carryover for the deferred WeChat DevTools slice
- `.planning/phases/17-indoor-runtime-evaluation-and-mini-program-alignment/17-UAT.md` - earlier runtime witness trail that Phase 21 partially superseded

### AI Closure Evidence
- `.planning/phases/19-ai-capability-platform-redesign-and-provider-model-orchestra/19-VERIFICATION.md` - backfilled AI workspace ownership
- `.planning/phases/22-ai-platform-verification-and-provider-default-closure/22-VERIFICATION.md` - live provider truth/default verification
- `.planning/phases/24-ai-tts-voice-library-and-voice-cloning/24-VERIFICATION.md` - voice workbench verification ownership
- `.planning/phases/26-ai-platform-verification-consolidation/26-VERIFICATION.md` - final cross-phase AI closure artifact

### Reward Closure Evidence
- `.planning/phases/25-reward-domain-synchronization-and-verification-closure/25-CONTEXT.md` - latest reward-gap closure intent and scope
- `.planning/phases/25-reward-domain-synchronization-and-verification-closure/25-01-SUMMARY.md` - first reward-sync closure output
- `.planning/phases/25-reward-domain-synchronization-and-verification-closure/25-02-SUMMARY.md` - second reward-sync closure output
- `.planning/phases/25-reward-domain-synchronization-and-verification-closure/25-03-SUMMARY.md` - final reward-domain closure summary

### Milestone Completion Path
- `.planning/phases/26-ai-platform-verification-consolidation/26-01-SUMMARY.md` - structured smoke consolidation summary
- `.planning/phases/26-ai-platform-verification-consolidation/26-02-SUMMARY.md` - backfilled verification ownership summary
- `.planning/phases/26-ai-platform-verification-consolidation/26-03-SUMMARY.md` - traceability closure summary

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- The main implementation surface for this phase is the `.planning/` documentation set rather than product code.
- Existing verification artifacts from Phases 21 and 26 already contain the key truth statements Phase 27 must reconcile.

### Established Patterns
- Recent late-milestone closure phases use `*-VERIFICATION.md` as the canonical truth artifact and `REQUIREMENTS.md` as the authoritative traceability map.
- Accepted carryover is already an established pattern in this project when the deferred scope is explicitly named and bounded.

### Integration Points
- `ROADMAP.md`, `STATE.md`, `REQUIREMENTS.md`, and `v2.1-MILESTONE-AUDIT.md` must agree after Phase 27.
- If milestone archival is enabled after reconciliation, the next workflow should be `/gsd-complete-milestone`, not a new feature phase.

</code_context>

<specifics>
## Specific Ideas

- The user already chose to defer the mini-program frontend acceptance slice to a later milestone, so Phase 27 should preserve that decision rather than re-litigate it.
- The final milestone story should say: indoor admin/public/runtime platform is closed, AI platform is closed, reward platform is closed, and only the explicitly deferred real WeChat experiential slice carries forward.

</specifics>

<deferred>
## Deferred Ideas

- Real WeChat DevTools indoor experiential acceptance and mini-program-side visual/runtime confirmation remain next-milestone work.
- The later mini-program frontend milestone will also handle the user's broader frontend linkage acceptance for AI and special runtime interactions.

</deferred>

---

*Phase: 27-milestone-traceability-and-carryover-reconciliation*
*Context gathered: 2026-04-19*
