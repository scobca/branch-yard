(ns util.subdomain-generator-test
  (:require [clojure.test :refer :all]
            [util.subdomain-generator :refer [generate-subdomain]]
            [buddy.core.hash :refer [sha1]]
            [buddy.core.codecs :refer [bytes->hex]]
            [core.config :refer [config]]))

(defn expected-hash [branch-name]
  (-> branch-name sha1 bytes->hex (subs 0 6)))

;; ==============================
;; Basic structure
;; ==============================

(deftest test-result-ends-with-hash
  (testing "Result always ends with a 6-char hex hash separated by '-'"
    (doseq [branch ["main" "feature/my-feature" "release/1.2.3" "fix/PROJ-123-bug"]]
      (let [result (generate-subdomain branch)
            hash   (expected-hash branch)]
        (is (clojure.string/ends-with? result (str "-" hash))
            (str "Expected result to end with '-" hash "', got: " result))))))

(deftest test-result-does-not-exceed-max-length
  (testing "Result length never exceeds :max-subdomain-length from config"
    (doseq [branch ["main"
                    "feature/very-long-branch-name-that-goes-on-and-on-and-on-forever"
                    "release/1.0.0-SNAPSHOT"]]
      (let [result (generate-subdomain branch)
            max-len (:max-subdomain-length config)]
        (is (<= (count result) max-len)
            (str "Result '" result "' exceeds max length " max-len))))))

;; ==============================
;; Prefix stripping
;; ==============================

(deftest test-strips-single-prefix
  (testing "Prefix before first '/' is stripped from the slug part"
    (let [result (generate-subdomain "feature/my-feature")]
      ;; slug is built from "my-feature", not "feature-my-feature"
      (is (not (clojure.string/starts-with? result "feature"))
          (str "Prefix 'feature' should be stripped, got: " result))
      (is (clojure.string/starts-with? result "my-feature")
          (str "Slug should start with 'my-feature', got: " result)))))

(deftest test-no-prefix-uses-full-name
  (testing "Branch without '/' uses the full name as the slug base"
    (let [result (generate-subdomain "main")]
      (is (clojure.string/starts-with? result "main")
          (str "Expected slug to start with 'main', got: " result)))))

(deftest test-only-first-segment-stripped
  (testing "Only the first path segment is stripped; remaining slashes become part of slug"
    ;; e.g. "team/scope/feature" → slug built from "scope/feature"
    (let [result  (generate-subdomain "team/scope/feature")
          hash    (expected-hash "team/scope/feature")]
      (is (clojure.string/ends-with? result (str "-" hash)))
      (is (not (clojure.string/starts-with? result "team"))))))

;; ==============================
;; Hash determinism
;; ==============================

(deftest test-same-input-same-output
  (testing "generate-subdomain is deterministic"
    (doseq [branch ["main" "feature/foo" "release/2.0"]]
      (is (= (generate-subdomain branch)
             (generate-subdomain branch))
          (str "Non-deterministic result for: " branch)))))

(deftest test-different-branches-different-hashes
  (testing "Different branch names produce different subdomains"
    (let [branches ["feature/foo" "feature/bar" "hotfix/foo"]
          results  (map generate-subdomain branches)]
      (is (= (count results) (count (set results)))
          "Expected all subdomains to be unique"))))

(deftest test-hash-derived-from-original-name
  (testing "The 6-char hash suffix is computed from the original branch name (with prefix)"
    (let [branch "feature/my-feature"
          result (generate-subdomain branch)
          hash   (expected-hash branch)]          ; sha1("feature/my-feature")
      (is (clojure.string/ends-with? result (str "-" hash))))))

;; ==============================
;; Slug format
;; ==============================

(deftest test-slug-is-lowercase
  (testing "Slug portion contains only lowercase characters"
    (doseq [branch ["feature/MyFeature" "HOTFIX/URGENT-FIX" "Release/V2"]]
      (let [result (generate-subdomain branch)
            slug   (clojure.string/replace result #"-[0-9a-f]{6}$" "")]
        (is (= slug (clojure.string/lower-case slug))
            (str "Slug is not lowercase: " slug))))))

(deftest test-slug-contains-no-special-chars
  (testing "Slug portion only contains alphanumerics and hyphens"
    (doseq [branch ["feature/hello_world" "fix/PROJ-123: some bug!" "feat/v2.0 final"]]
      (let [result (generate-subdomain branch)]
        (is (re-matches #"[a-z0-9\-]+" result)
            (str "Subdomain contains illegal chars: " result))))))

;; ==============================
;; Truncation
;; ==============================

(deftest test-long-branch-slug-is-truncated
  (testing "A very long branch name is truncated so the result fits within max-subdomain-length"
    (let [long-branch (str "feature/" (apply str (repeat 200 "x")))
          result      (generate-subdomain long-branch)
          max-len     (:max-subdomain-length config)]
      (is (<= (count result) max-len)
          (str "Result too long: " (count result) " > " max-len)))))

(deftest test-short-branch-not-padded
  (testing "A short branch name is not padded — result is shorter than max-subdomain-length"
    (let [result  (generate-subdomain "main")
          max-len (:max-subdomain-length config)]
      (is (< (count result) max-len)
          (str "Short branch result should be shorter than max-len " max-len)))))