(ns clj-jenkins.job
  (:require [clj-jenkins.build :as build]
            [clj-jenkins.util :as util]
            [clj-http.client :as client]
            [cheshire.core :as json]
            [clojure.string :as s]))

(declare job-url build-url get-build params->query-string)

(def jenkins-url (util/config-property "JENKINS_URL"))
(def auth-tokens [(util/config-property "JENKINS_USERNAME") (util/config-property "JENKINS_API_TOKEN")])

(defn get-job [job]
  (-> job
    job-url
    (client/get {:basic-auth auth-tokens})
    :body (json/parse-string)))

(defn post-job [job]
  (-> (job-url job)
    (client/post {:basic-auth auth-tokens})
    :body (json/parse-string)))

(defn builds [job]
  ((get-job job) "builds"))

(defn health-report [job]
  ((first ((get-job job) "healthReport")) "description"))

(defn last-unsuccessful-build [job]
  (get-build job "lastUnsuccessfulBuild"))

(defn last-build [job]
  (get-build job "lastBuild"))

(defn last-failed-build [job]
  (get-build job "lastFailedBuild"))

(defn last-stable-build [job]
  (get-build job "lastStableBuild"))

(defn last-unstable-build [job]
  (get-build job "lastUnstableBuild"))

(defn last-successful-build [job]
  (get-build job "lastSuccessfulBuild"))

(defn upstream-projects [job]
  ((get-job job) "upstreamProjects"))

(defn downstream-projects [job]
  ((get-job job) "downstreamProjects"))

(defn trigger-build
  ([job]
    (trigger-build job {}))
  ([job params]
    (-> (build-url job params)
      (client/post {:basic-auth auth-tokens
                    :content-type :json})
      :body (json/parse-string))))

(defn- get-build [job key]
  (build/get-build job (((get-job job) key) "number")))

(defn- job-url [job]
  (str jenkins-url "/job/" job "/api/json"))

(defn- build-url [job params]
  (let [params-string (params->query-string params)]
    (str jenkins-url "/job/" job "/buildWithParameters?" params-string)))

(defn- params->query-string [params]
  (s/join "&"
    (for [[k v] params]
      (str (name k) "=" (java.net.URLEncoder/encode v)))))