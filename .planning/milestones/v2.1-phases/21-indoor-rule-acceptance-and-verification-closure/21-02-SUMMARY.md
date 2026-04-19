---
phase: 21-indoor-rule-acceptance-and-verification-closure
plan: 02
subsystem: admin-acceptance
requirements-completed: [RULE-01, RULE-02, RULE-04, RULE-05]
completed: 2026-04-19
---

# Phase 21 Plan 02 Summary

## Outcome

The live admin acceptance path is now re-verified and two real deep-link/context defects were fixed during verification.

## Delivered

- Fixed `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/MapTileManagement.tsx` so authoring deep-links keep the requested `floorId` instead of falling back to the showcase default floor.
- Fixed `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/IndoorRuleCenter.tsx` so an earlier unfiltered overview response cannot overwrite the scoped `buildingId` / `floorId` result.
- Verified the real admin shell flow on `http://127.0.0.1:5173/admin`:
  - login shell exists
  - Lisboa `1F` authoring page opens
  - governance center deep-link opens
  - governance detail drawer opens
  - return to authoring lands back on the same floor

## Evidence

- Screenshots:
  - `test-results/phase21/phase21-login.png`
  - `test-results/phase21/phase21-authoring.png`
  - `test-results/phase21/phase21-governance.png`
  - `test-results/phase21/phase21-governance-detail.png`
- Admin tests:
  - `mvn -q "-Dtest=IndoorRuleAuthoringServiceTest,IndoorRuleGovernanceServiceTest" test`
- Admin UI build:
  - `npm.cmd run build`

## Notes

- The governance page route fix in `src/App.tsx` is now backed by actual browser evidence, not just a source-level assumption.
