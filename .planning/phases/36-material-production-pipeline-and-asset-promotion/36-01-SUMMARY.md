# Phase 36-01 Summary — Material Production Version Lineage

## Outcome

Implemented the backend foundation for package-scoped material production:

- Added immutable package item versioning through `story_material_package_item_versions`.
- Added current-version pointers to `story_material_package_items`.
- Added production DTOs carrying provider/model/cost/prompt/script/asset metadata consistently across preflight, import, AI candidate bind, promote, rollback, and version history.
- Added guarded local import handling with package-root path enforcement and package-prefix COS object key enforcement.
- Added package-scoped admin routes under `/api/admin/v1/content/material-packages`.
- Added service-level tests covering import lineage, rollback pointer flips, cost confirmation, path/key guards, AI candidate binding, CosyVoice failure handling, and publish promotion.

## Verification

- `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml`
- `mvn -q -Dtest=AdminStoryMaterialProductionServiceTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml`

Both commands passed locally.

## Notes

- Local import now validates both `relativeLocalPath` and `forcedCosObjectKey` before reading or uploading files.
- Rollback is implemented as a current-pointer flip and also writes a rollback marker version for auditability; no historical version rows are deleted.
- Failed or incomplete audio/CosyVoice candidates are rejected and leave the item in `manual_import_required` rather than silently publishing fallback content.
