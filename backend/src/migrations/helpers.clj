(ns migrations.helpers
  (:require [honeysql.helpers :as sql-helper]))

(sql-helper/defhelper order-by-desc [m fields]
  (assoc m :order-by-desc (sql-helper/collify fields)))
