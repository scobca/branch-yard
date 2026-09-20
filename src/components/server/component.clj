(ns components.server.component
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [ring.adapter.jetty9 :refer [run-jetty]]
            [components.server.router :refer [app-routes]])
  (:import (org.eclipse.jetty.server Server)))

(defrecord HttpServerComponent [config datasource]
  component/Lifecycle

  (start [component]
    (let [server-cfg (:server config)
          port (:port server-cfg 8080)
          jetty-opts (merge {:join? false} server-cfg)
          server (run-jetty (app-routes component) jetty-opts)]
      (log/info "HTTP server started on port" port)
      (assoc component :server server)))

  (stop [component]
    (log/info "Stopping HTTP server...")
    (when-let [^Server server (:server component)]
      (.stop server))
    (assoc component :server nil)))

(defn http-server-component [config]
  (map->HttpServerComponent {:config config}))
