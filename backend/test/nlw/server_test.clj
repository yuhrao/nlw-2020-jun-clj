(ns nlw.server-test
  (:require [nlw.server :as server]
            [clojure.test :refer [deftest is]]))

(deftest nothing
  (is (= 1 1)))
