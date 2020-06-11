(ns nlw.components.database
  (:require [integrant.core :as ig]
            [next.jdbc :as jdbc]))

(defmethod ig/init-key ::postgres [_ {:keys [config migrations]}]
  (let [data-source (jdbc/get-datasource config)]
    (when migrations (migrations data-source))
    data-source))
