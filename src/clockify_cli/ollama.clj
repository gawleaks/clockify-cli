(ns clockify-cli.ollama
  (:require [clojure.data.json :as json]
            [clj-http.client :as client]            
            [clockify-cli.api :as api])
  (:gen-class))

(def ollama-base-url "http://localhost:11434")

(defn completions [prompt]
  (let [response (api/make-request client/post (str ollama-base-url "/api/generate")
                             {:headers {:content-type :json :accept :json}
                              :body (json/write-str {:model "llama3.1" :stream false
                                                     :prompt prompt})})]
    (get response "response")))

