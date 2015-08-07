(ns hamster-to-harvest.core
  (:require [clojure.java.io :as io]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zip-xml])
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

(defn activities->xrel
  [root]
  (into #{}
        (for [activity (zip-xml/xml-> root :activity)]
          (-> activity first :attrs))))

(defn -main
  "Reads a sample Hamster XML export and dumps the parsed content"
  [& args]
  (let [root (read-xml "sample.xml")
        activities (activities->xrel root)]
        (println activities)))
