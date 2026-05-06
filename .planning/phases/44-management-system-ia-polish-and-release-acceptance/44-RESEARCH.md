# Phase 44: Management-System IA Polish and Release Acceptance - Research

**Researched:** 2026-05-04 [VERIFIED: system date]  
**Domain:** Brownfield React/Ant Design admin IA polish, media-detail UX, and release acceptance evidence [VERIFIED: .planning/ROADMAP.md]  
**Confidence:** HIGH for codebase scope and local tooling; MEDIUM for manual WeChat/device execution because the device step was not run during research [VERIFIED: .planning/phases/41-wechat-runtime-uat-harness-and-story-entry-hardening/41-VERIFICATION.md]

## User Constraints

`44-CONTEXT.md` exists for execution planning and resolves the post-research IA/evidence decisions. This research file was produced before that context artifact was finalized, so downstream agents must treat `44-CONTEXT.md` as the higher-precedence Phase 44 decision source. [RESOLVED]

Project-level constraints that apply to this phase: preserve the brownfield Taro/React mini-program, Spring Boot/MyBatis-Plus backends, existing admin UI, local Docker MySQL/Mongo setup, `/admin` as the authoritative control plane, MySQL as primary operational storage, backend-mediated COS uploads, externalized secrets, UTF-8/utf8mb4 handling, and real local verification against services. [VERIFIED: AGENTS.md]

Dirty-worktree constraint: several Phase 44 target files are already modified, including `MediaAssetDetailDrawer.tsx`, the Experience pages, `StorylineModeWorkbench/index.tsx`, `ExperienceWorkbench.css`, and `.planning/config.json`; planners must preserve those changes and avoid revert-style cleanup. [VERIFIED: git status]

<phase_requirements>

## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| UAT-02 | Operator can run a documented flagship story smoke on a real device or DevTools simulator and capture exact pass/fail evidence. [VERIFIED: .planning/REQUIREMENTS.md] | Reuse Phase 41/42/43 smoke artifacts, then add a Phase 44 evidence report that explicitly records DevTools/device status and screenshots or manual notes. [VERIFIED: 41-UAT.md, 42-UAT.md, 43-UAT.md] |
| UAT-03 | Operator can distinguish automated smoke, DevTools checks, physical-device checks, and accepted caveats in one release-readiness report. [VERIFIED: .planning/REQUIREMENTS.md] | Final report must split sections by evidence class and keep physical-device UAT as PENDING unless actually executed. [VERIFIED: 41-VERIFICATION.md, 42-VERIFICATION.md, 43-VERIFICATION.md] |
| ADMIN-01 | Operator-facing navigation has no misleading shell-only entries, duplicate missions, or wrong redirects for v3.2-owned workflows. [VERIFIED: .planning/REQUIREMENTS.md] | Audit `App.tsx` and `DefaultLayout.tsx`; current source still has placeholder routes and legacy redirects that need a deliberate keep/remove/merge decision. [VERIFIED: packages/admin/aoxiaoyou-admin-ui/src/App.tsx, packages/admin/aoxiaoyou-admin-ui/src/layouts/DefaultLayout.tsx] |
| ADMIN-02 | Story/gameplay operations pages have stable layout, readable columns, responsive panels, and Traditional Chinese labels. [VERIFIED: .planning/REQUIREMENTS.md] | Focus on Ant Design `Table` `scroll`, column `width`/`ellipsis`, `Empty`, responsive `Row`/`Col`, and existing CSS classes. [CITED: https://github.com/ant-design/ant-design/blob/master/components/table/index.en-US.md] |
| ADMIN-03 | Media/material/detail drawers open without broken previews, URL overflow, or dead package-version links. [VERIFIED: .planning/REQUIREMENTS.md] | Reuse existing `MediaAssetPreview`, `MediaAssetMeta`, and material package `VersionAssetPreview` patterns; add drawer truncation and unavailable states where missing. [VERIFIED: MediaAssetPreview.tsx, MediaAssetDetailDrawer.tsx, StoryMaterialPackageManagement.tsx] |
| ADMIN-04 | Interaction templates, governance checks, and runtime-state actions include concise inline explanations and avoid JSON-first workflows. [VERIFIED: .planning/REQUIREMENTS.md] | Existing pages already use structured fields plus folded JSON; plan should make JSON advanced-only and add operator-readable explanations for actions and check results. [VERIFIED: ExperienceTemplateLibrary.tsx, ExperienceGovernanceCenter.tsx, UserProgressWorkbench.tsx] |

</phase_requirements>

## Summary

Phase 44 is a brownfield admin UI and acceptance-evidence phase, not a new platform or backend architecture phase. [VERIFIED: .planning/ROADMAP.md] The implementation should stay inside the existing React 18, Vite, Ant Design 5, React Router 7, and Spring-backed admin/public API surfaces unless a specific acceptance check exposes a backend defect. [VERIFIED: packages/admin/aoxiaoyou-admin-ui/package.json, packages/admin/aoxiaoyou-admin-ui/src/services/api.ts]

The highest-risk planning issue is not component choice; it is evidence truthfulness. [VERIFIED: 41-VERIFICATION.md, 42-VERIFICATION.md, 43-VERIFICATION.md] Phases 41-43 have automated backend/build/admin smoke PASS evidence, but each verification document still marks WeChat DevTools or physical-device journey UAT as pending where it was not performed. [VERIFIED: 41-VERIFICATION.md, 42-VERIFICATION.md, 43-VERIFICATION.md]

**Primary recommendation:** Plan a small set of UI polish tasks plus one acceptance-report task; use the installed Ant Design patterns for tables/drawers/empty states, and make the final report explicitly separate automated smoke, browser/admin checks, DevTools checks, physical-device checks, caveats, and future gameplay scope. [VERIFIED: .planning/ROADMAP.md] [CITED: https://github.com/ant-design/ant-design/blob/master/components/table/index.en-US.md]

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Admin IA route/sidebar cleanup | Browser / Client | API / Backend | Route definitions and sidebar labels live in `App.tsx` and `DefaultLayout.tsx`; backend should not own menu presentation. [VERIFIED: App.tsx, DefaultLayout.tsx] |
| Runtime operations page readability | Browser / Client | API / Backend | Layout, columns, labels, empty states, and inline explanations are React/Ant Design concerns; API data shape only matters if fields are missing. [VERIFIED: ExperienceOrchestrationWorkbench.tsx, UserProgressWorkbench.tsx] |
| Media/material preview reliability | Browser / Client | CDN / Static, API / Backend | The drawer decides preview/fallback/truncation; `canonicalUrl` and COS-backed availability originate from backend/media storage. [VERIFIED: MediaAssetDetailDrawer.tsx, MediaAssetPreview.tsx] |
| Interaction/governance explanations | Browser / Client | API / Backend | Operator wording and JSON-folding belong in admin UI; governance findings are retrieved from admin APIs. [VERIFIED: ExperienceTemplateLibrary.tsx, ExperienceGovernanceCenter.tsx] |
| Final release acceptance evidence | Documentation / Planning | Browser / Client, Mini-program Client, API / Backend | The evidence file must collect results from admin browser checks, smoke scripts, and WeChat tooling/device runs without changing runtime ownership. [VERIFIED: 41-UAT.md, 42-UAT.md, 43-UAT.md] |

## Project Constraints (from AGENTS.md)

- Keep `/admin` as the authoritative write/control surface for content, settings, and assets consumed by the mini-program. [VERIFIED: AGENTS.md]
- Keep secrets in local environment/runtime config and out of tracked files and planning docs. [VERIFIED: AGENTS.md]
- Keep multilingual content and scripted writes UTF-8/utf8mb4; avoid Chinese text through inline PowerShell literals when file-based UTF-8 input is safer. [VERIFIED: AGENTS.md]
- Verify runnable interfaces locally against real services before claiming completion. [VERIFIED: AGENTS.md]
- Preserve dirty worktree changes not made by the current executor. [VERIFIED: AGENTS.md, git status]
- No `CLAUDE.md` exists in the repo root, so there are no additional CLAUDE.md-specific directives. [VERIFIED: filesystem]

## Standard Stack

### Core

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| React | 18.3.1 installed | Admin SPA component runtime. [VERIFIED: npm ls] | Existing admin UI is React functional components with hooks. [VERIFIED: package.json, source reads] |
| Ant Design | 5.29.3 installed; 6.3.7 latest on npm as of research | Tables, drawers, alerts, empty states, typography, image preview. [VERIFIED: npm ls, npm view] | Current codebase already uses Ant Design 5 broadly; do not upgrade major version in Phase 44. [VERIFIED: package.json] |
| @ant-design/pro-components | 2.8.10 installed/latest | `PageContainer` and ProTable-style admin pages. [VERIFIED: npm ls, npm view] | Existing pages use `PageContainer` and ProTable request patterns. [VERIFIED: UserManagement.tsx] |
| react-router-dom | 7.14.0 installed; 7.14.2 latest on npm as of research | Hash-routed admin routes, `Link`, `Navigate`, `Outlet`. [VERIFIED: npm ls, npm view] | Existing route graph is explicit in `App.tsx` and sidebar links use `Link`. [VERIFIED: App.tsx, DefaultLayout.tsx] |
| Vite | 6.4.2 installed; 8.0.10 latest on npm as of research | Admin UI dev/build/preview tooling. [VERIFIED: npm ls, npm view] | Existing scripts are `vite`, `tsc && vite build`, and `vite preview`; major upgrade is out of scope. [VERIFIED: package.json] |
| TypeScript | 5.9.3 installed; 6.0.3 latest on npm as of research | Type checking. [VERIFIED: npm ls, npm view] | Existing `npm run type-check` runs `tsc --noEmit`. [VERIFIED: package.json] |

### Supporting

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| @ant-design/icons | 6.0.0 declared | Existing admin button/menu icons. [VERIFIED: package.json] | Use existing icon set for navigation clarity and media states. [VERIFIED: DefaultLayout.tsx, MediaAssetPreview.tsx] |
| dayjs | 1.11.13 declared | Date/time formatting in admin UI. [VERIFIED: package.json] | Use where existing admin pages already depend on Dayjs inputs. [VERIFIED: UserProgressWorkbench.tsx] |
| PowerShell smoke scripts | Existing local scripts | Acceptance automation for Phases 41-43 and future Phase 44 report generation. [VERIFIED: scripts/local] | Reuse rather than inventing a separate runner. [VERIFIED: scripts/local/smoke-phase-41-wechat-runtime-uat.ps1, smoke-phase-42-gameplay-event-engine.ps1, smoke-phase-43-traveler-ops.ps1] |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Existing Ant Design tables/drawers | New grid/detail library | Do not add; Phase 44 is polish over existing Ant Design pages, and a new UI dependency would raise regression risk. [VERIFIED: package.json, source reads] |
| Current route graph | File-system routing | Do not add; routes are explicit today, and React Router supports current `Link`/`Navigate` approach. [VERIFIED: App.tsx] [CITED: https://github.com/remix-run/react-router/blob/main/docs/api/components/Link.md] |
| Existing smoke scripts | New e2e framework | Do not add unless browser automation is already available; admin UI has no source test files today. [VERIFIED: filesystem] |

**Installation:** none expected for Phase 44 if using the installed stack. [VERIFIED: package-lock.json, npm ls]

```bash
cd packages/admin/aoxiaoyou-admin-ui
npm run type-check
npm run build
```

**Version verification:** `npm view` and `npm ls` were run for `antd`, `@ant-design/pro-components`, `react-router-dom`, `vite`, `typescript`, and `@vitejs/plugin-react-swc`. [VERIFIED: npm registry, npm ls]

## Architecture Patterns

### System Architecture Diagram

```text
Operator request /admin route
  -> HashRouter route match in App.tsx
  -> ProtectedRoute token bootstrap
  -> DefaultLayout sidebar selection/openKeys
  -> Page component
      -> Structured filters/forms/tables/drawers
      -> services/api.ts admin/public endpoint calls
      -> Ant Design loading/empty/error/preview states
  -> Browser-admin evidence
      -> type-check/build smoke
      -> API smoke outputs
      -> DevTools/device manual notes
      -> Phase 44 acceptance report
```

This flow matches the existing admin SPA shape: `App.tsx` owns routes, `DefaultLayout.tsx` owns sidebar presentation, and page components call centralized API helpers. [VERIFIED: App.tsx, DefaultLayout.tsx, services/api.ts]

### Recommended Project Structure

```text
packages/admin/aoxiaoyou-admin-ui/src/
├── App.tsx                         # route graph and redirects
├── layouts/DefaultLayout.tsx       # sidebar/menu IA labels
├── components/media/               # shared media preview/detail behavior
├── pages/Experience/               # templates, flows, governance polish
├── pages/StorylineModeWorkbench/   # story-mode runtime operator polish
└── pages/UserManagement/           # progress/reward support action wording

scripts/local/
└── smoke-phase-44-*.ps1            # only if a new report collector is needed

.planning/phases/44-.../
├── 44-RESEARCH.md
├── 44-UAT.md                       # final release evidence
└── 44-VERIFICATION.md              # requirement-to-evidence summary
```

The structure above extends current ownership boundaries and avoids a new shared framework. [VERIFIED: filesystem, source reads]

### Pattern 1: Keep IA Source Of Truth In Route/Menu Pair

**What:** Every visible sidebar item should map to a real route, and every retained redirect should be intentional compatibility behavior rather than a visible operator entry. [VERIFIED: App.tsx, DefaultLayout.tsx]

**When to use:** ADMIN-01 navigation cleanup. [VERIFIED: .planning/REQUIREMENTS.md]

**Example:**

```tsx
// Source: existing React Router pattern in App.tsx and DefaultLayout.tsx
<Route path="users/story-progress" element={<Navigate to="/users/progress" replace />} />
<Link to="/users/progress">旅客進度與獎勵支援</Link>
```

React Router `Link` renders an anchor for client-side routing, and `replace` avoids pushing an extra history entry when used for redirect-style navigation. [CITED: https://github.com/remix-run/react-router/blob/main/docs/api/components/Link.md]

### Pattern 2: Fix Tables With Width, Scroll, Ellipsis, Empty Text

**What:** Wide operational tables should define stable column widths, `scroll.x`, optional fixed action columns, and Ant Design ellipsis/tooltip for long fields. [CITED: https://github.com/ant-design/ant-design/blob/master/components/table/index.en-US.md]

**When to use:** ADMIN-02 and ADMIN-03 table/detail polish. [VERIFIED: .planning/REQUIREMENTS.md]

**Example:**

```tsx
// Source: Ant Design Table docs + existing page patterns
<Table
  rowKey="id"
  columns={[
    { title: '素材', dataIndex: 'itemKey', width: 220, ellipsis: { showTitle: false } },
    { title: '操作', fixed: 'right', width: 160, render: renderActions },
  ]}
  locale={{ emptyText: '暫無可顯示資料' }}
  scroll={{ x: 980 }}
/>
```

Ant Design documents `width`, `ellipsis`, `responsive`, `fixed`, and `scroll.x` as table column/layout controls. [CITED: https://github.com/ant-design/ant-design/blob/master/components/table/index.en-US.md]

### Pattern 3: Treat Media Preview As Health-State UI

**What:** A valid asset with `canonicalUrl` gets an image/audio/video preview; Lottie/JSON and unavailable assets get explicit tags and explanatory fallback text. [VERIFIED: MediaAssetPreview.tsx, MediaAssetDetailDrawer.tsx]

**When to use:** ADMIN-03 media/material/detail drawer acceptance. [VERIFIED: .planning/REQUIREMENTS.md]

**Example:**

```tsx
// Source: existing MediaAssetPreview.tsx pattern
const healthLabel = !asset.canonicalUrl
  ? '無公開連結'
  : imageFailed
    ? '預覽載入失敗'
    : isLottieAsset(asset)
      ? 'JSON 動畫'
      : '不可直接預覽';
```

Ant Design `Image` supports fallback behavior and an `onError` handler, so preview failure should be a UI state rather than a broken image. [CITED: https://github.com/ant-design/ant-design/blob/master/components/image/index.zh-CN.md]

### Pattern 4: Structured First, JSON Advanced Only

**What:** Operators should use selects, switches, tags, descriptions, and safe preset buttons first; raw JSON should stay folded under an advanced section. [VERIFIED: ExperienceTemplateLibrary.tsx, StorylineModeWorkbench/index.tsx]

**When to use:** ADMIN-04 interaction templates, governance checks, and support actions. [VERIFIED: .planning/REQUIREMENTS.md]

**Example:**

```tsx
// Source: existing StorylineModeWorkbench.tsx pattern
<Alert
  type="info"
  showIcon
  message="進階 JSON 只作 fallback"
  description="結構化欄位會自動編譯成 schemaVersion: 1 JSON；只有需要精修 runtime payload 時才開啟。"
/>
```

### Anti-Patterns to Avoid

- **Visible placeholder-as-product:** Sidebar entries backed only by `ModulePlaceholder` fail ADMIN-01 when they imply a v3.2-owned workflow is usable. [VERIFIED: App.tsx, ModulePlaceholder.tsx]
- **JSON-first operations:** Showing raw payloads as the main operator path fails ADMIN-04; keep JSON as detail/advanced evidence. [VERIFIED: ExperienceTemplateLibrary.tsx, UserProgressWorkbench.tsx]
- **Dead external links:** Do not render links for missing `canonicalUrl` or missing material package versions. [VERIFIED: MediaAssetDetailDrawer.tsx, StoryMaterialPackageManagement.tsx]
- **Evidence conflation:** Do not label automated smoke as physical-device UAT. [VERIFIED: 41-VERIFICATION.md, 42-VERIFICATION.md, 43-VERIFICATION.md]

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Admin tables | Custom table layout engine | Ant Design `Table`/ProTable | Existing stack supports scroll, ellipsis, fixed columns, pagination, search, and empty text. [CITED: Ant Design docs, ProComponents docs] |
| Route navigation | Custom history/router state | React Router `Routes`, `Link`, `Navigate`, `Outlet` | Existing admin uses React Router; `Link` and `Navigate` already cover links and redirect compatibility. [VERIFIED: App.tsx] [CITED: React Router docs] |
| Media preview state | Custom canvas/player for all asset types | Existing `MediaAssetPreview`, HTML `audio`/`video`, Ant Design `Image` | Current component already detects image/audio/video/Lottie and supports fallback tags. [VERIFIED: MediaAssetPreview.tsx] |
| Acceptance reporting | Freeform mixed notes | Structured `44-UAT.md` and `44-VERIFICATION.md` sections | Prior phases already use evidence tables and explicit manual-UAT status. [VERIFIED: 41-UAT.md, 43-VERIFICATION.md] |
| Security validation taxonomy | Custom control taxonomy | OWASP ASVS categories from config-enforced security domain | Project config enables ASVS Level 1 security enforcement. [VERIFIED: .planning/config.json] [CITED: https://owasp.org/www-project-application-security-verification-standard/] |

**Key insight:** The phase succeeds by removing ambiguity, not by adding new capabilities; custom infrastructure would increase the chance of misleading acceptance evidence. [VERIFIED: .planning/ROADMAP.md]

## Runtime State Inventory

| Category | Items Found | Action Required |
|----------|-------------|------------------|
| Stored data | MySQL stores content assets/material package versions and runtime/admin records, but Phase 44 route/label polish does not require a data migration unless a UI bug reveals links to nonexistent version rows. [VERIFIED: AGENTS.md, StoryMaterialPackageManagement.tsx] | Code edit for UI link guarding; data migration only if browser/API evidence proves stale package-version records are being linked. [VERIFIED: source reads] |
| Live service config | None verified in git for admin IA labels; live COS URLs and material availability come from backend data and external storage. [VERIFIED: MediaAssetDetailDrawer.tsx, AGENTS.md] | Do not mutate COS or provider config in Phase 44; verify unavailable assets are marked clearly. [VERIFIED: AGENTS.md] |
| OS-registered state | WeChat DevTools CLI was previously used successfully in Phase 41, but `wechat-devtools-cli` is not currently on PATH in this research shell. [VERIFIED: 41-UAT.md, Get-Command] | Planner should include manual DevTools/device step with fallback instructions and not block automated UI polish on missing PATH command. [VERIFIED: environment audit] |
| Secrets/env vars | COS/provider/admin secrets must remain outside tracked files; admin UI has `.env.local` and `.env.production` present locally. [VERIFIED: AGENTS.md, filesystem] | Do not copy secret values into evidence. Redact tokens in smoke reports, matching Phase 43 behavior. [VERIFIED: 43-UAT.md] |
| Build artifacts | `packages/admin/aoxiaoyou-admin-ui/dist`, `node_modules`, and Vite logs exist; generated outputs can mask stale route behavior if not rebuilt. [VERIFIED: filesystem] | Run `npm run type-check` and `npm run build`; use built or dev-served admin for browser checks, not stale `dist`. [VERIFIED: package.json] |

## Common Pitfalls

### Pitfall 1: Removing Routes Without Checking Compatibility

**What goes wrong:** Visible duplicates are removed, but deep links or redirects used by prior documentation break unexpectedly. [VERIFIED: App.tsx, DefaultLayout.tsx]  
**Why it happens:** `resolveSelectedKey` contains route keys not all visible in the menu, and `App.tsx` contains redirects such as `/collection/rewards`, `/collection/badges`, and `/users/story-progress`. [VERIFIED: DefaultLayout.tsx, App.tsx]  
**How to avoid:** Separate "visible menu entry" cleanup from "compatibility redirect" cleanup. [VERIFIED: source reads]  
**Warning signs:** Sidebar selection points to a removed key, or browser back lands on a placeholder/redirect loop. [ASSUMED]

### Pitfall 2: Table Scroll Fixes That Hide Important Actions

**What goes wrong:** Horizontal scroll makes content readable but action buttons disappear or overflow on smaller screens. [CITED: Ant Design Table docs]  
**Why it happens:** Fixed columns require `scroll.x`, and long Traditional Chinese labels need predictable column widths. [CITED: https://github.com/ant-design/ant-design/blob/master/components/table/index.en-US.md]  
**How to avoid:** Fix action columns where needed, set explicit widths, and use ellipsis/tooltips for codes/URLs. [CITED: Ant Design Table docs]  
**Warning signs:** Buttons wrap into tall rows, URL columns push drawers wider than viewport, or table cards resize while loading. [ASSUMED]

### Pitfall 3: Linkifying Missing Assets

**What goes wrong:** Operators click a link for an asset or package version that has no `canonicalUrl` or no valid version. [VERIFIED: MediaAssetDetailDrawer.tsx, StoryMaterialPackageManagement.tsx]  
**Why it happens:** Detail drawers render external links if URL fields are truthy, but long or stale paths can still look usable. [VERIFIED: MediaAssetDetailDrawer.tsx]  
**How to avoid:** Gate all external links on HTTP URL validity, truncate paths with tooltip, and show explicit unavailable tags. [VERIFIED: StoryMaterialPackageManagement.tsx]  
**Warning signs:** `objectKey` exists but `canonicalUrl` is empty, or a version row displays file/path text without a valid public URL. [VERIFIED: MediaAssetPreview.tsx, StoryMaterialPackageManagement.tsx]

### Pitfall 4: JSON Detail Becoming The Workflow

**What goes wrong:** Pages technically expose data but remain unusable because the operator must read payload JSON to decide what to do. [VERIFIED: UserProgressWorkbench.tsx]  
**Why it happens:** Runtime state and governance are complex, and JSON previews are easy to expose. [VERIFIED: ExperienceGovernanceCenter.tsx, UserProgressWorkbench.tsx]  
**How to avoid:** Put concise action explanations next to controls; keep JSON as "view details" or advanced fallback. [VERIFIED: ExperienceTemplateLibrary.tsx, StorylineModeWorkbench/index.tsx]  
**Warning signs:** Primary button labels mention payloads, or important status appears only in modal JSON. [ASSUMED]

### Pitfall 5: Overclaiming Release Acceptance

**What goes wrong:** The final v3.2 report says PASS while physical-device or DevTools checks were not executed. [VERIFIED: 41-VERIFICATION.md, 42-VERIFICATION.md, 43-VERIFICATION.md]  
**Why it happens:** Automated smoke outputs are already green, so manual evidence can be accidentally treated as implied. [VERIFIED: 41-UAT.md, 42-UAT.md, 43-UAT.md]  
**How to avoid:** Use separate status rows for automated smoke, browser/admin checks, DevTools checks, physical-device checks, caveats, and future scope. [VERIFIED: .planning/ROADMAP.md]  
**Warning signs:** Report contains one final PASS without a manual UAT status table. [VERIFIED: prior verification pattern]

## Code Examples

Verified patterns from official and local sources:

### Stable Admin Table

```tsx
// Source: Ant Design Table docs + local tables
<Table
  rowKey="id"
  loading={loading}
  columns={columns}
  dataSource={items}
  pagination={{ pageSize: 10 }}
  locale={{ emptyText: '暫無資料' }}
  scroll={{ x: 1120 }}
/>
```

Ant Design `scroll.x` supports numeric, percent, `true`, and `max-content` values for horizontal table scrolling. [CITED: https://github.com/ant-design/ant-design/blob/master/components/table/index.en-US.md]

### Safe Media Link

```tsx
// Source: local StoryMaterialPackageManagement.tsx pattern
function isHttpUrl(value?: string | null) {
  return /^https?:\/\//i.test(value || '');
}

{isHttpUrl(asset.canonicalUrl) ? (
  <Typography.Link href={asset.canonicalUrl} target="_blank" rel="noreferrer">
    在新視窗開啟
  </Typography.Link>
) : (
  <Tag color="orange">無公開連結</Tag>
)}
```

### Acceptance Evidence Table

```markdown
| Evidence Class | Check | Status | Evidence | Command / Human Note |
| --- | --- | --- | --- | --- |
| automated smoke | Phase 41-43 smoke replay | PASS/FAIL | file links | command |
| browser/admin | sidebar + drawer check | PASS/FAIL | screenshot/note | browser route |
| WeChat DevTools | simulator journey | PASS/FAIL/PENDING | note | DevTools version |
| physical device | real device journey | PASS/FAIL/PENDING | note | tester/device/date |
| caveat | accepted limitation | ACCEPTED | reason | future requirement |
```

This mirrors prior UAT evidence tables while adding the requirement-specific evidence separation from UAT-03. [VERIFIED: 41-UAT.md, .planning/REQUIREMENTS.md]

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Treating admin pages as CRUD shells | Operator workbenches with explanations, previews, support actions, and audit evidence | Established across Phases 28-43 in this repo [VERIFIED: ROADMAP.md, 43-VERIFICATION.md] | Phase 44 should polish workflows, not add placeholder modules. [VERIFIED: ROADMAP.md] |
| Raw JSON as primary config UI | Structured controls with JSON folded under advanced sections | Already present in Experience and Storyline pages [VERIFIED: ExperienceTemplateLibrary.tsx, StorylineModeWorkbench/index.tsx] | ADMIN-04 should complete this pattern consistently. [VERIFIED: REQUIREMENTS.md] |
| Manual UAT caveat hidden in summary | Explicit evidence-class separation | Required by UAT-03 [VERIFIED: REQUIREMENTS.md] | Final release report must be truthful about missing device checks. [VERIFIED: 41-VERIFICATION.md] |
| Ant Design 5 as latest major | Ant Design 6 exists on npm, but repo uses installed Ant Design 5 | AntD 6.3.7 latest as of research [VERIFIED: npm view] | Do not upgrade for Phase 44; use installed AntD 5.29.3 patterns. [VERIFIED: npm ls] |
| Vite 6 as repo build line | Vite 8 exists on npm, but repo uses installed Vite 6.4.2 | Vite 8.0.10 latest as of research [VERIFIED: npm view] | Do not upgrade build tooling during release polish. [VERIFIED: npm ls] |

**Deprecated/outdated:**
- `PageContainer` title `"v3.0 體驗編排系統"` is stale for v3.2 release acceptance and should be renamed to current operator wording. [VERIFIED: ExperienceOrchestrationWorkbench.tsx]
- Comment text saying governance is left to "Phase 31" is stale in the current v3.2 context. [VERIFIED: StorylineModeWorkbench/index.tsx]
- Visible `ModulePlaceholder` entries are incompatible with ADMIN-01 for v3.2-owned workflows unless intentionally marked future/out-of-scope and removed from the main operator path. [VERIFIED: ModulePlaceholder.tsx, REQUIREMENTS.md]

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | Browser visual checks may require manual execution rather than an existing automated e2e suite. | Validation Architecture | Planner might under-allocate manual QA time. |
| A2 | Table/button wrapping warning signs are inferred from UI behavior, not captured by an automated screenshot in this research pass. | Common Pitfalls | Planner may need browser screenshots to confirm exact layout issues. |
| A3 | Removing visible placeholder entries is preferable to leaving them in the v3.2 operator path. | Common Pitfalls | If user wants future-scope placeholders visible, planner must keep them but clearly mark them as future. |

## Open Questions (RESOLVED)

1. **Will Phase 44 include actual physical-device UAT?** [VERIFIED: 41-VERIFICATION.md]
   - What we know: Prior verification marks physical-device UAT as pending. [VERIFIED: 41-VERIFICATION.md, 42-VERIFICATION.md, 43-VERIFICATION.md]
   - Decision: Phase 44 tooling must support PASS, FAIL, BLOCKED, or PENDING for physical-device checks, but execution may not mark physical-device UAT as PASS unless actual evidence is supplied. UI polish can complete without a device; final release acceptance remains BLOCKED for UAT-02 if neither DevTools nor physical-device flagship story smoke evidence is supplied. [RESOLVED]

2. **Which placeholder entries are considered v3.2-owned workflows?** [VERIFIED: ROADMAP.md]
   - What we know: `map-tiles`, `campaigns`, `ops/sandbox`, and `system/audit` route to `ModulePlaceholder`. [VERIFIED: App.tsx]
   - Decision: Keep future-scope placeholders visible only when their page clearly says they are future/out-of-scope and never use them as v3.2 acceptance paths. Do not rename the locked story/content workflow labels just to reduce overlap; instead add in-page mission copy and hide only low-level implementation routes such as experience bindings/overrides from the visible sidebar. [RESOLVED]

3. **Should Phase 44 add a new smoke script?** [VERIFIED: scripts/local]
   - What we know: Scripts exist for Phases 41-43; no Phase 44 smoke script exists. [VERIFIED: scripts/local]
   - Decision: Add a lightweight Phase 44 collector script. It must reuse existing smoke/build commands, redact secrets, read UTF-8 expectation data from files or built-in ASCII-safe markers where possible, and produce a report that cannot overclaim manual WeChat/browser evidence. [RESOLVED]

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Node.js | Admin UI type-check/build | Yes [VERIFIED: command probe] | 25.9.0 | Use installed Node; repo engine says `>=20`. [VERIFIED: package.json] |
| npm | Admin UI scripts | Yes [VERIFIED: command probe] | 11.13.0 | Use installed npm; package metadata says npm `>=10`. [VERIFIED: package.json] |
| Java | Backend smoke/compile if needed | Yes [VERIFIED: command probe] | 17.0.12 | None needed for UI-only tasks. [VERIFIED: command probe] |
| Maven | Backend compile/smoke if replayed | Yes [VERIFIED: command probe] | 3.8.8 | Use prior smoke evidence if services are already verified, but final report should state if not rerun. [VERIFIED: 43-VERIFICATION.md] |
| Docker | Local MySQL/Mongo stack | Yes [VERIFIED: command probe] | 27.1.1 | Existing services may already be running; otherwise use `docker-compose.local.yml`. [VERIFIED: AGENTS.md] |
| WeChat DevTools CLI | UAT-02 DevTools open/check | Not on PATH in research shell [VERIFIED: Get-Command] | — | Manual DevTools launch or restore CLI PATH; mark DevTools/device status honestly. [VERIFIED: 41-UAT.md] |
| Browser | Admin visual checks | Not probed in research pass [ASSUMED] | — | Manual browser check with Vite dev/preview. [CITED: https://github.com/vitejs/vite/blob/main/docs/guide/cli.md] |

**Missing dependencies with no fallback:** none for admin UI code polish. [VERIFIED: environment audit]

**Missing dependencies with fallback:** WeChat DevTools CLI is not on PATH; use manual DevTools launch or record PENDING. [VERIFIED: Get-Command, 41-UAT.md]

## Validation Architecture

### Test Framework

| Property | Value |
|----------|-------|
| Framework | No admin UI source test framework detected; validation is TypeScript, Vite build, smoke scripts, and manual/browser checks. [VERIFIED: filesystem, package.json] |
| Config file | `packages/admin/aoxiaoyou-admin-ui/tsconfig.json`; `vite.config.ts`. [VERIFIED: filesystem] |
| Quick run command | `cd packages/admin/aoxiaoyou-admin-ui; npm run type-check` [VERIFIED: package.json] |
| Full suite command | `cd packages/admin/aoxiaoyou-admin-ui; npm run build`; plus relevant Phase 41-43 smoke replay if services are available. [VERIFIED: package.json, scripts/local] |

### Phase Requirements -> Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| UAT-02 | Flagship story smoke on DevTools/device with exact evidence. [VERIFIED: REQUIREMENTS.md] | manual/smoke | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-41-wechat-runtime-uat.ps1 -IncludeBuild -OpenDevTools` if CLI available. [VERIFIED: 41-VERIFICATION.md] | Existing Phase 41 script exists; Phase 44 report file not yet created. [VERIFIED: scripts/local, filesystem] |
| UAT-03 | Final report separates evidence classes. [VERIFIED: REQUIREMENTS.md] | documentation verification | Manual inspection of `44-UAT.md` and `44-VERIFICATION.md`. [ASSUMED] | `44-UAT.md` absent. [VERIFIED: filesystem] |
| ADMIN-01 | No misleading placeholder/duplicate/wrong route entries. [VERIFIED: REQUIREMENTS.md] | browser/static | `npm run type-check`; browser route/sidebar clickthrough. [VERIFIED: package.json] | Source exists; no automated route test exists. [VERIFIED: filesystem] |
| ADMIN-02 | Operations pages readable/responsive/Traditional Chinese. [VERIFIED: REQUIREMENTS.md] | browser/static | `npm run build`; browser at admin routes. [VERIFIED: package.json] | Source exists; no screenshot test exists. [VERIFIED: filesystem] |
| ADMIN-03 | Media/material drawers have valid previews and unavailable/truncated states. [VERIFIED: REQUIREMENTS.md] | browser/manual | `npm run type-check`; browser drawer checks with assets. [VERIFIED: package.json] | Source exists. [VERIFIED: MediaAssetDetailDrawer.tsx] |
| ADMIN-04 | Explanations exist and JSON is advanced-only. [VERIFIED: REQUIREMENTS.md] | browser/manual | `npm run type-check`; inspect Experience/Governance/Support actions. [VERIFIED: package.json] | Source exists. [VERIFIED: ExperienceTemplateLibrary.tsx, UserProgressWorkbench.tsx] |

### Sampling Rate

- **Per task commit:** `cd packages/admin/aoxiaoyou-admin-ui; npm run type-check` [VERIFIED: package.json]
- **Per wave merge:** `cd packages/admin/aoxiaoyou-admin-ui; npm run build` [VERIFIED: package.json]
- **Phase gate:** Type-check/build green, browser/admin route checks recorded, smoke evidence copied or rerun, DevTools/device status recorded as PASS/FAIL/PENDING. [VERIFIED: REQUIREMENTS.md]

### Wave 0 Gaps

- [ ] `44-UAT.md` - final acceptance report for UAT-02/UAT-03 and ADMIN-01..04. [VERIFIED: filesystem]
- [ ] Browser/admin checklist - no automated source test exists for visual IA and drawer behavior. [VERIFIED: filesystem]
- [ ] Optional `scripts/local/smoke-phase-44-release-acceptance.ps1` - only if planner wants automated aggregation of Phase 41-43 report statuses. [VERIFIED: scripts/local]

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | yes | Preserve existing admin bearer-token gate and do not weaken `ProtectedRoute`. [VERIFIED: App.tsx] |
| V3 Session Management | yes | Preserve token bootstrap/clear behavior and avoid logging tokens in acceptance docs. [VERIFIED: App.tsx, 43-UAT.md] |
| V4 Access Control | yes | Do not expose hidden/admin-only operations through misleading routes; backend permission behavior remains out of scope unless broken. [VERIFIED: DefaultLayout.tsx, AGENTS.md] |
| V5 Input Validation | yes | Keep JSON validators requiring valid object/schemaVersion where existing forms use JSON advanced fields. [VERIFIED: ExperienceOrchestrationWorkbench.tsx, ExperienceTemplateLibrary.tsx] |
| V6 Cryptography | no direct implementation | Do not add crypto; keep secrets externalized and redacted. [VERIFIED: AGENTS.md] |

OWASP ASVS 5.0.0 is the latest stable version listed by OWASP, and the project describes ASVS as a basis for testing web application technical security controls and secure development requirements. [CITED: https://owasp.org/www-project-application-security-verification-standard/]

### Known Threat Patterns for Admin UI Polish

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Token leakage in release evidence | Information Disclosure | Redact tokens and credentials in reports; Phase 43 already redacts token fields. [VERIFIED: 43-UAT.md] |
| External link misuse for asset URLs | Spoofing / Information Disclosure | Link only valid HTTP(S) URLs, use `rel="noreferrer"` for external asset links, and do not expose secret object keys as clickable public URLs. [VERIFIED: StoryMaterialPackageManagement.tsx, AGENTS.md] |
| Misleading support actions | Tampering | Keep preview-first/confirmation behavior and add clear inline action explanations. [VERIFIED: UserProgressWorkbench.tsx, 43-VERIFICATION.md] |
| Hidden admin route exposure | Elevation of Privilege | Do not rely on UI hiding for authorization; preserve backend admin auth and route guards. [VERIFIED: App.tsx, AGENTS.md] |

## Sources

### Primary (HIGH confidence)

- `.planning/REQUIREMENTS.md` - Phase 44 requirement IDs and acceptance statements. [VERIFIED: file read]
- `.planning/ROADMAP.md` - Phase 44 goal and success criteria. [VERIFIED: file read]
- `.planning/STATE.md` - current phase, caveats, and prior decisions. [VERIFIED: file read]
- Phase 41/42/43 UAT and verification files - prior automated/manual evidence boundaries. [VERIFIED: file read]
- `packages/admin/aoxiaoyou-admin-ui/src/App.tsx` and `DefaultLayout.tsx` - current route/menu IA. [VERIFIED: file read]
- Required Experience, StorylineMode, and Media drawer components - current UI state and risk points. [VERIFIED: file read]
- npm registry and local `npm ls` - installed/latest package versions. [VERIFIED: npm registry, npm ls]
- Context7 docs for Ant Design, React Router, Vite, ProComponents. [CITED: Context7 CLI]
- OWASP ASVS project page. [CITED: https://owasp.org/www-project-application-security-verification-standard/]

### Secondary (MEDIUM confidence)

- Ant Design official docs excerpts for table scroll/columns, image fallback, typography ellipsis, and Empty actions. [CITED: https://github.com/ant-design/ant-design/blob/master/components/table/index.en-US.md]
- React Router official docs for `Link` and replace behavior. [CITED: https://github.com/remix-run/react-router/blob/main/docs/api/components/Link.md]
- Vite official docs for build and preview commands. [CITED: https://github.com/vitejs/vite/blob/main/docs/guide/cli.md]
- ProComponents docs for ProTable request/search behavior. [CITED: https://github.com/ant-design/pro-components/blob/master/site/components/table.en-US.md]

### Tertiary (LOW confidence)

- No unverified web-only implementation recommendations were used. [VERIFIED: research log]

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - confirmed by `package.json`, `package-lock`, `npm ls`, and `npm view`. [VERIFIED: npm registry, filesystem]
- Architecture: HIGH - confirmed by required file reads and existing route/layout/page ownership. [VERIFIED: source reads]
- Pitfalls: MEDIUM - major pitfalls are grounded in current files and prior UAT evidence; exact visual failures still need browser screenshots/manual checks. [VERIFIED: source reads] [ASSUMED]

**Research date:** 2026-05-04 [VERIFIED: system date]  
**Valid until:** 2026-06-03 for repo-internal planning; package latest-version facts should be rechecked if planning starts later. [ASSUMED]
