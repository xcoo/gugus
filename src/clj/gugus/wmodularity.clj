(ns gugus.wmodularity
  (:require [clojure.string :as string]
            [clojure.set :refer [union]]
            [clojure.data.priority-map :as p]
            [gugus.modularity :as modularity]))

(defn map-hashmap [f hashmap]
  (->> hashmap
       (map f)
       (into {})))

(defrecord Network [communities edge-table dq-heap a q last-edge])

(defn i [wpair]
  (nth wpair 0))

(defn j [wpair]
  (nth wpair 1))

(defn weight [wpair]
  (nth wpair 2))

(defn flip-pair [pair]
  (vector (j pair) (i pair) (weight pair)))

(defn bidirectional-pairs [wpairs]
  (concat (map flip-pair wpairs) wpairs))

(defn build-a [wpairs]
  (let [wpairs (bidirectional-pairs wpairs)
        global-weights (apply + (map weight wpairs))
        edge-group (group-by i wpairs)]
    (->> edge-group
         (map (fn [[i es]] [i (apply + (map #(/ (weight %) (* 2 global-weights)) es))]))
         (into {}))))

(defn build-dq-heap [wpairs a]
  (let [global-weights (apply + (map weight wpairs))
        dq-heap (p/priority-map-by >)]
    (reduce (fn [dq pair]
              (let [eij (/ (weight pair) (* 2.0 global-weights))]
                (assoc dq (vector (i pair) (j pair)) (* 2.0 (- eij (* (get a (i pair)) (get a (j pair))))))))
            dq-heap wpairs)))

(defn build-edge-table
  [wpairs]
  (let [wpairs (bidirectional-pairs wpairs)]
    (->> wpairs
         (group-by i)
         (map-hashmap (fn [[i es]] (vector i (set (map j es))))))))

(defn create-network [wpairs]
  (let [initial-communities (reduce (fn [m n] (assoc m n #{n})) {} (set (concat (map i wpairs) (map j wpairs))))
        initial-edge-table  (build-edge-table wpairs)
        initial-a (build-a wpairs)
        initial-dq-heap (build-dq-heap wpairs initial-a)]
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
