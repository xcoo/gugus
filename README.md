# gugus

[![Build Status](https://travis-ci.org/xcoo/gugus.svg?branch=master)](https://travis-ci.org/xcoo/gugus)

Fast and scalable network clustering library using [Fast Modularity](http://www.cs.unm.edu/~aaron/research/fastmodularity.htm) algorithm.

## Usage

```
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

### Test

```
lein run -- network.pairs
```

## Licence

Copyright [Xcoo, Inc.][xcoo.jp]

Licensed under the [Apache License, Version 2.0][apache-license-2.0].

[xcoo.jp]: https://xcoo.jp
[apache-license-2.0]: http://www.apache.org/licenses/LICENSE-2.0.html
