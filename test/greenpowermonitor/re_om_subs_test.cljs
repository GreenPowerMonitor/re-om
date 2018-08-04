(ns greenpowermonitor.re-om-subs-test
  (:require
   [cljs.core.async :as core.async :refer [<! go]]
   [cljs.test :refer-macros [deftest is async use-fixtures testing]]
   [dommy.core :refer-macros [sel1 sel]]
   [om.core :as om :include-macros true]
   [sablono.core :include-macros true :refer [html]]
   [greenpowermonitor.fixtures :as fixtures]
   [greenpowermonitor.re-om :as sut]))

(use-fixtures :each fixtures/mount-container-and-reset-re-om!)

(defn my-sub-fn [db args]
  :result)

(deftest registering-a-subscription
  (sut/register-sub! ::my-sub my-sub-fn)
  (is (= my-sub-fn (sut/get-handler :subs ::my-sub))))

(deftest when-subscription-does-not-exist-throw-an-exception
  (try
    (sut/subscribe [::does-not-exist] nil)
    (is false "should have thrown an exception")
    (catch :default e
      (is (= {:cause :no-handler-registered
              :id :greenpowermonitor.re-om-subs-test/does-not-exist
              :handler-type :subs}
             (ex-data e))))))

(defn- inner-html [container selector]
  (some-> container (sel1 selector) .-innerHTML))

(defn- mount-component-on-root! []
  (om/root
   (fn [_ owner]
     (om/component
      (html
       [:div#re-om-test
        (let [value (sut/subscribe [::test-subscription :args-value] owner)]
          value)])))
   {}
   {:target fixtures/c}))

(deftest subscriptions-works-with-re-om-db
  (async done
         (sut/register-sub!
          ::test-subscription
          (fn [db args]
            (apply str (:some-key db) args)))

         (go
           (sut/dispatch! [::sut/init-db])
           (mount-component-on-root!)

           (<! (core.async/timeout 300))

           (is (= (str nil :args-value)
                  (inner-html fixtures/c "#re-om-test")))

           (swap! sut/*db* assoc :some-key :koko)

           (<! (core.async/timeout 300))

           (is (= (str :koko :args-value)
                  (inner-html fixtures/c "#re-om-test")))

           (done))))

(deftest subscriptions-works-with-an-external-db
  (let [external-db (atom {:some-key :initial-value})]
    (async done
           (sut/register-sub!
            ::test-subscription
            (fn [db args]
              (apply str (:some-key db) args)))

           (go
             (sut/dispatch! [::sut/init-db external-db])
             (mount-component-on-root!)

             (<! (core.async/timeout 300))

             (is (= (str :initial-value :args-value)
                    (inner-html fixtures/c "#re-om-test")))

             (swap! external-db assoc :some-key :some-new-value)

             (<! (core.async/timeout 300))

             (is (= (str :some-new-value :args-value)
                    (inner-html fixtures/c "#re-om-test")))

             (done)))))
