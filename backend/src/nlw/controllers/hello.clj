(ns nlw.controllers.hello
  (:require [ring.util.response :as ring-res]))

(def hello
  {:name ::hello
   :enter (fn [context]
            (assoc context :response (ring-res/response "Hello World")))})

(def routes #{["/hello" :get hello]})
