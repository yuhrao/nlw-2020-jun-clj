(ns migrations.migrate
  (:require [aero.core :as aero]
            [clojure.java.io :as io]
            [honeysql-postgres.helpers :as pg-helper]
            [honeysql-postgres.format]
            [honeysql.core :as sql]
            [honeysql.helpers :as sql-helper]
            [medley.core :as medley]
            [migrations.helpers :as migration-helper]
            [next.jdbc :as jdbc]))

(def ^:private migration-table :public.internal-migrations)

(defn- create-migration-tables! [data-source]
  (let [execute! (partial jdbc/execute! data-source)]
    (-> (pg-helper/create-table migration-table true)
        (pg-helper/with-columns [[:id :serial (sql/call :primary-key)]
                                 [:file-name :varchar (sql/call :required) (sql/call :unique)]
                                 [:created-at :timestamp (sql/call :required) :default (sql/call :now)]])
        sql/format
        execute!)))

(defmethod aero/reader 'sql/call [_ _ args]
  (apply sql/call args))

(defn- get-migrated-files [data-source]
  (let [execute! (partial jdbc/execute! data-source)
        xforms   (map #(get % :internal_migrations/file_name))
        transducer (partial transduce xforms conj)]
    (-> (sql-helper/select :file-name)
        (sql-helper/from migration-table)
        (migration-helper/order-by-desc :id)
        sql/format
        execute!
        transducer)))

(defn- create-migration-entry! [data-source filename]
  (let [execute! (partial jdbc/execute! data-source)]
    (-> (sql-helper/insert-into migration-table)
        (sql-helper/columns :file-name)
        (sql-helper/values [[filename]])
        sql/format
        execute!)))

(defn- remove-migration-entry! [data-source filename]
  (let [execute! (partial jdbc/execute! data-source)]
    (-> (sql-helper/delete-from migration-table)
        (sql-helper/where [:= :file-name filename])
        sql/format
        execute!)))

(defn- exclude-migrated-files [data-source migration-map]
  (let [migrated-files (get-migrated-files data-source)]
    (apply dissoc migration-map migrated-files)))

(defn- execute-migration-stmt! [data-source operation [file stmt]]
  (let [execute! (partial jdbc/execute! data-source)]
    (execute! stmt)
    (case operation
      :up (create-migration-entry! data-source file)
      :down (remove-migration-entry! data-source file))))

(defn up! [data-source migration-map]
  (create-migration-tables! data-source)
  (->> migration-map
       (exclude-migrated-files data-source)
       (medley/map-vals :up)
       (medley/map-vals sql/format)
       (medley/map-kv (fn [& kv]
                        (execute-migration-stmt! data-source :up kv)))))

(defn down! [data-source migration-map]
  (create-migration-tables! data-source)
  (let [migrated-files   (get-migrated-files data-source)
        migration-sorted (-> (into (sorted-map-by #(compare %2 %1)) migration-map)
                             (select-keys migrated-files))]
    (->> migration-sorted
         (medley/map-vals :down)
         (medley/map-vals sql/format)
         (medley/map-kv (fn [& kv]
                          (execute-migration-stmt! data-source :down kv))))))

(defn get-migration-files [directory]
  (->> (io/file directory)
       file-seq
       (remove #(.isDirectory %))
       (sort)
       (map (juxt #(.getName %) aero/read-config))
       (reduce (fn [acm [filename content]]
                 (assoc acm filename content)) {})))
