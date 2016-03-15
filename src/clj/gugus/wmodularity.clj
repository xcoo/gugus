(ns gugus.wmodularity
  (:require [clojure.string :as string]
            [clojure.set :refer [union]]
            [clojure.data.priority-map :as p]
            [gugus.modularity :as modularity]))

(defrecord Network [communities edge-table dq-heap a q last-edge])

(defn build-edge-table [edge-pairs]
  (let [edge-pairs (concat edge-pairs (map (fn [[i j w]] [j i w]) edge-pairs))]
    (->> edge-pairs
         (group-by first)
         (map (fn [[k es]] [k (set (map rest es))]))
         (into {}))))

(defn build-edge-group [pairs]
  (group-by first (map #(take 2 %) pairs)))

(defn build-weight-table [pairs]
  (->> (group-by first pairs)
       (map (fn [[k es]]
              [k (map last es)]))
       (into {})))

(defn build-dq-heap [edge-pairs edge-group c]
  (let [heap (p/priority-map-by >)
        all-weights (apply + (map last edge-pairs))]
    (reduce
      (fn [hp k] (assoc hp k ;; (vec (sort k)) -> crash!
                        0)) ;; TODO
      heap edge-pairs)))

(defn build-a [edge-pairs edge-group c]
  (let [all-nodes (->> edge-pairs (mapcat #(take 2 %)) set)
        all-weights (apply + (map last edge-pairs))
        weight-table (build-weight-table edge-pairs)]
    (reduce (fn [a i]
              (assoc a i (double (apply + (map (fn [w] (/ w (* 2.0 all-weights))) (get weight-table i))))))
              {} all-nodes)))

(defn drop-weights [pairs]
  (map #(take 2 %) pairs))

; (let [pairs (gugus.io/read-pairs "./test-resources/sample.nwpairs")]
;   (build-dq-heap pairs nil nil))

(defn create-network [pairs]
  (let [pairs-with-weights pairs
        pairs (set (pmap sort (drop-weights pairs)))
        edge-group (build-edge-group pairs-with-weights)
        c (count pairs)
        initial-communities (reduce (fn [m n] (assoc m n #{n})) {} (set (apply concat pairs)))
        initial-edge-table (build-edge-table pairs-with-weights)
        initial-dq-heap (build-dq-heap pairs-with-weights edge-group c)
        initial-a (build-a pairs edge-group c)
        ]
    (Network. initial-communities initial-edge-table initial-dq-heap initial-a 0 nil)))

(defn fastcomm
  ([pairs]
   (fastcomm pairs nil))
  ([pairs log-out]
   (loop [net (create-network pairs)
          max-q Double/NEGATIVE_INFINITY
          max-comm nil]
     (when (and log-out (:last-edge net))
       (println (clojure.string/join "\t" (:last-edge net))))
     (if (pos? (count (:dq-heap net)))
       (recur (modularity/merge-community net)
              (max max-q (double (:q net)))
              (if (< max-q (:q net))
                (:communities net)
                max-comm))
       {:q max-q
        :communities max-comm}))))
