(ns gugus.t-core
  (:require [midje.sweet :refer :all]
            [clojure.java.io :as io]
            [gugus.io :refer [read-pairs]]
            [gugus.core :as core]))

(def sample-result {:q 0.420654296875
                    :communities {"LLL" #{"III" "KKK" "JJJ" "LLL" "GGG" "HHH"}
                                  "FFF" #{"BBB" "DDD" "EEE" "FFF" "CCC" "AAA"}}})

(fact "cluster mm"
  (let [f (-> "sample.pairs"
              io/resource
              io/file
              .getPath)]
    (core/cluster-mm (read-pairs f))) => sample-result)

(fact "cluster mm weight"
  (let [f (-> "sample.wpairs"
              io/resource
              io/file
              .getPath)]
    (core/cluster-weight-mm (read-pairs f))) => sample-result)
