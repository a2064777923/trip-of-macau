# Phase 44: Management-System IA Polish and Release Acceptance - Context

**Gathered:** 2026-05-04
**Status:** Ready for planning
**Source:** Roadmap, requirements, Phase 41-43 evidence, and recent admin UAT feedback

<domain>
## Phase Boundary

Phase 44 is the v3.2 release-acceptance polish phase. It does not add a new gameplay engine or a new content domain. It closes the operator-facing rough edges left after Phases 41-43 by making the admin information architecture, story/gameplay operational pages, media/material preview surfaces, and release evidence truthful enough for milestone closure.

The phase must keep `/admin` as the authoritative control plane, preserve existing backend/public runtime behavior, and separate automated smoke evidence from browser admin checks and WeChat DevTools or physical-device checks.
</domain>

<decisions>
## Implementation Decisions

### Information Architecture
- **D-01:** Remove or merge v3.2-owned sidebar entries that are misleading, duplicate, shell-only, or wrong-redirect. Future-scope placeholders may remain only when they are explicitly labelled as future work and do not look like completed workflows.
- **D-02:** Clarify the mission split between `故事線管理`, `故事路線與章節覆寫`, `章節管理`, `內容積木庫`, `故事素材包`, `媒體資源`, `體驗流程工作台`, `互動與任務模板庫`, and `體驗規則治理中心`. Each retained entry must have a concise Traditional Chinese purpose visible in-page.
- **D-03:** `故事路線與章節覆寫` owns story-mode route/override authoring. `章節管理` owns chapter records/content details. `內容積木庫` remains as reusable content blocks only if the UI explains that it feeds chapter/story pages instead of replacing the experience-flow system.
- **D-04:** `互動與任務模板庫` owns reusable templates and their usage. `體驗規則治理中心` owns cross-domain checks, conflict explanations, and detail inspection. Neither should look like an empty shell.

### Admin UI Polish
- **D-05:** Runtime operation tables must use readable columns, ellipsis tooltips for long codes/URLs, horizontal scroll where needed, and sane empty states instead of cramped or overflowing layouts.
- **D-06:** Experience flow, template, governance, story material, and traveler progress pages must provide inline operator guidance in Traditional Chinese. Raw JSON may remain only as advanced/debug detail, not as the primary path.
- **D-07:** Material/media/detail drawers must preview valid assets, mark invalid/unavailable assets clearly, truncate long URLs/object keys, and avoid dead links to missing package versions.
- **D-08:** Existing dirty admin UI changes in these files may already be partial Phase 44 work. Execution must read them carefully and refine them, not revert or overwrite them blindly.

### Release Evidence
- **D-09:** Phase 44 final evidence must include automated compile/type/build/smoke results, browser/admin checks, WeChat DevTools checks, physical-device checks, accepted caveats, and future gameplay scope as separate rows.
- **D-10:** If WeChat DevTools or physical-device checks are not actually performed, mark them `PENDING` or `BLOCKED` with exact reason. Do not claim them as `PASS`.
- **D-11:** Smoke reports must redact secrets and must not include tokens, API keys, COS secrets, local provider payloads, or sensitive prompt/script text.
- **D-12:** All new docs, scripts, JSON fixtures, and Traditional Chinese copy must be UTF-8. Do not write Chinese payloads through inline PowerShell literals.

### the agent's Discretion
- The executor may implement Phase 44 as targeted UI polish plus documentation/smoke tooling rather than broad backend changes, as long as all Phase 44 requirements are covered.
- The executor may create a browser-check checklist script/report without fully automating browser clicks when local browser tooling is unavailable, but the report must truthfully distinguish automated and manual checks.
- The executor may leave future-scope modules visible if they are explicitly labelled and not v3.2-owned release acceptance paths.
</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### v3.2 Scope
- `.planning/PROJECT.md` - Current project constraints, admin/public ownership, security, UTF-8, and verification rules.
- `.planning/REQUIREMENTS.md` - Requirements `UAT-02`, `UAT-03`, `ADMIN-01`, `ADMIN-02`, `ADMIN-03`, and `ADMIN-04`.
- `.planning/ROADMAP.md` - Phase 44 goal and success criteria.
- `.planning/STATE.md` - Milestone state, accepted caveats, and known dirty-worktree notes.

