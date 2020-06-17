(ns nlw.config
  (:require [clojure.java.io :as io]
            [aero.core :as aero]
            [utils.namespaces :as ns-utils]
            [integrant.core :as ig]))

(def config-path "resources/config/system.edn")

(defmethod aero/reader 'ig/ref [_ _ refs]
  (ig/ref refs))

(defn load
  ([] (load :dev))
  ([profile]
   (let [file (io/file config-path)]
    (if (.exists file)
      (aero/read-config file {:profile profile})
      (throw (ex-info (str "Configuration file not found at:" config-path) {}))))))
