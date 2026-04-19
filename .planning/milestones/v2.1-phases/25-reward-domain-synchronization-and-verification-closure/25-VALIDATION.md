---
phase: 25
slug: reward-domain-synchronization-and-verification-closure
status: draft
nyquist_compliant: true
wave_0_complete: true
created: 2026-04-19
---

# Phase 25 - Validation Strategy

> Per-phase validation contract for reward-domain synchronization closure and formal verification backfill.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 plus PowerShell smoke |
| **Config file** | `packages/admin/aoxiaoyou-admin-backend/pom.xml`, `packages/server/pom.xml`, and `scripts/local/*.ps1` |
| **Quick run command** | `mvn -q "-Dtest=AdminRewardDomainServiceImplTest,IndoorRuleGovernanceServiceTest,IndoorRuleAuthoringServiceTest" test` |
| **Full suite command** | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-23-reward-domain.ps1` |
| **Estimated runtime** | ~30 seconds |

---

## Sampling Rate

- **After every task commit:** Run `mvn -q "-Dtest=AdminRewardDomainServiceImplTest,IndoorRuleGovernanceServiceTest,IndoorRuleAuthoringServiceTest" test`
- **After every plan wave:** Run `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-23-reward-domain.ps1`
- **Before `/gsd-verify-work`:** Full suite must be green and the formal `23-VERIFICATION.md` must be current
- **Max feedback latency:** 60 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 25-01-01 | 01 | 1 | REWARD-04 | T-25-01 | Indoor behavior authoring writes shared reward-rule bindings through the canonical binding table, without duplicating JSON rule state | integration | `mvn -q "-Dtest=AdminRewardDomainServiceImplTest,IndoorRuleGovernanceServiceTest,IndoorRuleAuthoringServiceTest" test` | yes | pending |
| 25-01-02 | 01 | 1 | REWARD-04 | T-25-02 | Deleting or unbinding shared reward rules preserves deterministic owner visibility and guard rails across reward and indoor domains | integration | `mvn -q "-Dtest=AdminRewardDomainServiceImplTest,IndoorRuleGovernanceServiceTest,IndoorRuleAuthoringServiceTest" test` | yes | pending |
| 25-02-01 | 02 | 1 | REWARD-01 / REWARD-02 / REWARD-03 / REWARD-05 | T-25-03 | Reward-domain operator surfaces remain readable enough for honest verification and still show split-domain state, linked rules, and presentation ownership correctly | smoke | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-23-reward-domain.ps1` | yes | pending |
| 25-03-01 | 03 | 2 | REWARD-01 / REWARD-02 / REWARD-03 / REWARD-04 / REWARD-05 | T-25-04 | Live admin/public verification evidence exists and supersedes the old diagnosed reward-domain UAT trail | smoke | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-23-reward-domain.ps1` | yes | pending |
| 25-03-02 | 03 | 2 | REWARD-01 / REWARD-02 / REWARD-03 / REWARD-04 / REWARD-05 | T-25-05 | Public reward payloads still return split-domain prizes, rewards, presentations, and rule summaries without drifting back to legacy-only data | integration | `mvn -q "-Dtest=CatalogFoundationServiceImplTest,PublicRewardDomainServiceTest,PublicCatalogServiceImplCarryoverTest" test` | yes | pending |

*Status: pending / green / red / flaky*

---

## Wave 0 Requirements

- [x] `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminRewardDomainServiceImplTest.java` - canonical reward-domain binding and deletion guard coverage
- [x] `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/IndoorRuleGovernanceServiceTest.java` - indoor governance reward-link projection coverage
- [x] `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/IndoorRuleAuthoringServiceTest.java` - indoor behavior authoring regression anchor
- [x] `packages/server/src/test/java/com/aoxiaoyou/tripofmacau/PublicRewardDomainServiceTest.java` - split reward-domain public response coverage
- [x] `scripts/local/smoke-phase-23-reward-domain.ps1` - live admin/public reward smoke baseline

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Reward-side shared rule selection and readback | REWARD-02 / REWARD-04 | Requires real operator interaction through admin forms | Open redeemable-prize and game-reward authoring, attach one or more shared reward rules, save, reopen, and confirm linked rules and linked-owner counts remain correct. |
| Indoor-side synchronization witness | REWARD-04 | Requires the interactive indoor authoring surface, not only service tests | Open the indoor rule authoring surface or workbench, bind or unbind a shared reward rule from a behavior, then confirm governance detail and reward-rule owner lists update accordingly. |
| Reward-domain public closure | REWARD-01 / REWARD-03 / REWARD-05 | Requires live stack proof on the local runtime | Query admin `8081` and public `8080` reward endpoints after the synchronization repair and confirm split reward-domain data, presentations, and rule summaries remain current. |
| Formal artifact supersession | REWARD-01 / REWARD-02 / REWARD-03 / REWARD-04 / REWARD-05 | Artifact state cannot be proven by unit tests alone | Create `23-VERIFICATION.md`, reference current live evidence, and mark the old `23-UAT.md` diagnosed trail as superseded by formal verification. |

---

## Validation Sign-Off

- [x] All tasks have automated verify or explicit Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers the core reward-domain and indoor-governance regression anchors
- [x] No watch-mode flags
- [x] Feedback latency < 120s
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** pending execution
