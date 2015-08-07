(ns hamster-to-harvest.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str :refer [split]]
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

(defn activity->map
  "Given an activity element, return its attributes as a map,
  converting the source data to Clojure structures"
  [activity]
  {
    :name     (zip-xml/attr activity :name)
    :category (zip-xml/attr activity :category)
    :tags     (split (zip-xml/attr activity :tags) #",\s*")
    :description (zip-xml/attr activity :description)
    :duration_minutes (Integer/valueOf (zip-xml/attr activity :duration_minutes))
    :start_time (zip-xml/attr activity :start_time)
    :end_time (zip-xml/attr activity :end_time)
  })

(defn activities->xrel
  [root]
  (into #{}
        (map activity->map
             (zip-xml/xml-> root :activity))))

(defn -main
  "Reads a sample Hamster XML export and dumps the parsed content"
  [& args]
  (let [root (read-xml "sample.xml")
        activities (activities->xrel root)]
        (println activities)))
