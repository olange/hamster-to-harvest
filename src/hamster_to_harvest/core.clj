(ns hamster-to-harvest.core
  (:require [clojure.java.io :as io]
            [clojure.xml :as xml]
            [clojure.zip :as zip])
  (:gen-class))

(defn read-xml
  "Given the filename of an XML document, read and parse it,
  and return an XML zipper over the parsed document"
  [fname]
  (-> fname
      io/resource
      io/file
      xml/parse
      zip/xml-zip))

(defn -main
  "Reads a sample Hamster XML export and dumps the parsed content"
  [& args]
  (println (read-xml "sample.xml")))
