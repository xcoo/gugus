(ns gugus.core
  (:require [gugus.modularity :as modularity]
            [gugus.wmodularity :as wmodularity]
            [gugus.io :as io]))

(defn cluster-mm [pairs]
  (let [rule (io/key-index-rule pairs)
        npairs (io/prepare-pairs pairs rule)
        {:keys [q communities]} (modularity/fastcomm npairs)]
    {:q q
     :communities (io/transform-communities communities rule)}))

(defn cluster-mm-w [wpairs]
  (let [rule (io/key-index-rule wpairs)
        npairs (io/prepare-pairs wpairs rule)
        {:keys [q communities]} (wmodularity/fastcomm wpairs)]
    {:q q
     :communities (io/transform-communities communities rule)}))
