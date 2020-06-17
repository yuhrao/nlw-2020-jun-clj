(ns release
  (:require [badigeon.classpath :as cp]
            [badigeon.compile :as compiler]
            [badigeon.bundle :as bundler]
            [utils.namespaces :as util-ns]
            [nlw.config :as config]
            [clojure.java.io :as io]
            [integrant.core :as ig]
            [clojure.edn :as edn]))

(defn read-deps
  ([] (read-deps "deps.edn"))
  ([path]
   (-> path
       io/file
       slurp
       edn/read-string)))

(defn -main []
  (let [namespaces (util-ns/load-all-namespaces (config/load :prod)
                                                ['nlw.main
                                                 'migrations.format
                                                 'migrations.helpers
                                                 'migrations.migrate
                                                 'utils.namespaces
                                                 'utils.uuid])]
    (bundler/bundle (bundler/make-out-path 'lib nil) {:allow-unstable-deps? true})
    (compiler/compile namespaces {:classpath (cp/make-classpath {:deps-map (read-deps)})})
    (println "Compiled namespaces: " namespaces)))
