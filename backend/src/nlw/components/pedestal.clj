(ns nlw.components.pedestal
  (:require [integrant.core :as ig]
            [utils.namespaces :as util-ns]
            [io.pedestal.http :as http]
            [io.pedestal.http.cors :as cors]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.route.definition.table :as route-table]
            [io.pedestal.interceptor :as interceptor]
            [medley.core :as medley]))

(defn qualify-pedestal-ks [m]
  (medley/map-keys #(keyword "io.pedestal.http" (name %)) m))

(defn process-routes [routes]
  (->> routes
      (map util-ns/resolve-sym)
      (mapcat route-table/table-routes)))

(defn- map->base-service [m]
  (-> m
      (update :routes process-routes)
      (assoc :allowed-origins (cors/allow-origin {:creds true :allowed-origins (constantly true)}))
      qualify-pedestal-ks
      http/default-interceptors))

(defn map->interceptor [{:keys [type value args]}]
  (let [interceptor-map (case type
                          :map value
                          :fn  (apply value args))]
    (interceptor/map->Interceptor interceptor-map)))

(defn- add-default-interceptors [default-interceptors]
  (if default-interceptors
    (let [interceptors (->> default-interceptors
                            (map #(update % :value util-ns/resolve-sym))
                            (map map->interceptor))]
      (fn [config]
        (update config ::http/interceptors (comp vec #(concat % interceptors)))))
    identity))

(defmethod ig/init-key ::service [_ {:keys [default-interceptors config]}]
  (-> config
      map->base-service
      ((add-default-interceptors default-interceptors))
      http/create-server))

(defmethod ig/init-key ::server [_ {:keys [service]}]
  (let [server (http/start service)
        {::http/keys [routes port host]} service]
    {:instance server
     :stop     (fn [] (http/stop server))
     :url-for  (route/url-for-routes routes :port port :host host :scheme :http)
     :running? (fn [] (.isRunning (:http/server server)))}))

(defmethod ig/halt-key! ::server [_ {:keys [stop]}]
  (stop))
