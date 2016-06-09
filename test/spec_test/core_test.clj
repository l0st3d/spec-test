(ns spec-test.core-test
  (:require [clojure.test :refer [deftest is are]]
            [clojure.spec :as s]
            [spec-test.domain.product :as product]
            [spec-test.domain.order :as order]
            [spec-test.domain.customer :as customer]))

(defn valid? [spec data]
  (is (nil? (s/explain-data spec data))))

(deftest a-test
  (valid? ::product/sku {::product/code "abc"})

  (valid? ::product/bundle {::product/code "123"
                            ::product/contents [{::product/code "abc"
                                                 ::product/quantity 4}]})
  
  (valid? ::product/composite {::product/code "abcdef"
                               ::product/contents [{::product/code "abc" ::product/quantity 1}
                                                   {::product/code "def" ::product/quantity 1}]})

  (valid? ::customer/person {::customer/name "test"
                             ::customer/honorific "Mr"
                             ::customer/address "my house"})

  (valid? ::order/order {::order/customer {::customer/name "12"
                                           ::customer/address "my test house"}
                         ::order/lines []}))
