---
phase: 34
slug: public-runtime-and-mini-program-consumption-baseline
status: draft
nyquist_compliant: true
wave_0_complete: false
created: 2026-04-30
---

# Phase 34 - Validation Strategy

> Per-phase validation contract for public runtime and mini-program consumption baseline.

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Maven compile, Taro WeApp build, PowerShell smoke, MySQL import |
| **Config file** | `packages/server/pom.xml`, `packages/client/package.json`, `scripts/local/mysql/init/*.sql` |
| **Quick run command** | `mvn -q -DskipTests compile -f packages/server/pom.xml` |
| **Full suite command** | `npm run build:weapp --prefix packages/client` + `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-34-public-runtime.ps1` |
| **Estimated runtime** | 2-5 minutes after services are running |

## Sampling Rate

- **After backend DTO/service/controller changes:** Run public backend compile.
- **After client API/type/story-page changes:** Run mini-program build.
- **After smoke script changes:** Run script against live local public backend `8080` after importing Phase 28-33 seeds.
- **Before `/gsd-verify-work`:** Run all three full-suite commands.
- **Max feedback latency:** 300 seconds.

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 34-01-01 | 01 | 1 | LINK-02 | T34-01 | Public runtime exposes traveler-safe compiled DTOs, not admin raw payloads | compile | `mvn -q -DskipTests compile -f packages/server/pom.xml` | W0 | pending |
| 34-01-02 | 01 | 1 | OPS-04 | T34-02 | Public runtime filters to published/traveler-eligible lifecycle state | compile + smoke | `scripts/local/smoke-phase-34-public-runtime.ps1` | W0 | pending |
| 34-02-01 | 02 | 2 | LINK-02 | T34-03 | Mini-program API helpers centralize auth and runtime calls | build | `npm run build:weapp --prefix packages/client` | W0 | pending |
| 34-02-02 | 02 | 2 | VER-01 | T34-04 | Runtime mapping preserves fallback data and avoids mock-only truth | build | `npm run build:weapp --prefix packages/client` | W0 | pending |
| 34-03-01 | 03 | 3 | VER-01 | T34-05 | Story page degrades unsupported gameplay and failed media without blank-screening | build | `npm run build:weapp --prefix packages/client` | W0 | pending |
| 34-03-02 | 03 | 3 | LINK-02 | T34-06 | Auth-gated event reporting uses existing token/guard behavior | build + smoke | `scripts/local/smoke-phase-34-public-runtime.ps1` | W0 | pending |
| 34-04-01 | 04 | 4 | VER-01 | T34-07 | Smoke uses env-backed credentials and creates no tracked secrets | smoke | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-34-public-runtime.ps1` | W0 | pending |
| 34-04-02 | 04 | 4 | OPS-02 | T34-08 | Runtime lifecycle evidence documents publish-status behavior and deferred approval scope | docs | `Select-String` doc checks | W0 | pending |

## Wave 0 Requirements

- Local MySQL database `aoxiaoyou` exists and can import Phase 28-33 seed scripts with `--default-character-set=utf8mb4`.
- Public backend runs at `http://127.0.0.1:8080`.
- Traveler auth for stateful smoke is provided by one of:
  - `PHASE34_TRAVELER_BEARER_TOKEN`
  - dev/local-only `PHASE34_TRAVELER_DEV_IDENTITY` through `/api/v1/user/login/dev-bypass`
- No bearer token, password, WeChat secret, COS secret, or provider key is tracked.

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Story page traveler comprehension | VER-01 | Requires judging copy and flow readability | Build/open mini-program story page and confirm `故事互動流程`, `稍後開放`, runtime status, and fallback states are clear. |
| Full WeChat journey | Future | Explicitly deferred beyond v3.0 | Do not block Phase 34 on full DevTools route/map gameplay UAT. |

## Validation Sign-Off

- [ ] All tasks have automated verification or explicit smoke/manual coverage.
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify.
- [ ] Wave 0 covers credentials, SQL import, and service prerequisites.
- [ ] No watch-mode flags.
- [ ] Feedback latency target < 300s after services are running.
- [x] `nyquist_compliant: true` set in frontmatter.

**Approval:** pending execution.
