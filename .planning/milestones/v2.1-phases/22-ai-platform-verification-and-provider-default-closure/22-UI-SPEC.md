---
phase: 22
slug: ai-platform-verification-and-provider-default-closure
status: approved
shadcn_initialized: false
preset: not applicable
created: 2026-04-19
---

# Phase 22 - UI Design Contract

> Visual and interaction contract for truthful AI workspace verification and provider-status closure.

---

## Design System

| Property | Value |
|----------|-------|
| Tool | none |
| Preset | not applicable |
| Component library | Ant Design 5 |
| Router shell | existing `/ai` route group under `DefaultLayout` |
| Font | `Noto Sans TC`, `PingFang TC`, `Microsoft JhengHei`, sans-serif |

---

## Spacing Scale

| Token | Value | Usage |
|-------|-------|-------|
| xs | 4px | tag gaps, inline status notes |
| sm | 8px | compact meta blocks |
| md | 16px | default field and card spacing |
| lg | 24px | section separation |
| xl | 32px | page-level panel spacing |

Exceptions:

- the workspace nav cards may keep the existing 14px x 16px padding if that avoids shell drift

---

## Typography

| Role | Size | Weight | Line Height |
|------|------|--------|-------------|
| Body | 14px | 400 | 1.6 |
| Label | 13px | 600 | 1.5 |
| Heading | 20px | 700 | 1.35 |
| Display | 28px | 700 | 1.2 |

---

## Color Contract

| Role | Value | Usage |
|------|-------|-------|
| Shell background | `#F4F7FB` | existing workspace background |
| Card background | `#FFFFFF` | provider, model, capability, and observability cards |
| Verification accent | `#0F766E` | live-verified states and primary verification CTA only |
| Warning | `#D97706` | template-only, stale, or credential-missing states |
| Destructive | `#C62828` | failed, blocked, delete, or revoked states |

Rules:

- do not reuse one green badge for every positive state
- distinguish `live verified` from `template ready`
- distinguish `estimated cost` from vendor-billed cost

---

## Copywriting Contract

Shell and page-entry copy must stay in Traditional Chinese, but status semantics must be explicit rather than decorative.

Required exact labels or their direct equivalents:

- `已實測`
- `模板可用`
- `缺少憑證`
- `手動註冊`
- `同步自 API`
- `文檔目錄`
- `端點庫存`
- `成本為本地估算`
- `從作者表單啟動`

Forbidden patterns:

- one badge or paragraph that implies all built-in providers are live-available
- hidden route filtering that leaves ghost items in config
- redirect-only pages that still look like dedicated features

---

## Layout Contract

### Workspace shell

- keep the current overview banner and route card pattern
- visible workspace entries must match the actual route map exactly
- no filtered-but-still-declared ghost nav item for `traveler-services`

### Providers and models

- each provider card or row must expose:
  - template platform
  - sync semantics
  - inventory semantics
  - live status or non-live reason
- provider-model truthfulness should be scannable without opening JSON or drawers

### Creative verification surfaces

- when a real authoring form launches the AI workbench, the modal must visibly show:
  - target asset kind
  - capability or scenario
  - prompt title
  - current result state
  - finalize action and finalized asset linkage

### Observability

- provider health, stale-sync state, request counts, failures, fallback count, and estimated cost must be visible above raw logs
- logs remain secondary evidence, not the only observability surface

---

## Interaction Contract

- Provider truth is explicit. If a provider lacks live credentials, the UI says so.
- Sync and connectivity actions must return deterministic state language instead of generic success-only copy.
- A finalized candidate must immediately feel canonical, not still "temporary AI output".
- Route truthfulness beats IA nostalgia. If a page is not real, remove the operator-facing entry.

---

## Motion Contract

- keep motion minimal
- use only existing drawer, modal, and card hover motion
- no looping activity animations for provider status
- loading skeletons or spinners are allowed when proving live checks or creative jobs

---

## Registry Safety

| Registry | Blocks Used | Safety Gate |
|----------|-------------|-------------|
| Ant Design official | `Card`, `Tag`, `Table`, `Drawer`, `Modal`, `Form`, `Alert`, `Statistic`, `Select` | not required |
| Existing admin shell | `PageContainer`, current AI workspace cards, existing brand gradients | not required |

---

## Checker Sign-Off

- [x] Dimension 1 Copywriting: PASS
- [x] Dimension 2 Visuals: PASS
- [x] Dimension 3 Color: PASS
- [x] Dimension 4 Typography: PASS
- [x] Dimension 5 Spacing: PASS
- [x] Dimension 6 Registry Safety: PASS

**Approval:** approved 2026-04-19
