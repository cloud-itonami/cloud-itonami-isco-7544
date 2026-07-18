# Business Model: Pest and Weed Control Scheduling and Logistics Coordination Practice

## Classification

- Repository: `cloud-itonami-isco-7544`
- ISCO-08: `7544`
- Occupation: Fumigators and Other Pest and Weed Controllers
- Social impact: worker-safety, public-health, environmental-safety

## Customer

- pest-control and fumigation service operators
- independent applicator crews / crew cooperatives

## Offer

- crew shift/task scheduling coordination
- job/site/progress-record logging
- administrative/equipment supply-order coordination (not pesticide or
  fumigant chemicals themselves — chemical procurement/handling is
  entirely out of scope for this administrative-coordination actor)
- safety-concern surfacing to site safety officers

## Revenue

- monthly retainer
- per-crew coordination fee

## Trust Controls

- no direct finalization of a fumigation/pesticide-application-execution
  decision (e.g. deciding to proceed with a specific fumigation or
  pesticide application), ever
- no direct finalization of a chemical-safety-clearance decision (e.g.
  declaring a treated site safe for re-entry), ever
- no override of a site safety officer's judgment, ever
- flagged safety concerns (exposure risk, site condition, equipment
  condition) always route to human sign-off, regardless of confidence
- no supply order above the registered cost threshold without
  governor-gated human sign-off
- operating and coordination records are auditable, not editable
