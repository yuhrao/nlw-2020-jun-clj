(ns nlw.controllers.hello-test
  (:require [clj-http.client :as http-client]
            [clojure.test :refer [deftest is testing]]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.test :as pedestal-test]
            matcher-combinators.test
            [nlw.controllers.hello :as hello]
            [nlw.main :as server]
            [nlw.service :as service]))

(def test-service (::http/service-fn (-> service/base-config
                                         (service/map->service-map :test)
                                         http/create-servlet)))

(def url-for (route/url-for-routes service/app-routes))
(def full-url-for (route/url-for-routes service/app-routes :host "localhost" :port 3000 :scheme "http"))

(deftest local-hello-route
  (let [hello-route (url-for ::hello/hello)]
    (let [{:keys [body status]} (pedestal-test/response-for test-service :get hello-route)]
      (testing "Should return status code 200"
        (is (match? 200 status)))
      (testing "Should return hello body"
        (is (match? body "Hello World"))))))

(deftest ^:integration server-hello-route
  (try
    (server/start-server! :test)
    (let [request-url           (full-url-for ::hello/hello)
          {:keys [status body]} (http-client/get request-url)]
      (testing "Should return status code 200"
        (is (match? 200 status)))
      (testing "Should return hello body"
        (is (match? body "Hello World"))))
    (catch Throwable t
      (throw t))
    (finally
      (server/stop-server!))))
