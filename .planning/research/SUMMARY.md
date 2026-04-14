# v2.0 Research Summary

**Milestone:** `v2.0 後台管理系統的改進與完善`
**Research date:** 2026-04-13

## Key Findings

### Stack additions

- Keep the current Taro + React + Spring Boot + MyBatis-Plus + MySQL architecture.
- Add a real WeChat login gateway in `packages/server`.
- Add an AMap coordinate-normalization service in the admin backend.
- Add a Python translation sidecar/worker for `UlionTse/translators`.
- Extend the media pipeline with permission-aware preprocessing and richer upload metadata.

### Feature table stakes

- Traditional Chinese-first admin UX and corrected information architecture.
- Real WeChat-authenticated mini-program behavior with guest/mock paths removed from functional use.
- Four-language content authoring for `zh-Hant`, `zh-Hans`, `en`, and `pt`.
- Rebuilt spatial authoring for cities, sub-maps, POIs, indoor basics, media attachments, and story/chapter/task composition.
- Stronger user progress, media search, operations, and system configuration tooling.

### Watch out for

- Current public login is still based on client-supplied `openId`, so auth must be rebuilt before later flows.
- AMap coordinate conversion requires explicit source coord sys; "auto convert" must be an admin normalization layer, not wishful UI logic.
- The translation library is useful but inherently unstable at scale due to engine availability and rate limits, so it must be best-effort.
- v2.0 will fail if it tries to also deliver the full indoor rules engine or full AI provider platformization.

## Recommended v2.0 Scope Freeze

Build in this milestone:

- Admin UX/IA cleanup
- real auth alignment
- multilingual model + translation settings
- spatial/media/story/chapter/task/collectible/reward data-model rebuild
- indoor building/floor/tile basics
- user progress and operations visibility
- system settings cleanup

Defer to `v2.1`:

- full indoor trigger/effect/rules runtime
- full AI provider orchestration platform

## Recommended Build Order

1. Auth and admin IA foundation
2. Localization foundation
3. Spatial + media foundation
4. Story/chapter/task/collectible/reward rebuild
5. Indoor basics and imports
6. User progress + ops + system settings consolidation

## Source Notes

- WeChat login research came from official mini-program docs and the login API family.
- AMap coordinate conversion came from the official AMap Web Service docs.
- Translation-engine behavior came from the official `UlionTse/translators` README.
- Folder/drag/drop/paste upload feasibility came from MDN web platform documentation.

## Sources

- https://developers.weixin.qq.com/miniprogram/dev/framework/
- https://developers.weixin.qq.com/miniprogram/dev/api/open-api/login/wx.login.html
- https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html
- https://developers.weixin.qq.com/miniprogram/dev/api/media/video/wx.chooseMedia.html
- https://developers.weixin.qq.com/miniprogram/dev/api/device/clipboard/wx.getClipboardData.html
- https://lbs.amap.com/api/webservice/guide/api/convert
- https://github.com/UlionTse/translators
- https://developer.mozilla.org/en-US/docs/Web/API/HTMLInputElement/webkitdirectory
- https://developer.mozilla.org/en-US/docs/Web/API/DataTransferItem/webkitGetAsEntry
- https://developer.mozilla.org/en-US/docs/Web/API/ClipboardEvent/clipboardData
