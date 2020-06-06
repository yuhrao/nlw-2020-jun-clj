(ns user
  (:require [nlw.server :as server]))

(defn start
  ([] (start :dev))
  ([profile]
   (server/start-server! profile)))

(defn stop
  ([] (stop :dev))
  ([profile]
   (server/stop-server! profile)))
