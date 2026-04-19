# Phase 25: Reward Domain Synchronization and Verification Closure - Context

**Gathered:** 2026-04-19
**Status:** Ready for planning
**Source:** v2.1 milestone gap audit after Phase 23 reward-domain delivery

<domain>
## Phase Boundary

Phase 25 is a closure phase for the reward domain. It is not a second full redesign of reward IA.

This phase covers:
- closing the still-open `REWARD-04` gap around shared reward-trigger references between reward management and interaction authoring
- proving that the split reward-domain surfaces from Phase 23 are still live, synchronized, and verifiable on the current stack
- replacing the old diagnosed `23-UAT.md` trail with a formal `23-VERIFICATION.md`
- refreshing reward-domain traceability so `REQUIREMENTS.md` can honestly mark `REWARD-01` through `REWARD-05` complete

This phase does not cover:
- a new reward-domain information architecture redesign beyond what is necessary to close synchronization gaps
- new downstream mini-program reward UX beyond the contracts already owned by the reward domain
- moderation, approval workflow, or economy balancing work that belongs to later milestone scope

</domain>

<decisions>
## Implementation Decisions

### Closure intent
- **D-01:** Treat Phase 25 as a milestone-gap closure pass over Phase 23, not as a fresh product-discovery phase.
- **D-02:** Reuse the split reward-domain foundation from Phase 23 rather than collapsing back to the old overloaded reward model.
- **D-03:** Any new code added in this phase must directly serve either shared-rule synchronization, verification closure, or requirement traceability closure.

### Shared reward-trigger contract
- **D-04:** `REWARD-04` is the only still-unsatisfied reward requirement and must be closed explicitly in this phase.
- **D-05:** Reward-trigger rules must be canonical shared references, not duplicated JSON blobs maintained separately in reward forms and indoor interaction forms.
- **D-06:** Create, edit, bind, unbind, and delete flows for interaction-driven reward rules must stay synchronized across:
  - redeemable-prize authoring
  - game-reward authoring
  - reward-rule governance
  - indoor interaction-behavior authoring and governance
- **D-07:** If the current schema already provides the right canonical entities, Phase 25 should complete the missing linkage and proof instead of inventing a parallel rule system.
- **D-08:** If deletion or mutation of a shared rule would break existing bindings, the operator workflow must surface that relationship clearly and handle it deterministically.

### Verification ownership
- **D-09:** Phase 23's old diagnosed `23-UAT.md` is no longer the closure artifact. Phase 25 must produce a formal `23-VERIFICATION.md` that supersedes the diagnosed trail.
- **D-10:** Verification must use the real local stack on admin `8081` and public `8080`, not route-only inspection or source-only reasoning.
- **D-11:** Verification must cover the split reward-domain surfaces end to end:
  - redeemable-prize authoring and readback
  - game-reward and honor/title authoring and readback
  - reward presentation lookup
  - reward-rule synchronization into indoor interaction authoring
  - public reward-domain payload correctness
- **D-12:** Verification evidence must be strong enough that `REQUIREMENTS.md` can close `REWARD-01` through `REWARD-05` without hidden partial states.

### Scope discipline
- **D-13:** Do not reopen unrelated UI polish or copy-only work unless it blocks honest verification of reward-domain closure.
- **D-14:** Keep the reward-domain closure aligned with the existing indoor-rule platform from Phases 15, 16, and 21 instead of introducing a second governance concept.
- **D-15:** Any traceability refresh in this phase must stay honest about what was actually re-verified live.

</decisions>

<canonical_refs>
## Canonical References

### Milestone audit and traceability
- `.planning/v2.1-MILESTONE-AUDIT.md` - authoritative statement of the remaining reward-domain gap and missing verification status
- `.planning/ROADMAP.md` - Phase 25 goal, requirements, and success criteria
- `.planning/REQUIREMENTS.md` - current reward requirement traceability that must be updated honestly
- `.planning/STATE.md` - current project state, sequencing drift, and milestone continuity notes

### Prior reward-domain phase artifacts
- `.planning/phases/23-reward-domain-split-and-acquisition-presentation-system/23-CONTEXT.md` - original reward-domain boundary and decisions
- `.planning/phases/23-reward-domain-split-and-acquisition-presentation-system/23-01-SUMMARY.md` - schema and canonical backend foundation
- `.planning/phases/23-reward-domain-split-and-acquisition-presentation-system/23-02-SUMMARY.md` - split admin IA and reward authoring surfaces
- `.planning/phases/23-reward-domain-split-and-acquisition-presentation-system/23-03-SUMMARY.md` - public/runtime alignment and smoke baseline
- `.planning/phases/23-reward-domain-split-and-acquisition-presentation-system/23-04-SUMMARY.md` - repaired UTF-8 showcase reseed and live smoke evidence
- `.planning/phases/23-reward-domain-split-and-acquisition-presentation-system/23-UAT.md` - diagnosed artifact that Phase 25 must supersede with formal verification

### Reward-domain admin UI
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/RedeemablePrizeManagement.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/GameRewardManagement.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/HonorManagement.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/RewardRuleCenter.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/components/rewards/RewardDomainShared.tsx`

### Reward-domain admin backend
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminRewardDomainController.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminRewardDomainService.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminRewardDomainServiceImpl.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/IndoorRuleGovernanceService.java`

### Indoor interaction alignment
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/MapTileManagement.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/IndoorRuleCenter.tsx`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/IndoorNodeBehavior.java`

### Public reward-domain runtime
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/RewardController.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicRewardDomainServiceImpl.java`

### Local verification and seed tooling
- `scripts/local/mysql/init/23-phase-23-reward-domain.sql`
- `scripts/local/mysql/init/24-phase-23-reward-seed.sql`
- `scripts/local/smoke-phase-23-reward-domain.ps1`

</canonical_refs>

<specifics>
## Specific Product Direction To Preserve

- The split reward IA from Phase 23 stays in place:
  - redeemable prizes
  - game rewards
  - honors or titles as a filtered reward view
  - reward rule center
- Shared reward-rule references should feel operator-visible and inspectable, not hidden inside opaque JSON.
- The verified result must be strong enough for a milestone audit rerun to see Phase 23 as genuinely closed rather than "probably fixed."

</specifics>

<deferred>
## Deferred Ideas

- Full mini-program reward-experience acceptance remains a later milestone concern unless Phase 25 discovers a public contract blocker.
- Broader reward economy expansion, moderation workflow, or deeper reward-presentation redesign remain out of scope for this closure pass.

</deferred>

---

*Phase: 25-reward-domain-synchronization-and-verification-closure*
*Context gathered: 2026-04-19*
