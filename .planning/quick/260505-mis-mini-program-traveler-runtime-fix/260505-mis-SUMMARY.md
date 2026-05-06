# Quick Task 260505-mis Summary

## Completed
- Removed the traveler-visible map/city switch strip from the mini-program map page. Big-map switching remains on the home page selector.
- Reworded story and map copy away from admin/operator language: sync/config/anchor/binding/workbench terms are no longer shown in the inspected traveler-facing story and map surfaces.
- Added `SafeStoryImage` for story images. It converts SVG data URLs to cached local temp files when possible and falls back to a designed story placeholder instead of showing blank media.
- Updated story content block rendering to use `SafeStoryImage`, hide debug resource IDs, and avoid reporting unavailable media while the user is only browsing without an active story session.
- Hardened Lottie unavailable handling so empty/non-network animation paths show a traveler-friendly fallback instead of repeated low-level failure text.

## Verification
- `npm run build:weapp` passed twice.
- `GET http://127.0.0.1:8080/api/v1/health` returned `UP`.
- `GET http://127.0.0.1:8080/api/v1/story-lines?locale=zh-Hant` returned published Traditional Chinese story data.
- `GET http://127.0.0.1:8081/api/v1/admin/auth/captcha` returned `200`.

## Blocked
- WeChat DevTools CLI and MCP compile are still blocked by Windows port exclusion on `127.0.0.1:3799`.
- Error: `listen EACCES: permission denied 127.0.0.1:3799`.
- `netsh interface ipv4 show excludedportrange protocol=tcp` still shows the reserved range `3707-3806`, which includes `3799`.
