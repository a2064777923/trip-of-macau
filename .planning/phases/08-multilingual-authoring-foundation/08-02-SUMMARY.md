# Phase 8 Plan 02 Summary - Admin UI and Runtime Locale Alignment

## Outcome

- Completed the admin-side multilingual authoring workflow for the Phase 8-owned surfaces.
- Replaced shell-level garbled or reused placeholder copy on the admin route-entry surfaces with dedicated Traditional Chinese ownership.
- Aligned client locale propagation with the four-locale public/admin contract, including Portuguese.

## Implemented Changes

### Shared multilingual authoring UI

- Added `packages/admin/aoxiaoyou-admin-ui/src/components/localization/LocalizedFieldGroup.tsx` as the shared multilingual field component.
- Applied the shared four-locale authoring pattern to:
  - `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/CityManagement.tsx`
  - `packages/admin/aoxiaoyou-admin-ui/src/pages/POIManagement/index.tsx`
  - `packages/admin/aoxiaoyou-admin-ui/src/pages/StorylineManagement/index.tsx`
  - `packages/admin/aoxiaoyou-admin-ui/src/pages/SystemManagement/index.tsx`
- Exposed locale completeness, machine-translation state, primary authoring locale, bulk translation actions, and fallback preview behavior in one consistent UI pattern.

### Admin shell cleanup

- Rebuilt the remaining admin shell entry surfaces in clean Traditional Chinese:
  - `packages/admin/aoxiaoyou-admin-ui/src/App.tsx`
  - `packages/admin/aoxiaoyou-admin-ui/src/layouts/DefaultLayout.tsx`
  - `packages/admin/aoxiaoyou-admin-ui/src/pages/Login.tsx`
  - `packages/admin/aoxiaoyou-admin-ui/src/pages/ModulePlaceholder.tsx`
- Kept the shared mini-program icon/logo as the canonical admin brand asset.
- Ensured unfinished modules keep dedicated placeholders instead of wrong redirects or reused console pages.

### Client and runtime alignment

- Updated `packages/client/src/types/game.ts`, `packages/client/src/services/api.ts`, and `packages/client/src/services/gameService.ts` so locale-aware state and requests support `pt`.
- Kept public read behavior deterministic by using the same four-locale fallback assumptions already implemented on the backend.

## Verification

- `npm run build` in `packages/admin/aoxiaoyou-admin-ui`: passed
- `npm run build:weapp` in `packages/client`: passed
- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-08-localization.ps1`: passed

## Notes

- Terminal reads on this machine still render Traditional Chinese as mojibake in some `Get-Content` output, so build/smoke verification remained the reliable source of truth for the rewritten admin shell files.
- The latest live smoke run verified admin login, translation-settings read, city localization update, public locale reads, and translation bridge behavior against the local running services.
