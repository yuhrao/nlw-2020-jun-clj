(ns nlw.server-test
  (:require [clojure.test :refer [deftest is testing]]
            [io.pedestal.http :as http]
            [nlw.main :as server]))

(deftest server-startup
  (testing "Server should start withoud crash"
    (is (try
          (server/start-server! :test)
          (Thread/sleep 500)
          (.isRunning (::http/server @server/server))
          (catch Throwable _
            false)
          (finally
            (server/stop-server!))))))
