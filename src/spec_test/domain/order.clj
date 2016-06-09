(ns spec-test.domain.order
  (:require [clojure.spec :as s]
            [spec-test.domain.product :as product]
            [spec-test.domain.customer :as customer]))

(s/def ::delivery-address string?)
(s/def ::billing-address string?)

(s/def ::product (s/keys :req [::product/code] :opt [::product/description]))

(s/def ::customer (s/keys :req [::customer/name ::customer/address]))

(s/def ::charge (s/keys :req [::description ::price] :opt [::product]))

(s/def ::lines (s/* ::charge))

(s/def ::order (s/keys :opt [::customer ::lines ::delivery-address ::billing-address]))

