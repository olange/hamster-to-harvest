(ns hamster-to-harvest.core-test
  "Complete process integration test"
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clojure.data :as data :refer [diff]]
            [hamster-to-harvest.core :refer :all]))

(def INPUT-XML-FNAME "resources/hamster-sample.xml")
(def OUTPUT-CSV-FNAME "resources/harvest-sample.test.csv")
(def EXPECTED-OUTPUT-CSV-FNAME "resources/harvest-sample.csv")

(defn integration-fixture
  [testfn]
  ;; Complete process: parsing the command-line, reading an XML file,
  ;; mapping to Harvest time entries and writing them to a CSV file
  (do (-main INPUT-XML-FNAME "--output" OUTPUT-CSV-FNAME))
  (testfn))

(use-fixtures :once integration-fixture)

(defn- read-file-as-set [fname]
  (with-open [rdr (io/reader fname)]
    (into #{} (line-seq rdr))))

(deftest test-complete-process
  (testing "Complete process"
    (let [expected-csv (read-file-as-set EXPECTED-OUTPUT-CSV-FNAME)
          actual-csv   (read-file-as-set OUTPUT-CSV-FNAME)]
      (is (nil? (first (diff actual-csv
                             expected-csv)))
          "Reporting lines from the actual CSV file, that do not match the sample CSV file"))))
