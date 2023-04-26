(ns clockify-cli.config
  (:require [clojure.java.io :as io]
            [clojure.term.colors :as colors])
  (:gen-class))


(def default-dir ".clockify")
(def default-config-file-name (str default-dir "/config"))

(defn default-config-file-path [file-name]
  (.getPath (io/file (System/getProperty "user.home") file-name)))

(defn load-config
  ([]
   (load-config (default-config-file-path default-config-file-name)))
  ([config-file]
   (try (read-string (slurp config-file))
        (catch Exception e
          (println "Error while reading config file: " (.getMessage e))
          (System/exit 1)))))

(defn write-config
  ([config]
   (write-config config (default-config-file-path default-config-file-name)))
  ([config config-file]
   (spit config-file (pr-str config))))

(defn init-config
  ([]
   (init-config (default-config-file-path default-config-file-name)))
  ([config-file]
   (when-not (.exists (io/file config-file))
     (write-config (load-config "config.example") config-file))
   (println (colors/green "Config file initialized:") config-file)))