# Phase 25: Reward Domain Synchronization and Verification Closure - Research

**Researched:** 2026-04-19  
**Confidence:** High for the current reward-domain and indoor-rule code paths, medium for the exact amount of copy cleanup needed because some reward UI files still contain mojibake literals on this workstation

## Current-State Gap

Phase 23 delivered most of the reward-domain redesign:

- split redeemable prizes, game rewards, honors, rule center, and presentation center
- canonical `reward_rules`, `reward_rule_bindings`, and `reward_presentations`
- admin CRUD for prize and reward authoring
- public reward-domain payloads through `PublicCatalogServiceImpl`
- live seed and smoke coverage through the Phase 23 local scripts

But the milestone audit is correct that `REWARD-04` is still not honestly closed.

What exists today:

- reward-side authoring can attach shared rule IDs through guided forms
- the backend persists those bindings through `reward_rule_bindings`
- indoor governance can display linked reward rules and linked rewards for behaviors
- delete guards already know about indoor-behavior bindings

What still appears missing:

- an indoor authoring write path that lets operators add or remove shared reward-rule bindings from the interaction-authoring surface itself
- a formal `23-VERIFICATION.md` that supersedes the old diagnosed `23-UAT.md`
- refreshed traceability that marks `REWARD-01` through `REWARD-05` complete based on current live evidence instead of stale partial state

That means Phase 25 is mostly a synchronization-and-proof phase, not a schema-from-scratch phase.

## Existing Implementation Evidence

### Reward-side shared-rule foundation is already real

`packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminRewardDomainServiceImpl.java` already provides the canonical reward-side rule-binding flow:

- redeemable prizes call `syncRuleBindings(...)`
- game rewards call `syncRuleBindings(...)`
- reward CRUD returns `ruleIds` and `linkedRules`
- rule deletion is guarded when bindings still exist
- overview and detail loaders can resolve linked owners across prize, game reward, and indoor behavior

This is the strongest evidence that Phase 25 should extend the existing shared binding model rather than invent a parallel reward-trigger mechanism.

### Reward authoring UI already exposes shared rule selection

`packages/admin/aoxiaoyou-admin-ui/src/components/rewards/RewardDomainShared.tsx` already has:

- `RewardRelationSection`
- `Form.Item name="ruleIds"`
- selected-rule summary cards
- refreshable rule reference loading via `getAdminRewardRules(...)`

So the reward side is already form-first and operator-visible.

### Indoor governance can read reward links, but indoor authoring does not obviously write them

`packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/IndoorRuleGovernanceService.java` already:

- loads `reward_rule_bindings` where `owner_domain = indoor_behavior`
- returns `linkedRewardRuleIds`
- returns `linkedRewardRules`
- resolves linked reward owners across redeemable prizes and game rewards

That proves the indoor-governance read model is already aware of shared reward rules.

However, current code inspection did **not** surface a matching indoor authoring request contract:

- request DTOs visibly expose `ruleIds` only for reward-domain upsert requests
- `IndoorMarkerAuthoringService` handles `behaviors`, path graphs, and overlay geometry, but no visible reward-rule binding field was found in the request layer
- `MapTileManagement.tsx` did not surface reward-rule editing strings or `linkedRewardRuleIds` usage in the authoring page

This is the clearest technical explanation for why `REWARD-04` is still open: the shared rule system exists, but the operator cannot yet maintain the shared relationship from both sides of the intended contract.

### Public reward-domain contract is already in place

`packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java` already:

- lists published redeemable prizes
- lists published game rewards
- resolves `RewardPresentation`
- resolves `RewardRuleSummaryResponse`
- returns relation bindings and media attachments

This means Phase 25 probably does **not** need a brand-new public reward API family. It needs verification that the existing public payloads remain correct after the synchronization fix.

### Phase 23 already has live local smoke and seed tooling

The reward-domain verification baseline already exists in:

- `scripts/local/mysql/init/23-phase-23-reward-domain.sql`
- `scripts/local/mysql/init/24-phase-23-reward-seed.sql`
- `scripts/local/smoke-phase-23-reward-domain.ps1`

Phase 23 also already backfilled meaningful showcase data in `23-04-SUMMARY.md`.

That strongly suggests Phase 25 should extend the current smoke harness rather than starting a new verification harness from zero.

## Practical Gaps To Close

### 1. Indoor authoring must gain a real write path for shared reward-rule bindings

The most important implementation gap is not the binding table. It is the missing indoor authoring contract.

Phase 25 likely needs:

- indoor behavior request DTO support for shared `rewardRuleIds`
- backend sync logic that writes `reward_rule_bindings` for `owner_domain = indoor_behavior`
- reward-safe create, update, unbind, and delete semantics inside indoor behavior persistence
- UI controls in the indoor behavior workbench or equivalent authoring surface so operators can choose shared reward rules directly

Without this, governance readback alone is not enough to close `REWARD-04`.

### 2. Deletion and mutation semantics must remain deterministic

