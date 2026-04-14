---
phase: 04-public-progress-and-gameplay-writes
plan: 01
subsystem: api
tags: [user-state, auth, mysql, gameplay-writes]
requires: [03-03]
provides:
  - canonical public traveler profile, preference, progress, check-in, and reward-redemption persistence
  - public JWT-backed write authentication for `/api/v1/user/*`
  - transactional backend service logic for login/bootstrap, current-city updates, check-ins, and reward redemption
affects: [04-02, 04-03]
tech-stack:
  added: []
  patterns: [guest-session bootstrap, server-authoritative user state, transactional write orchestration]
key-files:
  created:
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/auth/JwtUtil.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/auth/PublicAuthContext.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/auth/PublicAuthInterceptor.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/UserProfile.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/UserPreference.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/UserProgress.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/UserCheckin.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/RewardRedemption.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/mapper/UserProfileMapper.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/mapper/UserPreferenceMapper.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/mapper/UserProgressMapper.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/mapper/UserCheckinMapper.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/mapper/RewardRedemptionMapper.java
  modified:
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/config/WebConfig.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/UserService.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/UserServiceImpl.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/request/UserLoginRequest.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/request/UserBootstrapStateRequest.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/request/UserCheckinRequest.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/request/UserCurrentCityUpdateRequest.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/request/UserPreferencesUpdateRequest.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/UserStateResponse.java
key-decisions:
  - "Use a lightweight public JWT flow for traveler writes while keeping public content reads anonymous."
  - "Bootstrap first-login server state from the mini-program's existing local snapshot to avoid wiping progress during cutover."
  - "Persist bootstrapped redeemed rewards into `reward_redemptions` so legacy mock rewards survive migration."
patterns-established:
  - "Guest traveler sessions are auto-created for the mini-program and then synchronized into canonical server state."
  - "Server snapshots, not local mock mutations, are now the authoritative source for progress/prefs after login."
requirements-completed: [PUB-03]
duration: brownfield pass
completed: 2026-04-12
---

# Phase 4: Plan 01 Summary

**Canonical public user-state persistence and write-side service logic now live in `packages/server`.**

## Accomplishments

- Added public JWT helpers and interceptor wiring so `/api/v1/user/*` writes execute under authenticated traveler context instead of legacy admin-coupled assumptions.
- Replaced the old `users`-table-centric flow with canonical MySQL-backed entities and mappers for `user_profiles`, `user_preferences`, `user_progress`, `user_checkins`, and `reward_redemptions`.
- Rebuilt `UserServiceImpl` around login/bootstrap, current-city updates, check-ins, preferences/emergency-contact writes, progress aggregation, and reward redemption.
- Fixed the bootstrap migration gap so `redeemedRewardIds` now generate persistent `reward_redemptions` rows and update reward inventory counters.

## Verification

- `mvn -q -DskipTests compile` in `packages/server`
- Live HTTP smoke against `http://127.0.0.1:8080/api/v1/user/*`
- MySQL row verification for `user_profiles`, `user_preferences`, `user_progress`, `user_checkins`, and `reward_redemptions`

## Notes

- The plan initially compiled before runtime verification, but the first live smoke exposed that the running `8080` process was stale. The phase was closed only after replacing that process with the current build and rerunning the write flow on `8080`.
- Reward-bootstrap persistence was the main runtime defect found in Plan 01; it was fixed in-flight and reverified through real API + SQL checks.

---
*Phase: 04-public-progress-and-gameplay-writes*
*Completed: 2026-04-12*
