(defproject greenpowermonitor/re-om "0.1.0-SNAPSHOT"
  :description "A re-frame inspired Om framework for both writing new SPA and giving new life to existing legacy SPAs."
  ;:url "https://github.com/GreenPowerMonitor/re-om"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  ;:scm {:name "git"
  ;      :url "https://github.com/GreenPowerMonitor/re-om"}

  :dependencies [[greenpowermonitor/reffectory "0.1.0-SNAPSHOT"]]

  :profiles {:provided {:dependencies [[org.clojure/clojure "1.9.0"]
                                       [org.clojure/clojurescript "1.10.339"]
                                       [org.omcljs/om "1.0.0-beta1"]]}
             :dev {:dependencies [[org.clojure/core.async "0.4.474"]
                                  [cljs-react-test "0.1.4-SNAPSHOT"]
                                  [cljsjs/react-dom "15.2.0-0" :exclusions [cljsjs/react]]
                                  [sablono "0.8.4" :exclusions [cljsjs/react cljsjs/react-with-addons cljsjs/react-dom]]
                                  [greenpowermonitor/test-doubles "0.1.3-SNAPSHOT"]
                                  [prismatic/dommy "1.1.0"]]}}

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-doo "0.1.10"]
            [lein-auto "0.1.3"]
            [lein-cljfmt "0.6.0"]]

  :auto {"test" {:file-pattern #"\.(clj|cljs|cljc|edn)$"}}

  :clean-targets ^{:protect false} ["resources/public/js" "target" "out"]

  :cljsbuild {:builds [{:id "unit-tests"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "out/unit_tests.js"
                                   :main greenpowermonitor.unit-tests-runner
                                   :target :phantom
                                   :optimizations :none
                                   :foreign-libs
                                   [{:provides ["cljsjs.react"]
                                     :file "https://cdnjs.cloudflare.com/ajax/libs/react/15.3.2/react-with-addons.js"
                                     :file-min "https://cdnjs.cloudflare.com/ajax/libs/react/15.3.2/react-with-addons.js"}]}}]})
