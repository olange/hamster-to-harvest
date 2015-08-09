(ns hamster-to-harvest.core
  "Core package of the Hamster to Harvest migration utility."
  (:require [hamster-to-harvest.hamster :as hamster]
            [hamster-to-harvest.harvest :as harvest]
            [hamster-to-harvest.mapping :as mapping]
            [clojure.tools.cli :as cli]
            [clojure.string :as str]
            [clojure.pprint :refer :all])
  (:gen-class))

(defn migrate
  "Reads a sample Hamster XML export and dumps the parsed content"
  [options arguments]
  (let [input-filename (first arguments)
        root (hamster/read-xml input-filename)
        activities (hamster/activities->xrel root)
        time-entries (map mapping/activity->time-entry activities)]

        (println
          "\nHamster Activities:\n"
          (with-out-str (pprint activities))
          "\nHarvest Time tracking entries:\n"
          harvest/csv-header-line "\n"
          (str/join "\n" (harvest/as-csv time-entries)))))

(def cli-options [
  ["-h" "--help" "Show help"]])

(defn usage [banner summary]
  (str banner
       "\n\nUsage:"
       "\n  hamster-to-harvest [options] FILE.xml"
       "\n\nOptions:\n"
       summary))

(defn error [banner errors]
  (str banner
      "\n\n"
      (str/join \newline errors)))

(defn exit [status msg]
   (println msg)
   (System/exit status))

(defn -main
  "Reads a sample Hamster XML export and dumps the parsed content"
  [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)
        BANNER "Migrate Hamster activities (XML) to Harvest time tracking entries (CSV)"]
    (cond
      (:help options)
        (exit 0 (usage BANNER summary))
      (not= 1 (count arguments))
        (exit 1 (error BANNER ["The input filename of an Hamster XML export is expected."]))
      errors
        (exit 1 (error BANNER errors))
      :else
        (migrate options arguments))))
