# Phase 23 Wave 1 Summary

## Outcome

Wave 1 established the canonical split reward-domain backend foundation.

- Added the new MySQL schema in [23-phase-23-reward-domain.sql](/D:/Archive/trip-of-macau/scripts/local/mysql/init/23-phase-23-reward-domain.sql) for:
  - `redeemable_prizes`
  - `game_rewards`
  - `reward_rules`
  - `reward_condition_groups`
  - `reward_conditions`
  - `reward_rule_bindings`
  - `reward_presentations`
  - `reward_presentation_steps`
- Added deterministic migration logic from legacy `rewards` and `badges` into the split domain, including:
  - legacy presentation creation
  - relation cloning from `content_relation_links`
  - legacy source traceability via `legacy_source_type` and `legacy_source_id`
- Added showcase seed data in [24-phase-23-reward-seed.sql](/D:/Archive/trip-of-macau/scripts/local/mysql/init/24-phase-23-reward-seed.sql) for:
  - `offline_pickup` prize
  - `voucher_code` prize
  - `badge`
  - `title`
  - `city_fragment`
  - `fullscreen_video` acquisition presentation
  - shared rule binding to an indoor behavior when one exists
- Added focused admin backend tests in [AdminRewardDomainServiceImplTest.java](/D:/Archive/trip-of-macau/packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminRewardDomainServiceImplTest.java) covering:
  - redeemable prize creation with shared rule bindings
  - reward presentation step persistence
  - delete guard when a shared rule is still referenced by indoor behavior

## Verification

- `mvn -q -DskipTests compile`
  - cwd: `D:\Archive\trip-of-macau\packages\admin\aoxiaoyou-admin-backend`
- `mvn -q -Dtest=AdminRewardDomainServiceImplTest test`
  - cwd: `D:\Archive\trip-of-macau\packages\admin\aoxiaoyou-admin-backend`

## Notes

- Existing legacy reward and badge tables remain intact for compatibility during the transition.
- The split-domain admin APIs created earlier in this phase now have matching schema and seed support.
- Indoor governance synchronization and public/runtime read contracts are deferred to Wave 3.
