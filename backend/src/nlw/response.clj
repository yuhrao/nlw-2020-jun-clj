(ns nlw.response)

(defn ->ok
  ([] (->ok nil))
  ([body] (->ok body nil))
  ([body headers]
  (cond-> {:status 200}
    body (assoc :body body)
    headers (assoc :headers headers))))

(defn ->internal-server-error
  ([] (->internal-server-error nil))
  ([body] (->internal-server-error body nil))
  ([body headers]
  (cond-> {:status 500}
    body    (assoc :body body)
    headers (assoc :headers headers))))
