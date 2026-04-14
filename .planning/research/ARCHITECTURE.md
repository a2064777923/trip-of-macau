# v2.0 Research: Architecture

**Milestone:** `v2.0 後台管理系統的改進與完善`

## Architectural Direction

Keep the current four-surface topology:

- `packages/client`: mini-program runtime
- `packages/server`: public traveler API
- `packages/admin/aoxiaoyou-admin-ui`: admin SPA
- `packages/admin/aoxiaoyou-admin-backend`: admin API/control plane

The main architectural change in v2.0 is not a new service boundary. It is a stronger canonical domain model so the admin can author richer runtime behavior without reintroducing mocks or ad hoc duplicated fields.

## Recommended Domain Foundations

### 1. Identity And Auth Foundation

**Public backend**

- Replace `openId`-in-request login with `wx.login code -> server exchange -> user profile binding -> JWT/session issuance`.
- Keep user profile enrichment optional and separate from identity establishment.
- Introduce auth guards in the client so stateful flows fail closed when the traveler is not authenticated.

**Why first**

- Many later domains depend on a trustworthy user identity and progress model.

### 2. Localization Foundation

Introduce a reusable localization pattern:

- `localized_resource` or equivalent reusable structure for four-language values.
- Apply it to names, subtitles, descriptions, popup copy, story content, task content, and media captions.
- Keep denormalized "primary display" fields only where query/index/search performance requires them.

**Inference:** using one-off columns everywhere will explode maintenance cost in v2.0 because almost every admin surface is being rebuilt at once.

### 3. Spatial Foundation

Canonical hierarchy:

- `city` (top-level switchable region)
- `sub_map` (child map beneath city)
- `poi`
- `indoor_building`
- `floor`
- `tile_set`
- `overlay/marker`

Coordinate storage strategy:

- `raw_lat`, `raw_lng`
- `raw_coord_sys`
- `gcj_lat`, `gcj_lng` for AMap consumption
- optional metadata about conversion status/confidence

This allows:

- safe re-normalization
- operator correction
- future map-provider interoperability

### 4. Media Foundation

All media should flow through a shared asset pipeline:

1. Admin UI selects/drops/pastes files or folders.
2. Admin backend creates/stages asset records.
3. Optional preprocessing runs according to admin permission + policy.
4. COS upload completes.
5. Metadata and canonical URL are stored.
6. Domain records bind assets through relation tables instead of raw string fields only.

Recommended asset concepts:

- asset kind
- mime type
- locale
- upload policy applied
- original filename / relative path
- width / height / duration / size
- processing status
- canonical URL

### 5. Story And Activity Composition Foundation

Use relation tables instead of single foreign keys wherever the user's requirement explicitly became many-to-many:

- storyline <-> city/sub-map
- storyline <-> POI
- chapter -> anchor entity (`POI`, `task`, `marker`, `overlay`)
- collectibles/badges/rewards <-> storyline/map/sub-map
- activity/task <-> maps/sub-maps/assets

The composition model should separate:

- core entity metadata
- localized presentation blocks
- relation bindings
- unlock/visibility condition definitions

For v2.0, keep unlock conditions structured but bounded. Avoid building the full runtime rule engine yet.

### 6. Indoor Basics Foundation

Indoor in v2.0 should stop at:

- building/floor/tile authoring
- zip import
- full-image slicing
- marker/overlay CRUD
- CSV preview validation
- zoom-bound calculation and storage

Do **not** make indoor overlays a generic event engine in this milestone. Instead:

- add schema hooks for future trigger/effect linkage
- keep runtime behavior limited to what the current milestone truly needs

### 7. Progress And Telemetry Foundation

Use a dual model:

- append-oriented interaction/event logs
- aggregate progress tables/materialized summaries

Reason:

- operators need detailed traceability
- exploration percentage needs recomputation when content changes
- computing everything from scratch on every request will become expensive

Recommended tracked objects:

- user interactions
- chapter completions
- storyline progress
- POI visits/check-ins
- collectible/badge/reward acquisition
- sub-map and city exploration aggregates

## Translation Assist Architecture

Because `UlionTse/translators` is Python-native, the cleanest bounded approach is:

- admin backend owns translation settings and request lifecycle
- a small Python sidecar/worker performs translation
- admin backend stores translation results, engine used, and fallback status

Do not:

- run arbitrary Python from the browser
- make admin save path strictly synchronous on third-party translation success

## Recommended Build Order

1. Auth and admin IA foundation
2. Localization and translation settings foundation
3. Spatial/media domain rebuild
4. Story/chapter/task/collectible/reward domain rebuild
5. Indoor basics and asset import flows
6. User progress, operations, and system settings consolidation

## Why This Fits The Existing Repo

- It preserves the current brownfield service boundaries.
- It keeps `/admin` authoritative.
- It keeps `packages/server` as the mini-program contract surface.
- It avoids forcing a new infra stack just to satisfy v2.0.
- It gives v2.1 a place to add the heavier rules/runtime systems later without undoing v2.0 work.

## Sources

- WeChat `wx.login`: https://developers.weixin.qq.com/miniprogram/dev/api/open-api/login/wx.login.html
- WeChat login API family: https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html
- AMap coordinate conversion: https://lbs.amap.com/api/webservice/guide/api/convert
- `UlionTse/translators`: https://github.com/UlionTse/translators
- MDN upload/clipboard references:
  - https://developer.mozilla.org/en-US/docs/Web/API/HTMLInputElement/webkitdirectory
  - https://developer.mozilla.org/en-US/docs/Web/API/DataTransferItem/webkitGetAsEntry
  - https://developer.mozilla.org/en-US/docs/Web/API/ClipboardEvent/clipboardData
