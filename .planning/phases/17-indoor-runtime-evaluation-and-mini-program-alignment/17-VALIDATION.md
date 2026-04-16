---
phase: 17
slug: indoor-runtime-evaluation-and-mini-program-alignment
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-04-17
---

# Phase 17 - Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Spring Boot Test + PowerShell smoke + Taro production build |
| **Config file** | `packages/server/pom.xml` and `packages/client/package.json` |
| **Quick run command** | `mvn -q "-Dtest=PublicIndoorRuntimeServiceTest,PublicIndoorRuntimeInteractionServiceTest" test` |
| **Full suite command** | `mvn -q test` in `packages/server`, `npm run build:weapp` in `packages/client`, and `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-17-indoor-runtime.ps1` |
| **Estimated runtime** | ~150 seconds |

---

## Sampling Rate

- **After every task commit:** Run `mvn -q "-Dtest=PublicIndoorRuntimeServiceTest,PublicIndoorRuntimeInteractionServiceTest" test`
- **After every plan wave:** Run `mvn -q test`, `npm run build:weapp`, and the Phase 17 smoke harness
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 150 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 17-01-01 | 01 | 1 | RULE-03 | T-17-01 | Public runtime payloads expose only published, runtime-supported indoor behaviors and strip admin-only state | unit | `mvn -q "-Dtest=PublicIndoorRuntimeServiceTest" test` | ❌ W0 | pending |
| 17-01-02 | 01 | 1 | RULE-03 | T-17-02 / T-17-03 | Public interaction evaluation returns deterministic `blockedReason`, `requiresAuth`, and effect results for indoor trigger submissions | unit | `mvn -q "-Dtest=PublicIndoorRuntimeInteractionServiceTest" test` | ❌ W0 | pending |
| 17-01-03 | 01 | 1 | RULE-03 | T-17-01 / T-17-03 | Lisboa runtime fixtures are promoted to `phase17_supported` so smoke reads the intended showcase behaviors | smoke | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-17-indoor-runtime.ps1 -SkipBuild` | ❌ W0 | pending |
| 17-02-01 | 02 | 2 | RULE-03 | T-17-02 | The mini-program client can fetch the new runtime snapshot and interaction contracts without type drift | build | `npm run build:weapp` | ❌ W0 | pending |
| 17-02-02 | 02 | 2 | RULE-03 | T-17-04 | The indoor runtime engine evaluates supported appearance and trigger subsets without crashing on unsupported categories | smoke | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-17-indoor-runtime.ps1 -SkipServerTests` | ❌ W0 | pending |
| 17-02-03 | 02 | 2 | RULE-03 | T-17-03 / T-17-04 | Anonymous users are routed to the auth wall for guarded indoor effects instead of mutating state silently | smoke | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-17-indoor-runtime.ps1` | ❌ W0 | pending |
| 17-03-01 | 03 | 3 | RULE-03 | T-17-05 / T-17-06 | Live local smoke exercises both `8080` public runtime flows and the deterministic Lisboa fixture | smoke | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-17-indoor-runtime.ps1` | ❌ W0 | pending |
| 17-03-02 | 03 | 3 | RULE-03 | T-17-07 | Manual mini-program UAT proves tap, dwell, path-motion, popup, and auth-gated behaviors are understandable in DevTools | manual | `See 17-UAT.md` | ❌ W0 | pending |

*Status: pending or green or red or flaky*

---

## Wave 0 Requirements

- [ ] `packages/server/src/test/java/com/aoxiaoyou/tripofmacau/PublicIndoorRuntimeServiceTest.java` - runtime snapshot projection coverage
- [ ] `packages/server/src/test/java/com/aoxiaoyou/tripofmacau/PublicIndoorRuntimeInteractionServiceTest.java` - interaction gating and blocked-reason coverage
- [ ] `scripts/local/smoke-phase-17-indoor-runtime.ps1` - full-stack runtime smoke harness

*If none: "Existing infrastructure covers all phase requirements."*

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Tap-triggered path guidance feels understandable in the indoor page | RULE-03 | Path-motion timing and cue clarity are UI/UX behaviors that a server test cannot judge | Open the Lisboa indoor page in WeChat DevTools, tap the Zipcity runtime marker, confirm the guide effect starts and the follow-up cue remains understandable |
| Dwell-trigger timing is understandable and not overly sensitive | RULE-03 | Dwell cadence depends on real page timing and operator perception | Stand on the Royal Palace runtime marker in DevTools or a controlled runtime session, wait through the configured dwell period, and confirm the expected popup/effect timing |
| Auth-wall copy and routing for guarded indoor effects is understandable | RULE-03 | Login prompts and routing need human UX validation | Trigger a guarded indoor effect while anonymous, confirm the modal copy is clear, and confirm it routes to the profile auth wall without freezing the indoor page |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 150s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
