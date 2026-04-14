---
phase: 01-canonical-backend-foundation
plan: 01
subsystem: api
tags: [contracts, enums, locale, assets]
requires: []
provides:
  - canonical mini-program/admin/public page-to-domain contract matrix
  - mirrored content status, locale, and asset-kind enums in public/admin backends
  - explicit publish, locale, and asset-resolution rules for later phases
affects: [phase-02-admin-control-plane-completion, phase-03-public-read-apis-cutover, phase-05-cos-media-pipeline]
tech-stack:
  added: []
  patterns: [contract-first backend cutover, mirrored enum semantics across services]
key-files:
  created:
    - docs/integration/miniapp-admin-public-contract.md
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/enums/ContentStatus.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/enums/LocaleCode.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/enums/AssetKind.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/enums/ContentStatus.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/enums/LocaleCode.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/enums/AssetKind.java
  modified:
    - docs/integration/miniapp-admin-public-contract.md
key-decisions:
  - "Define the live cutover from admin-owned data flowing outward to public APIs instead of from the current mock shapes inward."
  - "Mirror canonical publish, locale, and asset-kind codes in both Java backends before schema or controller expansion."
patterns-established:
  - "Contract matrix first: every mini-program page maps to public API groups and admin-owned entities before implementation expands."
  - "Canonical enum helpers: later phases should convert persisted string codes through shared enum lookup helpers instead of repeating raw literals."
requirements-completed: [DATA-03]
duration: 8min
completed: 2026-04-12
---

# Phase 1: Plan 01 Summary

**Canonical mini-program/admin/public ownership rules with mirrored status, locale, and asset-kind enums in both Java backends**

## Performance

- **Duration:** 8 min
- **Started:** 2026-04-12T03:10:00Z
- **Completed:** 2026-04-12T03:18:00Z
- **Tasks:** 2
- **Files modified:** 7

## Accomplishments
- Documented the mini-program page-to-API and entity ownership contract in `docs/integration/miniapp-admin-public-contract.md`.
- Added identical `ContentStatus`, `LocaleCode`, and `AssetKind` enums to `packages/server` and the admin backend.
- Locked in publish, locale fallback, and asset-resolution rules for later schema, admin, public API, and COS work.

## Task Commits

Atomic task commits were intentionally skipped because the repository already contained unrelated in-progress user changes and Phase 1 was executed as a single working-tree pass.

## Files Created/Modified

- `docs/integration/miniapp-admin-public-contract.md` - Canonical page/domain contract, publish rules, locale rules, and known backend gaps.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/enums/ContentStatus.java` - Public-backend publish-state enum with code lookup helper.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/enums/LocaleCode.java` - Public-backend locale enum with canonical `zh-Hans`, `zh-Hant`, and `en` codes.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/enums/AssetKind.java` - Public-backend asset-kind enum for later COS and content wiring.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/enums/ContentStatus.java` - Admin-backend mirror of the publish-state enum.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/enums/LocaleCode.java` - Admin-backend mirror of the locale enum.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/enums/AssetKind.java` - Admin-backend mirror of the asset-kind enum.

## Decisions Made

- The contract matrix is written from the admin/public ownership boundary, not from existing mock object shapes, so later CRUD and read APIs grow from the same source of truth.
- Shared domain codes are enforced in both backends now to prevent publish-state, locale, and asset semantics from drifting during later implementation phases.

## Deviations from Plan

The `Known Gaps` section in the contract matrix was refreshed after later Phase 1 work completed so the document no longer describes the public start helper and smoke script as missing. This kept the canonical contract accurate at phase close.

## Issues Encountered

None.

## User Setup Required

None - no external service configuration required.

## Verification

All checks passed.

- `node .codex/get-shit-done/bin/gsd-tools.cjs verify artifacts .planning/phases/01-canonical-backend-foundation/01-01-PLAN.md`
- `node .codex/get-shit-done/bin/gsd-tools.cjs verify key-links .planning/phases/01-canonical-backend-foundation/01-01-PLAN.md`
- Manual review confirmed no cloud secrets or COS credentials were written into the contract document or enum files.

## Next Phase Readiness

- Phase 2 can now implement admin CRUD and runtime-setting surfaces against an explicit ownership and value contract.
- Phase 3 can build public DTOs and controllers against the same shared status, locale, and asset semantics.

---
*Phase: 01-canonical-backend-foundation*
*Completed: 2026-04-12*
