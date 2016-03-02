(defproject gugus "0.1.0-SNAPSHOT"
  :description "Fast network clustering library"
  :url "https://github.com/xcoo/gugus"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/data.priority-map "0.0.7"]]
  :source-paths ["src/clj"]
  :profiles {:dev {:source-paths ["src/profile"]
                   :global-vars {*warn-on-reflection* true}
                   :dependencies [[org.clojure/clojure "1.8.0"]
                                  [thunknyc/profile "0.5.2"]]
                   :jvm-opts ["-Xmx2048m"]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.0"]]}
             :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}}
  :main gugus.cli)
