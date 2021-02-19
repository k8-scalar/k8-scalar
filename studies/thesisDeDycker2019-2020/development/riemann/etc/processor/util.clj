(ns processor.util
  (:require [clojure.string :as str])
)

(defn getenv-as-int 
  "Returns the value of the environment as an integer"
  [env-name]
  (if-let [v (System/getenv env-name)]
    (Integer/parseInt v)
    nil
  )
)

(defn parse-label
  "Parse the value matching the given technology label of given event"
  [event label_key]
  (if-let [labels (str/split (:labels event) #",")]
    (if-let [matching_labels (filter #(str/starts-with? %1 label_key) labels)]
      (second (str/split (first matching_labels) #":"))
      nil
    )
  )
)

(defn parse-technology
  "Parse the deployment's technology of given event"
  [event]
  (parse-label event "technology")
)

(defn parse-deployment
  "Parse the deployment of given event"
  [event]
  (if-let [pod_name (:pod_name event)]
    (str/join (first (split-at (str/last-index-of pod_name "-") pod_name)))
    "" 
  ) 
)

(defn labelled?
    "Returns true, if the event contains given technology label"
  [event label_key label_value]
  (if-let [label (parse-label event label_key)]
    (str/includes? label label_value)
    false
  )
)

(defn labelled-any?
  "Returns true, if the event contains one or more given technology labels"
  [event label_key label_values]
  (some true? (map #(labelled? event label_key %1) label_values))
)
