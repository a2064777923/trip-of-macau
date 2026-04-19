---
phase: 16
slug: indoor-rule-workbench-and-governance
status: planned
nyquist_compliant: true
wave_0_complete: false
created: 2026-04-16
updated: 2026-04-16
---

# Phase 16 - Validation Strategy

> Per-phase validation contract for the indoor rule workbench and governance-center upgrade.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 + Spring Boot Test + admin UI build verification + PowerShell admin smoke |
| **Config file** | `packages/admin/aoxiaoyou-admin-backend/pom.xml`, `packages/admin/aoxiaoyou-admin-ui/package.json` |
| **Quick run command** | `mvn test -q` in `packages/admin/aoxiaoyou-admin-backend` and `npm run build` in `packages/admin/aoxiaoyou-admin-ui` |
| **Full suite command** | `mvn test -q` in `packages/admin/aoxiaoyou-admin-backend`, `npm run build` in `packages/admin/aoxiaoyou-admin-ui`, and `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-16-indoor-rule-governance.ps1` |
| **Estimated runtime** | ~420 seconds |

---

## Sampling Rate

- **After every task commit:** run the touched package quick command
- **After every plan wave:** run the full Phase 16 command set
- **Before `/gsd-verify-work`:** the full suite must be green and the Phase 16 smoke must pass against the local stack
- **Max feedback latency:** 420 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 16-01-01 | 01 | 1 | RULE-05 | T-16-01 / T-16-02 | Governance overview and conflict classification never return orphaned or mis-scoped behavior records | unit/integration | `mvn test -q` in `packages/admin/aoxiaoyou-admin-backend` | add governance service tests | pending |
| 16-01-02 | 01 | 1 | RULE-05 | T-16-03 | Behavior enable/disable mutations reject invalid status transitions and keep parent-node state consistent | integration | `mvn test -q` in `packages/admin/aoxiaoyou-admin-backend` | add controller/service tests | pending |
| 16-02-01 | 02 | 1 | RULE-04 | T-16-04 / T-16-05 | Workbench edits remain staged until explicit apply and do not corrupt parent marker form state | build/manual-assist | `npm run build` in `packages/admin/aoxiaoyou-admin-ui` | add workbench components and state guards | pending |
| 16-02-02 | 02 | 1 | RULE-04 | T-16-05 | Minimap-assisted path and overlay tools work inside the workbench without reintroducing floor-switch jitter | build/manual-assist | `npm run build` in `packages/admin/aoxiaoyou-admin-ui` | add visual authoring shell | pending |
| 16-03-01 | 03 | 2 | RULE-05 | T-16-01 / T-16-02 | Governance center filters and conflict-only view reflect the backend projection exactly | build/smoke | `npm run build` in `packages/admin/aoxiaoyou-admin-ui` and `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-16-indoor-rule-governance.ps1 -Scenario overview` | add route/page and smoke assertions | pending |
| 16-03-02 | 03 | 2 | RULE-04, RULE-05 | T-16-03 / T-16-04 | Governance actions deep-link back to the indoor page and workbench summaries stay aligned with saved behavior data | smoke | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-16-indoor-rule-governance.ps1` | add end-to-end smoke flow | pending |
| 16-04-01 | 04 | 3 | RULE-04, RULE-05 | T-16-04 / T-16-05 | Manual UAT covers apply/cancel, naming, ordering, minimap tools, and conflict triage behavior | manual | `.planning/phases/16-indoor-rule-workbench-and-governance/16-UAT.md` updated during execution | create UAT scaffold | pending |

*Status: pending / green / red / flaky*

---

## Wave 0 Requirements

- [ ] `packages/admin/aoxiaoyou-admin-backend/src/test/java/.../IndoorRuleGovernance*` - conflict classification and overview projection tests
- [ ] `packages/admin/aoxiaoyou-admin-backend/src/test/java/.../AdminIndoorController*` - behavior status toggle and governance endpoint tests
- [ ] `scripts/local/smoke-phase-16-indoor-rule-governance.ps1` - live admin smoke for workbench/governance round-trip

---

## Planned Execution Results

- `mvn test -q` in `packages/admin/aoxiaoyou-admin-backend`: pending
- `npm run build` in `packages/admin/aoxiaoyou-admin-ui`: pending
- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-16-indoor-rule-governance.ps1`: pending

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Workbench cancel/apply clarity | RULE-04 | Build success cannot prove operators understand staged editing vs parent-form persistence | Open the workbench, change at least one behavior name and one path field, cancel once, reopen, then apply successfully and confirm only the applied version lands in the parent form |
| Behavior naming and ordering | RULE-04 | Automated tests cannot prove behavior identity is readable at scale | Create at least three behaviors, rename all three, reorder them, and confirm the rail remains readable without relying on generated numeric labels |
| Governance conflict triage | RULE-05 | Conflict meaning and operator actionability require human judgment | Open the governance center, filter to one Lisboa floor, inspect at least one conflict row, disable one conflicting behavior, and confirm the detail panel explains what changed |

---

## Validation Sign-Off

- [x] All tasks have automated verification expectations or explicit Wave 0 dependencies
- [x] Sampling continuity is preserved across admin backend, admin UI, and live admin smoke verification
- [x] Wave 0 captures the missing workbench/governance validation and smoke coverage required by Phase 16
- [x] No watch-mode flags
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** planned 2026-04-16
