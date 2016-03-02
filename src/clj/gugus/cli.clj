(ns gugus.cli
  (:require [gugus.modularity :as modularity]
            [gugus.io :as io]
            [clojure.pprint :refer [pprint]])
  (:gen-class))

(defn -main [& args]
  (when (pos? (count args))
    (let [f (first args)
          pairs (io/read-pairs f)
          rule (io/key-index-rule pairs)
          npairs (io/prepare-pairs pairs rule)
          {:keys [q communities]} (modularity/fastcomm npairs)]
      (pprint (io/transform-communities communities rule))
      (println "Q value =" q))
    (shutdown-agents)))
