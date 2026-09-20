(ns components.server.router
  (:require [compojure.core :refer [routes GET]]
            [components.server.middleware :as mw]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.middleware.cors :refer [wrap-cors]]))

(defn health-routes []
  [(GET "/health" [] {:status 200 :body "{\"status\":\"ok\"}"})])

(defn app-routes [component]
  (-> (apply routes (health-routes))
      (mw/wrap-exceptions)
      (mw/wrap-deps component)
      (mw/wrap-not-found)
      (wrap-keyword-params)
      (wrap-params)
      (wrap-json-params)
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :post :put :delete :patch])
      (mw/wrap-request-logging)))
