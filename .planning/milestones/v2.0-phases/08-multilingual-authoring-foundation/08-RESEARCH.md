# Phase 8 Research: Multilingual Authoring Foundation

## Objective

Research how to implement Phase 8 so the admin can author four-language content consistently while the public backend and mini-program read the correct locale from canonical MySQL storage.

## Current Starting Point

- Phase 7 completed the admin shell/auth baseline, but multilingual authoring is still partial and inconsistent.
- Both backend locale enums currently expose only `zh-Hans`, `zh-Hant`, and `en`.
- Public locale resolution is hardcoded to a 3-locale helper in `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/util/LocalizedContentSupport.java`.
- Brownfield storage is explicit-column based, not normalized into reusable multilingual value objects:
  - examples include `name_zh`, `name_en`, `name_zht`, `description_zh`, `description_en`, `description_zht`
- Admin UI forms still expose raw per-locale fields ad hoc instead of a shared authoring pattern.
- Current schema/bootstrap SQL also only provisions the existing 3-language columns.

## What Phase 8 Must Resolve

1. Add Portuguese support end to end without destabilizing the current brownfield stack.
2. Give operators a shared multilingual field workflow instead of repeating `ZH / EN / ZHT` inputs manually per module.
3. Support one-click translation with engine fallback, but keep saves independent from third-party translator availability.
4. Align admin read/write and public read paths so locale-specific values come from the same canonical storage.

## Recommended Architecture

### 1. Keep Canonical Storage Explicit-Column Based for v2.0

Recommended decision:
- extend the current explicit-column pattern with `*_pt` fields now
- add shared service/UI abstractions on top of those columns
- do not re-platform Phase 8 into JSON/blob-based multilingual storage

Why this is the lowest-risk approach:
- current entities, MyBatis mappings, SQL bootstrap files, and admin/public DTOs already assume explicit columns
- Phase 9-13 still need to reshape maps, POIs, storylines, media, and indoor authoring; adding a storage-model rewrite here would multiply migration risk
- MySQL search/filter/sort behavior for the current brownfield forms remains straightforward with columns
- admin/public compatibility can be preserved while adding a reusable `LocalizedText` mapping layer in Java/TypeScript helpers

Recommended storage rule for Phase 8:
- domain tables remain column-based (`nameZh`, `nameEn`, `nameZht`, `namePt`)
- service helpers assemble/disassemble a shared locale-keyed view (`zh-Hant`, `zh-Hans`, `en`, `pt`)
- public responses remain locale-resolved flat strings for runtime compatibility

### 2. Split Infrastructure Settings from Operator Settings

Recommended distinction:
- infrastructure/runtime config belongs in Spring env or application config
- operator-controlled translation behavior belongs in MySQL-backed system config

Use env/application config for:
- translation bridge enablement
- bridge base URL or subprocess mode
- timeout/circuit-breaker defaults
- safe engine allowlist defaults

Use admin-managed `sys_config` entries for:
- primary authoring locale default
- translation engine priority order
- whether translate actions should overwrite existing filled locales
- UI-facing default source locale and fallback preferences

Phase 8 should not store translation governance in `app_runtime_settings`, because those rows are public runtime content and already locale-scoped. Translation policy is an operator control-plane concern, so `sys_config` is the better fit.

### 3. Introduce a Translation Adapter Boundary Instead of Coupling Forms to a Library

