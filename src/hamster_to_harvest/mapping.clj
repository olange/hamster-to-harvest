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

(defn name->client+proj
  "Given the name of an Hamster project, return the corresponding client
  and project name of Harvest; this mapping is specific to each Harvest user"
  [name]
  (condp = name
    "BSAgedco"   ["Régie Brolliet SA" "GED e-mail"]
    "BSAsupdev"  ["Régie Brolliet SA" "Infrastruct. dév."]
    "CFDEwebdev" ["Cofideco SA" "Site web cofideco.ch"]
    [(str "***N" name) (str "***N" name)]))

(defn category+tags-and-more->task
  "Given the category and tags (a vector of strings) of an Hamster activity,
  return the matching task for Harvest; this mapping is specific to each Harvest user"
  [category tags name]
  (cond
    ;; Mappings common to all projects
    (some #{"Développement"} tags) "Développement"                  ;; BSA  CFDE
    (some #{"Documentation"} tags) "Documentation"                  ;; BSA  CFDE
    (some #{"Support"} tags) "Support"                              ;; BSA
    (some #{"Système"} tags) "Admin. système"                       ;; BSA  CFDE
    (some #{"Tests"} tags) "Tests intégration"                      ;; BSA
    (some #{"Déplacement"} tags) "Déplacement"                      ;; CFDE
    (some #{"Etude et veille"} tags) "Recherche et veille tech."    ;; CFDE
    (some #{"Publication"} tags) "Actualisation du site"            ;; CFDE
    (some #{"Séance de travail"} tags) "Séance de travail"          ;; CFDE

    ;; Mappings specific to given projects
    (= name "CFDEwebdev")
      (cond
        (some #{"Conception"} tags) "Design fonctionnel"
        (some #{"Coordination"} tags)
          (if (= "nonFacturé" category) "Administration" "Suivi de projet")
        (some #{"Enseignement"} tags) "Enseignement / Formation"    ;; CFDE
        (some #{"Design fonctionnel"} tags) "Design fonctionnel"    ;; CFDE
        (some #{"Design graphique"} tags) "Design graphique"        ;; CFDE
        (some #{"Stratégie et prospection"} tags) "Administration"  ;; CFDE
        :else (str "***C" category ";T" (join \, tags)))
    (= name "BSA*")
      (cond
        (some #{"Conception"} tags) "Conception"
        (some #{"Coordination"} tags) "Suivi de projet"
        :else (str "***C" category ";T" (join \, tags)))
    ;; Unknown categories and tasks
    :else (str "***C" category ";T" (join \, tags))))

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
        [client project]  (name->client+proj name)
        task              (category+tags-and-more->task category tags name)
        notes             (description-and-more->notes description category tags)
        hours             (duration->hours duration_minutes)]
        ;; beware: positional constructor; order of arguments matters hereafter
        (harvest/->HarvestTimeEntry
                          date client project task notes
                          hours firstname lastname)))
