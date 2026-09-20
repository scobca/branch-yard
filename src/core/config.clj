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
  (let [cfg       (read-edn "resources/config.edn")
        sensitive (read-edn "resources/config.sensitive.edn")]
    (-> cfg
        (assoc-in [:db :username] (get-in sensitive [:db :username]))
        (assoc-in [:db :password] (get-in sensitive [:db :secret])))))