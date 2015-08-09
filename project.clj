(defproject hamster-to-harvest "0.2.0-SNAPSHOT"
  :description "Migrate from Hamster activities in XML format to Harvest entries in CSV format"
  :url "https://github.com/olange/hamster-to-harvest-csv"
  :license {:name "Creative Commons Attribution-ShareAlike 4.0 International License"
            :url  "http://creativecommons.org/licenses/by-sa/4.0/"}
  :dependencies [
    [org.clojure/clojure "1.6.0"]
    [org.clojure/data.zip "0.1.1"]
    [org.clojure/tools.cli "0.3.2"]]
  :main ^:skip-aot hamster-to-harvest.core
  :target-path "target/%s"
  :profiles {
    :uberjar {:aot :all}
    :dev {
      :dependencies [[clj-stacktrace "0.2.8"]]
      :plugins [[lein-bin "0.3.5"]] }}
  :bin {:name "hamster-to-harvest" :bin-path "./"}
  :repl-options {:caught clj-stacktrace.repl/pst+})
