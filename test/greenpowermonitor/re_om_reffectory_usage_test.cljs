(ns greenpowermonitor.re-om-reffectory-usage-test
  (:require
   [cljs.test :refer [deftest is use-fixtures testing]]
   [greenpowermonitor.fixtures :as fixtures]
   [greenpowermonitor.re-om :as sut]))

(defn- make-cofxs-checker [expected-payload injected-cofxs]
  (fn [cofx payload]
    (is (contains? cofx :db))
    (is (= payload expected-payload))
    (doseq [[kw expected-val] injected-cofxs]
      (is (= expected-val (kw cofx))))
    {}))

(use-fixtures :each fixtures/reset-re-om!)

(deftest checking-cofxs-are-injected-when-registering-an-event
  (testing "the :db cofx is injected by default into any event handler"
    (let [passed-payload :some-payload]
      (sut/register-event-handler!
       ::db-cofx-is-injected-by-default
       (make-cofxs-checker [passed-payload] {}))

      (sut/dispatch! [::db-cofx-is-injected-by-default passed-payload])))

  (testing "cofxs are injected into the event handler"
    (let [expected-date-time :any-date
          passed-payload :some-payload]

      (sut/register-cofx!
       :date-time
       (fn [cofx]
         (assoc cofx :date-time expected-date-time)))

      (sut/register-event-handler!
       ::cofxs-are-injected
       [(sut/inject-cofx :date-time)]
       (make-cofxs-checker [passed-payload] {:date-time expected-date-time}))

      (sut/dispatch! [::cofxs-are-injected passed-payload]))))
