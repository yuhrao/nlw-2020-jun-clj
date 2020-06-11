(ns nlw.interceptors.json
  (:require [cheshire.core :as json]
            ))

(def json-interception
  {:name  ::json
   :enter (fn [{:keys [request] :as context}]
            (let [{method :request-method
                   body   :body} (select-keys request [:request-method :body])]
              (assoc-in context [:request :body]
                        (cond-> body
                          true                             slurp
                          (contains? #{:put :post} method) (json/parse-string true)))))
   :leave (fn [context]
            (update-in context [:response :body] #(json/encode % true)))})
