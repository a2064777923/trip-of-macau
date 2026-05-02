---
phase: 36
slug: material-production-pipeline-and-asset-promotion
status: verified
threats_open: 0
asvs_level: 1
created: 2026-05-02
updated: 2026-05-02
---

# Phase 36 - Security

Per-phase security contract for the material production pipeline, package-scoped imports, version promotion, rollback, local tooling, smoke verification, and ffmpeg-gated video generation.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| admin UI or local tooling -> admin backend | Authenticated operators and local scripts can request production actions that affect material package state. | bearer token, package id, item id, local path, object key, status transition metadata |
| production service -> COS/media/AI services | Backend-owned upload and candidate binding handles external binary metadata and generated files. | generated binaries, COS object keys, provider model/cost metadata, candidate URLs |
| local filesystem -> import endpoint | Generated files, board slices, subtitle files, and production reports are untrusted until validated. | relative paths, manifest item keys, crop metadata, subtitle metadata |
| package item pointer -> version journal | Publish and rollback mutate current pointers while preserving history. | current version id/no, immutable version rows, actor metadata |
| verification docs -> planning state | Smoke and verification output determine whether the roadmap can claim Phase 36 completion. | dependency readiness, item/version evidence, blocked requirement status |

---

## Threat Register

| Threat ID | Category | Component | Disposition | Mitigation | Status |
|-----------|----------|-----------|-------------|------------|--------|
| T36-01 | Information Disclosure | secrets exposure in provider/COS-backed production endpoints | mitigate | Production DTOs do not carry provider/COS secrets; backend storage uses runtime-configured services and version rows persist provider/model/cost metadata only. Evidence: `AdminStoryMaterialProductionRequest`, `AdminStoryMaterialProductionServiceImpl`, `AiProviderConfig` encrypted/masked fields. | closed |
| T36-02 | Tampering | path/object-key tampering for local imports and COS targets | mitigate | `MaterialProductionPathGuard` rejects traversal, absolute paths, drive escapes, unsafe URL-like strings, leading slash/tilde, duplicate object key slashes, and keys outside the package prefix before file read/upload. | closed |
| T36-03 | Elevation of Privilege | unauthorized publish/rollback routes | mitigate | `/production/*` routes remain admin-authenticated. Security pass tightened publish/rollback so callers must both have `SUPER_ADMIN`/`ROLE_SUPER_ADMIN` and send explicit confirmation; tests reject forged confirmation by a non-super-admin. | closed |
| T36-04 | Information Disclosure | untrusted provider URLs/downloads during candidate binding | mitigate | Candidate binding calls `AiOutboundUrlGuard.validatePublicSourceUrl` before accepting provider asset URLs. | closed |
| T36-05 | Repudiation | lineage loss across publish and rollback history | mitigate | `story_material_package_item_versions` stores actor/source/provider/model/cost/prompt/script/linkage/rollback metadata; rollback creates a rollback marker and flips current pointer without deleting history. | closed |
| T36-06 | Information Disclosure | secrets exposure in local production scripts | mitigate | Local tools read `OPENAI_API_KEY` and `PHASE36_ADMIN_BEARER_TOKEN` from environment only; scripts persist readiness/status, not secret values. | closed |
| T36-07 | Tampering | path/object-key tampering in local outputs and imports | mitigate | Local tools use manifest-mapped `relativeLocalPath` and `forcedCosObjectKey`, then backend `MaterialProductionPathGuard` remains authoritative before upload/import. | closed |
| T36-08 | Elevation of Privilege | unauthorized publish/rollback through batch production flow | mitigate | Live local tooling requires explicit `--confirm-production --upload --promote published`; backend still enforces admin auth and super-admin publish/rollback checks. | closed |
| T36-09 | Information Disclosure | untrusted provider URLs/downloads in narration handling | mitigate | Narration flow uses admin AI job/finalize/bind endpoints instead of accepting browser/local arbitrary download URLs. Backend candidate URL guard remains authoritative. | closed |
| T36-10 | Repudiation | lineage loss for slices, narration, and reward-cue branches | mitigate | Board slices and audio imports include parent version, crop metadata, script/provenance fields, manifest item keys, and version rows. | closed |
| T36-11 | Information Disclosure | secrets exposure through provider/COS metadata in UI | mitigate | UI renders lineage/cost/path/COS URL metadata but does not render API keys or raw provider secrets; AI provider secrets remain encrypted/masked backend fields. | closed |
| T36-12 | Tampering | path/object-key tampering from import modal inputs | mitigate | UI submits import data to backend validation and does not normalize/trust browser-side path handling. Backend rejects unsafe paths/keys. | closed |
| T36-13 | Elevation of Privilege | unauthorized publish/rollback buttons and actions | mitigate | UI actions are package-scoped, not a global production console; backend enforces the final super-admin role plus confirmation gate for publish/rollback. | closed |
| T36-14 | Information Disclosure | untrusted provider URLs/downloads surfaced through narration actions | mitigate | UI reuses existing AI helpers and Phase 36 bind endpoint only; it does not collect arbitrary provider download URLs. | closed |
| T36-15 | Repudiation | lineage loss in rollback and publish history | mitigate | Version drawer exposes current/published pointer, version numbers, provider/model/cost, parent/crop metadata, prompt/script/subtitle data, and rollback targets. | closed |
| T36-16 | Information Disclosure | secrets exposure in smoke and preflight evidence | mitigate | Smoke script reports boolean readiness for `PHASE36_ADMIN_BEARER_TOKEN`, `OPENAI_API_KEY`, `PHASE36_COS_READY`, and ffmpeg support without echoing secret values. | closed |
| T36-17 | Tampering | path/object-key tampering in smoke assertions | mitigate | Smoke asserts local path and COS object key are nonblank and contain no traversal before declaring success. | closed |
| T36-18 | Elevation of Privilege | unauthorized publish/rollback smoke operations | mitigate | Smoke uses authenticated admin routes and restores the sample pointer after reversible publish/rollback checks; backend role gate prevents non-super-admin confirmation forgery. | closed |
| T36-19 | Information Disclosure | untrusted provider URLs/downloads during verification | mitigate | Smoke verifies candidates and versions through guarded backend endpoints rather than raw local provider URLs. | closed |
| T36-20 | Repudiation | lineage loss in verification and rollback evidence | mitigate | `36-VERIFICATION.md` records required item/version fields and keeps MAT requirements blocked when live evidence is missing. | closed |
| T36-21 | Information Disclosure | secrets exposure through ffmpeg/video tooling | mitigate | `phase36-build-video.ps1` has no provider/COS secret handling; it delegates upload to the env-backed import script. | closed |
| T36-22 | Tampering | path/object-key tampering in video outputs and imports | mitigate | Video jobs use manifest output paths and forced COS keys, then import through the same backend path guard and package-scoped import endpoint. | closed |
| T36-23 | Elevation of Privilege | unauthorized publish/rollback via video imports | mitigate | Video import requires `-ConfirmProduction -Upload -Promote published`; promote still routes through authenticated package-scoped backend checks. | closed |
| T36-24 | Information Disclosure | untrusted provider URLs/downloads chained into video jobs | mitigate | Video builder only consumes local hero/audio/subtitle files proven by production report rows; it does not ingest arbitrary remote URLs. | closed |
| T36-25 | Repudiation | lineage loss between narration inputs and video outputs | mitigate | Video jobs/imports store `audioItemKey`, subtitle metadata, poster fallback key, manifest output path, and version metadata. | closed |

