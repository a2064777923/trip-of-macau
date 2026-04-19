---
status: verified
phase: 23-reward-domain-split-and-acquisition-presentation-system
verified_at: 2026-04-19T16:01:55.9696095+08:00
supersedes:
  - 23-UAT.md
evidence:
  - scripts/local/smoke-phase-23-reward-domain.ps1
  - packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/IndoorRuleAuthoringServiceTest.java
  - packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/IndoorRuleGovernanceServiceTest.java
  - packages/admin/aoxiaoyou-admin-ui/src/components/rewards/RewardDomainShared.tsx
---

## Live Stack

- Admin backend: `http://127.0.0.1:8081`
- Public backend: `http://127.0.0.1:8080`
- Admin backend runtime: repackaged from current source on 2026-04-19 with `mvn -DskipTests package`
- Public backend runtime: repackaged from current source on 2026-04-19 with `mvn -DskipTests package`

## Executed Verification

### 1. Indoor shared-rule regression

- Command: `mvn "-Dtest=IndoorRuleAuthoringServiceTest,IndoorRuleGovernanceServiceTest" test`
- Location: `packages/admin/aoxiaoyou-admin-backend`
- Result: pass
- Evidence:
  - indoor behavior authoring writes canonical `reward_rule_bindings` rows with `owner_domain = indoor_behavior`
  - indoor unbind paths clear only the indoor-side canonical binding
  - governance detail still returns `linkedRewardRuleIds`, `linkedRewardRules`, and mixed reward owners

### 2. Reward-domain admin UI build

- Command: `npm run build`
- Location: `packages/admin/aoxiaoyou-admin-ui`
- Result: pass
- Evidence:
  - split reward-domain routes compile as dedicated pages
  - reward-domain shared forms render readable Traditional Chinese witness copy
  - linked-owner summaries now surface readable domain labels for redeemable prizes, in-game rewards, and indoor interactions

### 3. Live reward smoke on rebuilt `8081` and `8080`

- Command: `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-23-reward-domain.ps1`
- Result: pass
- Evidence covered by the smoke script:
  - admin login on `8081`
  - admin reward governance summary and rule inventory
  - a live shared rule with mixed `game_reward` and `indoor_behavior` ownership
  - admin redeemable prize, rule, and honor witnesses with readable Traditional Chinese copy
  - public `GET /api/v1/redeemable-prizes?locale=zh-Hant`
  - public `GET /api/v1/game-rewards?locale=zh-Hant`
  - public `GET /api/v1/reward-presentations/{id}?locale=zh-Hant`
  - readable localized public prize, title, rule-summary, and presentation payloads

## Requirement Closure

### REWARD-01

- Closed by the split admin reward workspaces and governance surfaces exercised in the current admin build.
- Witnesses: redeemable prizes, in-game rewards, honors/titles, shared-rule governance, presentation governance.

### REWARD-02

- Closed by grouped redeemable-prize rule authoring plus live prize witnesses returned through admin/public reward-domain endpoints.
- Witnesses: grouped rule summaries, fulfillment configuration, readable rule summary payloads.

### REWARD-03

- Closed by the canonical in-game reward model covering badges, titles, fragments, and related map/sub-map bindings.
- Witnesses: live honor/title rewards returned from admin/public split reward endpoints.

### REWARD-04

- Closed by canonical shared-rule synchronization between reward management and indoor interaction authoring.
- Witnesses:
  - admin backend regression tests passed
  - live governance overview exposed `linkedIndoorBehaviorCount >= 1`
  - live shared rule `rule_fire_archive_fragment_grant` exposed both `game_reward` and `indoor_behavior` owners

### REWARD-05

- Closed by live reward-presentation payloads and admin presentation governance witnesses.
- Witnesses: public presentation contract includes presentation metadata, step list, queue policy, minimum display duration, and readable Traditional Chinese step titles.

## Notes

- During verification, `8080` initially served an older jar that did not expose the reward public routes. Repackaging and restarting from current source restored the live runtime to the current codebase before final smoke.
- `23-UAT.md` remains as the historical diagnosis trail only. This file is now the authoritative closure artifact for Phase 23.
