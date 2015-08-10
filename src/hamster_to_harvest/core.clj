(ns hamster-to-harvest.core
  "Core package of the Hamster to Harvest migration utility."
  (:require [hamster-to-harvest.hamster :as hamster]
            [hamster-to-harvest.harvest :as harvest]
            [hamster-to-harvest.mapping :as mapping]
            [clojure.tools.cli :as cli]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn])
  (:gen-class))

(defn output-stream
  "Given the command-line options, create an `OutputStream` out of the
  `--output` option filename, or return the `*out*` stream if none was specified"
  [{:keys [output-fname]} options]
  (if output-fname
    (io/output-stream output-fname)
    *out*))

(defn make-activity-filter-pred
  "Given a name, return a predicate to filter Hamster activities
  matching that name; if it is not specified, yield a predicate
  constantly returning true"
  [activity-name]
  (if (nil? activity-name)
    (constantly true)
    (fn name-eq [activity] (= activity-name (:name activity)))))

(defn summary
  [input-fname output-fname append? filter-name]
  (str
    (format "Converting Hamster activities from '%s'\nto Harvest time tracking entries into '%s'"
            input-fname output-fname)
    (if append? " (appending)" "")
    (if filter-name (format "\nfiltering activities on name '%s'" filter-name) "")))

(defn migrate
  "Reads all activities from an Hamster XML export file given as the
  the first (and only) argument, filter them according to the options,
  convert them to Harvest time Tracking entries and export the results
  in CSV format to the output file given in the options."
  [arguments options config]
  (let [input-fname   (first arguments)
        output-fname  (:output-fname options)
        append?       (:append? options)

        filter-name   (:filter-name options)
        activity-selector (make-activity-filter-pred filter-name)

        firstname     (or (:firstname config) "***Firstname")
        lastname      (or (:lastname config) "***Lastname")
        activity->time-entry (partial mapping/activity->time-entry firstname lastname)]

    (println (summary input-fname output-fname append? filter-name))

    (with-open [in  (io/input-stream input-fname)
                out (io/writer output-fname :append append? :encoding "UTF-8")]

      (let [root         (hamster/read-xml in)
            activities   (hamster/activities->xrel root)
            activities   (filter activity-selector activities)
            time-entries (map activity->time-entry activities)]

        (.write out (if append? "\n"
                                (str harvest/csv-header-line "\n")))
        (.write out (str/join "\n"
                              (harvest/as-csv time-entries)))))))

(defn load-config
  "Given the name of a configuration file, read and return
  the [EDN](http://edn-format.org) datastructure it contains"
  [fname]
  (edn/read-string (slurp fname)))

(def cli-options [
  ["-o" "--output FILENAME" "Output filename"
    :id :output-fname :default "harvest.csv"]
  ["-a" "--append" "Appends to existing output file (overwrites otherwise)"
    :id :append?]
  [nil "--filter:name NAME" "Filter Hamster activities on given project name"
    :id :filter-name]
  [nil "--config FILENAME" "File to read configuration from"
    :id :config-fname :default "hamster-to-harvest.conf"]
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
        BANNER "Migrate Hamster activities (XML) to Harvest time tracking entries (CSV)"
        config (load-config (:config-fname options))]
    (cond
      (:help options)
        (exit 0 (usage BANNER summary))
      (not= 1 (count arguments))
        (exit 1 (error BANNER ["The input filename of an Hamster XML export is expected."]))
      errors
        (exit 1 (error BANNER errors))
      :else
        (migrate arguments options config))))
