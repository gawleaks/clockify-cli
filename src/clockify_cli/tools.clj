(ns clockify-cli.tools
  (:require [clojure.data.json :as json]
            [clockify-cli.ollama :as ollama]
            [clojure.string :as strings]
            [java-time.api :as t]
            [java-time.format :as f]
            [clockify-cli.github-api :as github-api])
  (:gen-class))

(defn get-prompt [commits]
  (str "Given this list of objects representing git commits in json format: "
       (json/write-str commits)
       "\nWrite a daily summary of my work but make a one liner for each day.
        Make a shortened version of daily summary that should not be longer than 100 characters.        
        When there are multiple commits on the same day, combine the summaries.
        Remove conventional commits prefixes from messages like chore: fix: feat: and so on.
        Return the answer as pure json object with keys as dates in YYYY-MM-DD format.        
        Do not return any code just the json object."))

;; (defn is-weekend [date-str]
;;   (contains? #{"Saturday" "Sunday"} (t/day-of-week (t/local-date date-str))))
(defn is-weekend? [date]
  (let [day-of-week (t/day-of-week date)]
    (or (= day-of-week 6) (= day-of-week 7))))

(defn generate-dates [year month]
  (let [start-date (t/local-date year month 1)
        end-date (t/local-date)]
    (take-while #(t/before? % end-date) (iterate #(t/plus % (t/days 1)) start-date))))

(defn fill-gaps [data year month]
  (let [dates (filter (complement is-weekend?) (generate-dates year month))
        date-keys (map #(keyword (f/format "yyyy-MM-dd" %)) dates)]
    (reduce (fn [acc date]
              (let [prev-date (last (keys acc))]
                (assoc acc date (get data date (get acc prev-date)))))
            {}
            date-keys)))

(defn sort-by-key [data]
  (sort-by key data))

(defn report [repos year month]
  (-> (github-api/extract-commits repos year month)
      (get-prompt)
      (ollama/completions)
      (strings/replace #"^```json\n" "")
      (strings/replace #"```$" "")
      (json/read-json)
      (fill-gaps year month)
      sort-by-key))

