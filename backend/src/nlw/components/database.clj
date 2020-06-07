(ns nlw.components.database
  (:require [next.jdbc :as jdbc]
            [honeysql.core :as sql]
            [honeysql-postgres.helpers :as pg-helper]
            [honeysql-postgres.format]))

(def db-spec {:dbtype   "postgresql"
              :dbname   "postgres"
              :host     "localhost"
              :port     5432
              :user     "nlw"
              :password "next_level"})

(def data-source (jdbc/get-datasource db-spec))

(comment
  (-> (pg-helper/create-table :nlw.t-collecttable-item)
      (pg-helper/with-columns [[:id :uuid (sql/call :primary-key)]
                               [:title :varchar :not :null]
                               [:image :varchar :not :empty]])
      #_sql/format
      #_((partial jdbc/execute! data-source)))

  (sql/format {:create-schema [:nlw]})
  (sql/format {:drop-schema [:nlw]})

  (pg-helper/drop-table :nlw.t-collect-point)

  (with-open [ds (jdbc/get-datasource db-spec)]
    (jdbc/execute! ds "SELECT * from pg_tables"))

  (jdbc/execute! data-source ["CREATE TABLE t_point (id varchar(200) CONSTRAINT firstkey PRIMARY KEY)"])

  (jdbc/execute! data-source (sql/format {:select [:*]
                                          :from   [:pg-tables]
                                          :where  [:= :tablename "nwl_migrations"]}))
  (jdbc/execute!
   data-source
   ["CREATE SCHEMA nlw"])


  )
