(ns nlw.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route.definition.table :as route-table]
            [nlw.controllers.hello :as hello]
            [io.pedestal.http.cors :as cors]))

(defn add-local-config [m]
  (-> m
      (http/dev-interceptors)
      (assoc ::http/join? false)))

(defn add-prod-config [m]
  (-> m
      (assoc ::http/join? true)
      (assoc ::http/host "0.0.0.0")
      (assoc ::http/port 80)
      (assoc ::http/allowed-origins (cors/allow-origin {:creds true :allowed-origins (constantly true)}))))


(defn map->service-map
  ([m] (map->service-map m :dev))
  ([m profile]
   (let [base-config (-> m
                         (http/default-interceptors))]
     (cond-> base-config
           (contains? #{:dev :test} profile) (add-local-config)
           (= profile :prod)                 (add-prod-config)))))

(def app-routes (route-table/table-routes (concat hello/routes)))

(def base-config {::http/routes    app-routes
                  ::http/type      :immutant
                  ::http/file-path "resources/static/"
                  ::http/port      3000})
