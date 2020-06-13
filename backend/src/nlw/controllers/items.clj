(ns nlw.controllers.items
  (:require [utils.uuid :as uuid]
            [nlw.response :as res]
            [nlw.statements.items :as item-stmt]))

(def item->uuid (partial uuid/from-map [:title]))

(def get-all
  {:name ::get-all
   :enter (fn [{:keys [postgres] :as context}]
            (try
              (let [entities (item-stmt/fetch-all postgres) ]
                (-> context (res/->ok entities)))
              (catch Throwable _
                (-> context (res/->internal-server-error)))))})

(def routes #{["/items" :get get-all]})
