(ns utils.uuid
  (:import java.util.UUID))

(defn map->uuid-fn [m ks]
  (fn [m]
    (-> m
      (select-keys ks)
      sort
      vals
      set
      ((partial apply str))
      .getBytes
      (UUID/nameUUIDFromBytes))))
