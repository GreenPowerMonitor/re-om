(ns greenpowermonitor.unit-tests-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [greenpowermonitor.re-om-reffectory-usage-test]
   [greenpowermonitor.re-om-subs-test]))

(doo-tests
 'greenpowermonitor.re-om-reffectory-usage-test
 'greenpowermonitor.re-om-subs-test)
