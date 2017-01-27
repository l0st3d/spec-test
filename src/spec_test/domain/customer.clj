(ns spec-test.domain.customer
  (:require [clojure.spec :as s]))

(s/def ::address string?)
(s/def ::name string?)
(s/def ::honorific #{"Mr" "Mrs" "Dr" "Ms" "Miss" "Mx" "Prof"})
(s/def ::type keyword?)

(s/def ::contacts (s/* ::person))
(s/def ::delivery-addresses (s/* ::address))

(defmulti type ::type)

(defmethod type ::person [_]
  (s/keys :req [::name ::type ::address ::honorific]))

(defmethod type ::company [_]
  (s/keys :req [::name ::type ::address ::contacts ::delivery-addresses]))

(s/def ::customer (s/multi-spec type ::type))
