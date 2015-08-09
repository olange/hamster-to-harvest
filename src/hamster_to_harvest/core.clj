(ns hamster-to-harvest.core
  "Core package of the Hamster to Harvest migration utility."
  (:require [hamster-to-harvest.hamster :as hamster]
            [hamster-to-harvest.harvest :as harvest]
            [hamster-to-harvest.mapping :as mapping]
            [clojure.tools.cli :as cli]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :refer :all])
  (:gen-class))

(defn output-stream
  [{:keys [output-fname]} options]
  (if output-fname
    (io/output-stream output-fname)
    *out*))

(defn migrate
  "Reads a sample Hamster XML export and dumps the parsed content"
  [options arguments]
  (let [input-fname  (first arguments)
        output-fname (:output-fname options)
        append?      (:append options)]

    (println (format "Converting Hamster activities from '%s' to Harvest time tracking entries into '%s'%s"
                      input-fname output-fname (if append? " (appending)" "")))

    (with-open [in  (io/input-stream input-fname)
                out (io/writer output-fname :append append? :encoding "UTF-8")]

      (let [root         (hamster/read-xml in)
            activities   (hamster/activities->xrel root)
            time-entries (map mapping/activity->time-entry activities)]

      (when-not append?)
        (.write out
                (str harvest/csv-header-line "\n"))
      (.write out
              (str/join "\n" (harvest/as-csv time-entries)))))))

(def cli-options [
  ["-o" "--output FILENAME" "Output filename"
    :id :output-fname :default "harvest.csv"]
  ["-a" "--append" "Appends to existing output file (overwrites otherwise)"]
  ["-h" "--help" "Show help"]])

(defn usage [banner summary]
  (str banner
       "\n\nUsage:"
       "\n  hamster-to-harvest [options] FILENAME.xml"
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
