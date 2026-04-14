# Mini-Program / Admin / Public Contract Matrix

This document defines the Phase 1 canonical contract for the live Trip of Macau backend cutover.
The mini-program consumes traveler-safe public APIs from `packages/server`.
The admin system owns authoring, publishing, ordering, locale, and asset references for every mini-program-facing domain.

## Page-to-API Matrix

| Mini-program page | Current source | Public API group | Admin-managed entities / settings | Canonical notes |
| --- | --- | --- | --- | --- |
| `pages/index/index` (`index`) | local mock + derived state | `GET /api/v1/runtime/home`, `GET /api/v1/cities`, `GET /api/v1/notifications` | runtime setting groups `home`, `discover`; `cities`; notifications; featured assets | Home page must only render published cards, featured city state, and approved notices. |
| `pages/map/index` (`map`) | `gameService.ts` + `gameMock.ts` | `GET /api/v1/runtime/map`, `GET /api/v1/cities`, `GET /api/v1/pois`, `GET /api/v1/story-lines` | cities, POIs, storylines, map runtime rules, map assets | `pages/map` is the main read surface for location discovery and must not use admin endpoints directly. |
| `pages/map/indoor/index` (`map`) | mock indoor assets | `GET /api/v1/pois/{id}`, `GET /api/v1/assets/{id}` | POIs, indoor media assets, building / indoor config in later phases | Indoor content stays under the map domain and resolves through asset metadata rather than hardcoded file names. |
| `pages/discover/index` (`discover`) | mock discover cards | `GET /api/v1/discover/cards`, `GET /api/v1/runtime/discover` | discover cards, campaigns, runtime sort/filter settings | Discover cards are admin-curated composites linked to cities, POIs, activities, or rewards. |
| `pages/story/index` (`story`) | mock storylines | `GET /api/v1/story-lines`, `GET /api/v1/story-lines/{id}` | storylines, story chapters, associated assets | Storyline visibility is publish-state gated and localized. |
| `pages/stamps/index` (`stamps`) | local stamp collection state | `GET /api/v1/stamps`, `GET /api/v1/user/progress/stamps` | stamp definitions, reward link rules | Stamp definitions are admin-authored; user collection status is public-user state. |
| `pages/rewards/index` (`rewards`) | mock rewards | `GET /api/v1/rewards`, `GET /api/v1/user/progress/rewards` | rewards, reward inventory, redemption policy, media assets | Rewards expose published inventory and public-safe redemption status only. |
| `pages/tips/index` (`tips`) | mock tips + local UGC simulation | `GET /api/v1/tips`, `GET /api/v1/runtime/tips`, `GET /api/v1/notifications` | tip articles, tip categories, moderation/publish state, notification content | `pages/tips` must transition from mock article arrays to published tip/article records. |
| `pages/tips/detail/index` (`tips`) | mock article lookup | `GET /api/v1/tips/{id}` | tip articles, cover assets, related location metadata | Detail views must resolve canonical content IDs from MySQL. |
| `pages/tips/notifications/index` (`tips`) | mock notifications | `GET /api/v1/notifications` | notifications, notification targeting rules | Traveler-visible notifications are admin-authored and publish-gated. |
| `pages/tips/publish/index` (`tips`) | local draft-only state | `POST /api/v1/user/tips` (later), `POST /api/v1/assets/upload` (later) | moderation settings, asset policies, UGC review queues | User-generated publication flows require public write APIs plus admin moderation views. |
| `pages/profile/index` (`profile`) | local storage state | `GET /api/v1/user/profile`, `GET /api/v1/user/progress`, `GET /api/v1/user/preferences` | profile presentation config, badge / collectible metadata, emergency-contact policy | `pages/profile` must combine admin-defined metadata with user-owned progress state. |
| `pages/settings/index` (`settings`) | local storage state | `GET /api/v1/runtime/settings`, `PUT /api/v1/user/preferences` (later) | settings runtime groups, accessibility defaults, locale defaults | Settings defaults are admin-managed; user overrides live in public user-preference records. |
| `pages/senior/index` (`senior`) | local toggle state | `GET /api/v1/runtime/settings`, `PUT /api/v1/user/preferences` (later) | senior-mode defaults, accessibility runtime rules, voice-guide defaults | `senior` is a specialized settings/profile surface, not a separate data domain. |

## Canonical Entity Matrix

