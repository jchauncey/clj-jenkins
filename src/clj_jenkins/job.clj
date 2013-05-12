(ns clj-jenkins.job
  (:require [clj-jenkins.build :as build]
            [clj-jenkins.util :as util]
            [clj-http.client :as client]
            [cheshire.core :as json]))

(declare job-url get-build)

(def jenkins-url (util/config-property "JENKINS_URL"))

(defn get-job [job]
  (-> job
    job-url
    (client/get)
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

(defn- get-build [job key]
  (build/get-build job (((get-job job) key) "number")))

(defn- job-url [job]
  (str jenkins-url "/job/" job "/api/json"))