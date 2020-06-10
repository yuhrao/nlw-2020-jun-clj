(ns nlw.components.server
  (:require [integrant.core :as ig]
            [io.pedestal.http :as http]
            [io.pedestal.http.route.definition.table :as route-table]
            [io.pedestal.http.route :as route]
            [medley.core :as medley]
            [io.pedestal.http.cors :as cors]))

(defn qualify-pedestal-ks [m]
  (medley/map-keys #(keyword "io.pedestal.http" (name %)) m))

(defn- map->base-service [m]
  (-> m
      (update :routes (partial mapcat route-table/table-routes))
      (assoc :allowed-origins (cors/allow-origin {:creds true :allowed-origins (constantly true)}))
      qualify-pedestal-ks
      http/default-interceptors))

(defmethod ig/prep-key ::server [_ server]
  (-> server
      (update :config map->base-service)
      (update :config http/create-server)))

(defmethod ig/init-key ::server [_ {:keys [config]}]
  (let [server (http/start config)
        {::http/keys [routes port host]} config]
    {:instance server
     :stop     (fn [] (http/stop server))
     :url-for  (route/url-for-routes routes :port port :host host :scheme :http)
     :running? (fn [] (.isRunning (:http/server server)))}))

(defmethod ig/halt-key! ::server [_ {:keys [stop]}]
  (stop))
