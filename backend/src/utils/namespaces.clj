(ns utils.namespaces
  (:require [integrant.core :as ig]))

(defn resolve-sym [sym]
  (-> sym
      requiring-resolve
      var-get))

(defn symbol->namespace [sym]
  (or (some-> sym namespace symbol) sym))

(def try-require-symbols-xf
  (comp (filter symbol?)
        (map symbol->namespace)
        (distinct)
        (keep #'ig/try-require)))

(defn keyword->namespace [kw]
  (some-> kw namespace symbol))

(def try-require-keywords-ns-xf
  (comp
   (filter keyword?)
   (map keyword->namespace)
   (remove nil?)
   (distinct)
   (keep #'ig/try-require)))

(def try-require-every-ns-xf
  (comp
   (filter #(or (symbol? %) (keyword? %)))
   (map #(or (keyword->namespace %) (symbol->namespace %)))
   (remove nil?)
   (distinct)
   (keep #'ig/try-require)))

(defn depth-first-seq
  ([coll]
   (depth-first-seq identity coll))
  ([xf coll]
   (sequence xf (tree-seq coll? seq coll))))

(defn load-symbol-namespaces [coll]
  (depth-first-seq try-require-symbols-xf coll))

(defn load-keyword-namespaces [coll]
  (depth-first-seq try-require-keywords-ns-xf coll))

(defn load-every-namespace [coll]
  (depth-first-seq try-require-every-ns-xf coll))
