(ns nlw.server
  (:require [io.pedestal.http :as http]
            [nlw.service :as service]))

(defonce server (atom nil))

(defn stop-server! []
  (swap! server http/stop))

(defn start-server!
  ([] (start-server! :dev))
  ([profile]
   (let [server* (-> service/base-config
                    (service/map->service-map profile)
                    http/create-server)]
     (when @server
       (stop-server!))
     (reset! server (http/start server*)))))

(defn -main [& _]
  (start-server! :prod))
