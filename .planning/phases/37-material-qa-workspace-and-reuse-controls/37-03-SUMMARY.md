# 37-03 Summary — Material Reuse Controls and Verification

## Completed

- Extended admin content asset responses with material package, item, version, promotion, usage target, and chapter metadata.
- Extended content asset keyword search so media picker/library searches can match package code, item key, usage target, chapter code, object key, canonical URL, and filename.
- Updated the shared media picker to show material package context in option labels and selected asset cards.
- Updated the media library with material package search affordances, stats, and tags for package, item, and promotion state.
- Added `scripts/local/smoke-phase-37-material-qa.ps1` covering package lookup, QA overview, QA filters, QA detail, consistency check, safe action flow, and content asset metadata search.
- Created `37-VERIFICATION.md` with QA-01 through QA-04 evidence checklist and local verification caveats.

## Verification

- `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml`: passed.
- `npm run build --prefix packages/admin/aoxiaoyou-admin-ui`: passed with the existing Vite large chunk warning.
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-37-material-qa.ps1`: passed and printed `Material QA smoke passed`.

## Notes

- The implementation reuses existing content asset APIs and shared picker components instead of adding a parallel asset reuse surface.
- The smoke keeps destructive actions conservative: it approves a current usable version idempotently when available and does not reject or rollback production story assets.
