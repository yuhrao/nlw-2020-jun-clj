(ns nlw.components.migration
  (:require [aero.core :as aero]
            [clojure.java.io :as io]
            [honeysql-postgres.format]
            [honeysql-postgres.helpers :as pg-helper]
            [honeysql-postgres.util :as pg-util]
            [honeysql.core :as sql]
            [honeysql.format :as sql-fmt]
            [honeysql.helpers :as sql-helper]
            [medley.core :as medley]
            [next.jdbc :as jdbc]
            [clojure.string :as str]))

(def ^:private migration-table :public.internal-migrations)

(defmethod sql-fmt/fn-handler "varchar" [_ char-count]
  (str "VARCHAR(" char-count ")" ))

(defmethod sql-fmt/fn-handler "required" [& _]
  "NOT NULL")

(defmethod sql-fmt/format-clause :order-by-desc [[_ fields] _]
  (str "ORDER BY "
       (sql-fmt/comma-join (for [field fields]
                     (if (sequential? field)
                       (let [[field & modifiers] field]
                         (str/join " "
                                      (cons (sql-fmt/to-sql field)
                                            (for [modifier modifiers]
                                              (case modifier
                                                :desc "DESC"
                                                :asc "ASC"
                                                :nulls-first "NULLS FIRST"
                                                :nulls-last "NULLS LAST"
                                                "")))))
                       (sql-fmt/to-sql field)))) " DESC"))

(sql-helper/defhelper order-by-desc [m fields]
  (assoc m :order-by-desc (sql-helper/collify fields)))

(defmethod sql-fmt/format-clause :create-schema [[_ [schema-name if-not-exists]] _]
  (str "CREATE SCHEMA "
       (when if-not-exists "IF NOT EXISTS ")
       (-> schema-name
           pg-util/get-first
           sql-fmt/to-sql)))

(defmethod sql-fmt/format-clause :drop-schema [[_ [schema-name if-exists]] _]
  (str "DROP SCHEMA "
       (when if-exists "IF EXISTS ")
       (-> schema-name
           pg-util/get-first
           sql-fmt/to-sql)))

(defn create-migration-tables! [data-source]
  (let [execute! (partial jdbc/execute! data-source)]
    (-> (pg-helper/create-table migration-table true)
        (pg-helper/with-columns [[:id :serial (sql/call :primary-key)]
                                 [:file-name :varchar (sql/call :required) (sql/call :unique)]
                                 [:created-at :timestamp (sql/call :required) :default (sql/call :now)]])
        sql/format
        execute!)))

(defmethod aero/reader 'sql/call [_ _ args]
  (apply sql/call args))

(defn get-migration-files [directory]
  (->> (io/file directory)
       file-seq
       (remove #(.isDirectory %))
       (sort)
       (map (juxt #(.getName %) aero/read-config))
       (reduce (fn [acm [filename content]]
                 (assoc acm filename content)) {})))

(defn get-migrated-files [data-source]
  (let [execute! (partial jdbc/execute! data-source)
        xforms   (map #(get % :internal_migrations/file_name))
        transducer (partial transduce xforms conj)]
    (-> (sql-helper/select :file-name)
        (sql-helper/from migration-table)
        (order-by-desc :id)
        sql/format
        execute!
        transducer)))

(defn create-migration-entry! [data-source filename]
  (let [execute! (partial jdbc/execute! data-source)]
    (-> (sql-helper/insert-into migration-table)
        (sql-helper/columns :file-name)
        (sql-helper/values [[filename]])
        sql/format
        execute!)))

(defn remove-migration-entry! [data-source filename]
  (let [execute! (partial jdbc/execute! data-source)]
    (-> (sql-helper/delete-from migration-table)
        (sql-helper/where [:= :file-name filename])
        sql/format
        execute!)))

(comment
  (-> (sql-helper/delete-from migration-table)
      (sql-helper/where [:= :file-name :filename])
        sql/format))

(defn exclude-migrated-files [data-source migration-map]
  (let [migrated-files (get-migrated-files data-source)]
    (apply dissoc migration-map migrated-files)))

(defn execute-migration-stmt! [data-source operation [file stmt]]
  (let [execute! (partial jdbc/execute! data-source)]
    (execute! stmt)
    (case operation
      :up (create-migration-entry! data-source file)
      :down (remove-migration-entry! data-source file))))

(defn migrations-up! [data-source migration-map]
  (->> migration-map
       (exclude-migrated-files data-source)
       (medley/map-vals :up)
       (medley/map-vals sql/format)
       (medley/map-kv (fn [& kv]
                        (execute-migration-stmt! data-source :up kv)))))

(defn migrations-down! [data-source migration-map]
  (let [migrated-files   (get-migrated-files data-source)
        migration-sorted (-> (into (sorted-map-by #(compare %2 %1)) migration-map)
                             (select-keys migrated-files))]
    (->> migration-sorted
         (medley/map-vals :down)
         (medley/map-vals sql/format)
         (medley/map-kv (fn [& kv]
                          (execute-migration-stmt! data-source :down kv))))))

(comment

  (create-migration-tables! nlw.components.database/data-source)

  (get-migration-files "resources/migrations")

  (get-migrated-files nlw.components.database/data-source)

  (migrations-up! nlw.components.database/data-source (get-migration-files "resources/migrations"))

  (migrations-down! nlw.components.database/data-source (get-migration-files "resources/migrations"))

  )
