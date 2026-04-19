---
phase: 15
slug: indoor-interaction-rule-authoring
status: planned
nyquist_compliant: true
wave_0_complete: false
created: 2026-04-15
updated: 2026-04-15
---

# Phase 15 - Validation Strategy

> Per-phase validation contract for indoor interaction-rule authoring, behavior persistence, and admin workspace usability.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 + Spring Boot Test + admin UI build verification + PowerShell admin smoke |
| **Config file** | `packages/admin/aoxiaoyou-admin-backend/pom.xml`, `packages/admin/aoxiaoyou-admin-ui/package.json` |
| **Quick run command** | `mvn test -q` in `packages/admin/aoxiaoyou-admin-backend` and `npm run build` in `packages/admin/aoxiaoyou-admin-ui` |
| **Full suite command** | `mvn test -q` in `packages/admin/aoxiaoyou-admin-backend`, `npm run build` in `packages/admin/aoxiaoyou-admin-ui`, and `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-15-indoor-authoring.ps1` |
| **Estimated runtime** | ~360 seconds |

---

## Sampling Rate

- **After every task commit:** run the touched package quick command
- **After every plan wave:** run the full Phase 15 command set
- **Before `/gsd-verify-work`:** the full suite must be green and the Phase 15 admin smoke must pass against the local stack
- **Max feedback latency:** 360 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 15-01-01 | 01 | 1 | RULE-01, RULE-02 | T-15-01 / T-15-02 | Indoor node and behavior persistence reject invalid typed rule graphs and invalid overlay geometry instead of accepting arbitrary JSON | unit/migration | `mvn test -q` in `packages/admin/aoxiaoyou-admin-backend` | add schema and validation tests | pending |
| 15-01-02 | 01 | 1 | RULE-01 | T-15-03 | Canonical node-oriented admin endpoints round-trip structured appearance, trigger, effect, and path payloads without breaking legacy marker compatibility | integration | `mvn test -q` in `packages/admin/aoxiaoyou-admin-backend` | add controller/service tests | pending |
| 15-02-01 | 02 | 2 | RULE-01 | T-15-04 / T-15-05 | Admin indoor authoring UI exposes structured rule editors and does not force operators into raw JSON-first flows | build/component | `npm run build` in `packages/admin/aoxiaoyou-admin-ui` | add indoor editor component coverage where practical | pending |
| 15-02-02 | 02 | 2 | RULE-02 | T-15-05 | Path and overlay geometry editors persist normalized coordinates and do not reintroduce floor-switch flicker or unusable editor state | build/manual-assist | `npm run build` in `packages/admin/aoxiaoyou-admin-ui` | add smoke-visible UI state guards | pending |
| 15-03-01 | 03 | 3 | RULE-01, RULE-02 | T-15-06 | Lisboa showcase data seeds meaningful authored rule examples instead of empty test shells | seed/integration | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-15-indoor-authoring.ps1 -Scenario seed` | add seed script and smoke assertions | pending |
| 15-03-02 | 03 | 3 | RULE-01, RULE-02 | T-15-06 / T-15-07 | Phase 15 smoke proves admin write and readback of structured indoor rule graphs on the live local stack | smoke | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-15-indoor-authoring.ps1` | create smoke script | pending |
| 15-03-03 | 03 | 3 | RULE-01, RULE-02 | T-15-05 | Manual authoring checks cover path editing, trigger-chain composition, and draft recovery with a resumable UAT checklist | manual | `.planning/phases/15-indoor-interaction-rule-authoring/15-UAT.md` updated during execution | create UAT scaffold | pending |

*Status: pending / green / red / flaky*

---

## Wave 0 Requirements

- [ ] `packages/admin/aoxiaoyou-admin-backend/src/test/java/.../IndoorRuleAuthoring*` - behavior-profile validation and persistence tests
- [ ] `packages/admin/aoxiaoyou-admin-backend/src/test/java/.../AdminIndoorController*` - canonical node-endpoint tests
- [ ] `scripts/local/smoke-phase-15-indoor-authoring.ps1` - live admin authoring smoke for Lisboa showcase rules

---

## Planned Execution Results

- `mvn test -q` in `packages/admin/aoxiaoyou-admin-backend`: pending
- `npm run build` in `packages/admin/aoxiaoyou-admin-ui`: pending
- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-15-indoor-authoring.ps1`: pending

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Trigger-chain usability | RULE-01 | Build success cannot prove that operators understand prerequisite trigger ordering | Open one indoor node, create at least three trigger steps, make the second depend on the first, and confirm the editor remains understandable without switching to raw JSON |
| Path-editing clarity | RULE-02 | Automated tests cannot prove that point picking, reordering, and path cleanup are understandable | On a Lisboa floor, create one motion path with at least four points, reorder one point, delete one point, and confirm the preview updates correctly |
| Draft recovery | RULE-01 | Autosave and restore behavior depends on real operator flow | Start editing a node, leave without saving, reload the page, and confirm the draft can be restored and saved successfully |

---

## Validation Sign-Off

- [x] All tasks have automated verification expectations or explicit Wave 0 dependencies
- [x] Sampling continuity is preserved across admin backend, admin UI, and live admin smoke verification
- [x] Wave 0 captures the missing rule-authoring validation and smoke coverage required by Phase 15
- [x] No watch-mode flags
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** planned 2026-04-15
