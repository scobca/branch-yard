(ns core.config
  (:require [clojure.edn :as edn])
  (:import (java.io FileNotFoundException)))

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
      (throw (ex-info "FATAL: resources/config.sensitive.edn not found! Application cannot start." {})))))

(defonce config (load-config))
(defonce sensitive-config (load-sensitive-config))
