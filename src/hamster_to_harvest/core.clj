(ns hamster-to-harvest.core
  (:require [hamster-to-harvest.hamster :as hamster])
  (:gen-class))


(defn -main
  "Reads a sample Hamster XML export and dumps the parsed content"
  [& args]
  (let [root (hamster/read-xml "sample.xml")
        activities (hamster/activities->xrel root)]
        (println activities)))
