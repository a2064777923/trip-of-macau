# Phase 23: Reward Domain Split and Acquisition Presentation System - Research

**Researched:** 2026-04-18
**Confidence:** High for current-state gap analysis and recommended phase split

## Current-State Gap

The current reward stack is still shaped around one overloaded admin reward page plus a separate badge model:

- `RewardManagement.tsx` manages redeemable-like data such as `stampCost`, `inventoryTotal`, and `inventoryRedeemed`
- `BadgeManagement.tsx` remains a parallel root flow instead of a specialization of one canonical reward family
- `AdminCollectibleController` exposes reward CRUD under `/api/admin/v1/collectibles/rewards`, which keeps the domain coupled to the older collectible cluster
- `Reward` and `AdminRewardResponse` have no canonical concept of fulfillment mode, reward type, shared rule references, or acquisition presentation assets
- `IndoorNodeBehavior` still stores `appearance_rules_json`, `trigger_rules_json`, and `effect_rules_json`, which means any reward-trigger linkage would drift unless Phase 23 introduces reference-based rule reuse

The public runtime is similarly still split:

- `/api/v1/rewards` returns one reward list
- `/api/v1/badges` remains separate
- `PublicCatalogServiceImpl` projects relation bindings and attachment assets, but it does not expose eligibility, owned reward state, presentation payloads, or shared rule summaries

## Codebase Findings

### Admin reward model

Current reward entity and DTO fields are limited to:

- localized name / subtitle / description / highlight
- stamp cost and inventory counters
- cover asset and preset JSON
- publish window
- storyline / city / sub-map / indoor / attachment bindings

Missing from the canonical contract today:

- `rewardFamily`
- `rewardType`
- `fulfillmentMode`
- `ruleIds`
- `presentationId`
- stackability / equip / consume semantics
- code-pool / postal / venue structured fields

### Badge model

`Badge` is already closer to an in-game reward than a redeemable prize:

- icon / animation fields
- rarity
- visibility flag
- preset-driven presentation
- scope bindings

This strongly supports merging badges and titles into `game_reward` while keeping a filtered `榮譽與稱號` operator view.

### Binding infrastructure

`AdminCollectibleServiceImpl` already uses `AdminContentRelationService.syncTargetIds(...)` for:

- storyline bindings
- city bindings
- sub-map bindings
- indoor building bindings
- indoor floor bindings
- attachment asset bindings

That relation infrastructure is a strong fit for the new reward families, and Phase 23 should reuse it instead of inventing a second binding system.

### Interaction-rule platform

`IndoorNodeBehavior` currently persists:

- `appearance_rules_json`
- `trigger_rules_json`
- `effect_rules_json`
- `path_graph_json`
- `overlay_geometry_json`

`IndoorRuleCenter.tsx` already exposes governance patterns such as:

- overview filters
- linked entity summaries
- conflict counts
- status toggles

This is the right baseline for a reward rule center. Phase 23 should reuse its governance patterns, but the reward side must move from inline JSON to shared rule references plus human-readable summaries.

## Recommended Canonical Model

### Reward families

Use two root families:

- `redeemable_prize`
- `game_reward`

### Shared rule system

Use a shared rule model with:

- `reward_rule`
- `reward_condition_group`
- `reward_condition`
- `reward_rule_binding`

This allows one saved rule to be linked from:

- a redeemable prize
- a game reward
- an indoor interaction behavior

### Presentation model

Use a separate presentation layer with:

- `reward_presentation`
- `reward_presentation_step`

This prevents cinematic reward ceremonies from being buried as ad hoc JSON blobs inside reward content.

## Recommended Admin IA

The old reward page should split into:

- `兌換獎勵物品管理`
- `遊戲內獎勵配置`
- `獎勵規則與演出中心`

`榮譽與稱號` should exist as a filtered sub-view of `遊戲內獎勵配置`, not as a separate root data model.

## Migration Guidance

### Legacy mapping

Recommended mapping of existing data:

- legacy `rewards` rows with stock / stamp redemption semantics -> `redeemable_prize`
- legacy `badges` rows -> `game_reward` with `rewardType = badge`
- future titles -> `game_reward` with `rewardType = title`

### Compatibility discipline

Do not keep the old reward model as the canonical write model after Phase 23 begins. A temporary compatibility read projection is acceptable, but the write path should move to the split domain so the admin IA and runtime can stop drifting.

## Risks To Plan Around

### 1. JSON duplication across reward and interaction surfaces

If reward-trigger conditions stay duplicated inside indoor behavior JSON and reward JSON, the system will drift immediately when one side is edited first.

### 2. Presentation logic mixed directly into reward tables

If fullscreen video, animation, narration, and playback policy fields are bolted directly onto one reward table, operator UX and runtime payload design will become harder to scale.

### 3. Redeemable and in-game concerns mixed together

If offline pickup, postal delivery, code pools, title equip, fragment stacking, and badge rarity all remain on one form, the control plane will become less understandable than it is today.

## Recommended Plan Shape

Split execution into three plans:

1. Backend canonical domain split, schema, DTOs, migration, and admin APIs
2. Admin IA rebuild and structured operator authoring UX
3. Public/runtime alignment, indoor-rule synchronization, seed data, and verification

## Validation Focus

Execution should prove at least:

- admin routes are cleanly split and no longer reuse the old reward CRUD surface
- shared reward rules are referenced from both reward and indoor interaction surfaces
- public/runtime payloads can distinguish redeemable prizes from owned in-game rewards
- acquisition presentation payloads can carry fullscreen / audio ceremony metadata without leaking admin-only data

## Sources

Primary local references:

- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/RewardManagement.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/BadgeManagement.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/IndoorRuleCenter.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminCollectibleController.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminCollectibleServiceImpl.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/Reward.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/Badge.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/IndoorNodeBehavior.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/RewardController.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java`
