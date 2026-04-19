---
phase: 08
slug: multilingual-authoring-foundation
status: verified
nyquist_compliant: true
wave_0_complete: true
created: 2026-04-13
updated: 2026-04-13
---

# Phase 8 - Validation Strategy

> Per-phase validation contract for multilingual authoring, translation fallback, and locale-aware read-path alignment.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 + Spring Boot Test + TypeScript build verification |
| **Config file** | `packages/server/pom.xml`, `packages/admin/aoxiaoyou-admin-backend/pom.xml`, `packages/admin/aoxiaoyou-admin-ui/package.json`, `packages/client/package.json` |
| **Quick run command** | `mvn test` in both Java backends plus `npm run build` in the admin UI |
| **Full suite command** | `mvn test` in both Java backends, `npm run build` in `packages/admin/aoxiaoyou-admin-ui`, `npm run build:weapp` in `packages/client`, and a Phase 8 localization smoke script |
| **Estimated runtime** | ~180 seconds |

---

## Sampling Rate

- **After every task commit:** Run the affected package quick command for the modified surface.
- **After every plan wave:** Run the full Phase 8 command set including public/admin/backend plus admin UI build.
- **Before `/gsd-verify-work`:** Full suite must be green and localization smoke must pass.
- **Max feedback latency:** 180 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 08-01-01 | 01 | 1 | LOCL-01 | T-08-01 / T-08-02 | Unsupported locale codes cannot be stored or resolved silently | unit | `mvn test` in `packages/server` and `packages/admin/aoxiaoyou-admin-backend` | add new locale/helper tests | green |
| 08-01-02 | 01 | 1 | LOCL-02, LOCL-03 | T-08-03 / T-08-04 | Translation fallback is timeout-bounded and save-independent | unit/integration | `mvn test` in `packages/admin/aoxiaoyou-admin-backend` | add translation settings and adapter tests | green |
| 08-01-03 | 01 | 1 | LOCL-01, LOCL-04 | T-08-02 | Backfill/migrations preserve existing content while adding Portuguese slots | migration smoke | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-08-localization.ps1` | add smoke script | green |
| 08-02-01 | 02 | 2 | LOCL-01, LOCL-02 | T-08-05 | Admin forms show correct locale state and do not overwrite content unexpectedly | build/component | `npm run build` in `packages/admin/aoxiaoyou-admin-ui` | shared field component and page integration | green |
| 08-02-02 | 02 | 2 | LOCL-03 | T-08-03 | System settings persist translation policy without exposing infrastructure secrets | integration/build | `mvn test` in admin backend and `npm run build` in admin UI | settings API/UI updates | green |
| 08-02-03 | 02 | 2 | LOCL-04 | T-08-06 | Public/admin preview returns deterministic locale/fallback values for all four locales | integration/smoke | `mvn test` in `packages/server` and `npm run build:weapp` in `packages/client` | locale propagation and preview proof | green |

*Status: pending / green / red / flaky*

---

## Wave 0 Requirements

- [x] `packages/server/src/test/java/.../LocalizedContentSupport*` - locale fallback tests including `pt`
- [x] `packages/admin/aoxiaoyou-admin-backend/src/test/java/.../TranslationSettings*` - typed system-config and adapter fallback tests
- [x] `packages/admin/aoxiaoyou-admin-backend/src/test/java/.../LocaleMapping*` - request/response and entity mapping tests for Portuguese slots
- [x] `scripts/local/smoke-phase-08-localization.ps1` - end-to-end proof for admin write, preview, and public read

---

## Execution Results

- `mvn test` in `packages/server`: passed on 2026-04-13
- `mvn test` in `packages/admin/aoxiaoyou-admin-backend`: passed on 2026-04-13
- `npm run build` in `packages/admin/aoxiaoyou-admin-ui`: passed on 2026-04-13
- `npm run build:weapp` in `packages/client`: passed on 2026-04-13
- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-08-localization.ps1`: passed on 2026-04-13
  - Verified admin login, translation-settings read, city localized field update, public locale reads, and translation bridge response
  - Latest smoke marker: `phase8-1776094347917` on city `macau`

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Translate missing locales from a real admin form | LOCL-02 | UX state and overwrite behavior are hard to trust from build output alone | Open a milestone-owned form, set `zh-Hant` primary content, run `translate missing`, confirm success/failure badges per locale and confirm save remains available if one locale fails |
| Preview fallback labeling in admin | LOCL-04 | Need visual confirmation that fallback origin is clearly surfaced | Save a record with only 2 of 4 locales filled, switch preview locale, confirm displayed fallback is labeled rather than silently masquerading as filled content |
| Portuguese rendering in mini-program | LOCL-04 | Runtime proof requires actual client behavior | Set user/app locale to `pt`, load a localized public page, confirm no runtime error and expected Portuguese copy/fallback behavior |

---

## Validation Sign-Off

- [x] All tasks have an automated verification expectation or explicit Wave 0 dependency
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers the currently missing multilingual and translation tests
- [x] No watch-mode flags
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** verified 2026-04-13
