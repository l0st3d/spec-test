(ns spec-test.domain.customer
  (:require [clojure.spec :as s]))

(s/def ::address string?)
(s/def ::name string?)
(s/def ::honorific #{"Mr" "Mrs" "Dr" "Ms" "Miss" "Mx" "Prof"})
(s/def ::type keyword?)

(s/def ::person (s/keys :req [::name ::type ::address ::honorific]))

(s/def ::contacts (s/* ::person))
(s/def ::delivery-addresses (s/* ::address))

(s/def ::company (s/keys :req [::name ::type ::address ::contacts ::delivery-addresses]))

(defmulti type ::type)

(defmethod type ::person [_]
  ::person)

(defmethod type ::company [_]
  ::company)

(s/def ::customer (s/multi-spec type ::type))
