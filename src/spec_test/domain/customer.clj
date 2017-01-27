(ns spec-test.domain.customer
  (:require [clojure.spec :as s]))

(s/def ::address string?)
(s/def ::name string?)
(s/def ::honorific #{"Mr" "Mrs" "Dr" "Ms" "Miss" "Mx" "Prof"})
(s/def ::type #{::person ::company})

(s/def ::contacts (s/* ::person))
(s/def ::delivery-addresses (s/* ::address))

(s/def ::person (s/and (s/keys :req [::name ::type ::address ::honorific])
                       #(= ::person (::type %))))
(s/def ::company (s/and (s/keys :req [::name ::type ::address ::contacts ::delivery-addresses])
                        #(= ::company (::type %))))

(defmulti customer-type ::type)

(defmethod customer-type ::person [p] ::person)

(defmethod customer-type ::company [c] ::company)

(s/def ::customer (s/multi-spec customer-type ::type))
