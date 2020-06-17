(ns user
  (:require [shadow.cljs.devtools.api :as shadow]
            [shadow.cljs.devtools.server :as shadow-server]))

(defn start-server
  {:shadow/requires-server true}
  [& _]
  (shadow-server/start!)
  (shadow/browser-repl {:build-id :app}))

(defn stop-server
  {:shadow/requires-server true}
  []
  (shadow-server/stop!))

(comment
  (start-server)
  (+ 1 1)
  (stop-server))
