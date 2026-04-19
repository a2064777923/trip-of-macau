---
phase: 23-reward-domain-split-and-acquisition-presentation-system
plan: 04
subsystem: database
tags: [mysql, utf8mb4, rewards, smoke, admin, public-api]
requires:
  - phase: 23-01
    provides: reward-domain split schema and admin/public reward endpoints
  - phase: 23-02
    provides: reward governance and presentation authoring surfaces
  - phase: 23-03
    provides: live verification baseline and phase-23 UAT trail
provides:
  - UTF-8-safe Phase 23 reward showcase reseed
  - multiple exploration-threshold title rewards with city and sub-map bindings
  - byte-decoded anti-mojibake smoke verification for admin 8081 and public 8080
affects: [phase-23-verification, reward-copy, honors-showcase]
tech-stack:
  added: []
  patterns: [UTF-8 byte-decoded PowerShell smoke verification, replayable reward-domain reseed]
key-files:
  created: [.planning/phases/23-reward-domain-split-and-acquisition-presentation-system/23-04-SUMMARY.md]
  modified:
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminRewardDomainServiceImpl.java
    - scripts/local/mysql/init/24-phase-23-reward-seed.sql
    - scripts/local/smoke-phase-23-reward-domain.ps1
key-decisions:
  - "Use ASCII-safe PowerShell logic plus UTF-8 byte decoding in the smoke script so Windows code page behavior cannot masquerade as reward copy corruption."
  - "Promote Phase 23 honors from a single generic title into a city-plus-sub-map showcase with Macau Peninsula, Taipa, and Coloane exploration thresholds."
  - "Repair the live MySQL fixtures by replaying the Phase 23 reward seed directly, rather than assuming source-file fixes alone would clean existing polluted rows."
patterns-established:
  - "Reward-domain smoke checks both admin and public payloads, and asserts readable zh-Hant text on named showcase fixtures."
  - "Reward showcase seeds should delete-and-reinsert owned codes so local MySQL can be repaired deterministically."
requirements-completed: [REWARD-01, REWARD-02, REWARD-03, REWARD-05]
duration: 19 min
completed: 2026-04-18
---

# Phase 23 Plan 04: Reward Text Repair and Title Showcase Summary

**UTF-8-safe reward-domain reseed with four exploration titles, repaired admin/operator copy, and a live smoke guard that validates readable Traditional Chinese on admin `8081` and public `8080`**

## Performance

- **Duration:** 19 min
- **Started:** 2026-04-18T21:47:00+08:00
- **Completed:** 2026-04-18T22:06:07+08:00
- **Tasks:** 3
- **Files modified:** 4

## Accomplishments

- Rewrote the Phase 23 reward showcase seed so the live MySQL fixtures now include readable Traditional Chinese prizes, rules, and four meaningful title rewards.
- Added Macau Peninsula, Taipa, and Coloane exploration-threshold title examples, plus a cleaner fragment grant rule, so the honor domain demonstrates the intended regional design instead of a single generic title.
- Hardened the Phase 23 smoke script to decode HTTP payloads as UTF-8 bytes, then verify admin/public reward payloads for readable zh-Hant text, required showcase codes, and sub-map bindings.
- Local verification passed on the real stack: reseeded MySQL, started `packages/server` on `8080`, and confirmed the reward smoke succeeds against admin `8081` and public `8080`.

## Task Commits

No git commit was created in this turn. The repository already contains extensive unrelated in-flight work, so the gap closure was kept as local file changes plus verified runtime evidence.

## Files Created/Modified

- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminRewardDomainServiceImpl.java` - translated Phase 23 reward-domain validation and lookup errors to Traditional Chinese.
- `scripts/local/mysql/init/24-phase-23-reward-seed.sql` - repaired reward copy, added four title rewards, added exploration-threshold rules, and made the reseed capable of overwriting polluted local fixtures.
- `scripts/local/smoke-phase-23-reward-domain.ps1` - added admin/public UTF-8 smoke assertions for readable zh-Hant reward copy and honor bindings.
- `.planning/phases/23-reward-domain-split-and-acquisition-presentation-system/23-04-SUMMARY.md` - recorded the verified closure details for this gap plan.

## Decisions Made

- Used UTF-8 byte decoding inside the PowerShell smoke path because Windows PowerShell's default decoding produced false mojibake during direct HTTP inspection.
- Kept the fix centered on Phase 23-owned seed/runtime surfaces instead of reopening older unrelated content domains.
- Repaired the live local database immediately after the seed rewrite so verification reflects real runtime data, not just source-file intent.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Windows PowerShell mis-decoded UTF-8 reward payloads**

- **Found during:** Task 3
- **Issue:** direct HTTP inspection on this workstation showed mojibake even when MySQL rows were already correct, which would have made the smoke script report false failures.
- **Fix:** replaced REST decoding in the smoke script with explicit UTF-8 byte decoding via `System.Net.Http.HttpClient`, then kept the readable-text assertions on top.
- **Files modified:** `scripts/local/smoke-phase-23-reward-domain.ps1`
- **Verification:** reran the smoke script successfully after the decoding change.

**2. [Rule 3 - Blocking] Local MySQL reward fixtures were already polluted**

- **Found during:** Task 2
- **Issue:** the live Phase 23 rows in `game_rewards`, `reward_rules`, and `redeemable_prizes` had already been written with corrupted text, so source fixes alone would not change the running admin/public behavior.
- **Fix:** rewrote the owned Phase 23 seed to delete-and-reinsert the affected showcase rows, then replayed it against the local MySQL database with `utf8mb4`.
- **Files modified:** `scripts/local/mysql/init/24-phase-23-reward-seed.sql`
- **Verification:** direct MySQL queries showed readable Traditional Chinese titles, rules, and prizes after replay.

---

**Total deviations:** 2 auto-fixed (1 bug, 1 blocking)
**Impact on plan:** both deviations were necessary to produce honest live verification; no extra product scope was added beyond the Phase 23 gap closure target.

## Issues Encountered

- `powershell` on this workstation parses script files through the legacy Windows code page when the file contains certain non-ASCII literals. The smoke script was normalized to ASCII-safe control text plus explicit UTF-8 decoding to avoid parser and validation drift.
- `packages/server` health on `/actuator/health` remained `503` because local dependencies are included in the health surface, but the public reward endpoints themselves responded successfully and were used for live verification.

## User Setup Required

None - no new external service configuration was introduced by this gap closure.

## Next Phase Readiness

- Phase 23's reward-text and honor-showcase gap is now closed with real MySQL replay plus live admin/public verification.
- Future reward work can build on the repaired title showcase and smoke guard without re-litigating the encoding regression.

---
*Phase: 23-reward-domain-split-and-acquisition-presentation-system*
*Completed: 2026-04-18*
