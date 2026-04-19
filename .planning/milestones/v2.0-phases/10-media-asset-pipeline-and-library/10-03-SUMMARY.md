# Phase 10 Wave 3 Summary

## Outcome

Wave 3 completed the central media-library rollout for active admin authoring surfaces and closed the operational safety gaps around shared assets.

## Delivered

- Added asset usage tracing on the admin backend with `GET /api/admin/v1/content/assets/{id}/usages`.
- Guarded media deletion server-side so assets still referenced by direct asset fields, ordered attachment links, or legacy URL-based modules cannot be deleted.
- Extended usage aggregation across:
  - `content_asset_links`
  - direct asset columns on runtime settings, cities, sub-maps, POIs, storylines, story chapters, rewards, notifications, stamps, and tip articles
  - URL-based legacy references on collectibles, badges, and indoor buildings
- Replaced raw numeric media entry on milestone-owned pages with a shared library-driven picker:
  - storylines
  - story chapters
  - collectibles
  - badges
  - indoor buildings
- Generalized the spatial picker into shared media components so map-space forms and non-spatial forms now use the same media interaction contract.
- Reworked collectible and badge create flows so submitted admin form values are now actually persisted instead of being placeholder-generated on the backend.
- Added media usage visibility in the admin media detail drawer.
- Added `scripts/local/smoke-phase-10-media.ps1` for end-to-end verification.
- Fixed the Phase 10 MySQL migration so it is re-runnable on the local MySQL variant in this environment and patched the media policy config upsert to satisfy the required `_openid` column on `sys_config`.

## Verification

- `mvn test -q` in `packages/admin/aoxiaoyou-admin-backend`
- `npm run build` in `packages/admin/aoxiaoyou-admin-ui`
- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-10-media.ps1 -AdminBaseUrl http://127.0.0.1:18081`

## Notes

- Smoke verification was executed against a fresh admin backend instance started on port `18081` from the current workspace build, because the long-running process on `8081` was an older runtime that did not yet include the Phase 10 schema-aware media implementation.
- The temporary `18081` verification process was stopped after smoke completion.
