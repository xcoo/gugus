(ns gugus.core
  (:require [gugus.modularity :as modularity]
            [gugus.io :as io]))

(defn cluster-mm [pairs]
  (let [rule (io/key-index-rule pairs)
        npairs (io/prepare-pairs pairs rule)
        {:keys [q communities]} (modularity/fastcomm npairs)]
    {:q q
     :communities (io/transform-communities communities rule)}))
