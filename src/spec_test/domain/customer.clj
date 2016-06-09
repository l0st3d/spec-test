(ns spec-test.domain.customer
  (:require [clojure.spec :as s]))

(s/def ::address string?)
(s/def ::name string?)
(s/def ::honorific #{"Mr" "Mrs" "Dr" "Ms" "Miss" "Mx" "Prof"})

(s/def ::person (s/keys :req [::name ::address ::honorific]))
(s/def ::company (s/keys :req [::name ::address ::contacts ::delivery-addresses]))
