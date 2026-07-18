(ns pestcoord.store
  "SSoT for the ISCO-08 7544 fumigators and other pest and weed
  controllers pest-control/fumigation-operation scheduling/logistics
  coordination actor (itonami actor pattern, ADR-2607121000 /
  CLAUDE.md Actors section; README's 'Robotics premise' — a
  pest-control/fumigation-operation scheduling/logistics coordination
  robot performs applicator/site scheduling, job/site/progress-record
  logging and administrative/equipment supply-order coordination for a
  fumigation and pest/weed-control crew under this advisor/governor
  pair, which never dispatches hardware itself, never applies
  pesticides or fumigants itself, and never finalizes a fumigation/
  pesticide-application-execution decision or a chemical-safety-
  clearance/re-entry decision, and never overrides a site safety
  officer's judgment — those remain the site safety officer's
  exclusive judgment). Modeled closely on cloud-itonami-isco-7535's
  tannerycoord.store.

  Domain:

    applicator — a registered pest-control/fumigation crew member
                 (:applicator-id, :name)
    site       — a registered treatment site {:site-id :name
                 :max-supply-cost number}. `:max-supply-cost` is an
                 informational registered ceiling used only to decide
                 whether a `:coordinate-supply-order` proposal
                 escalates to human sign-off (the governor never
                 blocks a within-threshold order outright; it only
                 decides commit vs. escalate).
    record     — a committed operating record (a logged job/site/
                 progress entry, a scheduled crew/site operation, a
                 flagged safety concern, or a coordinated
                 administrative/equipment supply order) — written ONLY
                 via commit-record!.
    ledger     — append-only audit trail, commit or hold.")

(defprotocol Store
  (applicator [s applicator-id])
  (site [s site-id])
  (records-of [s applicator-id])
  (ledger [s])
  (register-applicator! [s applicator])
  (register-site! [s site])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (applicator [_ applicator-id] (get-in @a [:applicators applicator-id]))
  (site [_ site-id] (get-in @a [:sites site-id]))
  (records-of [_ applicator-id] (filter #(= applicator-id (:applicator-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-applicator! [s ap]
    (swap! a assoc-in [:applicators (:applicator-id ap)] ap) s)
  (register-site! [s st]
    (swap! a assoc-in [:sites (:site-id st)] st) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:applicators {} :sites {} :records [] :ledger []}
                                    seed)))))
