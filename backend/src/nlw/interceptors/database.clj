(ns nlw.interceptors.database)

(defn postgres-interceptor [database]
  {:name ::injector
   :enter (fn [context]
            (assoc context :postgres database))})
