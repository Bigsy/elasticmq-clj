(ns elasticmq-clj.elasticmq
  (:require [clojure.java.io :as io]
            [integrant.core :as ig]
            [clojure.tools.logging :as log])
  (:import [java.io File]
           [java.nio.file Files Paths LinkOption Path]
           [java.nio.file.attribute FileAttribute]))

(def ^:private download-url "https://s3-eu-west-1.amazonaws.com/softwaremill-public/elasticmq-server-1.3.9.jar")

(def ^:private elasticmq-directory (str (System/getProperty "user.home") File/separator ".clj-elasticmq-local"))

(def ^:private host {:name    (System/getProperty "os.name")
                     :version (System/getProperty "os.version")
                     :arch    (System/getProperty "os.arch")})

(defn- ->path
  "Create a path from the given strings."
  [str & strs]
  {:pre [(string? str) (every? string? strs)]}
  (Paths/get str (into-array String strs)))

(defn- path?
  "Is the given argument a path?"
  [x]
  (instance? Path x))

(defn- exists?
  "Does the given path exist?"
  [path]
  {:pre [(path? path)]}
  (Files/exists path (into-array LinkOption [])))

(defn- ensure-elasticmq-directory
  "Make sure the directory that elasticmq Local will be downloaded to
  exists."
  []
  (let [path (->path elasticmq-directory)]
    (when-not (exists? path)
      (-> (Files/createDirectory path (make-array FileAttribute 0))
          (.toString)))))



(defn- build-elasticmq-command
  "Build a java command to start elasticmq Local with the required
  options."
  [conf-path]
  (let [conf-path (when conf-path (str "-Dconfig.file=" conf-path))
        jar-path (str (io/file elasticmq-directory "elasticmq.jar"))]
    (if conf-path
      (format "java %s -jar %s" conf-path jar-path)
      (format "java -jar %s" jar-path))))

(defn start-elasticmq
  "Start elasticmq Local with the desired options."
  [config]
  (let [elasticmq (->> (build-elasticmq-command config)
                       (.exec (Runtime/getRuntime)))]
    (log/info "Started elasticmq Local")
    elasticmq))

(defn- download-elasticmq
  "Download elasticmq."
  [url]
  (log/info "Downloading elasticmq Local" {:elasticmq-directory elasticmq-directory})
  (ensure-elasticmq-directory)
  (io/copy (io/input-stream (io/as-url url)) (io/as-file (str elasticmq-directory "/" "elasticmq.jar"))))

(defn ensure-installed
  "Download and elasticmq Local if it hasn't been already."
  []
  (when-not (exists? (->path elasticmq-directory "elasticmq.jar"))
    (download-elasticmq download-url)))


(defn handle-shutdown
  "Kill the elasticmq Local process on JVM shutdown."
  [elasticmq-process]
  (doto elasticmq-process (.destroy) (.waitFor))
  (log/info (str "Exited" {:exit-value (.exitValue elasticmq-process)})))

(defn create-elasticmq-db-logger
  [log]
  (fn [& message]
    (apply log "elasticmq-local:" message)))

(defn halt! [elasticmq]
  (when elasticmq
    (handle-shutdown elasticmq)))

(defmethod ig/init-key ::elasticmq [_ config]
  (ensure-installed)
  (start-elasticmq config))

(defmethod ig/halt-key! ::elasticmq [_ elasticmq]
  (halt! elasticmq))