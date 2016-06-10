(ns spec-test.domain.repository
  (:require [clojure.spec :as s]
            [spec-test.domain.customer :as customer]
            [spec-test.domain.product :as product]
            [spec-test.domain.order :as order]))

(defn initial-customers []
  (ref {} :validator (partial s/valid? (s/map-of number? (s/or :person ::customer/person :company ::customer/company))))) ;; TODO missing abstraction?

(def customers (initial-customers))

(defn initial-products []
  (ref {} :validator (partial s/valid? (s/map-of number? (s/or :sku ::product/sku :bundle ::product/bundle :composite ::product/composite)))))

(def products (initial-products))

(defn initial-orders []
  (ref {} :validator (partial s/valid? (s/map-of number? ::order/invoice))))

(def orders (initial-orders))



