---
status: complete
phase: 21-indoor-rule-acceptance-and-verification-closure
source:
  - 21-01-SUMMARY.md
  - 21-02-SUMMARY.md
  - 21-03-SUMMARY.md
started: 2026-04-18T22:37:00+08:00
updated: 2026-04-19T00:29:01+08:00
---

## Current Test

[testing complete]

# Phase 21 UAT

Status: Complete for the current `v2.1` sequencing scope. Admin/browser and live runtime proof passed; the remaining WeChat DevTools experiential checks were explicitly accepted as future-milestone carryover by the user on 2026-04-19.

## Preconditions

- Admin backend running on `http://127.0.0.1:8081`
- Public backend running on `http://127.0.0.1:8080`
- Admin UI dev shell running on `http://127.0.0.1:5173/admin`
- Lisboa indoor fixture reseeded through `scripts/local/seed-lisboeta-indoor.ps1`
- Canonical witness floor: `lisboeta_macau / 1F / floorId=12`

## Witness Set

- Nodes:
  - `1f-phase15-night-market-overlay`
  - `1f-phase15-royal-palace-dwell`
  - `1f-phase15-zipcity-path`
- Behaviors:
  - `night-market-schedule-overlay`
  - `royal-palace-dwell-reveal`
  - `zipcity-guiding-path`

## Admin Acceptance

| Checkpoint | Result | Evidence |
| --- | --- | --- |
| Login shell renders with usable username/password form | PASS | Browser automation against `http://127.0.0.1:5173/admin/#/login`; screenshot `test-results/phase21/phase21-login.png` |
| Deep-link into indoor authoring preserves `buildingId=5` and `floorId=12` | PASS | Browser automation after Phase 21 fix in `MapTileManagement.tsx`; screenshot `test-results/phase21/phase21-authoring.png` |
| Authoring page renders the governance handoff entrypoint on Lisboa `1F` | PASS | Browser automation waited for the governance-entry button on the real authoring page |
| Governance center opens from the authoring page with the correct building/floor context | PASS | Browser automation opened `#/space/indoor-rules?buildingId=5&floorId=12`; screenshot `test-results/phase21/phase21-governance.png` |
| Governance overview stays scoped to `1F` instead of being overwritten by an earlier unfiltered response | PASS | Fixed request race in `IndoorRuleCenter.tsx`; browser diagnostic now shows `1F` rows only |
| Governance detail drawer opens for a scoped rule and shows conflict/status information | PASS | Browser automation opened the first visible row detail; screenshot `test-results/phase21/phase21-governance-detail.png` |
| Governance detail can return to the owning authoring floor without dropping back to `G` | PASS | Browser automation returned to `#/space/indoor-buildings?tab=authoring&buildingId=5&floorId=12` after detail handoff |

## Runtime Acceptance

| Checkpoint | Result | Evidence |
| --- | --- | --- |
| Phase 21 closure smoke runs fixture prep -> authoring smoke -> governance smoke -> runtime smoke on the live stack | PASS | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-21-indoor-closure.ps1 ...` |
| `zh-Hant` witness node and behavior labels remain coherent in the live runtime payload | PASS | `smoke-phase-17-indoor-runtime.ps1` asserts the three canonical witness node/behavior labels for the Lisboa `1F` showcase set |
| Supported path-motion guidance is evaluated safely against authored data | PASS | `zipcity-guiding-path` accepted with `path_motion` and `popup` effect categories |
| Guarded dwell behavior is blocked safely for anonymous use | PASS | `royal-palace-dwell-reveal` returned `requiresAuth=true` and `blockedReason=auth_required` |
| Admin governance/unit tests still pass after the Phase 21 deep-link fixes | PASS | `mvn -q "-Dtest=IndoorRuleAuthoringServiceTest,IndoorRuleGovernanceServiceTest" test` |
| Public runtime/unit tests still pass after the Phase 21 closure work | PASS | `mvn -q "-Dtest=PublicIndoorRuntimeServiceTest,PublicIndoorRuntimeInteractionServiceTest" test` |
| Current admin UI still builds after the Phase 21 deep-link fixes | PASS | `npm.cmd run build` in `packages/admin/aoxiaoyou-admin-ui` |
| Current mini-program still produces a WeChat build artifact | PASS | `npm.cmd run build:weapp` in `packages/client` |

## WeChat DevTools Checks

| Checkpoint | Result | Reason |
| --- | --- | --- |
| Floor switching shows loading guard or mask with no visible flicker/stale overlay residue | DEFERRED | Accepted carryover to a future milestone by user decision; not executed in this session |
| `night-market-schedule-overlay` is visually rendered with authored geometry and popup content | DEFERRED | Accepted carryover to a future milestone by user decision; runtime payload proof already exists |
| `zipcity-guiding-path` visibly animates in the mini-program viewport | DEFERRED | Accepted carryover to a future milestone by user decision; API-level acceptance already passed |
| Anonymous guarded interaction routes to the mini-program auth wall | DEFERRED | Accepted carryover to a future milestone by user decision; real `wx.login` was intentionally not exercised here |
| Authenticated retry after real WeChat login succeeds without a broken reload path | DEFERRED | Accepted carryover to a future milestone by user decision |
| Visual `zh-Hant` copy quality on the mini-program indoor page | DEFERRED | Accepted carryover to a future milestone by user decision |

## Summary

total: 21
passed: 15
issues: 0
pending: 0
skipped: 6
blocked: 0

## Notes

- Phase 21 fixed two real admin closure defects while gathering evidence:
  - authoring deep-link floor context was being dropped back to the showcase default floor
  - governance overview could be overwritten by an earlier unfiltered request, which then caused incorrect authoring-return targets
- The remaining mini-program frontend experiential checks are not treated as product defects for `v2.1`; they were explicitly deferred by the user to a later milestone.
