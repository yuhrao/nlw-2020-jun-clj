(ns nlw.components.database
  (:require [integrant.core :as ig]
            [next.jdbc :as jdbc]))

(defmethod ig/init-key ::postgres [_ {:keys [config migration]}]
  (let [data-source (jdbc/get-datasource config)]
    (migration data-source)
    {:data-source data-source
     :db-spec config}))

(comment
  (def db-spec {:dbtype   "postgresql"
                :dbname   "postgres"
                :host     "localhost"
                :port     5432
                :user     "nlw"
                :password "next_level"})

  (def data-source (jdbc/get-datasource db-spec)))
