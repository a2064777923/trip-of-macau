# v3.0 Research Summary

## Stack Additions

- Extend the existing Spring/MyBatis/MySQL stack with canonical story-domain services, user-progress projection/recompute services, operations dashboards, and shared reference contracts.
- Do not add a new framework or secondary stack unless a concrete bottleneck proves necessary.

## Feature Table Stakes

- Dedicated storylines, chapters, and content blocks
- Real traveler profile, progress, timeline, and recompute surfaces
- Real testing, operations, lifecycle, and dashboard tooling
- Canonical linkage across story, user, ops, map, POI, indoor, reward, media, AI, and public contracts

## Watch Out For

- Building isolated CRUD pages that do not link together
- Letting derived progress drift when content changes
- Shipping placeholder dashboards instead of actionable ops tools
- Quietly reintroducing the deferred mini-program acceptance scope into `v3.0`
- Leaving the new surfaces empty by skipping seeded examples

## Recommended Milestone Shape

Use five phases:
1. story/content completion
2. user/progress intelligence
3. testing/operations control
4. cross-domain linkage
5. seeded verification and closure
