(ns clj_jenkins.pipeline
  (:require [clj-jenkins.job :as job]
            [clj-jenkins.util :as util]
            [clj-http.client :as client]))

(def color-precedence {"red_anime" 0 "red" 1 "blue_anime" 2 "blue" 3})
(def jenkins-url (util/config-property "JENKINS_URL"))

(defn- vec->fetch-args
  [coll]
  (reduce
    (fn [result arg]
      (if (sequential? arg)
        (str result "[" (vec->fetch-args arg) "]")
        (if (clojure.string/blank? result)
          (name arg)
          (str result "," (name arg)))))
    ""
    coll))

(defn- url?
  [url]
  (if (re-find #"http(s)?://" url) true false))

(defn- fetch-url
  [job & [fetch]]
  (str
    (if (url? job) job (str jenkins-url "/job/" job))
    "/api/json"
    (when fetch (str "?tree=" (vec->fetch-args fetch)))))

(defn- fetch-job
  [job & [fetch]]
  (let [url (fetch-url job fetch)]
    (:body (client/get url {:as :json}))))

(defn- fetch-jobs
  [jobs & [fetch]]
  (map #(fetch-job % fetch) jobs))

(defn- pipeline
  [first-job & [fetch]]
  (let [fetch (conj (or fetch []) :downstreamProjects [:name ])]
    (tree-seq
      #(contains? % :downstreamProjects )
      #(fetch-jobs (map :name (:downstreamProjects %)))
      (fetch-job first-job fetch))))

(defn- colors [jobs]
  (map :color jobs))

(defn- color [colors]
  (first (sort-by color-precedence colors)))

(defn- color-for-jobs [jobs]
  (color (colors jobs)))

(defn unclaimed?
  [build]
  (let [description (:description build)]
    (if (re-find #".*(C|c)laim (T|t)his (B|b)uild.*" (or description "")) true false)))

(defn status [job-name]
  (let [jobs (pipeline job-name [:color :lastBuild [:url ]])
        builds (fetch-jobs (map #(get-in % [:lastBuild :url ]) jobs) [:description ])]
    {job-name {:color (color-for-jobs jobs)
               :unclaimed (boolean (some unclaimed? builds))
               :jobs (map #(select-keys % [:name :color :lastBuild]) jobs)}}))

(defn statuses [job-names]
  (apply merge (map #(status %) job-names)))