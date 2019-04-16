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

(defn- sub-view [data owner]
  (om/component
   (html
    [:div#re-om-test
     (let [value (sut/subscribe [::test-subscription :args-value] owner)]
       value)])))

(defn- no-sub-view [data owner]
  (om/component
   (html
    [:div#re-om-test
     "ok"])))

(defn- mount-component-on-root! []
  (om/root
   (fn [data owner]
     (om/component
      (html
       (case (:view data)
         :no-subscription (om/build no-sub-view {})
         (om/build sub-view {})))))
   sut/*db*
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
  (async done
         (let [external-db (atom {:some-key :initial-value})]
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

(deftest subscriptions-only-execute-when-component-is-mounted
  (async done
         (let [times-executed (atom 0)]
           (sut/register-sub!
            ::test-subscription
            (fn [db args]
              (swap! times-executed inc)
              (apply str (:some-key db) args)))
           (sut/register-event-handler!
            ::show-view-without-subscription
            (fn [{:keys [db]} _]
              {:db (assoc db :view :no-subscription)}))

           (go
             (sut/dispatch! [::sut/init-db])
             ;; Executes once
             (mount-component-on-root!)

             (<! (core.async/timeout 300))

             (is (= (str nil :args-value)
                    (inner-html fixtures/c "#re-om-test")))

             ;; Executes again
             (swap! sut/*db* assoc :some-key :koko)

             (<! (core.async/timeout 300))

             (is (= (str :koko :args-value)
                    (inner-html fixtures/c "#re-om-test")))

             ;; Executes again
             (sut/dispatch! [::show-view-without-subscription])

             (<! (core.async/timeout 300))

             ;; Should not execute sub
             (swap! sut/*db* assoc :some-key :should-not-rerun-sub)

             (is (= 3 @times-executed))

             (done)))))

(deftest subs-cache-is-cleared-after-an-unmount
  (let [owner-mounted? (atom false)]
    (with-redefs
     [om/mounted? (fn [_] @owner-mounted?)]
     (reset! owner-mounted? true)
     (sut/register-sub!
      ::test-subscription
      (fn [db args]
        [(get db :some-key) args]))
     (sut/register-event-handler!
      ::test-event
      (fn [{:keys [db]} [v]]
        {:db (assoc db :some-key v)}))

     ;; base behavior
     (sut/dispatch! [::sut/init-db])
     (sut/dispatch! [::test-event :one])
     (is (= [:one [:args1]] (sut/subscribe [::test-subscription :args1] :owner1)))

     ;; force re-evaluation of ::test-subscription, but mounted? returns false
     ;; => the subscription call is deregistered, and the cache should be cleared too
     (reset! owner-mounted? false)
     (sut/dispatch! [::test-event :two])

     ;; mounted? returns true now, evaluate the subscription. it shouldn't return the same
     ;; as the previous call
     (reset! owner-mounted? true)
     (is (= [:two [:args1]] (sut/subscribe [::test-subscription :args1] :owner1))))))