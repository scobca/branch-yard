(ns util.subdomain-generator
  (:require [core.config :refer [config]]
            [sluj.core :refer [sluj]]
            [buddy.core.hash :refer [sha1]]
            [buddy.core.codecs :refer [bytes->hex]]
            [clojure.string :as str]))

(defn generate-subdomain
  "Takes branch name from GitHub and returns unique subdomain"
  [branch-name]
  (let [base-slug (-> branch-name
                      (str/replace #"^[^/]+/" "")
                      (sluj)
                      (str/replace #"[^a-z0-9\-]+" "-")
                      (str/replace #"_" "-")
                      (str/replace #"^-+|-+$" ""))

        hash-part (-> (str branch-name)
                      (sha1)
                      (bytes->hex)
                      (subs 0 6))

        separator "-"
        max-slug-len (- (-> config :max-subdomain-length) (count hash-part) (count separator))
        truncated-slug (subs base-slug 0 (min (count base-slug) max-slug-len))]

    (str truncated-slug separator hash-part)))


