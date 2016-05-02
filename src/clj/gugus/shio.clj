(ns gugus.shio
  (:require [gugus.io :as io]))

(defn map-hashmap [f hashmap]
  (->> hashmap (map f) (into {})))

(defn i [pair]
  (first pair))

(defn j [pair]
  (second pair))

(defn bidirectional-pairs [pairs]
  (concat pairs (map #(-> % reverse vec) pairs)))

(defrecord Graph
  [v e t m neighbors belongs-to members])

(defn create-graph [pairs]
  (let [vert      (set (apply concat pairs))
        neighbors (->> (bidirectional-pairs pairs)
                       (group-by i)
                       (map-hashmap (fn [[u v]] [u (map-hashmap #(vector (j %) 1) v)])))
        belongs-to (->> vert (map-hashmap #(vector % %)))
        members    (->> vert (map-hashmap #(vector % #{%})))]
    (Graph. vert
            (bidirectional-pairs pairs)
            vert
            (count pairs)
            neighbors
            belongs-to
            members
            )))

(defn gamma [graph u]
  (set (keys (get-in graph [:neighbors u]))))

(defn e [graph u v]
  (get-in graph [:neighbors u v] 0))

(defn a [graph u]
  (reduce (fn [a [k v]] (+ a (if (not= k u) v 0)))
          0 (get-in graph [:neighbors u])))

(defn dQ [graph u v]
  (let [two-m-inverse (/ 1 (* 2 (:m graph)))]
    (double (* 2 (- (* (e graph u v) two-m-inverse)
                    (* (a graph u) (a graph v)
                       two-m-inverse two-m-inverse))))))

(defn prunable-vertices [graph]
  (filter #(= (count (gamma graph %)) 1) (:v graph)))

(defn merge-neighbors [graph u v w]
  (let [u-neighbors (get-in graph [:neighbors u])
        v-neighbors (get-in graph [:neighbors v])
        ]
    (merge-with + u-neighbors v-neighbors)))

(defn aggregate [graph u v]
  (let [v' (get-in graph [:belongs-to v])]
    (-> graph
        (assoc-in [:belongs-to u] v')
        (update-in [:members v'] conj u)
        (update :members dissoc u))))

(let [pair [[:A :B] [:A :C] [:A :D] [:B :C] [:C :D]]
      graph (create-graph pair)]
  (aggregate graph :A :B))
