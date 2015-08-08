(ns hamster-to-harvest.hamster
  (:require [clojure.java.io :as io]
            [clojure.string :as str :refer [split]]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zip-xml]))

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
    ;; Integer/MAX_VALUE should be enough to represent the duration (its about 4'074 years)
    :duration_minutes (Integer/valueOf (zip-xml/attr activity :duration_minutes))
    :start_time (zip-xml/attr activity :start_time)
    :end_time (zip-xml/attr activity :end_time)
  })

(defn activities->xrel
  "Given an XML zipper on the root element of an export of Hamster activities,
  return a set of maps (an xrel), each map containing all attributes of each
  activity (see function `activity->map` above for a description of the maps)"
  [root]
  (into #{}
        (map activity->map
             (zip-xml/xml-> root :activity))))
