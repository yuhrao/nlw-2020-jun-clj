(ns nlw.server-test
  (:require [clojure.set :as set]
            [clojure.test :refer [deftest is testing]]
            [io.pedestal.http :as http]
            [io.pedestal.test :as pedestal-test]
            [matcher-combinators.test]
            [nlw.server :as server]
            [clj-http.client :as http-client]
            [io.pedestal.http.route :as route]))

(def test-service (::http/service-fn (-> server/base-map
                                         (server/map->service :test)
                                         http/create-servlet)))

(defn service-map->interceptors-name [service]
  (->> service
       ::http/interceptors
       (map :name)
       set))

(deftest map->service-dev
  (let [dev-config           (server/map->service server/base-map :dev)
        dev-interceptors     (-> server/base-map
                                 http/dev-interceptors
                                 service-map->interceptors-name)
        default-interceptors (-> server/base-map
                                 http/default-interceptors
                                 service-map->interceptors-name)]
    (testing "Development service shoudn't block main thread"
      (is (false? (get dev-config ::http/join?))))
    (testing "Development service should have default interceptors"
      (is (match? default-interceptors (set/intersection (service-map->interceptors-name dev-config) default-interceptors))))
    (testing "Development service should have dev interceptors"
      (is (match? dev-interceptors (set/intersection (service-map->interceptors-name dev-config) dev-interceptors))))))

(deftest map->service-test
  (let [test-config          (server/map->service server/base-map :test)
        dev-interceptors     (-> server/base-map
                                 http/dev-interceptors
                                 service-map->interceptors-name)
        default-interceptors (-> server/base-map
                                 http/default-interceptors
                                 service-map->interceptors-name)]
    (testing "Test service shoudn't block main thread"
      (is (false? (get test-config ::http/join?))))
    (testing "Test service should have default interceptors"
      (is (match? default-interceptors (set/intersection (service-map->interceptors-name test-config) default-interceptors))))
    (testing "Test service should have dev interceptors"
      (is (match? dev-interceptors (set/intersection (service-map->interceptors-name test-config) dev-interceptors))))))

(deftest map->service-prod
  (let [prod-config          (server/map->service server/base-map :prod)
        dev-interceptors     (-> server/base-map
                                 http/dev-interceptors
                                 service-map->interceptors-name)
        default-interceptors (-> server/base-map
                                 http/default-interceptors
                                 service-map->interceptors-name)]
    (testing "Production service shoud block main thread"
      (is (true? (get prod-config ::http/join?))))
    (testing "Production service should have default interceptors"
      (is (match? default-interceptors (set/intersection (service-map->interceptors-name prod-config) default-interceptors))))
    (testing "Production service shouldn't have dev interceptors"
      (is (match? (set/intersection default-interceptors dev-interceptors)
                  (set/intersection (service-map->interceptors-name prod-config) dev-interceptors))))
    (testing "Production service must force port to 80"
      (is (match? 80 (::http/port prod-config))))))

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

(def url-for (route/url-for-routes server/app-routes))
(def full-url-for (route/url-for-routes server/app-routes :host "localhost" :port 3000 :scheme "http"))

(deftest local-hello-route
  (let [hello-route (url-for ::server/hello)]
    (let [{:keys [body status]} (pedestal-test/response-for test-service :get hello-route)]
      (testing "Should return status code 200"
        (is (match? 200 status)))
      (testing "Should return hello body"
        (is (match? body "Hello World"))))))

(deftest ^:integration server-hello-route
  (try
    (server/start-server! :test)
    (let [request-url           (full-url-for ::server/hello)
          {:keys [status body]} (http-client/get request-url)]
      (testing "Should return status code 200"
        (is (match? 200 status)))
      (testing "Should return hello body"
        (is (match? body "Hello World"))))
    (catch Throwable t
      (throw t))
    (finally
      (server/stop-server!))))
