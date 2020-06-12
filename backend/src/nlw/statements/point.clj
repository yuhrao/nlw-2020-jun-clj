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
