(ns hamster-to-harvest.harvest)

(defrecord HarvestTimeEntry [
  date
  client
  project
  task
  notes
  hours
  firstname
  lastname])
