(defproject gugus "0.1.0-SNAPSHOT"
  :description "Fast network clustering library"
  :url "https://github.com/xcoo/gugus"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/data.priority-map "0.0.7"]
                 [org.clojure/core.incubator "0.1.3"]]
  :source-paths ["src/clj"]
  :profiles {:dev {:source-paths ["src/profile"]
                   :global-vars {*warn-on-reflection* true}
                   :dependencies [[org.clojure/clojure "1.8.0"]
                                  [thunknyc/profile "0.5.2"]]
                   :jvm-opts ["-Xmx2048m"]}
             :test {:resource-paths ["test-resources"]
                    :plugins [[lein-midje "3.2"]]
                    :dependencies [[midje "1.8.3"]]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}}
  :main gugus.cli)
