(ns hamster-to-harvest.core
  (:require [hamster-to-harvest.hamster :as hamster]
            [hamster-to-harvest.harvest :as harvest]
            [hamster-to-harvest.mapping :as mapping]
            [clojure.string :as str]
            [clojure.pprint :refer :all])
  (:gen-class))


(defn -main
  "Reads a sample Hamster XML export and dumps the parsed content"
  [& args]
  (let [root (hamster/read-xml "sample.xml")
        activities (hamster/activities->xrel root)
        time-entries (map mapping/activity->time-entry activities)]

        (println
          "\nHamster Activities:\n"
          (with-out-str (pprint activities))
          "\nHarvest Time tracking entries:\n"
          harvest/csv-header-line "\n"
          (str/join "\n" (harvest/as-csv time-entries)))))
