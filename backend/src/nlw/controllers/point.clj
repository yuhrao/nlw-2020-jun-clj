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
                  (assoc context :response (res/->internal-server-error))))))})

(def get-all
  {:name ::get-all
   :enter (fn [{:keys [postgres] :as context}]
            (try
              (let [entities (point-stmt/fetch-all postgres)]
                (assoc context :response (res/->ok entities)))
              (catch Throwable t
                (assoc context :response (res/->internal-server-error)))))})

(def routes #{["/points" :post save]
              ["/points" :get get-all]})
