(ns sql
  (:require [honeysql.core :as sql]
            [honeysql.format :as sql-fmt]
            [honeysql-postgres.helpers :as pg-helper]
            [migrations.format]
            [migrations.helpers]
            [honeysql-postgres.format]))

(-> (pg-helper/create-table :nlw.point-item)
    (pg-helper/with-columns [[:id :uuid (sql/call :primary-key)]
                             [:point-id :uuid (sql/call :foreign :nlw.t-collect-point :id)]
                             [:item-id :uuid (sql/call :foreign :nlw.t-collection-item :id)]]))

(defn create-table-data [create-table & columns]
  (-> (pg-helper/create-table create-table)
      (pg-helper/with-columns columns)))

(defn create-table-stmt [& args]
  (sql/format (apply create-table-data args)))

(create-table-stmt :nlw.point-item
                   [:id :uuid (sql/call :primary-key)]
                   [:point-id :uuid (sql/call :foreign :nlw.t-collect-point :id)]
                   [:item-id :uuid (sql/call :foreign :nlw.t-collection-item :id)])
