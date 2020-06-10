(ns nlw.components.migration
  (:require [integrant.core :as ig]
            [migrations.migrate :as migrations]))

(defmethod ig/init-key ::migration [_ {:keys [path]}]
  (let [migrations (migrations/get-migration-files path)]
    (fn [data-source]
      (migrations/up! data-source migrations))))
