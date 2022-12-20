(ns elasticmq-clj.core
  (:require [clojure.pprint :as pprint]
            [clojure.tools.logging :as log]
            [integrant.core :as ig]
            [org.httpkit.client :as http]
            [elasticmq-clj.state :as state]))

(def default-config
  nil)

(defn ->ig-config [config-path]
  {:elasticmq-clj.elasticmq/elasticmq config-path})

(defn halt-elasticmq! []
  (when @state/state
    (swap! state/state
           (fn [s]
             (ig/halt! (:system s))
             nil))))

(defmacro retry
  [cnt expr]
  (letfn [(go [cnt]
            (if (zero? cnt)
              expr
              `(try ~expr
                    (catch Exception e#
                      (retry ~(dec cnt) ~expr)))))]
    (go cnt)))

(defn init-elasticmq
  ([] (init-elasticmq default-config))
  ([config]
   (let [ig-config (->ig-config config)
         config-pp (with-out-str (pprint/pprint config))]
     (log/info "starting elasticmq with config:" config-pp)
     (try
       (halt-elasticmq!)
       (ig/load-namespaces ig-config)
       (reset! state/state
               {:system (ig/init ig-config)
                :config ig-config})
       (retry 30 (when (:error @(http/get (format "http://localhost:%s/" "9325")))
                   (do (Thread/sleep 100) (throw (Exception.)))))
       (catch clojure.lang.ExceptionInfo ex
         (ig/halt! (:system (ex-data ex)))
         (throw (.getCause ex)))))))

(defn with-elasticmq-fn
  "Startup with the specified configuration; executes the function then shuts down."
  ([config f]
   (try
     (init-elasticmq config)
     (f)
     (finally
       (halt-elasticmq!))))
  ([f]
   (with-elasticmq-fn default-config f)))

(defmacro with-elasticmq
  "Startup with the specified configuration; executes the body then shuts down."
  [config & body]
  `(with-elasticmq-fn ~config (fn [] ~@body)))

(comment (init-elasticmq)
         (halt-elasticmq!))