(ns nlw.statements.point
  (:require [honeysql.core :as sql]
            [honeysql.format]
            [honeysql.helpers :as sql-helper]
            #_[honeysql-postgres.helpers :as pg-helper]
            [next.jdbc :as jdbc]))

(def ^:private point-table :nlw.t-collect-point)
(def ^:private item-table :nlw.t-collection-item)
(def ^:private point-item-table :nlw.t-point-item)

(defn save! [data-source & entities]
  (let [execute! (partial jdbc/execute! data-source)]
    (-> (sql-helper/insert-into point-table)
        (sql-helper/values entities)
        sql/format
        execute!)))

(defn fetch-point-items [data-source id]
  (let [execute! (partial jdbc/execute! data-source)]
     (-> (sql-helper/select :item.title [:item.image :image-url])
      (sql-helper/from [point-item-table :point-item])
      (sql-helper/join [item-table :item] [:= :point-item.item-id :item.id])
      (sql-helper/where [:= :point-item.point-id id])
      sql/format
      execute!)))

(defn- add-point-items* [data-source entity]
  (when entity
    (let [point-id (get entity :t_collect_point/id)]
      (assoc entity :t_collect_point/items (fetch-point-items data-source point-id)))))

(defn fetch-all
  ([data-source]
   (let [execute! (partial jdbc/execute! data-source)
         add-point-items (partial map (partial add-point-items* data-source))]
     (-> (sql-helper/select :*)
         (sql-helper/from point-table)
         sql/format
         execute!
         add-point-items)))
  ([data-source queries]
   (let [execute! (partial jdbc/execute! data-source)
         add-point-items (partial map (partial add-point-items* data-source))]
     (-> (sql-helper/select :*)
         (sql-helper/from point-table)
         (sql-helper/where queries)
         sql/format
         execute!
         add-point-items))))

(defn fetch-by-id [data-source id]
  (let [execute! (partial jdbc/execute! data-source)
        add-point-items (partial add-point-items* data-source)]
    (some-> (sql-helper/select :*)
            (sql-helper/from point-table)
            (sql-helper/where [:= :id id])
            sql/format
            execute!
            first
            add-point-items)))
