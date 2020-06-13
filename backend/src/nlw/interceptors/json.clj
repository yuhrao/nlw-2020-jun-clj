(ns nlw.interceptors.json
  (:require [cheshire.core :as json]
            [medley.core :as medley]
            [clojure.walk :as walk]))

(defn unqualify-keyword [k]
  ((comp keyword name) k))

(defn recursive-unqualify-keys [c]
  (walk/postwalk (fn [x]
                   (if (keyword? x)
                     (unqualify-keyword x)
                     x))
                 c))

(defn map->json [m]
  (some-> m
          recursive-unqualify-keys
          (json/encode true)))

(def json-interception
  {:name  ::parser
   :enter (fn [{:keys [request] :as context}]
            (let [{method :request-method
                   body   :body} (select-keys request [:request-method :body])]
              (assoc-in context [:request :body]
                        (cond-> body
                          true                             slurp
                          (contains? #{:put :post} method) (json/parse-string true)))))
   :leave (fn [{:keys [response] :as context}]
            (let [response-content-type (get-in response [:headers "Content-Type"] "application/json")]
              (cond-> context
                true                                         (assoc-in [:response :headers "Content-Type"] response-content-type)
                (= response-content-type "application/json") (update-in [:response :body] map->json))))})
