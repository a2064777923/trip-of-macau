---
phase: 1
slug: canonical-backend-foundation
status: approved
nyquist_compliant: true
wave_0_complete: false
created: 2026-04-12
---

# Phase 1 - Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 + Spring Boot Test |
| **Config file** | `packages/server/pom.xml` |
| **Quick run command** | `mvn -q -f packages/server/pom.xml test -Dtest=TripOfMacauServerApplicationTests,PublicFoundationContextTest` |
| **Full suite command** | `mvn -q -f packages/server/pom.xml test` |
| **Estimated runtime** | ~90 seconds |

---

## Sampling Rate

- **After every task commit:** Run `mvn -q -f packages/server/pom.xml test -Dtest=TripOfMacauServerApplicationTests,PublicFoundationContextTest`
- **After every plan wave:** Run `mvn -q -f packages/server/pom.xml test`
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 120 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 1-01-01 | 01 | 1 | DATA-03 | T-01-01 | Shared contract values stay explicit and reviewable | grep/doc | `Select-String -Path docs/integration/miniapp-admin-public-contract.md -Pattern 'Page-to-API Matrix|Canonical Entity Matrix|Publish Rules|Locale Rules|Known Gaps'` | pending | pending |
| 1-02-01 | 02 | 2 | DATA-01 | T-01-04 | Schema bootstrap is deterministic and table coverage is visible | sql/grep | `Select-String -Path scripts/local/mysql/init/02-live-backend-foundation.sql -Pattern 'CREATE TABLE IF NOT EXISTS app_runtime_settings|CREATE TABLE IF NOT EXISTS content_assets|CREATE TABLE IF NOT EXISTS cities|CREATE TABLE IF NOT EXISTS user_progress'` | pending | pending |
| 1-03-01 | 03 | 2 | DATA-01 | T-01-07 | Public backend boots without missing admin-only interceptor dependencies | unit | `mvn -q -f packages/server/pom.xml test -Dtest=TripOfMacauServerApplicationTests,PublicFoundationContextTest` | pending | pending |
| 1-04-01 | 04 | 3 | OPS-01 | T-01-10 | Local smoke flow checks real public/admin health endpoints | smoke | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-01-foundation.ps1` | pending | pending |

*Status: pending / green / red / flaky*

---

## Wave 0 Requirements

- [ ] `packages/server/src/test/java/com/aoxiaoyou/tripofmacau/PublicFoundationContextTest.java` - targeted Spring-context smoke coverage for Phase 1 foundation work
- [ ] `scripts/local/smoke-phase-01-foundation.ps1` - local public/admin/MySQL smoke flow
- [ ] `docs/integration/miniapp-admin-public-contract.md` - canonical contract matrix used as a verification anchor

---

## Manual-Only Verifications

All Phase 1 behaviors should have automated or scriptable verification.

---

## Validation Sign-Off

- [ ] All tasks have automated verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 120s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** approved 2026-04-12
