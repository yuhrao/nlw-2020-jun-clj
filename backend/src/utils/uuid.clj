(ns utils.uuid
  (:import java.util.UUID))

(defn from-map [ks m]
  (-> m
      (select-keys ks)
      sort
      vals
      set
      ((partial apply str))
      .getBytes
      (UUID/nameUUIDFromBytes)))

(defn from-str [s]
  (UUID/fromString s))
