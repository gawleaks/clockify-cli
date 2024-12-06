(ns clockify-cli.github-api
  (:require [clojure.data.json :as json]
            [clj-http.client :as client]            
            [clockify-cli.config :as config]
            [java-time.api :as t]
            )
  (:import [java.time OffsetDateTime]
           [java.time.format DateTimeFormatter])
  (:gen-class))

(defn extract-date [date-string]
  (let [offset-date-time (OffsetDateTime/parse date-string)
        local-date (.toLocalDate offset-date-time)
        formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd")]
    (.format local-date formatter)))

(defn to-iso-date-time [& args]
  (str (t/format :iso-date-time (apply t/local-date-time args)) "Z"))
       

(def gh-base-url "https://api.github.com")

(defn default-headers []
  (let [config (config/load-config)]
    {"Authorization" (str "token " (:github-token config))}))

(defn request-params
  ([] (request-params {}))
  ([params] (merge {:headers (merge (default-headers) (:headers params))
                    :content-type :json
                    :accept :json
                    :throw-entire-message? true} (dissoc params :headers))))
(defn trace [x]
  (println x)
  x)

(defn handle-response [response]
  (let [body (:body response)
        status (:status response)]
    (if (>= status 200)
      (json/read-str body)
      (throw (Exception. (str "Error: " status " " body))))))

(defn make-request
  ([method url]
   (make-request method url {}))
  ([method url params]
   (handle-response (method (str gh-base-url url) (request-params params)))))

(defn do-get
  ([url]
   (make-request client/get url))
  ([url params]
   (make-request client/get url params)))

(defn do-post [url params]
  (make-request client/post url params))


(defn get-commits [repo year month]
  (let [config (config/load-config)]    
   (do-get
    (str "/repos/" (:github-owner config) "/" (or repo (:github-repo config)) "/commits")
    {:query-params
     {:per_page 100
      :page 1
      :since (to-iso-date-time year month)
      :until (to-iso-date-time year
                               (Integer/parseInt (t/format "M" (t/local-date)))
                               (Integer/parseInt (t/format "dd" (t/local-date))))
      :author (:github-user config)}})))


(defn simplify-commits [commits]
  (map (fn [commit]
         {:date (extract-date (get-in commit ["commit" "author" "date"]))
          :message (get-in commit ["commit" "message"])
          :author (get-in commit ["commit" "author" "name"])})
       commits))

(defn skip-merge-commits [commits]
  (filter (fn [commit]
            (not (re-find #"Merge branch" (:message commit))))
          commits))

(defn extract-commits [repos year month]
  (let [f (apply juxt (map #(partial (comp skip-merge-commits simplify-commits get-commits) %) repos))]
    (reduce concat (f year month))))
