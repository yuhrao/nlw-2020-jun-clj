(ns nlw.components.server
  (:require [integrant.core :as ig]
            [io.pedestal.http :as http]
            [io.pedestal.http.cors :as cors]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.route.definition.table :as route-table]
            [io.pedestal.interceptor :as interceptor]
            [medley.core :as medley]))

(defn qualify-pedestal-ks [m]
  (medley/map-keys #(keyword "io.pedestal.http" (name %)) m))

(defn- map->base-service [m]
  (-> m
      (update :routes (partial mapcat route-table/table-routes))
      (assoc :allowed-origins (cors/allow-origin {:creds true :allowed-origins (constantly true)}))
      qualify-pedestal-ks
      http/default-interceptors))

(defn map->interceptor [{:keys [type value args]}]
  (let [interceptor-map (case type
                          :map  value
                          :function (apply value args))]
    (interceptor/map->Interceptor interceptor-map)))

(defn- add-default-interceptors [default-interceptors]
  (let [interceptors (map map->interceptor default-interceptors)]
    (fn [config]
      (update config ::http/interceptors (comp vec (partial concat interceptors))))))

(defmethod ig/prep-key ::server [_ {:keys [default-interceptors] :as server}]
  (-> server
      (update :config map->base-service)
      (update :config (add-default-interceptors default-interceptors))
      (update :config http/create-server)
      (dissoc :default-interceptors)))

(defmethod ig/init-key ::server [_ {:keys [config]}]
  (let [server (http/start config)
        {::http/keys [routes port host]} config]
    {:instance server
     :stop     (fn [] (http/stop server))
     :url-for  (route/url-for-routes routes :port port :host host :scheme :http)
     :running? (fn [] (.isRunning (:http/server server)))}))

(defmethod ig/halt-key! ::server [_ {:keys [stop]}]
  (stop))