| Canonical entity | Owned by admin | Served by public backend | Consumed by mini-program | Notes |
| --- | --- | --- | --- | --- |
| `app_runtime_settings` | Yes | Yes | `index`, `map`, `discover`, `tips`, `settings`, `senior` | Stores grouped runtime switches, copy blocks, and ordering hints. |
| `content_assets` | Yes | Yes | all media-backed pages | Stores canonical asset metadata, URLs, object keys, mime type, and kind. |
| `cities` | Yes | Yes | `index`, `map`, `profile` | Controls unlock metadata, map center data, display names, and hero assets. |
| `pois` | Yes | Yes | `map`, `story`, `discover` | Core location records with publish state, sort order, and asset references. |
| `storylines` | Yes | Yes | `story`, `map` | Story containers linked to chapters and optional POIs / cities. |
| `story_chapters` | Yes | Yes | `story` | Ordered localized story content within a storyline. |
| `tip_articles` | Yes | Yes | `tips`, `discover` | Published tips, guides, and moderated user-generated articles. |
| `rewards` | Yes | Yes | `rewards`, `profile` | Reward inventory and presentation data. |
| `stamps` | Yes | Yes | `stamps`, `profile` | Stamp definitions and display metadata. |
| `notifications` | Yes | Yes | `index`, `tips`, `profile` | Admin-authored traveler notices and alerts. |
| `user_profiles` | No | Yes | `profile`, `settings`, `senior` | Public-user state owned by traveler identity, not admin authoring. |
| `user_progress` | No | Yes | `stamps`, `story`, `profile` | User storyline, collectible, and gameplay progress. |
| `user_checkins` | No | Yes | `map`, `profile` | Check-in records and verification metadata. |
| `user_preferences` | No | Yes | `settings`, `senior`, `profile` | Accessibility, locale, font scale, high contrast, and interface-mode choices. |
| `reward_redemptions` | No | Yes | `rewards`, `profile` | User redemption history linked to reward inventory. |

## Publish Rules

1. Admin-authorable content records use one canonical publish-state vocabulary: `draft`, `published`, `archived`.
2. Public read APIs must return only `published` records unless an explicitly authenticated admin-only surface is being used.
3. `archived` records remain queryable for admin audit/history but must not appear in traveler-facing public responses.
4. Ordered public lists must sort first by `sort_order ASC`, then by stable `id ASC` unless a domain-specific override is documented.
5. Runtime setting groups are publish-gated at the record or group-item level; unpublished overrides must not leak into public responses.
6. User-owned state such as progress, check-ins, preferences, and redemptions is not publish-gated, but it may only reference published canonical content.

## Locale Rules

1. Canonical locale codes are `zh-Hans`, `zh-Hant`, and `en`.
2. Human-facing content tables use explicit localized columns or equivalent fields mapped to those locale codes.
3. Public APIs accept an optional locale hint but must always have a fallback chain:
   - requested locale
   - `zh-Hans`
   - first non-empty canonical value
4. Admin forms must author the same locale set for every shared domain instead of inventing per-screen locale keys.
5. Public DTOs should expose resolved display strings plus stable entity IDs; raw locale fallback logic stays server-side.

## Asset Resolution Rules

1. Media-backed entities reference assets by canonical asset record ID, not by frontend hardcoded file names.
2. Asset records store bucket, region, object key, canonical URL, mime type, checksum/etag, and `asset_kind`.
3. Canonical asset kinds are `image`, `icon`, `map_tile`, `audio`, `json`, and `other`.
4. Admin uploads always go through backend-managed storage flows; the frontend never chooses final COS object keys.
5. Public responses expose only canonical URLs and safe metadata needed by the mini-program.
6. Locale-specific assets may be represented as multiple asset records linked from the owning content entity or nested locale-aware fields.

## Known Gaps

- The current mini-program runtime still depends heavily on local mock data in `packages/client/src/services/gameMock.ts` and orchestration in `packages/client/src/services/gameService.ts`.
- Current mock-served domains include discover cards, city unlock state, storyline progression, stamps, rewards, tip articles, notifications, travel recommendations, emergency contact state, and traveler preferences.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/config/WebConfig.java` was a brownfield blocker because it referenced a missing `AdminAuthInterceptor`; Phase 1 removes that dependency, and later phases should keep the public backend free of admin-only wiring.
- The public backend currently exposes health, POI, storyline, stats, trigger-log, test-account, and user endpoints only; it does not yet cover the full runtime/tips/rewards/stamps/discover/profile contract.
- The admin backend already covers core map/content surfaces but does not yet expose every mini-program-facing setting or content entity needed to remove mock behavior end-to-end.
- Local startup ergonomics now exist for both backends through `scripts/local/start-public-backend.cmd`, `scripts/local/start-admin-backend.cmd`, and `scripts/local/smoke-phase-01-foundation.ps1`; later phases should extend these scripts rather than reintroducing ad hoc local steps.
