(ns hamster-to-harvest.mapping
  (:require [hamster-to-harvest.harvest :as harvest]
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
      "BSAgedco"   ["Régie Brolliet SA" "GED e-mail"]
      "BSAsupdev"  ["Régie Brolliet SA" "Infrastruct. dév."]
      "CFDEwebdev" ["Cofideco SA" "Site web cofideco.ch"]
      "HTAPwebdev" (if (some #{"Publication"} tags)
                     ["Fondation Horst Tappe" "Maintenance du site"]
                     ["Fondation Horst Tappe" "Création du site"]))))

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

      ;; Mappings specific to given projects
      (= name "HTAPwebdev")                                               ;; HTAP
        (if (some #{"Coordination"} tags)     "Suivi de projet"
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
      (= name "BSA*")                                                     ;; BSA
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
  (let [notes (condp = category
                "offert"     (str "++ Offert ++ " description)
                "nonFacturé" (str "++ Non facturé ++ " description)
                "bénévolat"  (str "++ Bénévolat ++ " description)
                "work"       description
                             (str "++ ***C" category " ++ " description))
        notes (if (some #{"facturé"} tags)
                (str notes " (facturé)") notes)]
    (str notes " [transcrit de Hamster]")))

(defn activity->time-entry
  "Given an Hamster activity record and a configuration map containing the
  firstname and lastname of the Harvest user, return a corresponding Harvest
  Time Tracking record."
  [firstname lastname activity]
  ;; see function `activity-elt->record` in hamster.clj to see
  ;; how the activity record is factored
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
