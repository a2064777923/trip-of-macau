# Phase 8 Plan 01 Summary - Multilingual Backend Foundation

## Outcome

- Completed the four-locale backend foundation for `zh-Hant`, `zh-Hans`, `en`, and `pt` across the public and admin backends.
- Added typed translation settings and a replaceable translation bridge surface in the admin backend.
- Added automated proof for locale fallback, translation fallback, and live localization smoke behavior.

## Implemented Changes

### Canonical locale expansion

- Expanded milestone-owned localized schema, entity, DTO, and mapping surfaces to include Portuguese `*Pt` fields.
- Added four-locale support to shared locale/fallback helpers so public and admin code resolve the same deterministic fallback chain.
- Updated the Phase 8 MySQL migration to apply safely on the live local database through an idempotent helper procedure rather than relying on unsupported `ADD COLUMN IF NOT EXISTS` behavior.

### Translation settings and adapter boundary

- Added admin translation settings endpoints:
  - `GET /api/admin/v1/system/translation-settings`
  - `PUT /api/admin/v1/system/translation-settings`
  - `POST /api/admin/v1/system/translate`
- Kept translation as an explicit operator action and separated it from normal content-save success paths.
- Added configurable engine-priority and timeout-aware translation fallback behavior behind a dedicated admin translation service boundary.

### Automated proof

- Added `packages/server/src/test/java/com/aoxiaoyou/tripofmacau/LocalizedContentSupportTest.java`.
- Added `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminTranslationServiceImplTest.java`.
- Added `scripts/local/smoke-phase-08-localization.ps1` to verify admin write, public locale read, and translation bridge behavior end to end.

## Verification

- `mvn test` in `packages/server`: passed
- `mvn test` in `packages/admin/aoxiaoyou-admin-backend`: passed
- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-08-localization.ps1`: passed

## Notes

- The initial live smoke run exposed a real schema drift issue: local MySQL was missing Portuguese columns such as `title_pt`.
- Reworking and applying `scripts/local/mysql/init/07-phase-08-localization.sql` resolved that runtime failure and restored the translation-settings API path.
