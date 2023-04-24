(ns clockify-cli.core
  (:require
   [clockify-cli.api :as api]
   [clockify-cli.config :as config]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.string :as strings]
   [clojure.term.colors :as colors])
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
  (println "- time-entry [description]: Add time entry with provided description (or default one from config file)")
  (println "Options:")
  (println (strings/join (map #(str (strings/join " " %) "\n") (butlast cli-options))))
  (println "If no options are given, the default [workspace, project, start, end, day] are used")
  (println "If no description is given, the default description (Working on [project]) is used")
  (println "\n- workspaces: List all workspaces")
  (println "\n- projects [workspace]: List all projects in provided workspace (or default one from config file)" "\n"))

(defn trace [x]
  (println x)
  x)

(defn list-workspaces []
  (println "Workspaces:")
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
    (api/add-time-entry {:workspace workspace :project project :description description :start start :end end :day day})))

(defn parse-command [opts config]
  (case (first (get opts :arguments))
    "time-entry" (add-time-entry opts config)
    "workspaces" (list-workspaces)
    "projects" (list-projects opts config)
    "help" (print-help)
    (print-help)))

(defn -main
  [& args]
  (let [config (config/load-config)
        opts (parse-opts args cli-options)]
    (if (get-in opts [:options :help])
      (print-help)
      (parse-command opts config))))