`AdminRewardDomainServiceImpl` already blocks deleting a rule that still has bindings.

Phase 25 must preserve that behavior after indoor authoring gains write access:

- removing a rule from a behavior should unbind only that relationship
- deleting a shared rule should either remain blocked while owners exist or require an explicit coordinated unbind flow
- operator messaging must name linked rewards and behaviors clearly enough for safe cleanup

### 3. Verification needs to become an artifact, not only a summary trail

The audit explicitly calls out that:

- `23-UAT.md` is still in diagnosed state
- `23-VERIFICATION.md` does not exist

Phase 25 must therefore produce a real verification artifact with:

- current live admin proof on `8081`
- current live public proof on `8080`
- explicit reward synchronization proof between reward authoring and indoor authoring or governance
- evidence strong enough to retire the diagnosed UAT state

### 4. Some reward-domain UI text is still mojibake and may block honest operator verification

Current reward UI files such as:

- `GameRewardManagement.tsx`
- `RedeemablePrizeManagement.tsx`
- `RewardDomainShared.tsx`

still show visibly corrupted strings in source on this workstation.

Phase 25 should not become a generic copy-repair phase, but it **does** need enough readable operator-facing reward surfaces for honest verification. If the shared-rule workflow cannot be reasonably verified because the relevant labels are corrupted, those specific reward-domain literals should be repaired as part of closure.

## Recommended Verification Scope

The minimum honest witness set for Phase 25 is:

### Admin reward-domain proof

- open redeemable-prize management and confirm existing split IA is still correct
- create or edit a reward-side shared rule attachment and confirm readback
- open game reward or honor management and confirm linked rule readback
- inspect reward-rule center and confirm linked owners span both reward and indoor domains

### Indoor-side synchronization proof

- open the indoor interaction authoring surface or workbench
- attach or detach a shared reward rule from a behavior
- verify the linked rule appears in governance detail and on the reward-rule owner list
- verify reward-side linked-owner summaries stay synchronized after the change

### Public proof

- query split public reward endpoints
- verify presentations and rule summaries are still returned correctly
- verify current live data still reflects the split reward-domain seed rather than legacy-only payloads

### Artifact proof

- create `23-VERIFICATION.md`
- update `REQUIREMENTS.md`
- refresh `STATE.md` through the standard planned-phase bookkeeping so milestone closure can consume the new truth state later

## Validation Architecture

Phase 25 should use a three-layer validation stack.

### Layer 1: focused backend regression tests

Purpose:

- prove reward-side and indoor-side binding writes both use the canonical `reward_rule_bindings` model
- protect delete guards and linked-owner resolution

Expected command shape:

- admin backend:
  - `mvn -q "-Dtest=AdminRewardDomainServiceImplTest,IndoorRuleGovernanceServiceTest,IndoorRuleAuthoringServiceTest" test`
- public backend:
  - `mvn -q "-Dtest=CatalogFoundationServiceImplTest,PublicRewardDomainServiceTest,PublicCatalogServiceImplCarryoverTest" test`

Likely new or expanded test cases:

- indoor behavior update persists shared reward rule bindings
- unbinding from indoor behavior removes only the intended rows
- reward rule owner list includes both reward and indoor owners after mixed edits

### Layer 2: deterministic live smoke

Purpose:

- prove the current live stack still works on admin `8081` and public `8080`
- keep verification tied to real MySQL-seeded reward-domain data

Best artifact shape:

- extend `scripts/local/smoke-phase-23-reward-domain.ps1` or create a Phase 25 closure wrapper around it

The smoke should verify:

- admin login succeeds
- reward-domain overview endpoints return split-domain data
- linked rule owners include indoor behavior where expected
- public endpoints return reward presentations and rule summaries
- no reward-domain text-health regression blocks operator verification on the key witness fixtures

### Layer 3: manual closure artifact

Purpose:

- replace the diagnosed UAT trail with a formal closure artifact

Expected artifacts:

- `23-VERIFICATION.md`
- optionally a short `25-01-SUMMARY.md` or equivalent execution summary that explains what was repaired and verified

Manual checkpoints must explicitly cover:

- reward-side shared rule authoring
- indoor-side linked rule editing or equivalent synchronization witness
- governance readback
- public reward payload proof

## Recommended Phase Shape

Phase 25 fits best as three execution plans:

1. add the missing indoor-side shared reward-rule write path on admin backend and UI
2. harden the synchronization proof surface, including any reward-domain readability fixes necessary for honest verification
3. run live smoke, create `23-VERIFICATION.md`, and refresh reward requirement traceability

This keeps product repair in Wave 1 and milestone-proof closure in Wave 2.

## What Should Not Be Reopened

- do not redesign the reward domain from scratch again
- do not reopen unrelated mini-program runtime rollout
- do not introduce a second reward-trigger representation beside `reward_rule_bindings`
- do not mark `REWARD-01` through `REWARD-05` complete unless the formal verification artifact is present and current

---

*Phase: 25-reward-domain-synchronization-and-verification-closure*  
*Research completed: 2026-04-19*
