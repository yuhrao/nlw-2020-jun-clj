(ns user
  (:require
   [nlw.config :as config]
   [integrant.repl :as ig-repl]
   [integrant.repl.state :as ig-state]
   [integrant.core :as ig]))

(defn prep!
  ([] (prep! :dev))
  ([env]
   (ig-repl/set-prep! (constantly (config/load env)))))

(defn go
  ([] (go :dev))
  ([env]
   (prep! env)
   (ig-repl/go)))

(defn halt!
  ([] (halt! :dev))
  ([env]
   (prep! env)
   (ig-repl/halt)))

(defn reset!
  ([] (reset! :dev))
  ([env]
   (halt! env)
   (go env)))

(defn config []
  (ig-state/config))

(defn preparer []
  (ig-state/preparer))

(defn system []
  (ig-state/system))

(comment
  (prep!)
  (go)
  )
