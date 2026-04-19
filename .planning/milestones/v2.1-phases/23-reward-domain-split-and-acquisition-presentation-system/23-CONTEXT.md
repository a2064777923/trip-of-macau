# Phase 23: Reward Domain Split and Acquisition Presentation System - Context

**Gathered:** 2026-04-18
**Status:** Ready for planning
**Source:** Reward-domain redesign discussion after Phase 20 closure

<domain>
## Phase Boundary

This phase rebuilds the reward domain. It is not a minor field-addition pass on top of the current reward CRUD.

It covers:
- splitting redeemable prizes from in-game rewards in both the admin IA and the canonical backend model
- folding badges, titles, city currencies, fragments, unlock passes, voice packs, and similar assets into one canonical in-game reward family
- introducing a shared `reward_rule` model that can be referenced from reward management and indoor interaction behaviors
- introducing configurable reward-acquisition presentation flows, including fullscreen animation, fullscreen video, SFX, and voice-over
- aligning the resulting contracts across admin backend, admin UI, public backend, and later mini-program runtime

It does not cover:
- final approval workflow or content moderation design for rewards
- external courier or offline POS integration beyond the canonical fulfillment data model and operator workflow
- broad gameplay economy balancing beyond what is needed to define fields, conditions, and issuance semantics
- replacing the existing indoor-rule platform; this phase reuses and extends it

</domain>

<decisions>
## Implementation Decisions

### IA split
- **D-01:** The current admin reward area must split into:
  - `兌換獎勵物品管理`
  - `遊戲內獎勵配置`
  - `獎勵規則與演出中心`
- **D-02:** `榮譽與稱號` should stop being a separate root data model. It may remain as a filtered view or owned subsection over the canonical in-game reward domain for honor-like reward types such as badges and titles.

### Canonical reward domains
- **D-03:** Use two top-level reward families:
  - `redeemable_prize`
  - `game_reward`
- **D-04:** `game_reward` must support at least:
  - badge
  - title
  - city_currency
  - city_fragment
  - unlock_pass
  - voice_pack
  - cosmetic
  - collectible_bonus
- **D-05:** Titles belong inside `game_reward`, not in a standalone subsystem.

### Conditions and rule reuse
- **D-06:** Reward conditions must move to a shared rule engine instead of freeform JSON embedded separately in rewards and interaction behaviors.
- **D-07:** The condition builder must support grouped logic:
  - all conditions in a group
  - any condition in a group
  - at least `N` conditions in a group
- **D-08:** Reward-trigger rules used by interaction behaviors must be referenced, not duplicated. Editing or deleting a rule must synchronize naturally across the reward UI and the interaction-authoring UI.

### Presentation and acquisition ceremony
- **D-09:** Reward acquisition requires its own presentation model, separate from reward content and separate from interaction behavior definitions.
- **D-10:** The acquisition presentation model must support:
  - toast
  - modal card
  - fullscreen animation
  - fullscreen video
  - mixed media sequence
  - trigger SFX
  - trigger voice-over / narration
- **D-11:** Presentation playback must carry policy fields such as first-time-only, skippable, minimum dwell, interruptibility, queueing, and severity / rarity priority.

### Fulfillment model
- **D-12:** Redeemable prizes must model fulfillment mode explicitly instead of inferring it from description text. The supported baseline modes are:
  - offline pickup
  - postal delivery
  - virtual issuance
  - voucher or code
- **D-13:** Fulfillment-specific fields must live in structured configs, not freeform notes.

### Scope discipline
- **D-14:** This phase should redesign the domain model and the operator workflows cleanly; it should not preserve the current overloaded reward DTO just for short-term compatibility if that would further corrupt the model.
- **D-15:** Public and mini-program alignment must be part of the plan, but full gameplay rollout of every reward type may still need follow-on phases if runtime complexity becomes large.

</decisions>

<canonical_refs>
## Canonical References

### Current admin surfaces
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/RewardManagement.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/CollectibleManagement.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/BadgeManagement.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/CollectionAuthoringShared.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/App.tsx`

### Current admin contracts
- `packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts`
- `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminCollectibleController.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminCollectibleService.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminCollectibleServiceImpl.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/Reward.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/Badge.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/Collectible.java`

### Interaction-rule references that must stay aligned
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/IndoorRuleCenter.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/MapTileManagement.tsx`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/IndoorNodeBehavior.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/IndoorRuleGovernanceService.java`

### Public/runtime references
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/RewardController.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/BadgeController.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java`
- `packages/client/src/services/api.ts`
- `packages/client/src/services/gameService.ts`

</canonical_refs>

<code_context>
## Existing Code Insights

- The current `RewardManagement.tsx` uses the same form pattern as collectibles and badges, which is why redeemable prizes and in-game rewards are currently conflated.
- The current `AdminRewardItem` contract models cost, stock, and presentation presets, but it has no native concept of fulfillment type, shared grouped conditions, title-like rewards, or acquisition ceremony assets.
- Interaction behavior tooling already exists in Phases 15-17, so the reward redesign should reuse rule references and governance concepts rather than inventing a second incompatible trigger system.
- Phase 20 proved that reward routing drift can break honest verification, so Phase 23 should avoid placeholder ownership ambiguity and keep each reward domain on its own dedicated page.

</code_context>

<specifics>
## Specific Product Direction To Preserve

- The admin IA should rename the current reward page to `兌換獎勵物品管理`.
- A new `遊戲內獎勵配置` page should manage badges, titles, currencies, fragments, and similar internally-issued reward assets.
- Reward conditions must support exploration progress, unlocked scenes, owned fragments, prior interaction triggers, and similar gameplay signals.
- Some rewards or titles may trigger a strong acquisition ceremony including fullscreen video or animation plus SFX. This must be explicit in the design, not a future afterthought.
- Editing a reward-trigger condition from the reward side or the interaction side must update one shared rule definition.

</specifics>

<deferred>
## Deferred Ideas

- Full downstream mini-program experience acceptance for every new reward type can be split after the control-plane and contract redesign if needed.
- Physical shipping provider integration and redemption logistics automation can remain a later follow-on if structured fulfillment configs exist first.

</deferred>

---

*Phase: 23-reward-domain-split-and-acquisition-presentation-system*
*Context gathered: 2026-04-18*
