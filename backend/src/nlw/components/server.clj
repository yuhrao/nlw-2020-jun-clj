(ns nlw.components.server
  (:require [integrant.core :as ig]
            [io.pedestal.http :as http]
            [io.pedestal.http.route.definition.table :as route-table]
            [medley.core :as medley]
            [io.pedestal.http.cors :as cors]))

(defn qualify-pedestal-ks [m]
  (medley/map-keys #(keyword "io.pedestal.http" (name %)) m))

(defmethod ig/prep-key ::server [_ config-map]
  (-> config-map
      (update-in [:config :routes] (partial mapcat route-table/table-routes))
      (assoc-in [:config :allowed-origins] (cors/allow-origin {:creds true :allowed-origins (constantly true)}))
      (update :config qualify-pedestal-ks)
      (update :config http/create-server)))

(defmethod ig/init-key ::server [_ {:keys [config]}]
  (let [server (http/start config)]
    {:server server
     :stop (fn [] (http/stop server))}))

(defmethod ig/halt-key! ::server [_ {:keys [stop]}]
  (stop))
