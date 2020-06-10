(ns nlw.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route.definition.table :as route-table]
            [nlw.controllers.hello :as hello]
            [io.pedestal.http.cors :as cors]))

(defn add-local-config [m]
  (-> m
      (http/dev-interceptors)
      (assoc ::http/join? false)))

(def app-routes (route-table/table-routes (concat hello/routes)))

(def base-config {::http/routes    app-routes
                  ::http/type      :immutant
                  ::http/file-path "resources/static/"
                  ::http/port      3000})
