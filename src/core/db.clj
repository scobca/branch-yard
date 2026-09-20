(ns core.db
  (:require [next.jdbc.connection :as connection]
            [core.config :refer [config sensitive-config]])
  (:import (com.zaxxer.hikari HikariDataSource)))

(def db-spec
  (let [db-conf (:db config)
        sensitive (:db sensitive-config)]
    {:dbtype   (:type db-conf)
     :dbname   (:name db-conf)
     :host     (:host db-conf)
     :port     (:port db-conf)
     :username (:username sensitive)
     :password (:secret sensitive)}))

(defonce datasource
  (connection/->pool HikariDataSource db-spec))