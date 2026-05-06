# Phase 44 Verification

## Requirement Evidence

| Requirement | Evidence | Status |
| --- | --- | --- |
| UAT-02 | `scripts/local/smoke-phase-44-release-acceptance.ps1` records DevTools and physical-device flagship story checks separately and refuses final PASS unless at least one flagship story smoke has PASS evidence. Current local report keeps these checks `PENDING` because no actual DevTools/device evidence was supplied. | PENDING |
| UAT-03 | `44-UAT.md` is generated with separate `Automated Smoke`, `Browser / Admin Checks`, `WeChat DevTools Checks`, `Physical Device Checks`, `Accepted Caveats`, and `Future Gameplay Scope` sections. | PASS |
| ADMIN-01 | `DefaultLayout.tsx` keeps the visible IA labels for `故事路線與章節覆寫`, `章節管理`, `內容積木庫`, `互動與任務模板庫`, and `體驗規則治理中心`, while low-level binding/override compatibility routes are hidden from visible sidebar entries. `check-phase-44-admin-text.ps1` verifies this from the UTF-8 fixture. | PASS |
| ADMIN-02 | Phase 44 usability fixture verifies wider experience-flow column handling, scroll-safe tables, governance filter labels, template helper copy, and support-workbench preview-first copy. Admin UI `npm run type-check` passes. | PASS |
| ADMIN-03 | Media preview and detail surfaces now include unavailable states, failed-preview copy, long-link truncation/copy affordance, material package no-version copy, and material URL truncation classes. These are fixture-checked in `requiredMediaText`. | PASS |
| ADMIN-04 | Experience template, governance, runtime action, story route, chapter, content block, and orchestration pages include concise Traditional Chinese mission/help text and avoid raw JSON as the primary operator path. | PASS |

## Command Evidence

| Command | Status | Notes |
| --- | --- | --- |
| `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | PASS | Admin backend compile passed locally. |
| `mvn -q -DskipTests compile -f packages/server/pom.xml` | PASS | Public backend compile passed locally. |
| `cd packages/admin/aoxiaoyou-admin-ui; npm run type-check` | PASS | TypeScript check passed after Wave 2 UI changes and before final release smoke. |
| `cd packages/admin/aoxiaoyou-admin-ui; npm run build` | PASS | Production build passed; Vite reported only the existing large chunk warning. |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-44-release-acceptance.ps1 -StaticOnly` | PASS / BLOCKED outcome expected | Static checks passed and the script wrote a truthful UAT report; final outcome stayed `BLOCKED` because manual evidence was absent. |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/check-phase-44-release-docs.ps1` | PASS | Release docs checker passed after UAT and verification docs were written. |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-44-release-acceptance.ps1 -Quick` | PASS / BLOCKED outcome expected | Quick automated checks passed; script exited 0 while keeping final outcome `BLOCKED` because release manual evidence was absent. |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-44-release-acceptance.ps1 -IncludeBuilds -IncludeExistingSmokes -IncludeBrowserChecklist -IncludeWeChatChecklist` | BLOCKED by missing manual evidence | Automated checks, builds, compiles, and Phase 41/42/43 quick smokes all passed. Browser/admin and WeChat/device rows remained `PENDING`, so full release PASS was correctly blocked. |

## Browser / Admin Checks

These routes are part of the Phase 44 browser checklist. Without explicit browser evidence, rows remain `PENDING`, not `PASS`.

| Route | Check | Status |
| --- | --- | --- |
| `/content/storyline-mode` | Story route and chapter override mission are distinct from chapter CRUD. | PASS |
| `/content/chapters` | Chapter management route does not redirect to storyline mode and explains its scope. | PASS |
| `/content/material-packages` | Material package versions, unavailable assets, and preview states are understandable. | PASS |
| `/content/media` | Media detail drawer/media list supports valid assets, unavailable states, Lottie assets, and long URL handling without error page. | PASS |
| `/content/experience` | Experience flow workbench has readable flow columns and stable panel layout. | PASS |
| `/content/experience/templates` | Template library explains reuse, usage locations, and non-JSON-first editing. | PASS |
| `/content/experience/governance` | Governance center filters have labels and empty state explains next action. | PASS |
| `/users/progress` | Traveler support actions are visible from the support workflow and are preview-first/audit-oriented. | PASS |

## WeChat DevTools / Device Checks

Current status is `PENDING` because no actual DevTools simulator or physical-device evidence was supplied during this execution.

| Surface | Required evidence | Status |
| --- | --- | --- |
| DevTools 旗艦故事進入 | Screenshot/log/video or written operator evidence showing flagship story opens from compiled mini-program against local backend. | PENDING |
| DevTools 章節與媒體播放 | Evidence showing chapters and media playback/fallback behavior. | PENDING |
| DevTools 事件上報 | Evidence showing story/gameplay event submit reaches public backend. | PENDING |
| 實機故事進入 | Physical-device evidence showing flagship story opens. | PENDING |
| 實機定位 / 靠近事件 | Physical-device evidence showing real proximity/location path. | PENDING |

## Release Outcome Rules

Final PASS 需要自動化檢查通過、瀏覽器管理端檢查通過，且 DevTools 或實機至少一項旗艦故事煙測有 PASS 證據。

Automated smoke alone is not equivalent to release acceptance. `BLOCKED` is the correct final state when automated checks pass but browser/admin evidence or DevTools/device evidence is missing.

## Accepted Caveats

未執行的 WeChat DevTools 或實機檢查不標記為 PASS。

Local automated checks may verify release readiness structure, admin IA text, type safety, and prior smoke wiring, but they do not replace real WeChat runtime evidence.

## Future Gameplay Scope

AR、語音、拼圖、防守玩法與生產級室內定位留待後續 gameplay milestone。

Advanced gameplay engines, production-grade indoor localization, and full physical-device journey UAT should be planned as later milestones after this management-system release acceptance pass.

## Current Verification Summary

Phase 44 implementation evidence is strong for ADMIN-01 through ADMIN-04 and UAT-03 structure. Browser/admin checks passed through a headed Playwright run on 2026-05-05. Final v3.2 release PASS remains blocked until at least one DevTools or physical-device flagship story smoke has concrete PASS evidence. The latest full UAT report shows all automated and browser/admin rows as `PASS`, with WeChat DevTools and physical-device rows still `PENDING`.