The user explicitly requested integration via [UlionTse/translators](https://github.com/UlionTse/translators). Research findings from the upstream project:
- the project is Python-based and exposes both `translate_text(...)` style usage and the `fanyi` CLI
- the upstream repository advertises many engines, which supports fallback sequencing
- the upstream repository is GPL-3.0 licensed, so the integration should remain replaceable and isolated

Recommended Phase 8 integration pattern:
- add an admin-backend `TranslationService` interface with provider-chain semantics
- implement a bridge client in the admin backend
- keep the actual `translators` dependency behind a lightweight Python bridge process or localhost HTTP service
- keep engine priority and fallback orchestration in Java so admin policy stays authoritative even if the underlying bridge changes later

Why a bridge boundary is preferable in this brownfield repo:
- the admin backend is Spring Boot/Java, while `translators` is Python
- a warm bridge avoids starting a fresh Python process for every translation request
- bridge isolation contains operational and legal blast radius and keeps replacement possible if the upstream library or a provider becomes unavailable
- failures can return structured partial results without blocking the form save path

Recommended translation API behavior:
- explicit on-demand translation only; never auto-translate every keystroke
- request accepts source locale, primary text, optional field payload, and target locales
- response returns per-locale text plus provider/engine metadata and failure reasons per locale when partial fallback occurs
- save endpoints never call translators implicitly

### 4. Normalize Locale Resolution Logic Across Admin and Public Read Paths

Current public helper:
- `resolveText(localeHint, zhHans, en, zhHant)` is 3-locale only

Recommended Phase 8 change:
- upgrade locale helpers to support `pt`
- centralize a deterministic fallback order
- use the same order for admin preview helpers and public runtime reads

Recommended fallback rule:
- requested locale first
- primary authoring locale second
- `zh-Hant` then `zh-Hans` then `en` then `pt` as final deterministic backup order, unless the requested locale already occupies one of those positions

This keeps public responses stable when some translated slots are still empty, while still encouraging operators to fill all four locales.

### 5. Use Shared Admin UI Authoring Patterns, Not Page-by-Page One-Off Fields

The admin needs a reusable multilingual field contract for all milestone-covered domains that already have CRUD surfaces or will be extended soon:
- cities
- POIs
- storylines
- chapters
- rewards
- stamps
- tip articles
- notifications
- runtime-setting copy fields

Recommended UI pattern:
- a shared `LocalizedFieldGroup` component bound to four locales
- primary authoring locale pinned first
- per-locale completeness indicators
- `translate missing` and `translate all from primary` actions
- inline preview and explicit failure states per locale
- no hidden auto-fill that silently overwrites human-edited content

Recommended backend/API compatibility strategy:
- keep existing DTO shapes backward-compatible during Phase 8
- add `pt` fields to DTOs/entities/requests/responses
- let the shared UI component map grouped UI state to explicit DTO fields
- defer deep API contract normalization until after the brownfield milestone domains stabilize

### 6. Keep Assets Locale-Taggable but Do Not Use Asset Locale as Text Storage

`content_assets.locale_code` is already present and should remain useful for:
- locale-specific images
- localized audio narration
- future locale-specific attachments

But multilingual text should remain stored on the owning domain entity, not in the asset table. Otherwise text lookup and public read semantics become fragmented.

## Recommended Phase Output Shape

Phase 8 should deliver:
- `LocaleCode.PT` support in both backends
- Portuguese columns in milestone-owned schema/entities/seed scripts
- shared localized-field mapping helpers
- typed translation settings in admin system config
- a generic admin translation endpoint/service with fallback sequencing
- shared admin UI multilingual field components
- locale-aware admin preview and public read alignment for `zh-Hant`, `zh-Hans`, `en`, `pt`

Phase 8 should not yet deliver:
- a generalized multilingual JSON schema rewrite
- background translation queues or translation memory
- full content redesign for future modules that are still placeholders
- AI-platform governance or non-translation provider orchestration

## Key Risks and Mitigations

### Translation Service Instability

Risk:
- third-party translator engines can throttle, block, or change behavior

Mitigation:
- explicit engine priority/fallback
- bounded per-engine timeout
- partial-result response model
- manual save path always independent of machine translation

### Brownfield DTO Explosion

Risk:
- adding Portuguese naively could create repetitive mapping drift across admin/public code

Mitigation:
- introduce shared locale-slot helpers and typed UI components immediately
- keep storage explicit, but stop hand-writing locale mapping logic page by page

### Draft Content Leakage to Third Parties

Risk:
- operator-authored unpublished text may be sent to external translation providers

Mitigation:
- translate only on explicit operator action
- show provider usage clearly in UI
- audit translation calls
- keep provider credentials and bridge config outside tracked files

### Licensing and Replaceability

Risk:
- the requested `translators` project is GPL-3.0 and Python-based

Mitigation:
- isolate it behind a replaceable adapter/bridge
- keep admin/public contracts provider-agnostic
- treat the bridge as infrastructure, not as an inlined domain dependency

## Recommendation Summary

- Use explicit-column multilingual storage for Phase 8 and add Portuguese now.
- Put reusable abstractions at the service/UI layers instead of rewriting the storage model.
- Store translation policy in admin system config, not public runtime settings.
- Integrate `translators` through a replaceable admin translation bridge with engine fallback and strict timeouts.
- Make admin write paths four-locale aware, while keeping public read responses locale-resolved and backward-compatible.

## Validation Architecture

- Backend verification should add targeted JUnit coverage in both Java services for:
  - `LocaleCode` including `pt`
  - locale fallback resolution including partially filled content
  - translation settings persistence and typed parsing
  - translation adapter fallback behavior with mocked bridge failures
- Admin UI verification should add at least build-level proof plus targeted component tests if a test runner is introduced during execution.
- End-to-end smoke for this phase should prove:
  - operator saves four-language content
  - translate action can partially succeed without blocking save
  - admin preview returns requested locale
  - public read APIs return the expected locale/fallback value
  - mini-program can request and render Portuguese content without schema or runtime errors

## Sources

### Local codebase references

- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/enums/LocaleCode.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/enums/LocaleCode.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/util/LocalizedContentSupport.java`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/SystemManagement/index.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts`
- `scripts/local/mysql/init/02-live-backend-foundation.sql`

### External references

- [UlionTse/translators repository](https://github.com/UlionTse/translators)
- [UlionTse/translators README (raw)](https://raw.githubusercontent.com/UlionTse/translators/master/README.md)
