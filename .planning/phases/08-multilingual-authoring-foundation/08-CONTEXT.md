# Phase 8: Multilingual Authoring Foundation - Context

**Gathered:** 2026-04-13
**Status:** Approved plan, ready for execution
**Source:** Milestone v2.0 requirements, Phase 7 outputs, brownfield multilingual inspection, and targeted translation integration research

<domain>
## Phase Boundary

Phase 8 establishes the four-language content model and shared authoring workflow for milestone-owned content domains.

This phase owns:
- four-locale support for `zh-Hant`, `zh-Hans`, `en`, `pt`
- Portuguese expansion of milestone-owned localized schema/entity/DTO fields
- shared multilingual field patterns in the admin UI
- translation settings and one-click translation flow with engine fallback
- locale-aware admin preview and public read alignment

This phase does not yet own:
- the full spatial-model rebuild for cities/sub-maps/POI redesign beyond locale foundations
- richer media authoring workflows beyond locale tagging and reused asset metadata
- deep chapter/activity/collection composition redesign beyond locale support
- translation memory, background bulk translation jobs, or AI-provider orchestration
</domain>

<decisions>
## Locked Decisions

- Supported locales are fixed in Phase 8 as `zh-Hant`, `zh-Hans`, `en`, and `pt`.
- Traditional Chinese remains the admin-first language, and the default primary authoring locale starts as `zh-Hant` until changed in system settings.
- Canonical multilingual storage remains explicit-column based for v2.0; Phase 8 adds Portuguese columns and shared helper abstractions instead of rewriting storage to JSON/blob localization.
- Admin/public DTOs remain brownfield-compatible; Phase 8 may add grouped helper structures, but execution must not require a full API-contract rewrite to ship.
- Translation is explicit on-demand behavior only. Saves must never depend on a translation provider being reachable.
- Translation policy is stored as typed admin/system config, not as public runtime-setting content.
- The requested `UlionTse/translators` integration must be isolated behind a replaceable admin translation adapter/bridge with engine priority and fallback.
- Public read APIs continue returning locale-resolved strings for runtime compatibility, while admin forms and previews become four-locale aware.
- Asset locale metadata can help choose localized media, but multilingual text stays on the owning domain entities.
</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Milestone and upstream decisions
- `.planning/ROADMAP.md` - Phase 8 goal, requirements, and dependency on Phase 7
- `.planning/REQUIREMENTS.md` - `LOCL-01` through `LOCL-04`
- `.planning/phases/07-admin-shell-and-real-auth-alignment/07-CONTEXT.md` - Phase 7 locked shell/auth decisions
- `.planning/phases/07-admin-shell-and-real-auth-alignment/07-01-SUMMARY.md` - current verified admin-shell baseline

### Public backend locale behavior
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/enums/LocaleCode.java` - current 3-locale enum
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/util/LocalizedContentSupport.java` - current 3-locale fallback helper
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java` - public locale-resolved read path

### Admin backend and settings shape
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/enums/LocaleCode.java` - current 3-locale enum
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/SysConfig.java` - system-config storage candidate
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminSystemManagementServiceImpl.java` - current system-config management baseline
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminContentManagementServiceImpl.java` - current content CRUD/query patterns

### Admin UI authoring surfaces
- `packages/admin/aoxiaoyou-admin-ui/src/pages/SystemManagement/index.tsx` - current locale/system-management surface
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/CityManagement.tsx` - current city authoring patterns
- `packages/admin/aoxiaoyou-admin-ui/src/pages/POIManagement/index.tsx` - current POI authoring patterns
- `packages/admin/aoxiaoyou-admin-ui/src/pages/StorylineManagement/index.tsx` - current storyline/chapter authoring patterns
- `packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts` - current explicit-locale DTO shape

### Schema foundations
- `scripts/local/mysql/init/02-live-backend-foundation.sql` - current canonical MySQL bootstrap
- `scripts/local/mysql/init/04-admin-control-plane-alignment.sql` - existing admin alignment migration patterns
- `scripts/local/mysql/init/05-admin-domain-alignment.sql` - domain alignment migration patterns

### External integration reference
- `https://github.com/UlionTse/translators` - requested translation engine source
</canonical_refs>

<specifics>
## Specific Ideas

- Add a shared locale-slot helper in both Java services so repetitive `nameZh/nameEn/nameZht/namePt` mapping does not keep spreading.
- Add a generic admin translation endpoint that returns per-locale results and per-locale failure metadata, so UI can show partial success.
- Build one shared admin multilingual field component and apply it to all Phase 8-owned forms instead of editing each locale field pattern by hand.
- Keep public runtime responses backward-compatible by resolving to a single string per field according to locale/fallback order.
</specifics>

<deferred>
## Deferred Ideas

- Translation memory, glossary management, and asynchronous bulk-translation jobs
- Full multilingual API-contract normalization around reusable nested locale objects
- Full media-library locale composition redesign
- AI-provider governance or cross-provider secret management beyond translation fallback
</deferred>

---

*Phase: 08-multilingual-authoring-foundation*
