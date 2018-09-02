(ns greenpowermonitor.fixtures
  (:require
   [greenpowermonitor.re-om :as re-om]
   [cljs-react-test.utils :as tu]
   [cljs.test :refer-macros [async]]))

(def ^:dynamic c)

(def reset-re-om!
  (let [initial-value (re-om/get-handlers-state)]
    {:before #(re-om/set-verbose! false)
     :after #(do
               (re-om/set-verbose! true)
               (re-om/set-handlers-state! initial-value))}))

(def mount-container-and-reset-re-om!
  (let [initial-value (re-om/get-handlers-state)]
    {:before #(async done
                     (re-om/set-verbose! false)
                     (set! c (tu/new-container!))
                     (done))
     :after #(do
               (tu/unmount! c)
               (re-om/set-verbose! true)
               (re-om/set-handlers-state! initial-value))}))
