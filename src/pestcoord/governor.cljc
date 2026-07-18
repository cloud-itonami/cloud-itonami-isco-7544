(ns pestcoord.governor
  "PestCoordGovernor — the independent safety/scope layer gating every
  pest-control/fumigation-operation scheduling/logistics proposal an
  advisor may make for a fumigation and pest/weed-control crew. The
  governor never dispatches hardware itself, never applies pesticides
  or fumigants itself, and never finalizes a fumigation/pesticide-
  application-execution decision (e.g. deciding to proceed with a
  specific fumigation or pesticide application) or a chemical-safety-
  clearance decision (e.g. declaring a treated site safe for
  re-entry), and never overrides a site safety officer's judgment —
  those are permanently out of this actor's scope and remain a site
  safety officer's exclusive judgment (README's 'Robotics premise':
  this actor coordinates PEST-CONTROL/FUMIGATION-OPERATION SCHEDULING/
  LOGISTICS ONLY — it never applies pesticides or fumigants itself,
  and it never makes a chemical-safety-clearance/re-entry decision
  itself). Modeled closely on cloud-itonami-isco-7535's
  tannerycoord.governor.

  HARD invariants (:hard? true, ALWAYS :hold, never overridable):
    1. applicator provenance — the crew member must be independently
                                verified/registered before any action.
    2. site provenance       — the treatment site must be
                                independently verified/registered
                                before any action.
    3. no-actuation           — proposal :effect must be :propose (the
                                governor never dispatches hardware and
                                never applies pesticides or fumigants
                                itself; it only gates what the advisor
                                may coordinate).
    4. closed op-allowlist    — only :log-work-record,
                                :schedule-crew-operation,
                                :flag-safety-concern and
                                :coordinate-supply-order may ever be
                                proposed; anything else is refused.
    5. scope-excluded action  — any proposal to directly finalize a
                                fumigation/pesticide-application-
                                execution decision (e.g. deciding to
                                proceed with a specific fumigation or
                                pesticide application), or a chemical-
                                safety-clearance decision (e.g.
                                declaring a treated site safe for
                                re-entry), or to override a site
                                safety officer's judgment, is a hard,
                                permanent block (checked both against
                                the proposed :op and, defense-in-
                                depth, against the proposal's
                                :rationale text — matched as full
                                finalization/execution ACTION phrases
                                such as \"finalize the pesticide
                                application\" / \"declare the treated
                                site safe for re-entry\" / \"override
                                the site safety officer's judgment\",
                                never as bare nouns like \"pesticide\",
                                \"fumigant\" or \"chemical\", so the
                                check can never self-trip on the
                                advisor's own routine rationale text,
                                e.g. \"logged work record for
                                applicator …\" or \"scheduled crew
                                operation for pest control task …\" or
                                \"…routed for site safety officer
                                review\" — all three legitimately
                                contain adjacent domain vocabulary but
                                none is a finalization action, and all
                                are exercised by
                                `governor-test/default-mock-advisor-proposals-never-self-trip-on-scope-exclusion`).
  ESCALATION invariants (:escalate? true, ALWAYS human sign-off
  regardless of confidence):
    6. :op :flag-safety-concern (an exposure-risk / site-condition /
                                equipment-condition concern always
                                escalates to a human, never auto-
                                commits).
    7. :op :coordinate-supply-order above `supply-cost-threshold`.
    8. low confidence (< `confidence-floor`).

  This actor coordinates pest-control/fumigation-operation scheduling/
  logistics ONLY — it never applies pesticides or fumigants itself,
  and it never makes a chemical-safety-clearance/re-entry decision
  itself; those decisions always route to a human site safety officer,
  either via a hard permanent block on the op-allowlist (rules 4/5
  above) or via a mandatory escalation (rule 6 above)."
  (:require [clojure.string :as str]
            [pestcoord.store :as store]))

(def confidence-floor 0.6)
(def supply-cost-threshold 2000)

(def allowed-ops
  #{:log-work-record :schedule-crew-operation
    :flag-safety-concern :coordinate-supply-order})

