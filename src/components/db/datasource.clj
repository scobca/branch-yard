(ns components.db.datasource
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [next.jdbc.connection :as connection])
  (:import (com.zaxxer.hikari HikariDataSource)))

(defrecord DatasourceComponent [config datasource]
  component/Lifecycle

  (start [component]
    (log/info "Starting datasource...")
    (let [db-cfg (:db config)
          spec   {:dbtype   (:type db-cfg)
                  :dbname   (:name db-cfg)
                  :host     (:host db-cfg)
                  :port     (:port db-cfg)
                  :username (:username db-cfg)
                  :password (:password db-cfg)}
          ds     (connection/->pool HikariDataSource spec)]
      (log/info "Datasource started.")
      (assoc component :datasource ds)))

  (stop [component]
    (log/info "Stopping datasource...")
    (when-let [^HikariDataSource ds (:datasource component)]
      (.close ds))
    (assoc component :datasource nil)))

(defn datasource-component [config]
  (map->DatasourceComponent {:config config}))