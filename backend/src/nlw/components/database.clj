(ns nlw.components.database
  (:require [next.jdbc :as jdbc]
            [integrant.core :as ig]))

(defmethod ig/init-key ::postgres [_ {:keys [config migrate]}]
  (let [data-source (jdbc/get-datasource config)
        connection  (jdbc/get-connection config)]

    (migrate data-source)
    {:data-source data-source
     :conn        connection}))

(comment
  (def db-spec {:dbtype   "postgresql"
                :dbname   "postgres"
                :host     "localhost"
                :port     5432
                :user     "nlw"
                :password "next_level"})

  (def data-source (jdbc/get-datasource db-spec)))
