(ns nlw.controllers.hello
  (:require [nlw.response :as res]))

(def hello
  {:name ::hello
   :enter (fn [context]
            (assoc context :response (res/->ok "Hello World")))})

(def hello-post
  {:name ::hello-post
   :enter (fn [{:keys [postgres] :as context}]
            (assoc context :response (res/->ok {:hello "world!"
                                                         :postgres (.toString postgres)})))})

(def routes #{["/hello" :get hello]
              ["/hello" :post hello-post]})
