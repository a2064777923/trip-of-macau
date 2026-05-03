---
phase: 38
slug: public-runtime-asset-consumption
status: planned
nyquist_compliant: true
wave_0_complete: true
created: 2026-05-03
---

# Phase 38 - Validation Strategy

> Per-phase validation contract for public runtime asset consumption, fallback filtering, event ingestion, and smoke evidence.

## Test Infrastructure

| Property | Value |
| --- | --- |
| **Framework** | JUnit/Spring Boot, PowerShell smoke, MySQL-backed local runtime |
| **Backend quick command** | `mvn -q -DskipTests compile -f packages/server/pom.xml` |
| **Asset-focused test** | `mvn -q -Dtest=PublicRuntimeAssetServiceTest test -f packages/server/pom.xml` |
| **Event-focused test** | `mvn -q -Dtest=PublicExperienceEventServiceTest test -f packages/server/pom.xml` |
| **Smoke command** | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-38-public-runtime-assets.ps1` |
| **Estimated runtime** | 120-360 seconds depending on local MySQL/server availability |

## Sampling Rate

- After runtime DTO and asset sanitation changes: run backend compile and `PublicRuntimeAssetServiceTest`.
- After event/session changes: run backend compile and `PublicExperienceEventServiceTest`.
- After smoke changes: run the full Phase 38 smoke against local public backend.
- Before `/gsd-verify-work`: run backend compile and Phase 38 smoke.

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Secure Behavior | Test Type | Automated Command | File Exists | Status |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 38-01-01 | 01 | 1 | RUN-01/RUN-02 | Public asset DTO excludes prompts, scripts, local paths, provider secrets, costs, and QA notes | compile/unit | `mvn -q -Dtest=PublicRuntimeAssetServiceTest test -f packages/server/pom.xml` | planned | pending |
| 38-01-02 | 01 | 1 | RUN-01/RUN-02 | Runtime fallback decisions happen server-side before client rendering | compile/unit | `mvn -q -DskipTests compile -f packages/server/pom.xml` | planned | pending |
| 38-02-01 | 02 | 2 | RUN-03 | Event writes are authenticated, allowlisted, JSON-bounded, and idempotent | unit | `mvn -q -Dtest=PublicExperienceEventServiceTest test -f packages/server/pom.xml` | planned | pending |
| 38-03-01 | 03 | 3 | RUN-04 | Smoke proves flagship asset chain, lifecycle filtering, banned-field absence, and event idempotency | smoke | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-38-public-runtime-assets.ps1` | planned | pending |

## Wave 0 Requirements

- [x] Phase 34 public runtime endpoint family exists.
- [x] Phase 36 material assets were generated/uploaded/promoted and have production evidence.
- [x] Phase 37 material QA/reuse controls exist and publish/reject semantics are documented.
- [x] Phase 38 context is written and locks traveler-safe public DTO boundaries.
- [x] Research identifies public-only material read model, event allowlist, and smoke commands.

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
| --- | --- | --- | --- |
| Runtime JSON privacy | RUN-01 | Banned-field absence should be visually inspected in addition to smoke grep | Fetch `/api/v1/storylines/{id}/runtime?locale=zh-Hant`, search for `promptText`, `scriptText`, `localPath`, `providerApiKey`, `secret`, `cost`, `qaNote`, and confirm none appear. |
| Media URL playback | RUN-01/RUN-02 | Local COS/CDN reachability can depend on runtime credentials/network | Open one image/audio/video URL from the runtime response if COS URLs are available. |
| Anonymous/auth boundary | RUN-03 | Requires local auth/runtime configuration | Confirm GET runtime works without token and POST event returns `4010` without token. |

## Validation Sign-Off

- [ ] Public backend compile passes.
- [ ] Asset sanitation tests cover available, fallback, unsupported, and banned-field DTO behavior.
- [ ] Event tests cover allowed event types, rejected invalid JSON, duplicate `clientEventId`, and session exit behavior.
- [ ] Smoke verifies flagship generated asset chain and lifecycle filtering.
- [ ] `RUN-01` through `RUN-04` are marked complete only after smoke passes.
- [ ] No secrets, provider keys, bearer tokens, or COS credentials are committed.

**Approval:** pending Phase 38 execution.
