(ns clockify-cli.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn])
  (:import [java.io PushbackReader])
  (:gen-class))


(def default-dir (str (System/getProperty "user.home") "/.clockify"))
(def default-config-file-name (str default-dir "/config"))

(defn default-config-file-path [file-name]
  (.getPath (io/file (System/getProperty "user.home") file-name)))

(defn load-config
  ([]
   (load-config (default-config-file-path default-config-file-name)))
  ([config-file]
   (with-open [rdr (io/reader config-file)]
     (edn/read (PushbackReader. rdr)))))
