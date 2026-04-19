---
phase: 10
slug: media-asset-pipeline-and-library
status: planned
nyquist_compliant: true
wave_0_complete: false
created: 2026-04-14
updated: 2026-04-14
---

# Phase 10 - Validation Strategy

> Per-phase validation contract for the richer COS-backed media pipeline, upload-policy enforcement, admin media library, and shared asset reuse.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 + Spring Boot Test + admin UI build verification + PowerShell media smoke |
| **Config file** | `packages/admin/aoxiaoyou-admin-backend/pom.xml`, `packages/admin/aoxiaoyou-admin-ui/package.json` |
| **Quick run command** | `mvn test` in `packages/admin/aoxiaoyou-admin-backend` plus `npm run build` in `packages/admin/aoxiaoyou-admin-ui` |
| **Full suite command** | `mvn test` in `packages/admin/aoxiaoyou-admin-backend`, `npm run build` in `packages/admin/aoxiaoyou-admin-ui`, and `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-10-media.ps1` |
| **Estimated runtime** | ~240 seconds |

---

## Sampling Rate

- **After every task commit:** run the touched package quick command (`mvn test` or `npm run build`)
- **After every plan wave:** run the full Phase 10 command set
- **Before `/gsd-verify-work`:** full suite must be green and the media smoke script must pass against real COS
- **Max feedback latency:** 240 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 10-01-01 | 01 | 1 | MEDIA-01, MEDIA-02 | T-10-01 / T-10-02 | Asset rows record policy/audit metadata and server-side lossless gating cannot be bypassed | unit/migration | `mvn test` in `packages/admin/aoxiaoyou-admin-backend` | add schema/service tests | pending |
| 10-01-02 | 01 | 1 | MEDIA-01, MEDIA-02 | T-10-02 / T-10-04 | Batch/folder intake validates upload source and relative-path metadata without trusting object keys from clients | integration | `mvn test` in `packages/admin/aoxiaoyou-admin-backend` | add upload API tests | pending |
| 10-02-01 | 02 | 2 | MEDIA-01, MEDIA-03 | T-10-05 | Media library route renders real upload/search/preview workflow in the admin shell | build/component | `npm run build` in `packages/admin/aoxiaoyou-admin-ui` | add media page/components | pending |
| 10-02-02 | 02 | 2 | MEDIA-02 | T-10-02 | Admin account and system settings surfaces expose lossless-upload and media-policy controls clearly | build/component | `npm run build` in `packages/admin/aoxiaoyou-admin-ui` | add admin/settings UI coverage | pending |
| 10-03-01 | 03 | 3 | MEDIA-03, MEDIA-04 | T-10-05 / T-10-06 | Shared asset usage tracing and delete protection cover both direct asset columns and link-table relations | unit/integration | `mvn test` in `packages/admin/aoxiaoyou-admin-backend` and `npm run build` in `packages/admin/aoxiaoyou-admin-ui` | add usage/delete tests and picker integration | pending |
| 10-03-02 | 03 | 3 | MEDIA-01, MEDIA-02, MEDIA-03, MEDIA-04 | T-10-01 through T-10-06 | Real COS uploads, policy variance, library filtering, usage tracing, and safe delete all work end-to-end | smoke | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-10-media.ps1` | add smoke script | pending |

*Status: pending / green / red / flaky*

---

## Wave 0 Requirements

- [ ] `packages/admin/aoxiaoyou-admin-backend/src/test/java/.../MediaUploadPolicy*` - permission + policy resolution tests
- [ ] `packages/admin/aoxiaoyou-admin-backend/src/test/java/.../AdminContentAssetUpload*` - single and batch upload contract tests
- [ ] `packages/admin/aoxiaoyou-admin-backend/src/test/java/.../AssetUsageTrace*` - usage aggregation and delete guard tests
- [ ] `scripts/local/smoke-phase-10-media.ps1` - end-to-end proof for upload policy, COS publication, library search, and usage tracing

---

## Planned Execution Results

- `mvn test` in `packages/admin/aoxiaoyou-admin-backend`: pending
- `npm run build` in `packages/admin/aoxiaoyou-admin-ui`: pending
- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-10-media.ps1`: pending

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Folder import UX in admin | MEDIA-01 | Browser folder selection and batch-result clarity need real UI confirmation | Open the media library, import a small folder, confirm the UI preserves client relative paths in the batch result and auto-refreshes the asset list |
| Clipboard and drag/drop upload clarity | MEDIA-01 | Build success cannot prove operator discoverability or failure messaging | Upload one asset via drag/drop and one via clipboard paste, then confirm success/failure states and previews are understandable |
| Lossless versus processed operator feedback | MEDIA-02 | Operators must understand why a file was compressed or kept lossless | Compare uploads from a lossless-enabled admin and a non-lossless admin, then confirm the library detail panel shows the applied policy and processing note |
| Asset usage and safe delete | MEDIA-03, MEDIA-04 | Delete warnings and usage explanations are operational UI behaviors | Open an in-use asset from the media library, inspect its usages, attempt delete, and confirm the UI blocks or escalates clearly instead of silently removing the asset |

---

## Validation Sign-Off

- [x] All tasks have an automated verification expectation or an explicit Wave 0 dependency
- [x] Sampling continuity is preserved across admin backend, admin UI, and real COS smoke verification
- [x] Wave 0 captures the missing upload-policy, usage-trace, and smoke coverage needed for Phase 10
- [x] No watch-mode flags
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** planned 2026-04-14
