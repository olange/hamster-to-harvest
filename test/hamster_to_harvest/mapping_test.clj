(ns hamster-to-harvest.mapping-test
  "Mapping script unit tests"
  (:require [clojure.test :refer :all]
            [hamster-to-harvest.mapping :refer :all]))

(deftest test-starttime->date
  (testing "Converting Hamster datetime to Harvest date only"
    (is (= "2011-01-07"
           (starttime->date "2011-01-07 09:00:00")))))

(deftest test-duration->hours
  (testing "120 minutes are 2.0 hours"
    (is (= 2.0 (duration->hours 120))))
  (testing "20 minutes are equivalent to 0.3333333333333333 hours"
    (is (== (/ 1.0 3.0) (duration->hours 20))))
  (testing "10 minutes is equivalent to 0.16666666666666666 hours"
    (is (== (/ 1.0 6.0) (duration->hours 10)))))

(def sample-hamster-activity
  { :name        "RZOhomepage"
    :description "Etude nouvelle mise en forme homepage"
    :category    "offert"
    :tags        ["Design graphique" "facturé"]
  })

(deftest test-name-and-more->client+proj
  (testing "Retrieving Harvest client and project names from Hamster activity"
    (is (= ["Client RZO" "Refonte homepage"]
           (name-and-more->client+proj  (:name sample-hamster-activity)
                                        (:category sample-hamster-activity)
                                        (:tags sample-hamster-activity))))))

(deftest test-category+tags-and-more->task
 (testing "Retrieving Harvest task name from Hamster activity"
   (is (= "Design graphique"
          (category+tags-and-more->task (:category sample-hamster-activity)
                                        (:tags sample-hamster-activity)
                                        (:name sample-hamster-activity))))))

(deftest test-description-and-more->notes
  (testing "Retrieving Harvest description from Hamster activity details"
    (is (= "++ Offert ++ Etude nouvelle mise en forme homepage (facturé) [transcrit de Hamster]"
           (description-and-more->notes (:description sample-hamster-activity)
                                        (:category sample-hamster-activity)
                                        (:tags sample-hamster-activity))))))
