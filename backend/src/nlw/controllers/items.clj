(ns nlw.controllers.items
  (:require [utils.uuid :as uuid]
            [nlw.response :as res]
            [nlw.statements.items :as item-stmt]))

(def item->uuid (partial uuid/from-map [:title]))

(def save
  {:name  ::save
   :enter (fn [{:keys [request postgres] :as context}]
            (let [{:keys [body]} request
                  item-id        (item->uuid body)
                  entity         (assoc body :id item-id)]
              (try
                (item-stmt/save! postgres entity)
                (-> context
                    (res/->created entity))
                (catch Throwable _
                  (-> context (res/->internal-server-error))))))})

(def get-all
  {:name  ::get-all
   :enter (fn [{:keys [postgres] :as context}]
            (try
              (let [entities (item-stmt/fetch-all postgres)
                    cnt      (count entities)]
                (-> context (res/->ok entities {"X-Entities-Count" (str cnt)})))
              (catch Throwable _
                (-> context (res/->internal-server-error)))))})

(def by-id
  {:name  ::by-id
   :enter (fn [{:keys [request postgres] :as context}]
            (try
              (let [{{:keys [id]} :path-params} request
                    entity                      (item-stmt/fetch-by-id postgres (uuid/from-str id))]

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

(def routes #{["/items" :get get-all]
              ["/items/:id" :get by-id]
              ["/items" :post save]})
