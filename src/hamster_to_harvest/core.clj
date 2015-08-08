(ns hamster-to-harvest.core
  (:require [hamster-to-harvest.hamster :as hamster]
            [hamster-to-harvest.mapping :as mapping]
            [clojure.pprint :refer :all])
  (:gen-class))


(defn -main
  "Reads a sample Hamster XML export and dumps the parsed content"
  [& args]
  (let [root (hamster/read-xml "sample.xml")
        activities (hamster/activities->xrel root)
        time-entries (map mapping/activity->time-entry activities)]

        (println
          "Hamster Activities:\n"
          (with-out-str (pprint activities))
          "\n\nHarvest Time tracking entries:\n"
          (with-out-str (pprint time-entries)))))
