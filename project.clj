(defproject clockify-cli "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clj-http "3.12.3"]
                 [org.clojure/data.json "1.0.0"]
                 [org.clojure/tools.cli "1.0.194"]
                 [clojure-term-colors "0.1.0"]
                 [clj-time "0.15.2"]]
  :main ^:skip-aot clockify-cli.core
  :target-path "target/%s"
  :jar-name "clockify-cli.jar"
  :uberjar-name "clockify-cli-standalone.jar"
  :profiles {:uberjar {:aot :all}})
