(ns spec-test.core-test
  (:require [clojure.test :refer [deftest is are]]
            [clojure.spec :as s]
            [spec-test.domain.product :as product]
            [spec-test.domain.order :as order]
            [spec-test.domain.customer :as customer]))

(defn valid? [spec data]
  (is (s/valid? spec data)
      (s/explain-str spec data)))

(deftest a-test
  (valid? ::product/bundle {::product/code "abc123"
                            ::product/contents [{::product/code "123"
                                                 ::product/quantity 4}]})

  (valid? ::customer/person {::customer/name "test"
                             ::customer/honorific "mr"
                             ::customer/address "my house"})

  (valid? ::order/order {::order/customer {::customer/name "12"
                                           ::customer/address "my test house"}
                         ::order/lines []}))
