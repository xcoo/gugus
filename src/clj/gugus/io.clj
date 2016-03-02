(ns gugus.io
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.set :refer [map-invert]]
            [gugus.util :refer [str->long str->double]]))

(defn key-index-rule
  [pairs]
  (let [nodes (->> pairs
                   (map #(take 2 %))
                   flatten
                   distinct)
        key->index (zipmap nodes (iterate inc 0))
        index->key (map-invert key->index)]
    {:key->index key->index
     :index->key index->key}))

(defn prepare-pairs
  [pairs rule]
  (vec (pmap (fn [[k1 k2 w]]
               (let [edge [(get (:key->index rule) k1)
                           (get (:key->index rule) k2)]]
                 (if w
                   (conj edge w)
                   edge)))
             pairs)))

(defn transform-communities
  [communities rule]
  (->> communities
       (pmap (fn [[k v]]
                     [(get (:index->key rule) k)
                      (set (pmap #(get (:index->key rule) %) v))]))
       (reduce (fn [coll [k v]]
                 (assoc coll k v)) {})))

;;; string key

(defn- read-pairs-aux
  [f]
  (with-open [r (io/reader f)]
    (doall
     (mapv (fn [line]
             (vec (string/split line #"\s")))
           (line-seq r)))))

(defn- read-wpairs-aux
  [f]
  (with-open [r (io/reader f)]
    (doall
     (mapv (fn [line] (let [row (string/split line #"\s")
                            edge (vec (take 2 row))
                            v (str->double (last row))]
                        (conj edge v)))
           (line-seq r)))))

;;; numbered

(defn- read-numbered-pairs-aux
  [f]
  (with-open [r (io/reader f)]
    (doall
     (mapv (fn [line] (->> (string/split line #"\s")
                           (map str->long)
                           sort
                           vec))
           (line-seq r)))))

(defn- read-numbered-wpairs-aux
  [f]
  (with-open [r (io/reader f)]
    (doall
     (mapv (fn [line] (let [row (string/split line #"\s")
                            edge (mapv str->long (take 2 row))
                            v (str->double (last row))]
                        (conj edge v)))
           (line-seq r)))))

;;; export

(defn read-pairs
  [f]
  (let [ext (last (string/split f #"\."))]
    (case ext
      "pairs" (read-pairs-aux f)
      "wpairs" (read-wpairs-aux f)
      "npairs" (read-numbered-pairs-aux f)
      "nwpairs" (read-numbered-wpairs-aux f))))
