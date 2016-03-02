(ns gugus.modularity
  (:require [clojure.string :as string]
            [clojure.set :refer [union]]
            [clojure.data.priority-map :as p]))

(defrecord Network [communities edge-table dq-heap a q last-edge])

(defn build-edge-table
  ([edge-pairs]
   (let [edge-pairs (concat edge-pairs (map reverse edge-pairs))]
     (->> edge-pairs
          (group-by first)
          (map (fn [[k es]] [k (set (map second es))]))
          (into {}))))
  ([edge-pairs with-weight]
   (let [edge-pairs (concat edge-pairs (map reverse edge-pairs))]
     (->> edge-pairs
          (group-by first)
          (map (fn [[k es]] [k (set (map second es))]))
          (into {})))))

(defn build-dq-heap
  [edge-pairs edge-group c]
  (let [heap (p/priority-map-by >)]
    (reduce
      (fn [hp k] (assoc hp k ;; (vec (sort k)) -> crash!
                        (double (- (/ 0.5 c)
                                   (* (/ (count (get edge-group (first k))) (* 2 c))
                                      (/ (count (get edge-group (second k))) (* 2 c)))))))
      heap edge-pairs)))

(defn build-a [edge-pairs edge-group c]
  (->> edge-pairs (apply concat) set
       (pmap (fn [i] [i (double (/ (count (get edge-group i)) (* 2 c)))]))
       (into {})))

(defn create-network
  ([pairs]
   (let [pairs (set (pmap sort pairs))
         edge-group (group-by first pairs)
         c (count pairs)
         initial-communities (reduce (fn [m n] (assoc m n #{n})) {} (set (apply concat pairs)))
         initial-edge-table (build-edge-table pairs)
         initial-dq-heap (build-dq-heap pairs edge-group c)
         initial-a (build-a pairs edge-group c)
         ]
     (Network. initial-communities initial-edge-table initial-dq-heap initial-a 0 nil)))
  ([pairs with-weight]
   (let [wpairs (set pairs)
         edge-group (group-by first pairs)
         c (count pairs)
         initial-communities (reduce (fn [m n w] (assoc m n #{n})) {} (set (apply concat pairs)))
         initial-edge-table (build-edge-table pairs true)
         initial-dq-heap (build-dq-heap pairs edge-group c true)
         initial-a (build-a pairs edge-group c true)
         ]
     (Network. initial-communities initial-edge-table initial-dq-heap initial-a 0 nil))))

(defn merge-community [network]
  (let [[[i j] v] (peek (:dq-heap network))
        new-communities (:communities network)
        new-communities (assoc new-communities j
                               (clojure.set/union (get (:communities network) i)
                                                  (get (:communities network) j)))
        new-communities (dissoc new-communities i)
        new-a (assoc (:a network) j (+ (get (:a network) i) (get (:a network) j)) i 0)
        edge-with-j (get (:edge-table network) j)
        edge-with-i (get (:edge-table network) i)
        edge-ij (clojure.set/intersection edge-with-j edge-with-i)
        edge-with-j (clojure.set/difference edge-with-j edge-ij)
        edge-with-i (clojure.set/difference edge-with-i edge-ij)
        new-edge-table (update (:edge-table network) j clojure.set/union (get (:edge-table network) i)) ;; add new reachable edges
        new-edge-table (reduce (fn [e-table e] (update e-table e disj i)) new-edge-table (get (:edge-table network) i))
        new-edge-table (reduce (fn [e-table e] (update e-table e conj j)) new-edge-table (get (:edge-table network) i))
        new-edge-table (update new-edge-table j disj j)
        new-edge-table (dissoc new-edge-table i)
        dq-ij (keep (fn [e]
                      (let [jk (vec (sort [j e])) ik (vec (sort [i e]))]
                        (when (and (apply not= jk)); (not= e i)) ;; TODO
                          [jk ik
                           (+ (get (:dq-heap network) ik)
                              (get (:dq-heap network) jk))])))
                    edge-ij)
        dq-i  (keep (fn [e]
                      (let [jk (vec (sort [j e])) ik (vec (sort [i e]))]
                        (when (and (apply not= jk)); (not= e i))
                          [jk ik
                           (- (get (:dq-heap network) ik)
                              (* 2 (get (:a network) j) (get (:a network) e)))])))
                    edge-with-i)
        dq-j  (keep (fn [e]
                      (let [jk (vec (sort [j e])) ik (vec (sort [i e]))]
                        (when (and (apply not= jk)); (not= e i))
                          [jk ik
                           (- (get (:dq-heap network) jk)
                              (* 2 (get (:a network) i) (get (:a network) e)))])))
                    edge-with-j)
        new-dq-heap (pop (:dq-heap network))
        new-dq-heap (reduce (fn [dq-heap dq] (assoc dq-heap (first dq) (last dq))) new-dq-heap dq-ij)
        new-dq-heap (reduce (fn [dq-heap dq] (assoc dq-heap (first dq) (last dq))) new-dq-heap dq-i)
        new-dq-heap (reduce (fn [dq-heap dq] (assoc dq-heap (first dq) (last dq))) new-dq-heap dq-j)
        new-dq-heap (dissoc new-dq-heap [i j])
        new-dq-heap (reduce (fn [dq-heap dq] (dissoc dq-heap (second dq))) new-dq-heap dq-ij)
        new-dq-heap (reduce (fn [dq-heap dq] (dissoc dq-heap (second dq))) new-dq-heap dq-i)
        new-dq-heap (reduce (fn [dq-heap dq] (dissoc dq-heap (second dq))) new-dq-heap dq-j)]
    (Network. new-communities new-edge-table new-dq-heap new-a (+ (:q network) v) [i j])))

(defn fastcomm
  ([pairs]
   (fastcomm pairs nil))
  ([pairs log-out]
   (loop [net (create-network pairs)
          max-q Double/NEGATIVE_INFINITY
          max-comm nil]
     (when (and log-out (:last-edge net))
       (println (str (clojure.string/join "\t" (:last-edge net)) "\t" (:q net))))
     (if (pos? (count (:dq-heap net)))
       (recur (merge-community net)
              (max max-q (double (:q net)))
              (if (< max-q (:q net))
                (:communities net)
                max-comm))
       {:q max-q
        :communities max-comm}))))

(defn fastcomm-w
  ([pairs]
   (fastcomm pairs nil))
  ([pairs log-out]
   (loop [net (create-network-w pairs)
          max-q Double/NEGATIVE_INFINITY
          max-comm nil]
     (when (and log-out (:last-edge net))
       (println (str (clojure.string/join "\t" (:last-edge net)) "\t" (:q net))))
     (if (pos? (count (:dq-heap net)))
       (recur (merge-community net)
              (max max-q (double (:q net)))
              (if (< max-q (:q net))
                (:communities net)
                max-comm))
       {:q max-q
        :communities max-comm}))))
