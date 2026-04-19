# v3.0 Research - Stack

## Scope

Research focused on what the existing brownfield stack needs in order to complete the remaining admin core domains without changing the fundamental architecture.

## Keep

- React 18 + Vite + Ant Design Pro for the admin UI
- Spring Boot 3.2 + MyBatis-Plus + MySQL for admin and public backends
- COS-backed media flow and existing AI-capability infrastructure
- Existing Taro mini-program runtime as a downstream consumer, not a planning center for this milestone

## Additions Needed

- Canonical story-domain tables and DTOs for storylines, chapters, chapter anchors, content blocks, and unlock metadata if they are still fragmented or incomplete
- User-progress projection and recomputation services that derive city, sub-map, indoor, story, chapter, and reward progress from canonical source events
- Operations-facing aggregate queries and dashboard DTOs that summarize health, content volume, lifecycle state, and failure counts without overloading transactional endpoints
- Shared selector/reference endpoints or DTO fragments so admin modules reuse one binding model for maps, POIs, indoor entities, rewards, media, and interaction rules
- Verification harness extensions that can exercise the new admin/public domains on the live local stack

## Do Not Add

- A new frontend framework or state stack
- A second operational database for the milestone core path
- Queue-heavy architecture unless a specific recomputation bottleneck proves it necessary
- A separate mock-only adapter for unfinished domains

## Recommendation

Stay inside the current stack and use incremental schema, service, and query additions. The milestone risk is not missing technology; it is fragmented domain ownership and incomplete linkage.
