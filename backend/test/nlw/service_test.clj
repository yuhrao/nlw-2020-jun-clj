(ns nlw.service-test
  (:require [nlw.service :as service]
            [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [io.pedestal.http :as http]
            [clojure.set :as set]))

(defn service-map->interceptors-name [service]
  (->> service
       ::http/interceptors
       (map :name)
       set))

(deftest map->service-dev
  (let [dev-config           (service/map->service-map service/base-config :dev)
        dev-interceptors     (-> service/base-config
                                 http/dev-interceptors
                                 service-map->interceptors-name)
        default-interceptors (-> service/base-config
                                 http/default-interceptors
                                 service-map->interceptors-name)]
    (testing "Development service shoudn't block main thread"
      (is (false? (get dev-config ::http/join?))))
    (testing "Development service should have default interceptors"
      (is (match? default-interceptors (set/intersection (service-map->interceptors-name dev-config) default-interceptors))))
    (testing "Development service should have dev interceptors"
      (is (match? dev-interceptors (set/intersection (service-map->interceptors-name dev-config) dev-interceptors))))))

(deftest map->service-test
  (let [test-config          (service/map->service-map service/base-config :test)
        dev-interceptors     (-> service/base-config
                                 http/dev-interceptors
                                 service-map->interceptors-name)
        default-interceptors (-> service/base-config
                                 http/default-interceptors
                                 service-map->interceptors-name)]
    (testing "Test service shoudn't block main thread"
      (is (false? (get test-config ::http/join?))))
    (testing "Test service should have default interceptors"
      (is (match? default-interceptors (set/intersection (service-map->interceptors-name test-config) default-interceptors))))
    (testing "Test service should have dev interceptors"
      (is (match? dev-interceptors (set/intersection (service-map->interceptors-name test-config) dev-interceptors))))))

(deftest map->service-prod
  (let [prod-config          (service/map->service-map service/base-config :prod)
        dev-interceptors     (-> service/base-config
                                 http/dev-interceptors
                                 service-map->interceptors-name)
        default-interceptors (-> service/base-config
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
