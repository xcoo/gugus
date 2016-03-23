# gugus

[![Build Status](https://travis-ci.org/xcoo/gugus.svg?branch=master)](https://travis-ci.org/xcoo/gugus)

[![Clojars Project](https://img.shields.io/clojars/v/gugus.svg)](https://clojars.org/gugus)

Fast and scalable network clustering library using [Fast Modularity](http://www.cs.unm.edu/~aaron/research/fastmodularity.htm) algorithm.

## Usage

Add the following dependency to your project.clj:

```
[gugus "0.1.0-SNAPSHOT"]
```

### Simple Clustering

```clojure
(require 'gugus.core)

(def pairs [["AAA" "BBB"]
            ["AAA" "CCC"]
            ["AAA" "DDD"]
            ["AAA" "EEE"]
            ...
            ["III" "LLL"]
            ["JJJ" "KKK"]
            ["JJJ" "LLL"]
            ["KKK" "LLL"]]) ; graph edges

(gugus.core/cluster-mm pairs)
;; => {:q 0.420654296875
;;     :communities {"LLL" #{"III" "KKK" "JJJ" "LLL" "GGG" "HHH"}
;;                   "FFF" #{"BBB" "DDD" "EEE" "FFF" "CCC" "AAA"}}}
```

### Weighted Clustering

```clojure
(require 'gugus.core)

(def pairs [["AAA" "BBB" "0.403"]
            ["AAA" "CCC" "0.135"]
            ["AAA" "DDD" "0.224"]
            ["AAA" "EEE" "0.336"]
            ...
            ["III" "LLL" "0.378"]
            ["JJJ" "KKK" "0.068"]
            ["JJJ" "LLL" "0.949"]
            ["KKK" "LLL" "0.363"]]) ; graph edges with weight

(gugus.core/cluster-weight-mm pairs)
;; => {:q 0.8618539514068584
;;     :communities {"LLL" #{"III" "KKK" "JJJ" "LLL" "GGG" "HHH"}
;;                   "FFF" #{"BBB" "DDD" "EEE" "FFF" "CCC" "AAA"}}}
```

### Test

```shell
lein with-profile test,1.8 midje
```

## Licence

Copyright [Xcoo, Inc.][xcoo.jp]

Licensed under the [Apache License, Version 2.0][apache-license-2.0].

[xcoo.jp]: https://xcoo.jp
[apache-license-2.0]: http://www.apache.org/licenses/LICENSE-2.0.html
