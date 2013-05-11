(ns clj-jenkins.build
  (:require [clj-jenkins.util :as util]
            [clj-http.client :as client]
            [cheshire.core :as json]))

(declare build-url)

(def jenkins-url (util/config-property "JENKINS_URL"))

(defn get-build [job build-number]
  (-> (build-url job build-number)
    (client/get)
    :body
    (json/parse-string)))

(defn build-url [job build-number]
  (str jenkins-url "/job/" job "/" build-number "/api/json"))