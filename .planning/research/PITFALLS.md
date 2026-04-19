# v3.0 Research - Pitfalls

## Pitfall 1: Isolated CRUD Instead of Linked Domain Closure

If story, user, and ops pages are built as separate CRUD islands, the milestone will look larger on paper but still fail the user's complaint that the system is not truly linked together.

**Prevention**
- Define shared selectors and bindings early.
- Make linkage a dedicated roadmap phase instead of implicit cleanup.

## Pitfall 2: Derived Progress Drift

User progress is derived from changing content. If recomputation ownership is unclear, progress and availability will drift whenever story, task, reward, or map content changes.

**Prevention**
- Define canonical recomputation entrypoints.
- Add audit trails for manual repairs.
- Verify content-change propagation explicitly in milestone closure.

## Pitfall 3: Placeholder Operations Consoles

It is easy to add another dashboard that looks populated but does not actually help operators publish, inspect impact, or resolve failures.

**Prevention**
- Tie each ops surface to a concrete workflow: inspect, filter, act, and audit.
- Reuse live data, not presentation-only mock summaries.

## Pitfall 4: Over-scoping Deferred Mini-program Work

The user explicitly postponed the mini-program experiential slice. If it leaks back into implementation, the milestone can lose focus and reopen already archived truth.

**Prevention**
- Keep mini-program frontend acceptance explicit in out-of-scope notes.
- Limit runtime work to the admin/public contracts needed by the completed domains.

## Pitfall 5: Empty New Surfaces

Even a correct admin implementation will feel unfinished if the new pages open to empty states with no meaningful examples.

**Prevention**
- Plan seeded example content as part of milestone closure.
- Treat sample data as a requirement, not an optional demo step.
