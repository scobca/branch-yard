(ns core.config
  (:require [clojure.edn :as edn])
  (:import (java.io FileNotFoundException)))

(defn- read-edn [path]
  (try
    (edn/read-string (slurp path))
    (catch FileNotFoundException _
      (throw (ex-info (str "FATAL: " path " not found!") {})))))

(defn fetch-config
  "Returns merged config map for use in components."
  []
  (let [cfg (read-edn "resources/config.edn")
        sensitive (read-edn "resources/config.sensitive.edn")]
    (-> cfg
        (assoc-in [:db :username] (get-in sensitive [:db :username]))
        (assoc-in [:db :password] (get-in sensitive [:db :secret])))))

(defn load-config
  "Load common properties from resources."
  []
  (try
    (edn/read-string (slurp "resources/config.edn"))
    (catch FileNotFoundException _
      (throw (ex-info "FATAL: resources/config.edn not found! Application cannot start." {})))))

(defn load-sensitive-config
  "Load sensitive properties from resources."
  []
  (try
    (edn/read-string (slurp "resources/config.sensitive.edn"))
    (catch FileNotFoundException _
      (println "WARN: config.sensitive.edn not found, using empty sensitive config.")
      {})))

(defonce config (load-config))
(defonce sensitive-config (load-sensitive-config))