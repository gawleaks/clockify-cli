(ns clockify-cli.core
  (:require
   [clockify-cli.api :as api]
   [clockify-cli.config :as config]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.string :as strings]
   [clojure.term.colors :as colors]
   [clojure.pprint :refer [pprint]])
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
  (doseq [workspace (api/get-workspaces)]
    (println (str "- " (colors/on-grey (get workspace "name"))))))

(defn list-projects [opts config]
  (let [workspace (trace (or (second (get opts :arguments)) (:workspace config)))
        workspace-id (if (or (nil? workspace) (empty? workspace))
                       nil
                       (api/get-workspace-id workspace))]
    (if (nil? workspace-id)
      (println (colors/red "Workspace not found"))
      (do
        (println "Projects in workspace " (colors/bold workspace) ":")
        (doseq [project (api/get-projects workspace-id)]
          (println (str "- " (colors/on-grey (get project "name")))))))))

(defn add-time-entry [opts config]
  (let [description (or (second (get opts :arguments)) (str "Working on " (:project config)))
        project (get-in opts [:options :project] (:project config))
        workspace (get-in opts [:options :workspace] (:workspace config))
        start (get-in opts [:options :start] (:start config))
        end (get-in opts [:options :end] (:end config))
        day (get-in opts [:options :day] (:day config))]
    (println "Adding time entry: " (colors/bold description) "to project" (colors/bold project) "in workspace" (colors/bold workspace))
    (println "Start:" (colors/bold start) " End:" (colors/bold end) " Day:" (colors/bold day))
    (api/add-time-entry {:workspace workspace :project project :description description :start start :end end :day day})
    (println (colors/green "Time entry added"))))

(defn parse-command [opts]
  (case (first (get opts :arguments))
    "time-entry" (add-time-entry opts (config/load-config))
    "workspaces" (list-workspaces)
    "projects" (list-projects opts (config/load-config))
    "config" (manage-config opts)
    "help" (print-help)
    (print-help)))

(defn -main
  [& args]
  (let [opts (parse-opts args cli-options)]
    (if (get-in opts [:options :help])
      (print-help)
      (parse-command opts))))