;; Defense-in-depth: none of these ops are ever in `allowed-ops`
;; above, so they are already refused by the closed-allowlist check
;; below; they are named again here — as explicit finalization/
;; execution ACTIONS, never bare nouns — so a future allowlist edit
;; cannot silently re-open this specific out-of-scope path without
;; also touching this list.
(def ^:private scope-excluded-ops
  #{:finalize-fumigation-decision :finalize-pesticide-application-decision
    :finalize-pesticide-application :authorize-pesticide-application
    :authorize-fumigation :proceed-with-pesticide-application
    :proceed-with-fumigation
    :finalize-chemical-safety-clearance
    :declare-site-safe-for-reentry
    :declare-treated-site-safe-for-reentry
    :clear-site-for-reentry
    :override-site-safety-officer-judgment
    :override-safety-officer-judgment})

;; Full finalization/execution ACTION phrases only — never bare nouns
;; ("pesticide", "fumigant", "chemical", "pest", "weed", "site",
;; "safety", "officer") — so this can never match inside the mock
;; advisor's own default rationale text (which legitimately contains
;; those bare nouns, e.g. "pest control task" / "site safety officer
;; review"). See
;; `governor-test/default-mock-advisor-proposals-never-self-trip-on-scope-exclusion`.
(def ^:private scope-excluded-phrases
  ["proceed with the pesticide application" "proceed with the fumigation"
   "authorize the pesticide application" "authorize the fumigation"
   "finalize the fumigation decision" "finalize the pesticide application decision"
   "finalize the pesticide application"
   "declare the site safe for re-entry" "declare the treated site safe for re-entry"
   "clear the site for re-entry" "clear the treated site for re-entry"
   "finalize the chemical safety clearance" "finalize the chemical-safety clearance"
   "declare the site chemical-safety cleared" "declare the treated site chemical safety cleared"
   "override the site safety officer's judgment"
   "override the safety officer's judgment"
   "override site safety officer judgment"])

(defn- contains-excluded-phrase? [s]
  (let [s (str/lower-case (or s ""))]
    (boolean (some #(str/includes? s %) scope-excluded-phrases))))

(defn- hard-violations [proposal applicator-record site-record]
  (let [{:keys [op rationale]} proposal]
    (cond-> []
      (nil? applicator-record)
      (conj {:rule :no-applicator
             :detail "未登録 applicator への提案は不可（applicator record は独立して検証・登録済みでなければならない）"})

      (nil? site-record)
      (conj {:rule :no-site
             :detail "未登録 site への提案は不可（site record は独立して検証・登録済みでなければならない）"})

      (not= :propose (:effect proposal))
      (conj {:rule :no-actuation
             :detail "effect は :propose のみ許可（governor は防除・薫蒸作業を直接実行しない）"})

      (not (contains? allowed-ops op))
      (conj {:rule :unknown-op
             :detail (str op " は closed op-allowlist に無い — 提案不可")})

      (or (contains? scope-excluded-ops op) (contains-excluded-phrase? rationale))
      (conj {:rule :scope-excluded-action
             :detail "薬剤散布・薫蒸の実行判断・薬剤安全(chemical-safety)クリアランス／再入場判断の確定、および site safety officer の判断の上書きは、この actor の権限外 — 常に永続ブロック"}))))

(defn check
  "Assess a proposal against `request`/`context`/`proposal` and a
  `store` implementing `pestcoord.store/Store`. Pure — never mutates
  the store, never dispatches a pest-control/fumigation operation."
  [request _context proposal store]
  (let [applicator-record (store/applicator store (:applicator-id request))
        site-record (some->> (:site-id proposal) (store/site store))
        hard (hard-violations proposal applicator-record site-record)
        hard? (boolean (seq hard))
        conf (or (:confidence proposal) 0.0)
        low? (< conf confidence-floor)
        supply-order-over-threshold?
        (and (= :coordinate-supply-order (:op proposal))
             (number? (:cost proposal))
             (> (:cost proposal) supply-cost-threshold))
        always-risky? (or (= :flag-safety-concern (:op proposal))
                           supply-order-over-threshold?)]
    {:ok? (and (not hard?) (not low?) (not always-risky?))
     :violations hard
     :confidence conf
     :hard? hard?
     :escalate? (and (not hard?) (or low? always-risky?))}))
