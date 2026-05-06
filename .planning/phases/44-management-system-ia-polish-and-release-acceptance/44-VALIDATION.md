---
phase: 44
slug: management-system-ia-polish-and-release-acceptance
status: draft
nyquist_compliant: true
wave_0_complete: true
created: 2026-05-04
---

# Phase 44 - Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Admin UI type-check/build, Maven compile, PowerShell release smoke/report, browser/admin manual checks |
| **Config file** | `packages/admin/aoxiaoyou-admin-ui/package.json`, `packages/admin/aoxiaoyou-admin-backend/pom.xml`, `packages/server/pom.xml` |
| **Quick run command** | `cd packages/admin/aoxiaoyou-admin-ui; npm run type-check` |
| **Full suite command** | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-44-release-acceptance.ps1 -IncludeExistingSmokes` |
| **Estimated runtime** | 180-420 seconds depending on build and smoke flags |

## Sampling Rate

- **After IA/navigation task:** Run `cd packages/admin/aoxiaoyou-admin-ui; npm run type-check`.
- **After media/material/task-governance polish:** Run `cd packages/admin/aoxiaoyou-admin-ui; npm run type-check` and targeted `Select-String` checks.
- **After release smoke script:** Run `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-44-release-acceptance.ps1 -Quick`.
- **Before verification:** Run admin UI type-check, admin UI build, admin backend compile, public backend compile, and Phase 44 smoke script.
- **Max feedback latency:** 420 seconds.

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| T44-01-01 | 01 | 1 | ADMIN-01/ADMIN-04 | T44-01-A/B | Navigation avoids misleading completed-state claims and keeps future placeholders explicit | type/select | `cd packages/admin/aoxiaoyou-admin-ui; npm run type-check` | yes | pending |
| T44-01-02 | 01 | 1 | ADMIN-01/ADMIN-04 | T44-01-A/B | Story/content/experience pages explain mission split without raw JSON-first workflows | type/select | `cd packages/admin/aoxiaoyou-admin-ui; npm run type-check` | yes | pending |
| T44-02-01 | 02 | 2 | ADMIN-02/ADMIN-04 | T44-02-A/B | Experience and traveler operation pages use labelled filters, readable tables, and inline guidance | type/select | `cd packages/admin/aoxiaoyou-admin-ui; npm run type-check` | yes | pending |
| T44-02-02 | 02 | 2 | ADMIN-03 | T44-02-C/D | Media/material drawers expose valid previews, unavailable states, and truncated URLs without dead version links | type/select | `cd packages/admin/aoxiaoyou-admin-ui; npm run type-check` | yes | pending |
| T44-03-01 | 03 | 3 | UAT-02/UAT-03/ADMIN-01/ADMIN-02/ADMIN-03/ADMIN-04 | T44-03-A/B/C | Release report redacts secrets and separates automated, browser, DevTools, device, caveat, and future-scope evidence | smoke/docs | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-44-release-acceptance.ps1 -Quick` | yes | pending |
| T44-03-02 | 03 | 3 | UAT-02/UAT-03 | T44-03-A/B/C | Verification does not claim unperformed WeChat/device checks as PASS | docs/select | `Select-String` evidence checks in 44-03 plan | yes | pending |

## Wave 0 Requirements

Existing Phase 41-43 smoke scripts and verification reports exist and can be referenced.

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Browser sidebar and admin workflow feel | ADMIN-01/ADMIN-02/ADMIN-04 | Visual density, navigation clarity, and layout stability require browser inspection | Open admin UI at `/admin/`, navigate sidebar groups, and record screenshots or checklist rows for story, material, media, experience, governance, and traveler support pages. |
| WeChat DevTools flagship story smoke | UAT-02/UAT-03 | WeChat runtime/device behavior cannot be proven by admin UI build alone | Open the built mini-program in WeChat DevTools or device, run the flagship story smoke, and record exact pass/fail evidence in `44-UAT.md`. |

## Validation Sign-Off

- [x] All tasks have automated verify or manual-only rationale.
- [x] Sampling continuity: no 3 consecutive tasks without automated verify.
- [x] Wave 0 covers existing smoke and evidence dependencies.
- [x] No watch-mode flags.
- [x] Feedback latency target documented.
- [x] `nyquist_compliant: true` set in frontmatter.

**Approval:** planned 2026-05-04
