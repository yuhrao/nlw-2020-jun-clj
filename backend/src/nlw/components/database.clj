(ns nlw.components.database
  (:require [integrant.core :as ig]
            [next.jdbc :as jdbc]))

(defmethod ig/init-key ::postgres [_ {:keys [config migration]}]
  (let [data-source (jdbc/get-datasource config)]
    (migration data-source)
    data-source))
