# v2.0 Research: Features

**Milestone:** `v2.0 後台管理系統的改進與完善`

## Category 1: Admin UX And Information Architecture

**Table stakes**

- Traditional Chinese-first admin labels, forms, menus, and helper text.
- Reuse the mini-program icon/brand assets in `/admin`.
- Replace wrong cross-domain navigation reuse such as chapter redirects and content-console fragments in unrelated modules.
- Convert placeholder/deferred views into milestone-backed modules or explicitly remove them from the menu.

**Differentiators**

- Context-aware editor layouts for maps, POIs, stories, indoor data, and media rather than one generic CRUD style.
- Safer editing flows with preview, validation, and publish-state clarity for operators.

**Dependencies**

- Requires shared admin navigation rework and a stable domain taxonomy before page-by-page rewrites.

## Category 2: Authentication And Traveler Identity

**Table stakes**

- Mini-program features require a real WeChat-authenticated session instead of guest/mock fallback.
- All privileged or state-changing actions must fail closed when unauthenticated.
- Admin traveler views must reflect the same real identity/progress records written by the public backend.

**Differentiators**

- Clear login trigger policy: entering "My", performing check-ins, using rewards, and other interactive flows consistently demand login.
- Developer/experience mode behavior remains testable without silently becoming a fake identity system.

**Dependencies**

- Depends on public backend auth contract replacement and client-side gating cleanup.

## Category 3: Multilingual Authoring And Translation Assist

**Table stakes**

- Every admin-managed UI/content field needed by the mini-program supports `zh-Hant`, `zh-Hans`, `en`, and `pt`.
- Admin editors expose four-language inputs in a consistent pattern.
- System settings define the primary source language and translation-engine priority.

**Differentiators**

- One-click translation from the primary field into the other three locales.
- Engine fallback when one translator fails or rate-limits.
- Translation provenance/status so operators know what was machine-filled versus manually edited.

**Dependencies**

- Depends on reusable localization storage and a server-side translation helper.

## Category 4: Spatial Authoring

**Table stakes**

- City means a user-switchable top-level region.
- Each city can own child maps/sub-maps.
- Cities and sub-maps require cover imagery and can have multiple media attachments.
- Coordinates entered in admin are normalized for AMap display/use.
- Storylines and POIs can bind to the spatial hierarchy rather than only the old single-map model.

**Differentiators**

- Auto-enrich city metadata such as country and center coordinate defaults, with manual override.
- Popup/display composition controls for city, sub-map, and POI introduction content.

**Dependencies**

- Depends on media model, coordinate normalization, and multilingual content blocks.

## Category 5: Story, Chapter, Task, Collectible, Reward Composition

**Table stakes**

- Storylines can bind to more than one map/sub-map and define unlock constraints.
- Chapters are a first-class editor, not a redirect into storyline CRUD.
- Activities/tasks move out of the current misfit "live content console" grouping and become proper operational content.
- Collectibles, badges, and rewards support richer relationships to storylines, maps, sub-maps, and icons/effects metadata.

**Differentiators**

- Chapter anchors can point to POIs, tasks, markers, or overlays.
- Storyline/chapter editors can express route-like narrative experiences similar to the user's Macau example.
- Activities support cover media, HTML-rich content, signup metadata, schedule windows, and pinning.

**Dependencies**

- Depends on spatial model, media attachments, localization, and progress tracking hooks.

## Category 6: Indoor Maps And Media Operations

**Table stakes**

- Indoor building management becomes indoor building + sub-map management.
- Buildings can bind to city/sub-map or POI.
- Floors carry area, cover image, media, popup configuration, and tile set metadata.
- Floor tile import supports pre-cut zip packages and full-image slicing.
- Uploads support folder select, drag/drop, and clipboard paste on the admin side.
- Upload policy respects per-admin "lossless upload" permission before COS storage.

**Differentiators**

- CSV validation preview for indoor markers/overlays before write.
- Default zoom-bound calculation from floor area and map dimensions, with system-config overrides.
- Marker editing aided by clickable minimap coordinate picking.

**Dependencies**

- Depends on media processing, COS persistence, and stronger system settings.

## Category 7: User Progress, Operations, And System Settings

**Table stakes**

- User management shows more detailed account, progress, collection, and interaction information.
- Exploration progress is computed from the right domain objects at sub-map and map level.
- Media resource center supports search and retrieval across uploaded assets.
- System configuration becomes a clearer control panel rather than a reused miscellaneous CRUD surface.

**Differentiators**

- Progress and interaction timelines are explainable to operators.
- Operations/testing pages expose the practical runtime levers needed after the live cutover.

**Dependencies**

- Depends on event logging, aggregate progress rules, and domain relationship cleanup.

## Deferred To v2.1

- Full indoor overlay appearance/trigger/effect rules engine.
- Animation-path authoring and chained trigger graphs.
- Full AI capability platformization: multi-provider switching, quotas, suspicious-concurrency throttling, and per-capability governance.
- Any other feature that requires a general-purpose rules/runtime engine instead of the v2.0 data-model rebuild.

## Sources

- WeChat mini-program docs: https://developers.weixin.qq.com/miniprogram/dev/framework/
- AMap Web Service docs: https://lbs.amap.com/api/webservice/guide/api/convert
- `UlionTse/translators`: https://github.com/UlionTse/translators
