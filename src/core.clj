(ns core
  (:require [com.stuartsierra.component :as component]
            [core.config :refer [fetch-config]]
            [components.datasource :refer [datasource-component]]
            [components.server.component :refer [http-server-component]])
  (:gen-class))

(defn create-system []
  (let [config (fetch-config)]
    (component/system-map
      :datasource
      (datasource-component config)

      :http-server
      (component/using
        (http-server-component config)
        [:datasource]))))

(defn -main [& _args]
  (let [system (-> (create-system)
                   (component/start))]
    (.addShutdownHook
      (Runtime/getRuntime)
      (Thread. #(component/stop system)))
    (println "BranchYard started.")))
