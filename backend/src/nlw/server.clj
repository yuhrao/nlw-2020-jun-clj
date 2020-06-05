(ns nlw.server
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.route.definition.table :as route-table]
            [ring.util.response :as ring-res]
            [io.pedestal.http.cors :as cors]))

(def hello
  {:name ::hello
   :enter (fn [context]
            (assoc context :responde (ring-res/response "Hello World")))})

(def app-routes (route-table/table-routes #{["/hello" :get hello]}))
(def url-for (route/url-for-routes app-routes))
(defn -main [& _]
  (doto "Hello World!"
    prn))
