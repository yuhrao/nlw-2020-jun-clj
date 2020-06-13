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
              (let [entities (item-stmt/fetch-all postgres) ]
                (-> context (res/->ok entities)))
              (catch Throwable _
                (-> context (res/->internal-server-error)))))})

(def routes #{["/items" :get get-all]
              ["/items" :post save]})
