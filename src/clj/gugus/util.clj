(ns gugus.util)

(defn str->long
  [s]
  (if-not (nil? s)
    (try
      (let [[^String n _ _] (re-matches #"(|-|\+)(\d+)" s)]
        (Long. n))
      (catch Exception e
        nil))
    nil))

(defn str->double
  [s]
  (if-not (nil? s)
    (try
      (let [[^String n _ _ _] (re-matches #"(|-|\+)(\d+)\.?(\d*)" s)]
        (Double. n))
      (catch Exception e nil))
    nil))
