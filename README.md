# elasticmq-clj

Embedded elasticmq for clojure

## Usage

[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.bigsy/elasticmq-clj.svg)](https://clojars.org/org.clojars.bigsy/elasticmq-clj)
### Development:

```clojure
(require 'elasticmq-clj.core)

;; Start a local elasticmq with default port:
(init-elasticmq)

;; another call will halt the previous system:
(init-elasticmq)

;; When you're done:
(halt-elasticmq!)
```

### Testing:

**NOTE**: these will halt running elasticmq instances

```clojure
(require 'clojure.test)

(use-fixtures :once with-elasticmq-fn)

(defn around-all
  [f]
  (with-elasticmq-fn (merge default-config)
                    f))

(use-fixtures :once around-all)

;;; You can also wrap ad-hoc code in init/halt:
(with-elasticmq default-config
	,,, :do-something ,,,)
```