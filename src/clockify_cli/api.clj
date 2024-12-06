(ns clockify-cli.api
  (:require [clojure.data.json :as json])
  (:gen-class))


(defn request-params
  ([] (request-params {}))
  ([params] (merge {:headers (:headers params)
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
   (handle-response (method url (request-params params)))))