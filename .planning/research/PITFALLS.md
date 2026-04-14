# v2.0 Research: Pitfalls

**Milestone:** `v2.0 後台管理系統的改進與完善`

| Pitfall | Why It Hurts | Prevention | When To Address |
|---------|--------------|------------|-----------------|
| Keeping client-generated guest IDs in the login flow | It produces fake identity continuity and breaks the user's "must login with WeChat" requirement | Replace public login with `code` exchange and isolate any dev bypass behind explicit non-production controls | Earliest foundation work |
| Treating `getUserProfile()` as identity | It provides profile data, not the core login-state exchange required for WeChat auth | Use `wx.login` + server-side exchange for identity, and treat profile/avatar as optional enrichment | Earliest foundation work |
| Assuming AMap can auto-detect coordinate systems | The official conversion API requires `coordsys`; wrong assumptions will silently place POIs and maps incorrectly | Store source coord sys explicitly whenever possible, add confidence-scored heuristics only as a fallback, and keep raw coordinates | Spatial foundation |
| Repeating four locale columns ad hoc on every table and screen | The number of affected entities is too large, and inconsistent field naming will become a maintenance trap | Create one reusable localization pattern and shared admin editor components | Early domain foundation |
| Making translation a blocking dependency for every save | Third-party engines can rate-limit, fail by region, or become slow | Run translation as a best-effort assist pipeline with retry/fallback and visible status | Localization foundation |
| Compressing all uploads blindly | The user explicitly wants a permission-based lossless path for some admin roles | Introduce upload policy evaluation before processing; persist the applied policy on each asset | Media foundation |
| Using image-only processing for mixed media requirements | v2.0 includes image, video, audio, zip tiles, and floor images | Separate image optimization from video/audio processing; use the right tool per media kind | Media foundation |
| Binding new rich relationships back into old single-foreign-key schemas | Storylines, maps, POIs, rewards, and activities now need many-to-many relationships | Introduce relation tables and composition tables instead of overloading old entity rows | Domain rebuild |
| Recomputing exploration percentage directly from live joins on every request | Progress definitions now change when content changes, and the graph becomes larger in v2.0 | Use event logs plus aggregate summaries/rebuild jobs | User progress workstream |
| Letting menu reuse hide unfinished modules | The current admin already redirects chapters to storylines and reuses unrelated system pages for multiple domains | Give each major domain its own route and ownership, or explicitly defer it out of the menu | Admin IA workstream |
| Pulling the full indoor rules engine back into v2.0 | It will swamp the milestone and delay the control-plane rebuild | Keep only schema hooks and bounded indoor authoring in v2.0; defer full trigger/effect runtime to v2.1 | Scope control throughout |
| Pulling full AI orchestration into v2.0 | Multi-provider quotas, switching, usage, and throttling are a platform program, not a page rename | Limit v2.0 to IA cleanup and safe groundwork only; reserve full provider orchestration for v2.1 | Scope control throughout |

## Highest-Risk Items

1. Real login replacement
2. Localization model choice
3. Spatial relationship/data-model redesign
4. Asset pipeline redesign
5. Scope creep from indoor rules and AI platformization

## Sources

- WeChat `wx.login`: https://developers.weixin.qq.com/miniprogram/dev/api/open-api/login/wx.login.html
- WeChat login API family: https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html
- AMap coordinate conversion: https://lbs.amap.com/api/webservice/guide/api/convert
- `UlionTse/translators` README: https://github.com/UlionTse/translators
