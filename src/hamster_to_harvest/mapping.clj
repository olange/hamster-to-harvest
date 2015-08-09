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
    "BSAgedco"  ["Régie Brolliet SA" "GED e-mail"]
    "BSAsupdev" ["Régie Brolliet SA" "Infrastruct. dév."]
    [(str "***C" name) (str "***P" name)]))

(defn category+tags->task
  "Given the category and tags (a vector of strings) of an Hamster activity,
  return the matching task for Harvest; this mapping is specific to each Harvest user"
  [category tags]
  (cond
    (some #{"Conception"} tags) "Conception"
    (some #{"Coordination"} tags) "Suivi de projet"
    (some #{"Développement"} tags) "Développement"
    (some #{"Documentation"} tags) "Documentation"
    (some #{"Support"} tags) "Support"
    (some #{"Système"} tags) "Admin. système"
    :else (str "***T" category ";" (join \, tags))))

(defn description-and-more->notes
  "Given the description of an Hamster activity, as well as its category
  and tags, compose and return the corresponding notes for Harvest"
  [description category tags]
  (let [notes (if (= "offert" category) (str "++ Offert ++ " description) description)
        notes (if (some #{"facturé"} tags) (str notes " (facturé)") notes)]
    (str notes " [transcrit de Hamster]")))

(defn activity->time-entry
  "Given an Hamster activity record, return a corresponding Harvest
  Time Tracking record."
  [activity]
  ;; see function `activity-elt->record` in hamster.clj to see
  ;; how the activity record is factored
  (let [{:keys [name category tags description duration_minutes start_time]} activity
        date              (starttime->date start_time)
        [client project]  (name->client+proj name)
        task              (category+tags->task category tags)
        notes             (description-and-more->notes description category tags)
        hours             (duration->hours duration_minutes)
        firstname         "Olivier"
        lastname          "Lange"]
        ;; beware: positional constructor; order of arguments matters hereafter
        (harvest/->HarvestTimeEntry
                          date client project task notes
                          hours firstname lastname)))
