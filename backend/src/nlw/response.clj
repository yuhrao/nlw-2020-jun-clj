(ns nlw.response
  (:require [medley.core :as medley]))

(defn- response-header [response header-map]
  (if header-map
    (update response :headers merge header-map)
    response))

(defn- response-body [response body]
  (if body
    (assoc response :body body)
    response))

(defn- response-status [response status]
  (assoc response :status status))

(defn ->response [status]
  (let [res-fn (fn [context body header-map]
                 (-> context
                     (update :response merge {})
                     (update :response response-status status)
                     (update :response response-body body)
                     (update :response response-header header-map)))]
    (fn
      ([context] (res-fn context nil nil))
      ([context body] (res-fn context body nil))
      ([context body header-map] (res-fn context body header-map)))))

(def ->ok
  (->response 200))

(def ->created
  (->response 201))

(def ->bad-request
  (->response 400))

(def ->not-found
  (->response 404))

(def ->internal-server-error
  (->response 500))
