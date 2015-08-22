(ns hamster-to-harvest.hamster
  "Definition of Hamster activity record and helpers to read and
  parse an Hamster exported XML file into Hamster activity records."
  (:require [clojure.java.io :as io]
            [clojure.string :as str :refer [split]]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zip-xml]))

(defrecord HamsterActivity [
  name
  category
  tags
  description
  duration_minutes
  start_time
  end_time])

(defn read-xml
  "Given an input stream to an XML document, read and parse it,
  and return an XML zipper over the parsed document"
  [input]
  (-> input
      xml/parse
      zip/xml-zip))

(defn activity-elt->record
  "Given an activity element, return a corresponding `HamsterActivity` record,
  which is a map with all the values of the element, casted to Clojure data structures"
  [activity]
  (map->HamsterActivity {
    :name     (zip-xml/attr activity :name)
    :category (zip-xml/attr activity :category)
    :tags     (split (zip-xml/attr activity :tags) #",\s*")
    :description (zip-xml/attr activity :description)
    ;; Integer/MAX_VALUE should be enough to represent the duration (its about 4'074 years)
    :duration_minutes (Integer/valueOf (zip-xml/attr activity :duration_minutes))
    :start_time (zip-xml/attr activity :start_time)
    :end_time (zip-xml/attr activity :end_time)
  }))

(defn activities->xrel
  "Given an XML zipper on the root element of an export of Hamster activities,
  return a set of records (an xrel), each record containing the attributes of each
  activity (see function `activity-elt->record` above for a description of the maps)"
  [root]
  (into #{}
        (map activity-elt->record
             (zip-xml/xml-> root :activity))))
