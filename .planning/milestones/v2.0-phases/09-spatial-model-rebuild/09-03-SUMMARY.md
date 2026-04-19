# Phase 9 Wave 3 Summary

## Outcome

Wave 3 completed the public/runtime cutover for the rebuilt spatial hierarchy and proved it end to end against real local services.

- The mini-program runtime now treats `macau`, `hengqin`, `hong-kong`, and `ecnu` as the canonical top-level cities and handles Macau sub-maps as a second spatial layer.
- The homepage no longer carries the old Taipa/Coloane pseudo-city selector state; city switching now aligns with the canonical public catalog.
- The local smoke proof now upgrades stale MySQL schema, reapplies the canonical seed, verifies admin write -> MySQL -> public read, and finishes with a real `build:weapp`.

## Delivered Artifacts

- `packages/client/src/services/gameService.ts`
- `packages/client/src/pages/index/index.tsx`
- `scripts/local/mysql/init/06-live-backend-mock-migration.sql`
- `scripts/local/smoke-phase-09-spatial.ps1`

## Verification

- `npm run build:weapp` in `packages/client`
- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-09-spatial.ps1`

## Notes

- The smoke flow had to preflight missing local Phase 9 schema pieces such as `sub_maps`, `content_asset_links`, and the new spatial columns before replaying the seed, because the existing local database was behind the current model.
- During verification both the public backend on port `8080` and the admin backend on port `8081` were restarted from the current workspace so the smoke run exercised the latest code instead of stale background instances.
