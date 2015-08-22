(ns hamster-to-harvest.harvest
  (:require [clojure.string :as str]))

(defrecord HarvestTimeEntry [
  date
  client
  project
  task
  notes
  hours
  firstname
  lastname])

(def csv-header-line
  "\"Date\",\"Client\",\"Project\",\"Task\",\"Notes\",\"Hours\",\"First name\",\"Last name\"")

(defn quoted [s]
  ;; inspired by [clojure/data.csv](https://github.com/clojure/data.csv)
  (let [quote-char \"]
    (str quote-char
         (str/escape s {quote-char (str quote-char quote-char)})
         quote-char)))

(defn entry-as-vec
  [entry]
  [ (:date entry)
    (quoted (:client entry))
    (quoted (:project entry))
    (quoted (:task entry))
    (quoted (:notes entry))
    (:hours entry)
    (quoted (:firstname entry))
    (quoted (:lastname entry)) ])

(defn entry-as-csv
  [entry]
  (str/join \, (entry-as-vec entry)))

(defn as-csv
  "Return a Harvest time tracking record as a CSV line, quoting and escaping
  all values, but the date and hours attributes, which should remain unquoted
  (as the Harvest API expects them)"
  [entries]
  (map entry-as-csv entries))
