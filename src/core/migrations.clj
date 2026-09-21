(ns core.migrations
  (:require [clojure.tools.logging :as log]
            [core.config :refer [config sensitive-config]]
            [migratus.core :as migratus]))

(defn build-migratus-config []
  (let [db-config (:db config)
        db-sensitive (:db sensitive-config)]
    {:store         :database
     :migration-dir "migrations"
     :db            {:dbtype   (:type db-config)
                     :dbname   (:name db-config)
                     :host     (:host db-config)
                     :port     (:port db-config)
                     :user     (:username db-sensitive)
                     :password (:secret db-sensitive)}}))

(defn -main [& args]
  (let [migratus-config (build-migratus-config)]
    (case (first args)

      "migrate" (migratus/migrate migratus-config)
      "rollback" (migratus/rollback migratus-config)
      "create" (migratus/create migratus-config (second args))

      (log/info "Usage: clojure -M:migrate [migrate|rollback|create]"))))
