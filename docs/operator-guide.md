# Operator Guide

## First Deployment

1. Define the operator's service coverage area and crew intake process.
2. Define consent and purpose categories for applicator/site records.
3. Run synthetic operating cases (work-log entry, crew-operation
   scheduling, supply coordination, safety-concern flagging).
4. Enable human-reviewed sign-off for `:high`/`:safety-critical`
   actions (all flagged safety concerns, above-threshold supply
   orders).
5. Measure operating outcomes and audit coverage.

## Minimum Production Controls

- consent and disclosure log
- safety-critical escalation path (exposure risk, site condition,
  equipment condition)
- provenance for all operating records (applicator and site both
  independently registered)
- human review for high-risk cases
- audit export for all gated actions
- a hard, unconditional block on any attempt to route a fumigation/
  pesticide-application-execution decision, a chemical-safety-
  clearance/re-entry decision, or a site-safety-officer override
  decision, through this actor — those decisions stay the site safety
  officer's exclusive authority end to end

## Certification

Certified operators must prove that the governor gates every
safety-critical robot action, that safety-critical risks escalate to
humans, and that no deployment configuration can route a fumigation/
pesticide-application-execution decision, a chemical-safety-clearance/
re-entry decision, or a site-safety-officer judgment override through
this actor.
