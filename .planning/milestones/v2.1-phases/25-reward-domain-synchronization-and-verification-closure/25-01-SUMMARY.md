# 25-01 Summary

## Completed

- Extended indoor behavior authoring with canonical `rewardRuleIds`.
- Persisted indoor reward links through `reward_rule_bindings` using `owner_domain = indoor_behavior` and `binding_role = attached`.
- Loaded canonical reward-rule references into the indoor workbench so operators can pick shared rules directly from authored reward governance data.
- Preserved governance readback by returning linked reward-rule IDs and linked reward-rule summaries after mixed indoor/reward edits.

## Verification

- `mvn "-Dtest=IndoorRuleAuthoringServiceTest,IndoorRuleGovernanceServiceTest" test`
- `npm run build` in `packages/admin/aoxiaoyou-admin-ui`

## Outcome

`REWARD-04` is now backed by a real indoor-side write path instead of governance-only readback.