---

## Accepted Risks Log

No accepted risks.

---

## Audit Evidence

| Area | Evidence |
|------|----------|
| Backend path guard | `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/material/MaterialProductionPathGuard.java` rejects traversal, absolute paths, drive escapes, URL-like input, and unsafe COS object keys. |
| Backend URL guard | `AdminStoryMaterialProductionServiceImpl.bindFinalizedCandidate` calls `AiOutboundUrlGuard.validatePublicSourceUrl` before binding candidate provider URLs. |
| Publish/rollback auth | `AdminStoryMaterialProductionServiceImpl.promoteItemVersion` and `rollbackItemVersion` now require both super-admin role and explicit confirmation. |
| Security regression test | `AdminStoryMaterialProductionServiceTest.requiresSuperAdminConfirmationForCostOverrideAndPublish` rejects non-super-admin callers even when they send `superAdminConfirmation=true`. |
| Immutable lineage | `scripts/local/mysql/init/51-phase-36-material-production-versioning.sql` creates `story_material_package_item_versions` and backfills baseline version rows with utf8mb4. |
| Failed audio gate | `AdminStoryMaterialProductionServiceTest.cosyVoiceFailureLeavesUnpublishedAndDoesNotCreateVersion` verifies failed/empty CosyVoice output does not create a publishable version. |
| Live smoke fail-closed | `scripts/local/smoke-phase-36-material-production.ps1` reserves the success line for complete package/API/version/COS/rollback evidence and prints dependency readiness without secrets. |
| Video gate | `scripts/local/material-production/phase36-build-video.ps1` fails with `FFMPEG_SUBTITLES_UNAVAILABLE` before work if subtitle-capable ffmpeg is unavailable. |
| UI lineage disclosure | `StoryMaterialPackageManagement.tsx` displays version lineage, status, parent/crop metadata, and rollback confirmation while keeping provider secret material out of the browser. |

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-05-02 | 25 | 25 | 0 | Codex security review |

---

## Verification

| Check | Result |
|-------|--------|
| `mvn -q -Dtest=AdminStoryMaterialProductionServiceTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | PASS |
| `npm run type-check` in `packages/admin/aoxiaoyou-admin-ui` | PASS |
| `npm run build` in `packages/admin/aoxiaoyou-admin-ui` | PASS |

---

## Sign-Off

- [x] All threats have a disposition.
- [x] Accepted risks documented.
- [x] `threats_open: 0` confirmed.
- [x] `status: verified` set in frontmatter.

Approval: verified 2026-05-02
