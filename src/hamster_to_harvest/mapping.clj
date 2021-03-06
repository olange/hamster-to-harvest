(ns hamster-to-harvest.mapping
  "Actual mapping of Hamster activities to Harvest time entries."
  (:require [hamster-to-harvest.harvest :as harvest]
            [hamster-to-harvest.hamster :as hamster]
            [clojure.string :refer [split join]]))

(defn starttime->date
  "Given the start time of an Hamster activity, which is a string in
  format `YYYY-MM-DD HH:mm:ss` (for instance, `\"2011-01-07 09:00:00\"`),
  return its date part in format `YYYY-MM-DD` (`\"2011-01-07\"`)."
  [starttime]
  (first (split starttime #"\s")))

(defn duration->hours
  "Given the duration of an activity of Hamster, minutes represented
  as an integer, return the equivalent duration in hours, as a decimal"
  [duration-in-min]
  (/ duration-in-min 60.0)) ;; will cast implicitely to Double

(defn name-and-more->client+proj
  "Given the name of an Hamster project, return the corresponding client
  and project name of Harvest; this mapping is specific to each Harvest user"
  [name category tags]
  (let [unmatched [(str "***N" name) (str "***N" name)] ]
    (condp = name
      "BSAgedco"    ["Client BSA" "GED e-mail"]
      "BSAsupdev"   ["Client BSA" "Infrastruct. dév."]
      "CFDEwebdev"  ["Client CFDE" "Création site web"]
      "HTAPwebdev"  (if (some #{"Publication"} tags)
                      ["Client HTAP" "Maintenance du site"]
                      ["Client HTAP" "Création du site"])
      "ZENwebdev"   ["Client ZEN" "Création site web"]
      "RZOhomepage" ["Client RZO" "Refonte homepage"])))

(defn category+tags-and-more->task
  "Given the category and tags (a vector of strings) of an Hamster activity,
  return the matching task for Harvest; this mapping is specific to each Harvest user"
  [category tags name]
  (let [unmatched (str "***C" category ";T" (join \, tags))]
    (cond
      ;; Mappings common to all projects
      (some #{"Déplacement"} tags)            "Déplacement"               ;; CFDE, HTAP
      (some #{"Développement"} tags)          "Développement"             ;; BSA, CFDE, HTAP
      (some #{"Documentation"} tags)          "Documentation"             ;; BSA, CFDE
      (some #{"Support"} tags)                "Support"                   ;; BSA, HTAP
      (some #{"Système"} tags)                "Admin. système"            ;; BSA, CFDE, HTAP
      (some #{"Tests"} tags)                  "Tests intégration"         ;; BSA
      (some #{"Etude et veille"} tags)        "Recherche et veille tech." ;; CFDE
      (some #{"Publication"} tags)            "Actualisation du site"     ;; CFDE, HTAP
      (some #{"Séance de travail"} tags)      "Séance de travail"         ;; CFDE, HTAP

      ;; Mappings specific to projects
      (= name "HTAPwebdev")                                               ;; HTAP
        (if (some #{"Coordination"} tags)     "Suivi de projet"
                                              unmatched)
      (= name "RZOhomepage")                                              ;; RZO
        (if (some #{"Design graphique"} tags) "Design graphique"
                                              unmatched)
      (= name "CFDEwebdev")                                               ;; CFDE
        (cond
          (some #{"Conception"} tags)         "Design fonctionnel"
          (some #{"Coordination"} tags)
            (if (= "nonFacturé" category)     "Administration"
                                              "Suivi de projet")
          (some #{"Enseignement"} tags)       "Enseignement / Formation"
          (some #{"Design fonctionnel"} tags) "Design fonctionnel"
          (some #{"Design graphique"} tags)   "Design graphique"
          (some #{"Stratégie et prospection"} tags) "Administration"
          :else unmatched)
      (= (subs name 0 3) "BSA")                                           ;; BSA
        (cond
          (some #{"Conception"} tags)         "Conception"
          (some #{"Coordination"} tags)       "Suivi de projet"
          :else unmatched)
      ;; Unknown categories and tasks
      :else unmatched)))

(defn description-and-more->notes
  "Given the description of an Hamster activity, as well as its category
  and tags, compose and return the corresponding notes for Harvest"
  [description category tags]
  (let [unmatched (str "++ ***C" category " ++ " description)
        notes     (condp = category
                    "offert"     (str "++ Offert ++ " description)
                    "nonFacturé" (str "++ Non facturé ++ " description)
                    "bénévolat"  (str "++ Bénévolat ++ " description)
                    "work"       description
                                 unmatched)
        notes     (if (some #{"facturé"} tags)
                    (str notes " (facturé)")
                    notes)]
    (str notes " [transcrit de Hamster]")))

(defn activity->time-entry
  "Given an Hamster activity record and a configuration map containing the
  firstname and lastname of the Harvest user, return a corresponding Harvest
  Time Tracking record."
  [firstname lastname activity]
  ;; See the function `activity-elt->record` in ./hamster.clj to see how
  ;; the `activity` record is factored. As well as the `HarvestTimeEntry`
  ;; record definition in ./harvest.clj, to find the structure of the
  ;; Harvest time entry that is returned.
  (let [{:keys [name category tags description duration_minutes start_time]} activity
        date              (starttime->date start_time)
        [client project]  (name-and-more->client+proj name category tags)
        task              (category+tags-and-more->task category tags name)
        notes             (description-and-more->notes description category tags)
        hours             (duration->hours duration_minutes)]
        ;; beware: positional constructor; order of arguments matters hereafter
        (harvest/->HarvestTimeEntry
                          date client project task notes
                          hours firstname lastname)))
