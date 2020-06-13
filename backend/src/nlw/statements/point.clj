(ns nlw.statements.point
  (:require [honeysql.core :as sql]
            [honeysql.format]
            [honeysql.helpers :as sql-helper]
            #_[honeysql-postgres.helpers :as pg-helper]
            [next.jdbc :as jdbc]))

(def ^:private table-name :nlw.t-collect-point)

(defn save! [data-source & entities]
  (let [execute! (partial jdbc/execute! data-source)]
    (-> (sql-helper/insert-into table-name)
        (sql-helper/values entities)
        sql/format
        execute!)))

(defn fetch-all
  ([data-source]
   (let [execute! (partial jdbc/execute! data-source)]
     (-> (sql-helper/select :*)
         (sql-helper/from table-name)
         sql/format
         execute!)))
  ([data-source queries]
   (let [execute! (partial jdbc/execute! data-source)]
     (-> (sql-helper/select :*)
         (sql-helper/from table-name)
         (sql-helper/where queries)
         sql/format
         execute!))))
