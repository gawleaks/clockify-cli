(ns clockify-cli.core
  (:require
   [clockify-cli.clockify-api :as clock-api]
   [clockify-cli.config :as config]
   [clockify-cli.tools :as tools]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.string :as strings]
   [clojure.term.colors :as colors]
   [clojure.pprint :refer [pprint]]
   [java-time.api :as t])
  (:gen-class))

(def cli-options
  [["-p" "--project PROJECT" "Project to add time entry to"]
   ["-w" "--workspace WORKSPACE" "Workspace to add time entry to"]
   ["-s" "--start START-TIME" "Start time of time entry"]
   ["-e" "--end END-TIME" "End time of time entry"]
   ["-d" "--day (today | yesterday | custom date YYYY-MM-DD)" "Date of time entry"]
   ["-h" "--help" "Show help"]])

(defn print-help []
  (println "Usage: clockify-cli [options] [command] [arguments]")
  (println "\nGeneral options:\n")
  (println (strings/join " " (last cli-options)))
  (println "\nCommmands:\n")
  (println "- time-entry [description]: Add time entry with provided description (or default one from config file)"
           "\n  Options:"
           (strings/join (map #(str "\n    " (strings/join " " %)) (butlast cli-options)))
           "\n  If no options are given, the default [workspace, project, start, end, day] are used"
           "\n  If no description is given, the default description (Working on [project]) is used")
  (println "\n- workspaces: List all workspaces")
  (println "\n- projects [workspace]: List all projects in provided workspace (or default one from config file)")
  (println "\n- report [year] [month] [repositories]: Generate report for for the given year, month and repositories. Apply it after user confirmation"           
           "\n  - year: Year of the report"
           "\n  - month: Month of the report"
           "\n  - repositories: List of repositories to generate report for (separated by space)")
  (println "\n- config [subcommand] [arguments]: Play with config file"
           "\n  - show: Show config file"
           "\n  - set [key] [value]: Set value of key in config file"
           "\n  - init: Initialize config file with example values")
  (println "\n- help: Show this help" "\n"))

(defn trace [x]
  (println x)
  x)

(defn manage-config [opts]
  (let [subcommand (second (get opts :arguments))
        arguments (nthnext (get opts :arguments) 2)]
    (cond
      (= subcommand "show") (pprint (config/load-config))
      (= subcommand "set") (config/write-config (assoc (config/load-config) (keyword (first arguments)) (second arguments)))
      (= subcommand "init") (config/init-config)
      :else (pprint (config/load-config)))))

(defn list-workspaces []
  (println "Your Workspaces:")
  (doseq [workspace (clock-api/get-workspaces)]
    (println (str "- " (colors/on-grey (get workspace "name"))))))

(defn list-projects [opts config]
  (let [workspace (trace (or (second (get opts :arguments)) (:workspace config)))
        workspace-id (if (or (nil? workspace) (empty? workspace))
                       nil
                       (clock-api/get-workspace-id workspace))]
    (if (nil? workspace-id)
      (println (colors/red "Workspace not found"))
      (do
        (println "Projects in workspace " (colors/bold workspace) ":")
        (doseq [project (clock-api/get-projects workspace-id)]
          (println (str "- " (colors/on-grey (get project "name")))))))))

(defn extract-args [opts config]
  (let [description (or (second (get opts :arguments)) (str "Working on " (:project config)))
        project (get-in opts [:options :project] (:project config))
        workspace (get-in opts [:options :workspace] (:workspace config))
        start (get-in opts [:options :start] (:start config))
        end (get-in opts [:options :end] (:end config))
        day (get-in opts [:options :day] (:day config))]
        {:workspace workspace :project project :description description :start start :end end :day day}))

(defn add-time-entry [opts config]
  (let [description (or (second (get opts :arguments)) (str "Working on " (:project config)))
        project (get-in opts [:options :project] (:project config))
        workspace (get-in opts [:options :workspace] (:workspace config))
        start (get-in opts [:options :start] (:start config))
        end (get-in opts [:options :end] (:end config))
        day (get-in opts [:options :day] (:day config))]
    (println "Adding time entry: " (colors/bold description) "to project" (colors/bold project) "in workspace" (colors/bold workspace))
    (println "Start:" (colors/bold start) " End:" (colors/bold end) " Day:" (colors/bold day))
    (clock-api/add-time-entry {:workspace workspace :project project :description description :start start :end end :day day})
    (println (colors/green "Time entry added"))))
                                   

(defn generate-report [opts config]
  (let [[y m & repos] (rest (get opts :arguments))
        year (Integer/parseInt (or y (t/format "yyyy" (t/local-date))))
        month (Integer/parseInt (or m (t/format "M" (t/local-date))))]    
    (println "Generating report for year:" (colors/bold year) " month:" (colors/bold month) " and repositories:" (colors/bold repos))
    (if-not (and (nil? repos) (empty repos))
      (let [report (tools/report repos year month)]
        (if-not (nil? report)
          (do
            (pprint report)
            (println (colors/on-grey "\nDo you want to apply this report? (y/n)"))
            (let [confirm (read-line)]
              (if (= confirm "y")
                (do
                  (println "Applying report")
                  (doseq [[date message] report]
                    (clock-api/add-time-entry (merge (extract-args opts config) {:day (name date) :description message})))
                  (println (colors/green "Report applied")))
                (println (colors/yellow "Report not applied"))))
            )
          (println (colors/red "No report generated"))))
      (println (colors/red "No repositories provided")))))

  (defn parse-command [opts]
    (case (first (get opts :arguments))
      "time-entry" (add-time-entry opts (config/load-config))
      "workspaces" (list-workspaces)
      "projects" (list-projects opts (config/load-config))
      "report" (generate-report opts (config/load-config))
      "config" (manage-config opts)
      "help" (print-help)
      (print-help)))

(defn -main
  [& args]
  (let [opts (parse-opts args cli-options)]
    (if (get-in opts [:options :help])
      (print-help)
      (parse-command opts))))


