(ns nlw.controllers.point
  (:require [nlw.statements.point :as point-stmt]
            [utils.uuid :as uuid]
            [nlw.response :as res]))

(def point->uuid (partial uuid/from-map [:email :name]))

(def save
  {:name  ::save
   :enter (fn [{:keys [request postgres] :as context}]
            (let [{:keys [body]} request
                  point-id       (point->uuid body)
                  entity         (assoc body :id point-id)]
              (try
                (point-stmt/save! postgres entity)
                (-> context (res/->created entity))
                (catch Throwable _
                  (-> context
                      (res/->internal-server-error))))))})

(def get-all
  {:name  ::get-all
   :enter (fn [{:keys [postgres] :as context}]
            (try
              (let [entities (point-stmt/fetch-all postgres)
                    cnt      (count entities)]
                (-> context
                    (res/->ok entities {"X-Entities-Count" (str cnt)})))
              (catch Throwable _
                (-> context
                    (res/->internal-server-error)))))})

(def by-id
  {:name  ::by-id
   :enter (fn [{:keys [request postgres] :as context}]
            (try
              (let [{{:keys [id]} :path-params} request
                    entity                      (point-stmt/fetch-by-id postgres (uuid/from-str id))]

                (if  entity
                  (-> context
                      (res/->ok entity))
                  (-> context
                      (res/->not-found))))
              (catch IllegalArgumentException _
                (-> context
                    (res/->bad-request)))
              (catch Throwable _
                (-> context
                    (res/->internal-server-error)))))})

(def routes #{["/points" :get get-all]
              ["/points/:id" :get by-id]
              ["/points" :post save]})
