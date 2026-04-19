---
phase: 22
slug: ai-platform-verification-and-provider-default-closure
status: complete
nyquist_compliant: true
wave_0_complete: true
created: 2026-04-19
---

# Phase 22 - Validation Strategy

> Per-phase validation contract for AI platform verification and provider default closure.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 plus PowerShell smoke |
| **Config file** | `packages/admin/aoxiaoyou-admin-backend/pom.xml` and `scripts/local/*.ps1` |
| **Quick run command** | `mvn -Dtest=AdminAiServiceImplTest test` |
| **Full suite command** | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-22-ai-platform-verification.ps1` |
| **Estimated runtime** | ~15 seconds |

---

## Sampling Rate

- **After every task commit:** Run `mvn -Dtest=AdminAiServiceImplTest test`
- **After every plan wave:** Run `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-22-ai-platform-verification.ps1`
- **Before `/gsd-verify-work`:** Full suite must be green and the manual AI workspace UAT must be current
- **Max feedback latency:** 30 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 22-01-01 | 01 | 1 | AI-05 / AI-06 | T-22-01 / T-22-02 | Default text, image, and TTS routes resolve only to real witness inventory and fail honestly when missing | integration | `mvn -Dtest=AdminAiServiceImplTest test` | yes | green |
| 22-01-02 | 01 | 1 | AI-04 / AI-05 | T-22-03 / T-22-04 | Route map and provider semantics remain truthful, with no ghost `traveler-services` entry and no overstated live-provider state | smoke | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-22-ai-platform-verification.ps1 -Scope workspace` | yes | green |
| 22-02-01 | 02 | 1 | AI-08 | T-22-05 / T-22-06 | POI visual and audio authoring slots can launch the shared workbench and receive finalized asset IDs back into the form field | smoke | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-22-ai-platform-verification.ps1 -Scope creative` | yes | green |
| 22-02-02 | 02 | 1 | AI-08 | T-22-07 | Candidate restore and finalize remain owner-scoped and preserve canonical `content_assets` linkage | integration | `mvn -Dtest=AdminAiServiceImplTest test` | yes | green |
| 22-03-01 | 03 | 2 | AI-04 / AI-05 / AI-06 / AI-07 / AI-08 | T-22-08 / T-22-09 | Full AI workspace closure evidence exists for routes, providers, defaults, observability, and authoring-surface finalize | smoke | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-22-ai-platform-verification.ps1` | yes | green |

*Status: pending / green / red / flaky*

---

## Wave 0 Requirements

- [x] `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminAiServiceImplTest.java` - proves witness provider resolution, finalize, restore, and owner scope behavior
- [x] `scripts/local/smoke-phase-22-ai-platform-verification.ps1` - deterministic live smoke for workspace, providers, and creative finalize

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| AI workspace route truthfulness | AI-04 | Requires browser inspection of visible navigation, page copy, and redirect behavior | Open `/ai`, `/ai/providers`, `/ai/models`, `/ai/capabilities`, one capability detail page, `/ai/creative-studio`, `/ai/observability`, and `/ai/settings`. Confirm every visible entry lands on a truthful dedicated page and that no operator-facing `traveler-services` ghost route remains. |
| Provider status honesty | AI-05 | Needs page-level review of badges, help text, and template vs live status wording | In the providers and models pages, confirm live-verified witness providers are visually distinct from template-ready or credential-missing providers. Confirm optional providers are not reported as passed without credentials. |
| POI authoring-surface backfill | AI-08 | Requires real form launch and backfill behavior | From `POIManagement`, open a POI editor, launch the AI workbench from a visual asset slot and the `audioAssetId` slot, finalize a candidate, and confirm the asset field shows the finalized asset ID and preview without leaving standalone-workbench-only state. |
| Cost wording honesty | AI-07 | Needs human judgment on whether the page overclaims billing precision | Open overview and observability pages and confirm any cost number derived from local request logs or static price rules is labeled as an estimate rather than vendor-billed truth. |

---

## Validation Sign-Off

- [x] All tasks have automated verify or explicit Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all missing verification files
- [x] No watch-mode flags
- [x] Feedback latency < 120s
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** approved with honest non-pass witness notes for Bailian image and TTS model entitlement
