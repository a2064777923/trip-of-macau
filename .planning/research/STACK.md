# v2.0 Research: Stack

**Milestone:** `v2.0 後台管理系統的改進與完善`  
**Scope:** Research only the stack changes needed for the post-cutover admin/control-plane rebuild.

## Existing Baseline

- Mini-program: Taro 3.6 + React 18 + TypeScript + WeChat runtime APIs.
- Admin UI: React 18 + Vite 6 + Ant Design 5 + React Query + Zustand.
- Public backend: Spring Boot 3.2 + MyBatis-Plus + MySQL + Redis.
- Admin backend: Spring Boot 3.2 + MyBatis-Plus + MySQL + MongoDB + Tencent COS SDK.
- Data stores: MySQL is the operational source of truth for mini-program-facing content and progress.

## Recommended Additions For v2.0

| Area | Recommendation | Why | Notes |
|------|----------------|-----|-------|
| WeChat auth | Add a real mini-program auth gateway in `packages/server` that accepts `wx.login` code and exchanges it server-side for `openid` and `session_key` | Current public login still accepts client-supplied `openId`, which is not a real WeChat-authenticated identity flow | Keep client profile/avatar enrichment separate from identity establishment |
| Session validity | Add server-side session freshness checks and token issuance around the WeChat login exchange | Official WeChat login flow centers on code exchange and login-state validation, not permanent client-generated IDs | Keep devtools compatibility explicit and environment-gated |
| Coordinate normalization | Add an AMap coordinate-normalization service in the admin backend | AMap expects explicit source coordinate systems and does not offer "auto detect" in the conversion API | Store raw coordinates + source coord sys + normalized GCJ-02 |
| Localization model | Introduce a reusable localized-value model instead of scattering ad hoc extra columns everywhere | v2.0 touches many domains and every display field now needs four locales | Denormalize only where search/index performance really needs it |
| Translation assist | Add a small Python sidecar or worker wrapper around `UlionTse/translators` | The requested translation library is Python-native, not Java-native | Treat translation as assistive and retryable, not a blocking source of truth |
| Upload UX | Use browser-native file input, folder input, drag/drop, and paste support in admin UI | The user explicitly wants open-folder, drag/drop, and clipboard upload | Use `webkitdirectory`, drag/drop `DataTransferItem`, and clipboard APIs in the web admin |
| Media processing | Add server-side image compression and optional FFmpeg-based video/audio processing | v2.0 needs permission-aware lossy/lossless upload handling before COS upload | Image-only compression is insufficient for the requested media mix |
| Tile import | Add zip unpack + tile manifest validation + whole-image slicing pipeline in admin backend | Indoor floor uploads require both "pre-cut zip" and "single floor image" flows | Zip handling can stay inside Java; image slicing may use Java imaging libraries |
| Background jobs | Prefer DB-backed jobs/scheduled workers before introducing a new broker | Translation retries, metadata enrichment, tile slicing, and recompute jobs are needed, but a new queue stack would inflate scope | Stay brownfield-friendly in v2.0 |

## Strong Recommendations

- Do **not** replace the existing Spring/Taro/Admin stack for this milestone.
- Do **not** keep translation logic in the frontend; translation settings belong in admin/system config, execution belongs server-side.
- Do **not** bolt four-language support onto each screen independently; make it a reusable backend + UI pattern.
- Do **not** attempt the full AI provider orchestration platform in v2.0; only lay safe groundwork where needed.

## Stack Implications By Domain

### Real WeChat Login

- Official login starts with `wx.login`, which returns a short-lived `code`.
- The developer server must exchange that code for `openid`, `unionid` (when available), and `session_key`.
- WeChat also exposes server-side login-state validation/reset endpoints in the login API family.

**Inference:** the current `openId`-in-request contract in `packages/server` should be replaced by a `code`-based request in v2.0, with any dev/test bypass clearly isolated from production behavior.

### AMap Coordinate Conversion

- AMap Web Service coordinate conversion requires explicit `locations` and `coordsys`.
- Supported source systems include `gps`, `mapbar`, `baidu`, and `autonavi`.
- A single conversion request supports up to 40 coordinate pairs.

**Inference:** "auto convert if not AMap coordinates" must be implemented as an admin-side normalization layer that first knows or infers the source coordinate system, then converts into the stored AMap/GCJ-02 value.

### Translation Service

- `UlionTse/translators` supports many engines and exposes a shared `translate_text(...)` API.
- The project documents pre-acceleration/speed-test support and a large engine pool.
- The README also explicitly warns about `429`/high-frequency failures and region/network issues.

**Inference:** translation should be an operator-assist pipeline with engine priority and fallback, not a hard dependency for saving core admin content.

### Admin Upload Interactions

- Browser folder selection can be implemented with `webkitdirectory`.
- Drag/drop folder traversal can use `DataTransferItem.webkitGetAsEntry()`.
- Clipboard paste handling can use `ClipboardEvent.clipboardData`.

**Inference:** the requested upload UX is viable in the admin SPA without changing frameworks, but the server upload contract must support batches, relative paths, media metadata, and post-processing state.

## What Not To Add In v2.0

- A new workflow engine or message broker unless later execution proves DB-backed jobs are insufficient.
- A second media storage provider alongside Tencent COS.
- A frontend-only translation feature that bypasses admin/system configuration.
- A fully generic event-scripting engine for indoor overlays; that belongs to v2.1.

## Sources

- WeChat `wx.login`: https://developers.weixin.qq.com/miniprogram/dev/api/open-api/login/wx.login.html
- WeChat mini-program login API list: https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html
- WeChat `wx.chooseMedia`: https://developers.weixin.qq.com/miniprogram/dev/api/media/video/wx.chooseMedia.html
- WeChat `wx.getClipboardData`: https://developers.weixin.qq.com/miniprogram/dev/api/device/clipboard/wx.getClipboardData.html
- AMap coordinate conversion: https://lbs.amap.com/api/webservice/guide/api/convert
- `UlionTse/translators` README: https://github.com/UlionTse/translators
- MDN `webkitdirectory`: https://developer.mozilla.org/en-US/docs/Web/API/HTMLInputElement/webkitdirectory
- MDN `webkitGetAsEntry()`: https://developer.mozilla.org/en-US/docs/Web/API/DataTransferItem/webkitGetAsEntry
- MDN `ClipboardEvent.clipboardData`: https://developer.mozilla.org/en-US/docs/Web/API/ClipboardEvent/clipboardData
