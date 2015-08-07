(defproject hamster-to-harvest "0.1.0-SNAPSHOT"
  :description "Migrate an XML export of Hamster facts to Harvest entries in CSV format"
  :url "https://github.com/olange/hamster-to-harvest-csv"
  :license {:name "Creative Commons Attribution-ShareAlike 4.0 International License"
            :url  "http://creativecommons.org/licenses/by-sa/4.0/"}
  :dependencies [
    [org.clojure/clojure "1.6.0"]
    [org.clojure/data.zip "0.1.1"]
  ]
  :main ^:skip-aot hamster-to-harvest.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
