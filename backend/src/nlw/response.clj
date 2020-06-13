(ns nlw.response)

(defn ->ok
  ([] (->ok nil))
  ([body] (->ok body nil))
  ([body headers]
  (cond-> {:status 200}
    body (assoc :body body)
    headers (assoc :headers headers))))

(defn ->created
  ([] (->created nil))
  ([body] (->created body nil))
  ([body headers]
  (cond-> {:status 201}
    body    (assoc :body body)
    headers (assoc :headers headers))))

(defn ->internal-server-error
  ([] (->internal-server-error nil))
  ([body] (->internal-server-error body nil))
  ([body headers]
  (cond-> {:status 500}
    body    (assoc :body body)
    headers (assoc :headers headers))))

(defn ->bad-request
  ([] (->bad-request nil))
  ([body] (->bad-request body nil))
  ([body headers]
  (cond-> {:status 400}
    body    (assoc :body body)
    headers (assoc :headers headers))))

(defn ->not-found
  ([] (->not-found nil))
  ([body] (->not-found body nil))
  ([body headers]
  (cond-> {:status 500}
    body    (assoc :body body)
    headers (assoc :headers headers))))
