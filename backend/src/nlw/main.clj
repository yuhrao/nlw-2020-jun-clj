(ns nlw.main
  (:require [nlw.config :as config]
            [integrant.core :as ig]
            [utils.namespaces :as util-ns])
  (:gen-class))

(defn -main [& _]
  (let [config (config/load :prod)]

    (ig/load-namespaces config)
    (util-ns/load-all-namespaces config ['migrations.format
                                         'migrations.helpers
                                         'migrations.migrate
                                         'utils.namespaces
                                         'utils.uuid])
    (-> config
        ig/prep
        ig/init)))
