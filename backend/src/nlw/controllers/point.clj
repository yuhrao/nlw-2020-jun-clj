(ns nlw.controllers.point
  (:require [nlw.statements.point :as point-stmt]
            [utils.uuid :as uuid]
            [nlw.response :as res]))

(def point->uuid (uuid/map->uuid-fn :email :name))

(def save
  {:name ::save
   :enter (fn [{:keys [request postgres] :as context}]
            (let [{:keys [body]} request
                  point-id (point->uuid body)
                  entity (assoc body :id point-id)]
              (try
                (point-stmt/save! postgres entity)
                (assoc context :response (res/->ok entity))
                (catch Throwable t
                  (println t)
                  (assoc context :response (res/->internal-server-error))))))})

(def routes #{["/point" :post post-point-interceptor]})
(def routes #{["/points" :post save]
