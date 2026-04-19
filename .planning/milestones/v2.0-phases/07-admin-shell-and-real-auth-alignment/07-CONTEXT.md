# Phase 7: Admin Shell and Real Auth Alignment - Context

**Gathered:** 2026-04-13
**Status:** Approved plan, ready for execution
**Source:** Approved user plan plus brownfield inspection of `packages/server`, `packages/client`, and `packages/admin/aoxiaoyou-admin-ui`

<domain>
## Phase Boundary

Phase 7 rebuilds the admin shell/navigation baseline and replaces guest-style mini-program identity bootstrapping with real WeChat-authenticated session flow.

This phase owns:
- public backend WeChat login and explicit local/dev dev-bypass login
- mini-program auth-state normalization and consistent auth gates for stateful flows
- admin shell/login/sidebar/placeholder cleanup in Traditional Chinese
- route-entry cleanup so each visible admin module lands on its own owned page or owned placeholder

This phase does not yet own:
- four-language authoring/editing surfaces beyond shell-level copy
- deep module redesign for maps, media, story composition, and system settings
- AI provider platformization or full indoor interaction rules
</domain>

<decisions>
## Locked Decisions

- Anonymous travelers may browse read-only public content only.
- Any personal, stateful, or interactive flow must require authentication.
- Production and experience builds must use real `wx.login -> packages/server`.
- Dev bypass must be explicit, opt-in, and available only in local/dev plus devtools.
- Admin sidebar keeps the final IA visible now, but unfinished modules must have dedicated Traditional Chinese placeholders instead of wrong redirects or shared consoles.
- Phase 7 translation scope is shell-level and route-entry surfaces only.
</decisions>

<specifics>
## Implementation Notes

- `packages/server` currently accepts raw client-supplied `openId`; this must be replaced by real code exchange.
- `packages/client` currently auto-creates synthetic guest identities and persists synthetic `openId`; this must stop.
- `packages/admin/aoxiaoyou-admin-ui` currently has English shell copy, a wrong chapter redirect, and multiple unrelated routes reusing `SystemManagement`.
- Shared branding source is `packages/client/src/assets/logo.png`.
</specifics>

---

*Phase: 07-admin-shell-and-real-auth-alignment*
