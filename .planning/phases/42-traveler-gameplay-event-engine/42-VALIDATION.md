---
phase: 42
slug: traveler-gameplay-event-engine
status: draft
nyquist_compliant: true
wave_0_complete: true
created: 2026-05-04
---

# Phase 42 — Validation Strategy

> Per-phase validation contract for traveler gameplay event-engine execution.

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Spring/Maven compile, Taro WeChat build, PowerShell smoke |
| **Config file** | `packages/server/pom.xml`, `packages/client/package.json` |
| **Quick run command** | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-42-gameplay-event-engine.ps1 -Quick` |
| **Full suite command** | `mvn -q -DskipTests compile -f packages/server/pom.xml; npm run build:weapp; npm run smoke:phase42:gameplay-event-engine` |
| **Estimated runtime** | ~180 seconds |

## Sampling Rate

- **After every task commit:** Run the task-specific `<verify>` command in the relevant plan.
- **After every plan wave:** Run `mvn -q -DskipTests compile -f packages/server/pom.xml` for backend waves and `npm run build:weapp` for mini-program waves.
- **Before `/gsd-verify-work`:** Run `npm run smoke:phase42:gameplay-event-engine` from `packages/client`.
- **Max feedback latency:** 240 seconds.

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| T42-01-01 | 01 | 1 | PLAY-02, PLAY-03 | T42-01-A / T42-01-B | Backend accepts only known events and returns truthful feedback | grep + compile | `mvn -q -DskipTests compile -f packages/server/pom.xml` | ✅ | ⬜ pending |
| T42-01-02 | 01 | 1 | PLAY-02, PLAY-04 | T42-01-B / T42-01-C | Duplicate client event ids become already-synced responses | grep + compile | `mvn -q -DskipTests compile -f packages/server/pom.xml` | ✅ | ⬜ pending |
| T42-01-03 | 01 | 1 | PLAY-03, PLAY-04 | T42-01-A / T42-01-C | Exploration summary is backend-derived and optional | grep + compile | `mvn -q -DskipTests compile -f packages/server/pom.xml` | ✅ | ⬜ pending |
| T42-02-01 | 02 | 2 | PLAY-02, PLAY-03 | T42-02-A / T42-02-B | Stateful actions are gated and duplicates are success states | client build | `npm run build:weapp` | ✅ | ⬜ pending |
| T42-02-02 | 02 | 2 | PLAY-02, PLAY-03 | T42-02-B / T42-02-C | Runtime cards show Traditional Chinese state feedback | client build | `npm run build:weapp` | ✅ | ⬜ pending |
| T42-02-03 | 02 | 2 | PLAY-04 | T42-02-A / T42-02-D | Session exit/re-entry follows backend state | client build | `npm run build:weapp` | ✅ | ⬜ pending |
| T42-03-01 | 03 | 3 | PLAY-02, PLAY-04 | T42-03-A / T42-03-B | Smoke validates idempotency without leaking secrets | smoke | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-42-gameplay-event-engine.ps1 -Quick` | ❌ W3 | ⬜ pending |
| T42-03-02 | 03 | 3 | PLAY-02, PLAY-03, PLAY-04 | T42-03-B | UAT docs avoid false real-device pass | docs grep | `Select-String` checks in plan | ✅ | ⬜ pending |

## Wave 0 Requirements

Existing infrastructure covers all phase requirements:

- `packages/server/pom.xml` for backend compile.
- `packages/client/package.json` for `build:weapp`.
- Phase 41 smoke harness as a reference for safe report writing.

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| WeChat DevTools visual confirmation of action cards and feedback copy | PLAY-02, PLAY-03 | Current repo has no automated mini-program UI runner | Build with `npm run build:weapp`, open `packages/client/dist` in WeChat DevTools, enter the flagship story, start story mode, tap one pickup/task/reward/unsupported action, and record pass/fail. |
| Physical-device GPS/proximity realism | PLAY-02 | Device location permissions and real movement cannot be proven by local smoke | Do not mark PASS unless tested on a real device or simulator with documented location input. |

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies.
- [x] Sampling continuity: no 3 consecutive tasks without automated verify.
- [x] Wave 0 covers all MISSING references.
- [x] No watch-mode flags.
- [x] Feedback latency < 240s.
- [x] `nyquist_compliant: true` set in frontmatter.

**Approval:** pending execution
