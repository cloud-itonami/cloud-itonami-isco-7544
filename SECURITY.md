# Security Policy

This project handles fumigators and other pest and weed controllers
operating workflows. Treat vulnerabilities as potentially high impact even
when the demo data is synthetic — this domain's failure modes include real
toxic-chemical-exposure risk from pesticide and fumigant application,
alongside physical worker-safety and public-health risk to building
occupants and the environment.

## Do Not Disclose Publicly

Report privately before opening public issues for:

- credential exposure
- real applicator, site or operator data exposure
- authorization bypass
- Pest and Weed Control Scheduling Coordination Governor bypass
- audit-ledger tampering
- over-disclosure in reports or exports
- unsafe robot action dispatch
- any path that lets a proposal reach a fumigation/pesticide-application-
  execution decision, a chemical-safety-clearance/re-entry decision, or a
  site-safety-officer-override decision

## Reporting

Use GitHub private vulnerability reporting when available for the repository.
If that is unavailable, contact the repository maintainers through the
cloud-itonami organization before publishing details.

Include:

- affected commit or version
- reproduction steps
- expected and actual behavior
- impact on applicator/site data, policy enforcement or audit logging
- suggested fix, if known

## Production Guidance

- Store secrets outside Git.
- Keep real applicator/site/operator data outside this repository.
- Run policy tests before deployment.
- Export and review audit logs regularly.
- Use least privilege for operators and service accounts.
