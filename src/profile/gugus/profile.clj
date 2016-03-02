(ns gugus.profile
  (:require [gugus.modularity :as modularity]
            [gugus.io :as io]
            [profile.core :refer [profile profile-vars]]))

(profile-vars modularity/create-network
              modularity/build-edge-table
              modularity/build-dq-heap
              modularity/build-a
              modularity/merge-community
              io/read-pairs)

(defn profile-sample []
  (profile {}
           (let [pairs (io/read-pairs "./data/sample.pairs")
                 network (modularity/create-network pairs)]
             (modularity/merge-community network))))

(defn profile-large-sample []
  (profile {}
           (let [pairs (io/read-pairs "./data/solar_cell_2012.num.pairs")
                 network (modularity/create-network pairs)]
             (modularity/merge-community network))))

(defn run-sample []
  (modularity/fastcomm (io/read-pairs "./data/sample.pairs") true))
