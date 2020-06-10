(ns utils.namespaces)

(defn resolve-sym [sym]
  (-> sym
      requiring-resolve
      var-get))
