---
phase: 21-indoor-rule-acceptance-and-verification-closure
status: complete-with-accepted-carryover
completed: 2026-04-19
---

# Phase 21 Verification

Overall result: COMPLETE WITH ACCEPTED CARRYOVER

Phase 21 has live local proof for the indoor authoring, governance, and public-runtime chain. The only unexecuted slice was the WeChat DevTools experiential acceptance for the mini-program indoor page. On 2026-04-19, the user explicitly decided that mini-program frontend acceptance belongs to a later milestone, so that remaining slice is now recorded as accepted carryover rather than a blocker for moving on to Phase 22.

## Evidence Inventory

- Live closure smoke:
  - `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-21-indoor-closure.ps1 -AdminBaseUrl http://127.0.0.1:8081 -PublicBaseUrl http://127.0.0.1:8080 -Username admin -Password admin123 -SkipAdminTests -SkipAdminBuild -SkipServerTests -SkipClientBuild`
- Admin browser verification:
  - Headless Edge automation against `http://127.0.0.1:5173/admin`
  - Screenshots:
    - `test-results/phase21/phase21-login.png`
    - `test-results/phase21/phase21-authoring.png`
    - `test-results/phase21/phase21-governance.png`
    - `test-results/phase21/phase21-governance-detail.png`
- Admin backend tests:
  - `mvn -q "-Dtest=IndoorRuleAuthoringServiceTest,IndoorRuleGovernanceServiceTest" test`
- Public backend tests:
  - `mvn -q "-Dtest=PublicIndoorRuntimeServiceTest,PublicIndoorRuntimeInteractionServiceTest" test`
- Frontend builds:
  - `npm.cmd run build` in `packages/admin/aoxiaoyou-admin-ui`
  - `npm.cmd run build:weapp` in `packages/client`

## Requirement Mapping

### RULE-01
Status: PASS

Proof:
- Phase 21 smoke replays `scripts/local/smoke-phase-15-indoor-authoring.ps1` on live Lisboa `1F`.
- Authoring witness set was normalized by `scripts/local/seed-lisboeta-indoor.ps1`.
- The current authoring surface loads on the real admin shell and exposes structured rule composition entrypoints on the live floor.

Why this passes:
- Operators still have a working structured authoring path for appearance/trigger/effect data on real authored nodes, and Phase 21 did not find any regression that blocks those flows.

### RULE-02
Status: PASS

Proof:
- `smoke-phase-15-indoor-authoring.ps1` still round-trips persisted path/overlay rule content.
- `smoke-phase-17-indoor-runtime.ps1` still returns the authored `zipcity-guiding-path` with `path_motion`.
- Browser authoring verification loads the actual Lisboa `1F` floor with the path-bearing witness content.

Why this passes:
- Persisted geometry/path-based behavior metadata remains intact from authoring through public runtime evaluation.

### RULE-03
Status: PASS WITH ACCEPTED CARRYOVER

Proof:
- `smoke-phase-17-indoor-runtime.ps1` passed on `8080` and confirmed:
  - runtime snapshot loads for Lisboa `1F`
  - `zipcity-guiding-path` is accepted with `path_motion` + `popup`
  - anonymous `royal-palace-dwell-reveal` is blocked with `auth_required`
  - `zh-Hant` witness labels are correct
- `mvn -q "-Dtest=PublicIndoorRuntimeServiceTest,PublicIndoorRuntimeInteractionServiceTest" test` passed.
- `npm.cmd run build:weapp` passed.

Accepted carryover:
- No actual WeChat DevTools session was executed here, so the visual runtime checks and real mini-program auth-wall / authenticated retry flow remain unproven in this phase artifact.
- On 2026-04-19 the user explicitly deferred that mini-program frontend acceptance work to a later milestone.

Why this closes for now:
- Backend/runtime contract proof is strong and current.
- The missing evidence is limited to the future mini-program frontend acceptance slice, not a reproduced runtime defect in the shipped admin/public chain.

### RULE-04
Status: PASS

Proof:
- Browser automation verified:
  - authoring deep-link into Lisboa `1F`
  - workbench host page renders on the real admin shell
  - governance handoff entrypoint is available from the authoring page
- Phase 21 fixed `MapTileManagement.tsx` so floor-specific deep-links no longer collapse back to the showcase default floor.
- Admin governance/authoring tests passed.

Why this passes:
- Dedicated validated workbench flows remain operational, and Phase 21 closed a real context-preservation defect in the authoring route.

### RULE-05
Status: PASS

Proof:
- Browser automation verified governance center open, detail inspection, and return-to-authoring flow.
- Phase 21 fixed `IndoorRuleCenter.tsx` so scoped governance rows are not overwritten by a stale unfiltered request.
- `smoke-phase-16-indoor-rule-governance.ps1` passed after the witness refresh.
- `mvn -q "-Dtest=IndoorRuleAuthoringServiceTest,IndoorRuleGovernanceServiceTest" test` passed.

Why this passes:
- Operators now have a live, scoped governance surface that can be entered from authoring, inspected, and returned from without losing floor context.

## Real Fixes Discovered During Verification

- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/MapTileManagement.tsx`
  - preserved external `floorId` preference when the building detail reloads
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/IndoorRuleCenter.tsx`
  - added stale-request protection so the initial unfiltered overview response cannot overwrite the scoped building/floor result
- `scripts/local/smoke-phase-21-indoor-closure.ps1`
  - child stage output is now emitted to the console without polluting the returned `stages` JSON payload

## Closure Decision

Phase 21 is closed for current milestone sequencing.

What is closed now:
- admin authoring acceptance
- governance acceptance
- live public runtime contract proof
- formal verification artifact gap

Accepted carryover to a later milestone:
- WeChat DevTools indoor visual acceptance
- mini-program auth-wall routing and authenticated retry acceptance under real `wx.login`
- final mini-program-side visual copy and animation confirmation

Next sequencing step:
- move to Phase 22 AI platform verification and provider default closure
