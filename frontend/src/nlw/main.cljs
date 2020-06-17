(ns nlw.main
  (:require [reagent.dom :as rdom]
            [reagent.core :as r]))

(def atm (r/atom {:name "Yuhri"}))

(defn index []
  [:h1 (str "Hello " (:name @atm) " !!")])

(defn main []
  (rdom/render
   (index)
   (.getElementById js/document "app")))

(defn ^:dev/after-load force-reload []
  (main))
