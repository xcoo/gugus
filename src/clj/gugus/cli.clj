(ns gugus.cli
  (:require [gugus.core :refer [cluster-mm]]
            [gugus.io :as io]
            [clojure.pprint :refer [pprint]])
  (:gen-class))

(defn -main [& args]
  (when (pos? (count args))
    (let [f (first args)
          pairs (io/read-pairs f)
          {:keys [q communities]} (cluster-mm pairs)]
      (pprint  communities)
      (println "Q value =" q))
    (shutdown-agents)))
