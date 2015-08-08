(ns hamster-to-harvest.mapping
  (:require [hamster-to-harvest.harvest :as harvest]
            [clojure.string :as str :refer [split]]))

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
  [(str \C name) (str \P name)])

(defn category+tags->task
  "Given the category and tags (a vector of strings) of an Hamster activity,
  return the matching task for Harvest; this mapping is specific to each Harvest user"
  [category tags]
  (str \T category ";" tags))

(defn activity->time-entry
  "Given an Hamster activity record, return a corresponding Harvest
  Time Tracking record."
  [activity]
  ;; see function `activity-elt->record` in hamster.clj to see
  ;; how the activity record is factored
  (let [date              (starttime->date (:start_time activity))
        [client project]  (name->client+proj (:name activity))
        task              (category+tags->task (:category activity) (:tags activity))
        notes             (:description activity)
        hours             (duration->hours (:duration_minutes activity))
        firstname         "Olivier"
        lastname          "Lange"]
        ;; beware: positional constructor; order of arguments matters hereafter
        (harvest/->HarvestTimeEntry
                          date client project task notes
                          hours firstname lastname)))
