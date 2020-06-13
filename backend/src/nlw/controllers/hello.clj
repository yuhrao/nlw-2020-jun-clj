(ns nlw.controllers.hello
  (:require [nlw.response :as res]))

(def hello
  {:name ::hello
   :enter (fn [context]
            (-> context (res/->ok "Hello World")))})

(def hello-post
  {:name  ::hello-post
   :enter (fn [{:keys [postgres] :as context}]
            (-> context(res/->ok {:hello    "world!"
                                  :postgres (.toString postgres)})))})

(def routes #{["/hello" :get hello]
              ["/hello" :post hello-post]})