### Prior Phase Evidence
- `.planning/phases/41-wechat-runtime-uat-harness-and-story-entry-hardening/41-UAT.md` - WeChat runtime harness evidence and remaining UAT caveat.
- `.planning/phases/41-wechat-runtime-uat-harness-and-story-entry-hardening/41-VERIFICATION.md` - Phase 41 verified runtime/media evidence.
- `.planning/phases/42-traveler-gameplay-event-engine/42-UAT.md` - Gameplay event-engine smoke evidence.
- `.planning/phases/42-traveler-gameplay-event-engine/42-VERIFICATION.md` - Phase 42 verification evidence.
- `.planning/phases/43-traveler-progress-and-reward-operations/43-UAT.md` - Traveler operations smoke evidence.
- `.planning/phases/43-traveler-progress-and-reward-operations/43-VERIFICATION.md` - Phase 43 verification evidence.

### Admin IA and Layout
- `packages/admin/aoxiaoyou-admin-ui/src/App.tsx` - Route registration and placeholder/redirect behavior.
- `packages/admin/aoxiaoyou-admin-ui/src/layouts/DefaultLayout.tsx` - Sidebar labels, selection, and grouping.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/ModulePlaceholder.tsx` - Placeholder pattern for future modules.

### Story, Experience, Material, Media Pages
- `packages/admin/aoxiaoyou-admin-ui/src/pages/StorylineModeWorkbench/index.tsx` - Story route and chapter override workbench.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryChapterWorkbench.tsx` - Chapter management page.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryContentBlockManagement.tsx` - Content block library page.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryMaterialPackageManagement.tsx` - Story material package page and package-version links.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/MediaLibraryManagement.tsx` - Media resource center.
- `packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetPreview.tsx` - Shared preview and unavailable-asset state.
- `packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetDetailDrawer.tsx` - Shared asset detail drawer.
- `packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetPickerField.tsx` - Shared picker used by story/reward/spatial forms.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Experience/ExperienceOrchestrationWorkbench.tsx` - Flow/binding/override/exploration tabs.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Experience/ExperienceTemplateLibrary.tsx` - Interaction/task template library.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Experience/ExperienceGovernanceCenter.tsx` - Cross-domain governance checks.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Experience/ExperienceWorkbench.css` - Experience workbench layout and responsive polish.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/UserManagement/UserProgressWorkbench.tsx` - Traveler progress and reward support page.

### Smoke and Release Tooling
- `scripts/local/smoke-phase-40-release-readiness.ps1` - Existing release-readiness smoke/report pattern.
- `scripts/local/smoke-phase-41-wechat-runtime-uat.ps1` - WeChat runtime UAT smoke pattern.
- `scripts/local/smoke-phase-42-gameplay-event-engine.ps1` - Gameplay smoke pattern.
- `scripts/local/smoke-phase-43-traveler-ops.ps1` - Traveler operations smoke pattern.
- `packages/admin/aoxiaoyou-admin-ui/package.json` - Admin UI verification commands.
- `packages/admin/aoxiaoyou-admin-backend/pom.xml` - Admin backend compile command.
- `packages/server/pom.xml` - Public backend compile command.
</canonical_refs>

<specifics>
## Specific Ideas

- Add visible "本頁用途" / "如何使用" cards to pages that operators found confusing, especially `故事路線與章節覆寫`, `章節管理`, `內容積木庫`, `互動與任務模板庫`, and `體驗規則治理中心`.
- Use consistent URL/code truncation via `Typography.Text ellipsis={{ tooltip: value }}` or a shared CSS class instead of allowing long COS URLs to overflow tables and cards.
- Make unavailable materials obvious by grouping `待生產`, `缺少資產`, `無公開連結`, `COS 不可用`, and `預覽失敗` as quality states instead of letting them occupy the same visual priority as usable assets.
- Browser checks should cover sidebar navigation, story/material/media detail drawers, experience templates, governance filters, traveler progress support, and responsive table/panel behavior.
- Release acceptance should explicitly state that complex AR/photo, speech, route coverage, puzzle/cannon-defense, and production indoor positioning remain future gameplay scope.
</specifics>

<deferred>
## Deferred Ideas

- Full physical-device story journey remains pending unless actually performed in Phase 44.
- New production approval workflow remains future `REL-01`.
- Staging/experience/production snapshot comparison remains future `REL-02`.
- Advanced gameplay engines remain `ADV-01` through `ADV-04`.
</deferred>

---

*Phase: 44-management-system-ia-polish-and-release-acceptance*
*Context gathered: 2026-05-04*
