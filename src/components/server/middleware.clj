(ns components.server.middleware
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [ring.util.response :as response]))

(defn wrap-deps
  "Inject datasource into every request under :deps."
  [handler deps]
  (fn [request]
    (handler (assoc request :deps deps))))

(defn wrap-request-logging
  [handler]
  (fn [request]
    (log/info "→" (:request-method request) (:uri request))
    (let [resp (handler request)]
      (log/info "←" (:status resp))
      resp)))

(defn wrap-exceptions
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception ex
        (log/error "Unhandled exception:" (.getMessage ex) ex)
        (-> (response/response (json/write-str {:error "Internal server error"}))
            (response/status 500)
            (response/header "Content-Type" "application/json"))))))

(defn wrap-not-found
  [handler]
  (fn [request]
    (let [resp (handler request)]
      (if (:status resp)
        resp
        {:status  404
         :headers {"Content-Type" "application/json"}
         :body    (json/write-str {:error "Not found"})}))))
