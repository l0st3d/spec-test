(ns spec-test.domain.customer
  (:require [clojure.spec :as s]))

(s/def ::address string?)
(s/def ::name string?)
(s/def ::honorific #{"Mr" "Mrs" "Dr" "Ms" "Miss" "Mx" "Prof"})
(s/def ::type keyword?)

;; (s/def ::person (s/keys :req [::name ::address ::honorific]))

(s/def ::contacts (s/* ::person))
(s/def ::delivery-addresses (s/* ::address))

;; (s/def ::company (s/keys :req [::name ::address ::contacts ::delivery-addresses]))

(defmulti type ::type)

(defmethod type ::person [_]
  (s/keys :req [::type ::name ::address ::honorific]))

(defmethod type ::company [_]
  (s/keys :req [::type ::name ::address ::contacts ::delivery-addresses]))

(s/def ::customer (s/multi-spec type ::type))
