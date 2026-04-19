# v3.0 Research - Features

## Story and Content Orchestration

**Table stakes**
- Dedicated storyline management
- Dedicated chapter composition
- Unlock and completion logic
- Multimedia content blocks
- Preview before publish

**Differentiators**
- Strong bindings to maps, POIs, indoor entities, rewards, and interaction rules
- Narrative templates and reusable content blocks
- Rich seeded examples that show intended authoring patterns instead of empty forms

**Anti-features**
- Reusing unrelated placeholder modules
- Hiding critical logic in raw JSON by default
- Building story composition without linking to the already completed spatial and reward domains

## User and Progress Intelligence

**Table stakes**
- Traveler profile drill-down
- Progress by city/sub-map/POI/indoor/story/task/reward
- Timeline of key user events
- Manual recompute and repair actions with audit

**Differentiators**
- Derived progress that reacts when source content changes
- Operator-readable exploration logic instead of opaque counters
- Unified progress and reward/story visibility instead of separate islands

**Anti-features**
- Static counters that drift from current content
- User pages that show only shallow account metadata
- Recompute actions without auditability

## Testing, Operations, and Lifecycle Control

**Table stakes**
- Runtime health views
- Smoke/test actions
- Publish/unpublish/remove lifecycle controls
- Content and exception dashboards

**Differentiators**
- Dependency-aware lifecycle operations
- Domain-wide status consistency
- Direct links from ops views into the affected entity or failure source

**Anti-features**
- Another generic placeholder dashboard
- Lifecycle controls that ignore bound entities
- Separate status semantics per domain

## Cross-domain Linkage

**Table stakes**
- Shared selectors and bindings
- Aligned admin/public contracts
- Predictable derived updates when content changes

**Differentiators**
- One canonical linkage model reused across story, user, ops, reward, media, AI, and indoor flows
- Verification that proves the linkage instead of assuming it

**Anti-features**
- Copy-pasted dropdown logic per page
- Hidden manual repair scripts required to keep progress or availability correct
