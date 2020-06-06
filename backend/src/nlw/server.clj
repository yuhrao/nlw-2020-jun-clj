(ns nlw.server
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.route.definition.table :as route-table]
            [ring.util.response :as ring-res]
            [io.pedestal.http.cors :as cors]))

(def hello
  {:name ::hello
   :enter (fn [context]
            (assoc context :response (ring-res/response "Hello World")))})

(def app-routes (route-table/table-routes #{["/hello" :get hello]}))

(defonce server (atom nil))

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

(defn map->service
  ([m] (map->service m :dev))
  ([m profile]
   (let [base-config (-> m
                         (http/default-interceptors))]
     (cond-> base-config
           (contains? #{:dev :test} profile) (add-local-config)
           (= profile :prod)                 (add-prod-config)))))

(def base-map {::http/routes app-routes
               ::http/type   :immutant
               ::http/port   3000})

(defn stop-server! []
  (swap! server http/stop))

(defn start-server!
  ([] (start-server! :dev))
  ([profile]
   (let [server* (-> base-map
                    (map->service profile)
                    http/create-server)]
     (when @server
       (stop-server!))
     (reset! server (http/start server*)))))

(defn -main [& _]
  (start-server! :prod))
