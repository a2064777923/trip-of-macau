# Mini-program Runtime UAT Fixes

## Status

in-progress

## Symptoms

- Map page renders a blank cream screen in WeChat DevTools while page stack reports `pages/map/index`.
- Home and story entry still prefer the old duplicate storyline in some places.
- Story gameplay is still compressed into single confirmation buttons instead of presenting visible step targets and media.
- First chapter cinematic media is mismatched and image rendering duplicates the same flower photo.
- Home assessment result and settings font scale UI have oversized text layout.
- Rewards show redeemed states while available stamps are zero.
- Some traveler pages still expose fallback/runtime/dev wording or old mock-like content.

## Current Approach

- Fix mini-program traveler-facing runtime first.
- Keep backend/admin lifecycle work aligned with Phase 45 but do not hard delete data.
- Do not introduce mock fallback business data as a replacement for missing backend data.

## Evidence

Screenshots captured with WeChat DevTools MCP under `screenshots/uat-*.png`.

## 2026-05-06 Fix Pass

### Root Cause Notes

- The blank cream screenshot can occur when WeChat DevTools compile state is stale: page stack exists but page data only contains `__webviewId__`, with no Taro `root`. This is an IDE/runtime cache state, not a valid business UI state.
- `dist/pages/index/index.js` contains `Page(createPageConfig(... root ...))`, proving the built artifact has Taro root data.
- MCP compile reported success but also reported DevTools had moved its server to another random port and needed restart on `9420`, so screenshots from stale ports are not reliable without re-opening/cleaning.

### Fixes Applied

- Hardened map native component inputs:
  - Converts string/number coordinates safely.
  - Filters invalid marker/circle/polyline points before passing them to native `Map`.
  - Guards marker tap IDs before selection.
- Removed empty emergency contact card from the map page unless a real name and phone exist.
- Changed map story-mode copy from configuration-like wording to player-facing journey copy.
- Kept big-map selection responsibility on the home page; map page now labels the selected large map separately from the sub-area route text.
- Reworked story play copy so the active chapter shows one current prompt first, with details only after the prompt, instead of feeling like a full configuration dump.
- Replaced traveler-visible backend/runtime/fallback wording in home, media, indoor, and Lottie presentation copy.
- Restored Lottie playback path using `lottie-miniprogram` with lazy/static canvas creation and animation destruction on unmount or asset change to reduce memory growth.
- Cleaned rewards page wording so unavailable rewards no longer show misleading large inventory counts or "continue collecting stamps" as a disabled-looking dead end.

### Verification

- `npm run build:weapp` in `packages/client` completed successfully.
- Build warning remains: `pages/story/index.js` is 248 KiB, slightly above the recommended 244 KiB limit. This is a performance warning, not a compile failure.
- MCP cache clean and compile succeeded, but automator page screenshot remained unreliable because DevTools port/cache state reported inconsistent ports (`9420` vs random IDE server port). Need a clean manual DevTools restart before accepting screenshot evidence.
- Static scan found no traveler-facing matches for the forbidden admin/dev copy outside internal code identifiers and sanitizer regexes.

### Residual Risks

- Story page bundle should be split in a later phase if it keeps growing.
- Full interactive gameplay still depends on backend runtime events and story assets being configured correctly; this pass only fixes traveler presentation and runtime safety, not missing content authoring.
- If WeChat DevTools shows a blank cream page again, first clean compile cache and reopen the project before diagnosing business logic.

## 2026-05-06 Map Coordinate And Marker Fix

### Root Cause Notes

- WeChat mini-program native `map` is expected to render through the WeChat/Tencent map stack. The provider label is not the issue by itself.
- WeChat official map component documentation states that map coordinates use GCJ-02 and `wx.getLocation` should use `type: gcj02`; the current mini-program already requests location with `type: 'gcj02'`.
- Several Macau POIs were flagged as `GCJ02` while their stored values were WGS84-like source coordinates. Rendering those directly on WeChat native map caused an obvious Macau marker offset.
- Story POIs also reused a shared data URI SVG marker asset. Native map marker icons are more reliable with concrete PNG/JPG local or network paths, so SVG/data URI marker URLs are rejected client-side.

### Fixes Applied

- Updated Macau city/sub-map centers and selected POIs to preserve WGS84 source coordinates while storing normalized GCJ-02 display coordinates.
- Generated and uploaded five 2.5D PNG POI marker icons for the flagship route, then bound them to `ama_temple`, `lilau_square`, `dom_pedro_v_theatre`, `monte_fort`, and `senado_square`.
- Added replayable seed script `52-phase-44-map-coordinate-and-poi-icon-fix.sql` so fresh local databases also get the icon asset rows and corrected coordinates.
- Hardened the mini-program map marker path resolver so native markers only use HTTP(S) PNG/JPG/JPEG URLs from backend assets, falling back to known PNG marker assets otherwise.
- Improved POI admin list and coordinate input copy to show source coordinates, normalized GCJ-02 coordinates, and marker icon previews.

### Verification

- `GET /api/v1/pois?cityCode=macau&page=1&pageSize=50` returns the flagship POIs with `sourceCoordinateSystem=WGS84`, normalized GCJ-02 `latitude/longitude`, and COS PNG `mapIconUrl`.
- `GET /api/v1/cities?locale=zh-Hant` and `GET /api/v1/maps?locale=zh-Hant` return large-map and sub-map data. The earlier `/api/v1/maps/cities` request was an invalid path.

## 2026-05-06 Image-1 POI Marker Board Pass

### Requirement

- Generate multiple 2.5D cartoon map POI icons from one `image-1` sprite board and slice them into transparent marker PNGs to reduce generation cost.
- Do not put text, labels, numbers, or UI frames on the icons.

### Fixes Applied

- Reprocessed `local-content/phase44/poi-icons-image1/phase44-poi-icon-board.png` without spending another image generation call.
- Sliced five transparent 512x512 PNGs from the board:
  - `ama-temple-2_5d-image1.png`
  - `lilau-square-2_5d-image1.png`
  - `hill-watch-2_5d-image1.png`
  - `monte-fort-2_5d-image1.png`
  - `senado-square-2_5d-image1.png`
- Uploaded the sliced icons through the admin backend media API into Tencent COS.
- Bound the resulting content asset IDs to the flagship route POIs:
  - `ama_temple` -> `333199`
  - `lilau_square` -> `333200`
  - `dom_pedro_v_theatre` -> `333201`
  - `monte_fort` -> `333202`
  - `senado_square` -> `333203`
- Archived the previous temporary script-drawn POI marker assets `333193-333198` because none are now used by POIs.
- Updated `52-phase-44-map-coordinate-and-poi-icon-fix.sql` so a fresh local DB replays the image-1 icon bindings.

### Verification

- Pixel alpha check passed: all five icons are 512x512, have transparent corners, and have bounded alpha content.
- COS HEAD checks passed: all five final icon URLs return HTTP 200 `image/png`.
- SQL replay passed against the current local MySQL database.
- Public POI API returns the new image-1 COS URLs in `mapIconUrl` for the five flagship POIs.
