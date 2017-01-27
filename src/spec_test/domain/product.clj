(ns spec-test.domain.product
  (:require [clojure.spec :as s]))

(s/def ::code string?)
(s/def ::description string?)
(s/def ::quantity integer?)

(s/def ::sku       (s/keys :req [::code] :opt [::description])) ;; TODO multi-spec abstraction
(s/def ::bundle    (s/keys :req [::code ::quantity] :opt [::description]))
(s/def ::contents  (s/* ::bundle))
(s/def ::composite (s/keys :req [::code ::contents] :opt [::description]))
