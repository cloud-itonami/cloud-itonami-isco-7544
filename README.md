# cloud-itonami-isco-7544

Open Occupation Blueprint for **ISCO-08 7544**: Fumigators and Other Pest and
Weed Controllers.

This repository designs a forkable OSS business for a pest-control and
fumigation-operation scheduling and logistics coordination practice: a
scheduling and supply-coordination robot manages crew/site/task records
under a governor-gated actor, so a fumigation and pest/weed-control crew
keeps its own operating records instead of renting a closed
workforce-management SaaS.

**Maturity: `:implemented`.** `src/pestcoord/` implements the
`PestCoordActor` as a `langgraph.graph/state-graph` (`pestcoord.actor`)
wired to a `Pest and Weed Control Scheduling Coordination Advisor`
(`pestcoord.advisor`) and an independent `PestCoordGovernor`
(`pestcoord.governor`), following the itonami actor pattern
(ADR-2607121000): `:intake -> :advise -> :govern -> :decide -+-> :commit
(:ok? true) +-> :request-approval (:escalate? true, human-in-the-loop
interrupt) +-> :hold (:hard? true)`. HARD invariants (always hold, never
overridable): applicator provenance, site provenance, no-actuation
(`:effect` must be `:propose`), a closed op-allowlist (`:log-work-record`,
`:schedule-crew-operation`, `:flag-safety-concern`,
`:coordinate-supply-order` — nothing else may ever be proposed), and a
permanent, unconditional block on any proposal that would directly finalize
a fumigation/pesticide-application-execution decision (e.g. deciding to
proceed with a specific fumigation or pesticide application) or a
chemical-safety-clearance decision (e.g. declaring a treated site safe for
re-entry), or that would override a site safety officer's judgment.
Always-escalate paths (human sign-off regardless of confidence, mapping
this repo's Trust Controls in
[`docs/business-model.md`](docs/business-model.md)): `:flag-safety-concern`
(always) and `:coordinate-supply-order` above the registered cost threshold.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a pest-control/fumigation-operation
scheduling/logistics coordination robot performs crew scheduling,
job/site/progress-record logging and administrative/equipment supply-order
coordination for a fumigation and pest/weed-control crew, under an actor
that proposes actions and an independent **Pest and Weed Control Scheduling
Coordination Governor** that gates them. The governor never dispatches
hardware itself, never applies pesticides or fumigants itself, and never
finalizes a fumigation/pesticide-application-execution decision or a
chemical-safety-clearance decision, and never overrides a site safety
officer's judgment; `:high`/`:safety-critical` actions (such as a flagged
exposure-risk/site-condition/equipment-condition concern, or an
above-threshold supply order) require human sign-off. **This actor
coordinates PEST-CONTROL/FUMIGATION-OPERATION SCHEDULING/LOGISTICS ONLY —
it never applies pesticides or fumigants itself, and it never makes a
chemical-safety-clearance/re-entry decision itself.**

Fumigators and other pest and weed controllers apply pesticides and
fumigants (toxic-chemical agents) to control pests, weeds and other
organisms, a real toxic-chemical-exposure hazard to both the applicator and
building occupants/environment if application protocols are not followed.
This actor never applies chemicals and never clears a site as safe — it
only schedules and logs around that work, and always routes exposure/safety
concerns to a human site safety officer.

## Core Contract

```text
crew roster + site registration + safety-reporting policy
        |
        v
Pest and Weed Control Scheduling Coordination Advisor -> PestCoordGovernor -> log/schedule/coordinate, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses,
finalize a fumigation/pesticide-application-execution decision, finalize a
chemical-safety-clearance decision, override a site safety officer's
judgment, suppress an operating record, or disclose sensitive data without
governor approval and audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `7544`). Required capabilities:

- :robotics
- :identity
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
