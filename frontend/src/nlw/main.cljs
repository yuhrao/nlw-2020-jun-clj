(ns nlw.main
  (:require [reagent.dom :as rdom]
            [reagent.core :as r]))

(def state (r/atom {:name "Yuhri"}))

(defn index []
  [:div "Hello" (:name @state)])

(defn main []
  (rdom/render
   (index)
   (.getElementById js/document "app")))

(defn ^:dev/after-load force-reload []
  (main))
