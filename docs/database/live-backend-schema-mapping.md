# Live Backend Schema Mapping

This document maps the current mini-program mock domains and admin/public contract to the canonical MySQL schema introduced in Phase 1.
The table list below is the target foundation for later admin CRUD, public read APIs, public write APIs, COS asset linking, and mock-data migration.

## Table Coverage

| Table | Primary key | Key foreign keys / lookup links | Publish / sort columns | Mini-program surface |
| --- | --- | --- | --- | --- |
| `app_runtime_settings` | `id` | optional `asset_id -> content_assets.id` | `status`, `sort_order` | `index`, `map`, `discover`, `tips`, `settings`, `senior` |
| `content_assets` | `id` | none; referenced by content tables | `status`, `created_at` | all media-backed pages |
| `cities` | `id` | `cover_asset_id`, `banner_asset_id` logical links to `content_assets` | `status`, `sort_order` | `index`, `map`, `profile` |
| `pois` | `id` | `city_id -> cities.id`, `storyline_id -> storylines.id`, asset links to `content_assets` | `status`, `sort_order` | `map`, `discover`, `story` |
| `storylines` | `id` | `city_id -> cities.id`, asset links to `content_assets` | `status`, `sort_order` | `story`, `map` |
| `story_chapters` | `id` | `storyline_id -> storylines.id`, `media_asset_id` logical link | `status`, `sort_order` | `story` |
| `tip_articles` | `id` | `city_id -> cities.id`, `cover_asset_id` logical link | `status`, `sort_order` | `tips`, `discover` |
| `rewards` | `id` | `cover_asset_id` logical link | `status`, `sort_order` | `rewards`, `profile` |
| `stamps` | `id` | `icon_asset_id` logical link, `related_poi_id -> pois.id`, `related_storyline_id -> storylines.id` | `status`, `sort_order` | `stamps`, `profile` |
| `notifications` | `id` | `cover_asset_id` logical link | `status`, `sort_order` | `index`, `tips`, `profile` |
| `user_profiles` | `id` | `current_city_id -> cities.id` | not publish-gated; `created_at` | `profile`, `settings`, `senior` |
| `user_progress` | `id` | `user_id -> user_profiles.id`, `storyline_id -> storylines.id` | not publish-gated; `updated_at` | `story`, `stamps`, `profile` |
| `user_checkins` | `id` | `user_id -> user_profiles.id`, `poi_id -> pois.id` | not publish-gated; `checked_at` | `map`, `profile` |
| `user_preferences` | `id` | `user_id -> user_profiles.id` | not publish-gated; `updated_at` | `settings`, `senior`, `profile` |
| `reward_redemptions` | `id` | `user_id -> user_profiles.id`, `reward_id -> rewards.id` | not publish-gated; `created_at` | `rewards`, `profile` |

## Entity-to-Table Mapping

| Domain concept | Canonical table(s) | Notes |
| --- | --- | --- |
| Runtime cards, copy blocks, ordering, and UI toggles | `app_runtime_settings` | Grouped by `setting_group` such as `home`, `discover`, `map`, `tips`, `profile`, and `settings`. |
| Images, icons, map tiles, audio, JSON resources | `content_assets` | Stores canonical object metadata before and after Tencent COS integration. |
| City / region progression | `cities`, `user_profiles` | City presentation is admin-owned; current-city state is traveler-owned. |
| POIs and geofenced locations | `pois`, `user_checkins` | Public location reads use `pois`; gameplay writes append to `user_checkins`. |
| Storyline shells and ordered chapters | `storylines`, `story_chapters`, `user_progress` | Progress records reference storyline completion state and completed chapter IDs. |
| Tips, travel articles, and moderated UGC | `tip_articles` | Phase 1 lays the schema; publish/moderation workflows arrive later. |
| Collectible stamp definitions and collection state | `stamps`, `user_progress` | Definitions are admin-owned; collection state stays user-scoped. |
| Reward catalog and redemption history | `rewards`, `reward_redemptions` | `reward_redemptions` snapshots cost/state for auditability. |
| Notifications and announcements | `notifications` | Traveler-safe public reads serve only currently published notifications. |
| Traveler profile / accessibility / preferences | `user_profiles`, `user_preferences` | Split persistent identity/progress from mutable preference toggles. |

## Publish and Locale Columns

- Content tables use `status` with canonical values `draft`, `published`, and `archived`.
- Ordered public tables use `sort_order` so admin-defined ordering is stable across public reads.
- Human-facing tables use localized columns such as `name_zh`, `name_en`, `name_zht`, `title_zh`, `title_en`, `title_zht`, `summary_zh`, `summary_en`, and `summary_zht`.
- Runtime settings carry `locale_code` so localized copy blocks or per-locale overrides can coexist with global settings.
- Public DTO resolution should follow the locale fallback chain defined in the contract matrix: requested locale -> `zh-Hans` -> first non-empty canonical field.

## Asset Link Strategy

- `content_assets` is the canonical registry for all admin-managed and public-resolved media.
- Content tables store asset link columns such as `cover_asset_id`, `banner_asset_id`, `icon_asset_id`, `audio_asset_id`, or `media_asset_id`.
- Some asset relationships remain logical links only in Phase 1 to keep bootstrap order simple and migration-safe; these still receive explicit indexes.
- Asset rows reserve fields for bucket, region, object key, canonical URL, mime type, checksum/etag, and media dimensions so Phase 5 can add COS upload flows without redefining downstream schemas.

## Deferred Migration Notes

- Full mock-data backfill from `packages/client/src/services/gameMock.ts` is deferred to Phase 6.
- Phase 1 creates the table foundation and deterministic seed scaffolding only; it does not attempt to fully transform every existing mock record.
- Support/provenance metadata such as `seed_runs` may exist alongside the canonical domain tables so later migration scripts can record exactly what bootstrap data has run.
- Existing brownfield tables like `story_lines` are not deleted in Phase 1; the canonical tables above are the forward path for the live mini-program contract.

