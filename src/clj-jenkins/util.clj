(ns clj-jenkins.util)

(defn- env-variable [name]
  (-> (System/getenv)
    (get name)))

(defn config-property [name]
  (or (env-variable name) (System/getProperty name)))