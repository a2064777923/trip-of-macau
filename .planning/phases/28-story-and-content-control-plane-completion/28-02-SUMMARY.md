---
phase: 28-story-and-content-control-plane-completion
plan: 02
subsystem: admin-ui
tags: [react, vite, ant-design, experience-workbench, story-content, media-assets]
requires:
  - phase: 28-01
    provides: Versioned admin experience API and public runtime contract
provides:
  - Dedicated admin experience workbench routes
  - Typed admin UI experience API coverage
  - Media and content-block management surfaces with Lottie-aware asset components
  - Chapter pre-publish assembled content preview
affects: [phase-29-poi-experience, phase-30-storyline-workbench, phase-31-template-governance, phase-33-content-package]
tech-stack:
  added: []
  patterns:
    - Three-panel admin orchestration workbench
    - Shared media asset picker, array picker, preview, upload, and detail drawer components
    - Form validation feedback with scroll, focus, and shake affordance
key-files:
  created:
    - packages/admin/aoxiaoyou-admin-ui/src/pages/Experience/ExperienceOrchestrationWorkbench.tsx
    - packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryContentBlockManagement.tsx
    - packages/admin/aoxiaoyou-admin-ui/src/pages/Content/MediaLibraryManagement.tsx
    - packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryChapterWorkbench.tsx
    - packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetPickerField.tsx
    - packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetPreview.tsx
    - packages/admin/aoxiaoyou-admin-ui/src/utils/formErrorFeedback.ts
  modified:
    - packages/admin/aoxiaoyou-admin-ui/src/services/api.ts
    - packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts
    - packages/admin/aoxiaoyou-admin-ui/src/App.tsx
    - packages/admin/aoxiaoyou-admin-ui/src/layouts/DefaultLayout.tsx
key-decisions:
  - "Experience workbench routes stay inside the protected admin shell under /content/experience and subroutes."
  - "The workbench exposes the A-Ma Temple preset as structured controls rather than requiring operators to hand-write the entire JSON payload."
  - "Chapter preview assembles content-block links in form order before publish and surfaces missing or draft-state warnings."
patterns-established:
  - "Admin forms call focusFirstInvalidField on validation failure for scroll, focus, and shake feedback."
  - "Story content surfaces reuse the shared media asset components instead of forking a second asset model."
requirements-completed: [STORY-01, STORY-02, STORY-03, STORY-04, LINK-01]
duration: 20 min
completed: 2026-04-28
---

# Phase 28 Plan 02: Admin Workbench Summary

**Admin experience orchestration workbench with Traditional Chinese routes, A-Ma Temple presets, shared media components, and chapter assembled preview**

## Performance

- **Duration:** 20 min
- **Started:** 2026-04-28T15:30:26Z
- **Completed:** 2026-04-28T15:50:25Z
- **Tasks:** 3
- **Files modified:** 14

## Accomplishments

- Aligned `api.ts` and `types/admin.ts` to the Phase 28 experience backend contract for templates, flows, steps, bindings, overrides, exploration elements, and governance overview.
- Exposed dedicated Traditional Chinese admin routes and navigation entries for the experience workbench, template library, bindings, overrides, exploration rules, and governance center.
- Added structured A-Ma Temple preset controls for intro popup, route planning, proximity full-screen media, check-in task release, pickups, hidden achievement, and reward or title grants.
- Added reusable media asset components and connected content-block, media-library, and chapter workbench pages with a pre-publish assembled preview and missing/draft warnings.

## Task Commits

Each task was committed atomically:

1. **Task 28-02-01: Typed admin UI experience API coverage** - `f0ad72c` (feat)
2. **Task 28-02-02: Experience workbench routes and validation UX** - `b62a93b` (feat)
3. **Task 28-02-03: Content/media surfaces and assembled preview** - `871a968` (feat)

**Plan metadata:** pending in docs commit

## Files Created/Modified

- `packages/admin/aoxiaoyou-admin-ui/src/pages/Experience/ExperienceOrchestrationWorkbench.tsx` - Three-panel workbench with canonical options, governance, validation feedback, and A-Ma Temple presets.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryChapterWorkbench.tsx` - Chapter editor with structured rules/effects and發布前組裝預覽 for ordered content blocks.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryContentBlockManagement.tsx` - Reusable content-block library including Lottie, audio, video, gallery, and attachment asset selection.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/MediaLibraryManagement.tsx` - Global media center with previews, usage visibility, and Lottie filtering.
- `packages/admin/aoxiaoyou-admin-ui/src/components/media/*` - Shared upload, picker, preview, array selection, and detail drawer components.
- `packages/admin/aoxiaoyou-admin-ui/src/App.tsx` and `packages/admin/aoxiaoyou-admin-ui/src/layouts/DefaultLayout.tsx` - Dedicated admin shell routes and sidebar entries.

## Decisions Made

The UI now treats the backend canonical vocabulary as the source of truth. Invalid options such as stale flow modes or noncanonical statuses were corrected in the workbench so operators do not save values the backend rejects.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Corrected stale workbench option values**

- **Found during:** Task 28-02-02 (workbench UX verification)
- **Issue:** Some hardcoded select options used stale values such as `storyline_mode`, `indoor_node`, `manual`, and extra statuses that do not match the Phase 28 backend vocabulary.
- **Fix:** Updated template, flow, trigger, owner, binding-role, inherit-policy, mode, and status options to the canonical backend vocabulary.
- **Files modified:** `ExperienceOrchestrationWorkbench.tsx`
- **Verification:** Admin UI build and source checks passed.
- **Committed in:** `b62a93b`

---

**Total deviations:** 1 auto-fixed bug.
**Impact on plan:** The fix improves contract alignment and avoids UI-driven validation failures.

## Issues Encountered

None.

## Verification

- `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` passed.
- Source checks confirmed `/api/admin/v1/experience/**` API coverage and experience DTO types.
- Source checks confirmed `/content/experience` route/menu entries, Traditional Chinese labels, invalid-submit focus/shake handling, and A-Ma Temple preset controls.
- Source checks confirmed content blocks, media resource center, shared media preview/picker components, Lottie support labels, and the chapter發布前組裝預覽 path.

## User Setup Required

None.

## Next Phase Readiness

Wave 3 can seed and smoke-test the same experience and content surfaces. The admin UI is ready to consume seeded A-Ma and first-chapter foundation rows.

---
*Phase: 28-story-and-content-control-plane-completion*
*Completed: 2026-04-28*
