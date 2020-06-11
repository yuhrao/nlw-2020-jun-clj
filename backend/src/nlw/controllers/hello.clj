(ns nlw.controllers.hello
  (:require [ring.util.response :as ring-res]))

(def hello
  {:name ::hello
   :enter (fn [context]
            (assoc context :response (ring-res/response "Hello World")))})

(def hello-post
  {:name ::hello-post
   :enter (fn [{:keys [postgres] :as context}]
            (assoc context :response (ring-res/response {:hello "world!"
                                                         :postgres (.toString postgres)})))})

(def routes #{["/hello" :get hello]
              ["/hello" :post hello-post]})
