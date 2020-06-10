(ns nlw.controllers.hello-test
  (:require [clj-http.client :as http-client]
            [clojure.test :refer [deftest is testing]]
            [matcher-combinators.test]
            [nlw.controllers.hello :as hello]
            [nlw.components.server :as server]
            [nlw.config :as config]
            [integrant.core :as ig]))

(def config (-> (config/load :test)
                ig/prep))

(deftest ^:integration server-hello-route
  (let [sys                      (ig/init config)
        {server ::server/server} sys]
    (try
      (let [url-for               (get server :url-for)
            request-url           (url-for ::hello/hello)
            {:keys [status body]} (http-client/get request-url)]
        (testing "Should return status code 200"
          (is (match? 200 status)))
        (testing "Should return hello body"
          (is (match? body "Hello World"))))
      (catch Throwable t
        (throw t))
      (finally
        (ig/halt! sys)))))
