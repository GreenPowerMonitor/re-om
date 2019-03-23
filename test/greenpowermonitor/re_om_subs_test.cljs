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

(defn- derived-sub-view [data owner]
  (om/component
   (html
    [:div#re-om-test
     (let [value (sut/subscribe [::derived-subscription 4] owner)]
       value)])))

(defn- complex-sub-view [data owner]
  (om/component
   (html
    [:div#re-om-test
     (let [value-1 (sut/subscribe [::final-subscription-1 "subs-1"] owner)
           value-2 (sut/subscribe [::final-subscription-2 "subs-2"] owner)]
       (str value-1 "_" value-2))])))

(defn- mount-component-on-root! []
  (om/root
   (fn [data owner]
     (om/component
      (html
       (case (:view data)
         :complex-subscription (om/build complex-sub-view {})
         :derived-subscription (om/build derived-sub-view {})
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

(deftest derived-subscriptions-work
  (async done
         (let [times-executed (atom 0)
               times-derived-executed (atom 0)
               external-db (atom {:view :derived-subscription
                                  :source-value -2})]
           (sut/register-sub!
            ::test-subscription
            (fn [db args]
              (swap! times-executed inc)
              (:source-value db)))

           (sut/register-sub!
            ::derived-subscription
            :<- [::test-subscription]
            (fn [value args]
              (swap! times-derived-executed inc)
              (+ value (first args))))

           (go
             (sut/dispatch! [::sut/init-db external-db])
             (mount-component-on-root!)

             (<! (core.async/timeout 300))

             (is (= "2"
                    (inner-html fixtures/c "#re-om-test")))

             (swap! sut/*db* assoc :source-value 3)

             (<! (core.async/timeout 300))

             (is (= "7"
                    (inner-html fixtures/c "#re-om-test")))

             (swap! sut/*db* assoc :some-key :should-not-rerun-derived-sub)
             (swap! sut/*db* assoc :some-key :also-should-not)
             (swap! sut/*db* assoc :some-key :same-thing)

             (is (= 5 @times-executed))
             (is (= 2 @times-derived-executed))

             (done)))))

(deftest derived-subscriptions-work-with-multiple-levels
  (async done
         (let [times-executed (atom 0)
               times-middle-executed (atom 0)
               times-final-1-executed (atom 0)
               times-final-2-executed (atom 0)
               external-db (atom {:view :complex-subscription
                                  :source-string "original"})]
           (sut/register-sub!
            ::test-subscription
            (fn [db args]
              (swap! times-executed inc)
              (:source-string db)))

           (sut/register-sub!
            ::middle-subscription
            :<- [::test-subscription]
            (fn [s args]
              (swap! times-middle-executed inc)
              (str s "-middle")))

           (sut/register-sub!
            ::final-subscription-1
            :<- [::middle-subscription]
            (fn [s args]
              (swap! times-final-1-executed inc)
              (str s "-sub-1")))

           (sut/register-sub!
            ::final-subscription-2
            :<- [::middle-subscription]
            (fn [s args]
              (swap! times-final-2-executed inc)
              (str s "-sub-2")))

           (go
             (sut/dispatch! [::sut/init-db external-db])
             (mount-component-on-root!)

             (<! (core.async/timeout 300))

             (is (= "original-middle-sub-1_original-middle-sub-2"
                    (inner-html fixtures/c "#re-om-test")))

             (swap! sut/*db* assoc :source-string "changed")

             (<! (core.async/timeout 300))

             (is (= "changed-middle-sub-1_changed-middle-sub-2"
                    (inner-html fixtures/c "#re-om-test")))

             (swap! sut/*db* assoc :some-key :should-not-rerun-derived-sub)
             (swap! sut/*db* assoc :some-key :also-should-not)
             (swap! sut/*db* assoc :some-key :same-thing)

             (is (= 5 @times-executed))
             (is (= 2 @times-middle-executed))
             (is (= 2 @times-final-1-executed))
             (is (= 2 @times-final-2-executed))

             (done)))))

(deftest derived-subscriptions-dedup-with-multiple-levels
  (async done
         (let [equal-strings "Strings are equal"
               diff-strings "Strings are different"
               times-source-1-executed (atom 0)
               times-source-2-executed (atom 0)
               times-middle-executed (atom 0)
               times-final-executed (atom 0)
               external-db (atom {:view :derived-subscription
                                  :source-value-1 "source-1"
                                  :source-value-2 "source-2"})]
           (sut/register-sub!
            ::source-subscription-1
            (fn [db args]
              (swap! times-source-1-executed inc)
              (:source-value-1 db)))

           (sut/register-sub!
            ::source-subscription-2
            (fn [db args]
              (swap! times-source-2-executed inc)
              (:source-value-2 db)))

           (sut/register-sub!
            ::middle-subscription
            :<- [::source-subscription-1]
            :<- [::source-subscription-2]
            (fn [[s1 s2] args]
              (swap! times-middle-executed inc)
              (= s1 s2)))

           (sut/register-sub!
            ::derived-subscription
            :<- [::middle-subscription]
            (fn [s args]
              (swap! times-final-executed inc)
              (if s
                equal-strings
                diff-strings)))

           (go
             (sut/dispatch! [::sut/init-db external-db])
             (mount-component-on-root!)

             (<! (core.async/timeout 300))

             (is (= diff-strings
                    (inner-html fixtures/c "#re-om-test")))

             (swap! sut/*db* assoc :source-value-1 "source-2")

             (<! (core.async/timeout 300))

             (is (= equal-strings
                    (inner-html fixtures/c "#re-om-test")))

             (swap! sut/*db* merge {:source-value-1 "source-4"
                                    :source-value-2 "source-4"})

             (<! (core.async/timeout 300))

             (is (= equal-strings
                    (inner-html fixtures/c "#re-om-test")))

             (swap! sut/*db* assoc :some-key :should-not-rerun-derived-sub)
             (swap! sut/*db* assoc :some-key :also-should-not)
             (swap! sut/*db* assoc :some-key :same-thing)

             (is (= 6 @times-source-1-executed))
             (is (= 6 @times-source-2-executed))
             (is (= 3 @times-middle-executed))
             (is (= 2 @times-final-executed))

             (done)))))
