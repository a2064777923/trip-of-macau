---
phase: 18
slug: ai-capability-center
status: complete
nyquist_compliant: true
wave_0_complete: true
created: 2026-04-17
---

# Phase 18 - Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Spring Boot Test + admin UI production build + PowerShell smoke |
| **Config file** | `packages/admin/aoxiaoyou-admin-backend/pom.xml` and `packages/admin/aoxiaoyou-admin-ui/package.json` |
| **Quick run command** | `mvn -q "-Dtest=AdminAi*Test" test` |
| **Full suite command** | `mvn -q test` in `packages/admin/aoxiaoyou-admin-backend`, `npm run build` in `packages/admin/aoxiaoyou-admin-ui`, and `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-18-ai-capability-center.ps1` |
| **Estimated runtime** | ~180 seconds |

---

## Sampling Rate

- **After every task commit:** Run `mvn -q "-Dtest=AdminAi*Test" test`
- **After every plan wave:** Run `mvn -q test`, `npm run build`, and the Phase 18 smoke harness
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 180 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 18-01-01 | 01 | 1 | AI-01 | T-18-01 | Provider profiles and capability policies can be created and updated without secret leakage in read DTOs | unit | `mvn -q test` | yes | green |
| 18-01-02 | 01 | 1 | AI-02 | T-18-01 / T-18-02 | Secret writes support create, masked read, and replace-without-echo semantics | unit | `mvn -q test` | yes | green |
| 18-02-01 | 02 | 1 | AI-02 | T-18-02 / T-18-03 | Routing, fallback, quota, and suspicious-concurrency controls resolve deterministically | unit | `mvn -q test` | yes | green |
| 18-02-02 | 02 | 1 | AI-03 | T-18-04 | Overview and audit rollups reflect provider health, fallback activity, and usage accurately | unit | `mvn -q test` | yes | green |
| 18-03-01 | 03 | 2 | AI-01 | T-18-01 | Admin UI masked-secret forms, provider CRUD, and policy screens compile and bind to the real admin API | build | `npm run build` | yes | green |
| 18-03-02 | 03 | 2 | AI-03 | T-18-04 | Admin overview renders capability status, provider health, and recent usage without placeholder data assumptions | smoke | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-18-ai-capability-center.ps1` | yes | green |
| 18-04-01 | 04 | 2 | AI-01 / AI-02 | T-18-02 / T-18-05 | Creative-foundation generation history and candidate-finalization flows persist safely through COS and asset binding | smoke | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-18-ai-capability-center.ps1` | yes | green |

*Status: pending or green or red or flaky*

---

## Wave 0 Requirements

- [x] `packages/admin/aoxiaoyou-admin-backend/src/test/java/**/AiSecretCryptoServiceTest.java` - secret crypto and masked-read coverage
- [x] `packages/admin/aoxiaoyou-admin-backend/src/test/java/**/AiGovernanceServiceTest.java` - routing / quota / suspicious-concurrency governance coverage
- [x] `scripts/local/smoke-phase-18-ai-capability-center.ps1` - local end-to-end admin capability-center smoke harness
- [x] `npm run build` in `packages/admin/aoxiaoyou-admin-ui` - admin UI contract/build verification

*If none: "Existing infrastructure covers all phase requirements."*

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Masked secret edit flow is clear to operators | AI-01 / AI-02 | UX clarity of create-versus-replace secret flows is not a unit-test concern | Open the provider management screen, save a provider with secrets, reopen it, confirm only masked values are shown, then replace the secret and confirm plaintext is never echoed |
| Capability overview is coherent for operators | AI-03 | Human judgment is required to verify dashboard scanability and operational usefulness | Open the AI capability center overview and confirm an operator can understand provider health, usage, fallback state, and enabled capabilities without drilling into raw logs |

---

## Validation Sign-Off

- [x] All tasks have automated verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all missing references
- [x] No watch-mode flags
- [x] Feedback latency < 180s
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** approved
