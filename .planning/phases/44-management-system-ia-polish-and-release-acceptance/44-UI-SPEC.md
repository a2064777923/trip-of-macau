# Phase 44 UI Spec: Management-System IA Polish

**Created:** 2026-05-04
**Scope:** Admin IA, story/gameplay operational pages, media/material previews, and release acceptance evidence surfaces

## Design Goal

Make the management system feel like one coherent operator console instead of a collection of overlapping workbenches. Operators should understand which page to use, see whether assets and runtime records are usable, and complete release acceptance checks without reading raw JSON or guessing from console output.

## Visual Direction

- Preserve the existing Ant Design Pro shell, rounded white panels, warm travel gradients, and shared brand icon.
- Use Traditional Chinese for all operator-facing labels and helper copy.
- Prefer clear cards, labelled filter groups, readable tables, and progressive disclosure over dense JSON panels.
- Avoid broad redesign. Phase 44 is polish and acceptance, not a new design system.

## Required Page Contracts

### Navigation and IA

- Sidebar entries must not be duplicate, misleading, wrong-redirect, or shell-only for v3.2-owned workflows.
- Retained future placeholders must visibly say they are future work and must not be used as completed acceptance paths.
- Story/content pages must explain their distinct missions:
  - `故事線管理`: story master data.
  - `故事路線與章節覆寫`: story-mode route, chapter order, inherited flow override.
  - `章節管理`: chapter record/content editing.
  - `內容積木庫`: reusable content blocks used by chapters.
  - `故事素材包`: generated/imported material package QA and versioning.
  - `媒體資源`: global asset library.

### Experience Pages

- `體驗流程工作台` must keep flow/step tables readable, including the flow-name/code column.
- `互動與任務模板庫` must show how templates are applied, where they are used, and when to clone a preset.
- `體驗規則治理中心` must label each filter and explain what conflict checks mean.
- Raw JSON must remain in advanced drawers or code snapshots only.

### Media and Material Pages

- Valid image/audio/video/Lottie assets must have a visible preview or playback affordance.
- Unavailable assets must show a clear state such as `無公開連結`, `預覽載入失敗`, `待生產`, `缺少資產`, or `COS 不可用`.
- Long URLs, object keys, and local paths must truncate with tooltip or detail drawer disclosure.
- Package-version actions must not open `無此資料`; unavailable versions must show disabled state or explanatory copy.

### Runtime Operations

- Traveler progress and reward support tables must have labelled filters, scroll-safe columns, and visible empty states.
- Mutating support actions must stay preview-first and disabled while submitting.
- Action explanations must be concise and visible near the action, not hidden in docs.

## Responsive and Motion

- Desktop: tables may use horizontal scroll, but key identity/status columns should remain readable.
- Narrow widths: filter controls stack into one column and labels remain visible.
- Loading states should prevent layout jitter where data changes tab content.
- Use subtle card transitions only if already present; do not introduce unrelated animation.

## Acceptance Checks

- `DefaultLayout.tsx` and `App.tsx` contain no v3.2-owned sidebar route that redirects to the wrong workflow.
- Experience workbench CSS contains responsive filter/grid rules and code ellipsis rules.
- Media/material components contain unavailable-asset labels and URL/code ellipsis handling.
- Browser UAT report records sidebar navigation, material/media preview, template/governance explanations, and traveler support layout separately.

---

*Phase: 44-management-system-ia-polish-and-release-acceptance*
