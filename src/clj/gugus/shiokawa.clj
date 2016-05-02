(ns gugus.shiokawa
  (:require [clojure.set]
            [clojure.core.incubator :as incubator]))

;; Fast Algorithm for Modularity-Based Graph Clustering
;; c.f. https://www.aaai.org/ocs/index.php/AAAI/AAAI13/paper/view/6188/6884

(defn map-hashmap [f hashmap]
  (->> hashmap (map f) (into {})))

(defn bidirectional-pairs [pairs]
  (concat (map reverse pairs) pairs))

(defn i [pair]
  (first pair))

(defn j [pair]
  (second pair))

(defrecord Graph
  [edges vertices neighbors a e belongs-to t m])

(defn gamma [graph u]
  (get-in graph [:neighbors u]))

(defn a [graph u]
  (get-in graph [:a u] 0))

(defn e [graph u v]
  (get-in graph [:e u v] 0))

(defn dQ [graph u v]
  (let [inverse-m (double (/ 1.0 (* 2 (:m graph))))]
    (* 2 (- (* (e graph u v) inverse-m)
            (* (a graph u) (a graph v) inverse-m inverse-m)))))

(defn create-graph [pairs]
  (let [vert      (set (apply concat pairs))
        neighbors (->> (bidirectional-pairs (remove #(= (first %) (second %)) pairs))
                       (group-by i))
        weights   (->> neighbors
                       (map-hashmap (fn [[u v]] [u (map-hashmap #(vector (j %) 1) v)])))
        neighbors (->> neighbors
                       (map-hashmap (fn [[u v]] [u (set (map second v))])))
        belongs-to (->> vert (map-hashmap #(vector % %)))
        as (map-hashmap (fn [[v neighs]] [v (count neighs)]) neighbors)]
    (Graph. pairs
            vert
            neighbors
            as
            weights
            belongs-to
            vert
            (count vert))))

(defn prunable? [graph u]
  (= (count (gamma graph u)) 1))

(defn aggregate [graph u v w]
  (let [ew  (merge-with + (get-in graph [:e u]) (get-in graph [:e v]))
        eww (+ (get-in graph [:e u u] 0) (get-in graph [:e v v] 0)
               (* 2 (get-in graph [:e v u] 0)))
        aw  (+ (get-in graph [:a u]) (get-in graph [:a v]))
        affected-edges (clojure.set/intersection (gamma graph u) (gamma graph v))
        affected-graph (loop [g graph xs affected-edges]
                         (if (seq xs)
                           (recur (-> g
                                      (incubator/dissoc-in [:e (first xs) u])
                                      (incubator/dissoc-in [:e (first xs) v])
                                      (assoc-in [:e (first xs) w] (get ew (first xs)))
                                      (update-in [:neighbors (first xs)] #(as-> % x (remove #{u v} x) (conj x w) (set x))))
                                  (rest xs))
                           g))
        e-aggregated (-> affected-graph
                         (assoc-in [:e w] ew)
                         (incubator/dissoc-in [:e w u])
                         (incubator/dissoc-in [:e w v])
                         (assoc-in [:e w w] eww))
        neighbor-dropped (-> e-aggregated
                             (update-in [:neighbors w] #(->> % (remove #{u v}) set)))
        belongs-to-updated (-> neighbor-dropped
                               (assoc-in [:belongs-to u] w))
        new-graph (assoc-in belongs-to-updated [:a w] aw)]
    new-graph))

(defn shiokawa-iteration
  [graph]
  (let [ps (filter (partial prunable? graph) (:t graph))
        prune-sets (map (fn [p] (vector p (first (get-in graph [:neighbors p])))) ps)
        pruned-graph (reduce (fn [graph prune-set]
                               (aggregate graph
                                          (first prune-set)
                                          (second prune-set)
                                          (second prune-set)))
                             graph
                             prune-sets)
        t-updated (as-> pruned-graph x
                      (update x :t #(clojure.set/difference % (set (map first prune-sets))))
                      (update x :t #(clojure.set/difference % (set (filter (fn [v] (zero? (count (gamma pruned-graph v)))) (:t x))))))]
    (if (zero? (count (:t t-updated)))
      t-updated
      (let [selected-vertex (apply min-key #(count (gamma t-updated %)) (:t t-updated))
            dQ-sets (map #(vector % (dQ t-updated selected-vertex %)) (gamma t-updated selected-vertex)); (get-in t-updated [:neighbors selected-vertex]))
            vertex-v (apply max-key second dQ-sets)]
        (if (> (second vertex-v) 0)
          (-> t-updated
              (aggregate selected-vertex (first vertex-v) (first vertex-v))
              (update :t #(-> %
                              (clojure.set/difference #{(first vertex-v) selected-vertex})
                              (conj (first vertex-v)))))
          (update t-updated :t #(clojure.set/difference % #{selected-vertex})))))))

(defn shiokawa
  [pairs]
  (let [graph (create-graph pairs)]
    (shiokawa-iteration
     (last (take-while #(> (count (:t %)) 0)
                       (iterate shiokawa-iteration graph))))))
