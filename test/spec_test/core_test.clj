(ns spec-test.core-test
  (:require [clojure.test :refer [deftest is testing are]]
            [clojure.test.check :as tc]
            [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [spec-test.domain.product :as product]
            [spec-test.domain.order :as order]
            [spec-test.domain.customer :as customer]
            [spec-test.domain.repository :as repo]))

(deftest a-test
  (is (nil? (s/explain-data ::product/sku {::product/code "abc"})))

  (is (nil? (s/explain-data ::product/bundle {::product/code "123"
                                              ::product/quantity 4})))
  
  (is (nil? (s/explain-data ::product/composite {::product/code "abcdef"
                                                 ::product/contents [{::product/code "abc" ::product/quantity 1}
                                                                     {::product/code "def" ::product/quantity 1}]})))

  (is (nil? (s/explain-data ::customer/person {::customer/name "test"
                                               ::customer/honorific "Mr"
                                               ::customer/address "my house"})))

  (is (nil? (s/explain-data ::customer/company {::customer/name "test"
                                                ::customer/address "my house"
                                                ::customer/contacts [{::customer/name "test"
                                                                      ::customer/honorific "Mr"
                                                                      ::customer/address "my house"}]
                                                ::customer/delivery-addresses []})))

  (is (nil? (s/explain-data ::order/invoice {::order/customer {::customer/name "12"
                                                               ::customer/address "my test house"
                                                               ::customer/honorific "Mr"
                                                               ::customer/type ::customer/person}
                                             ::order/lines []
                                             ::order/price 0.0M}))))

(deftest b-test
  (testing "Repository"
    (let [customers (repo/initial-customers)
          products (repo/initial-products)
          orders (repo/initial-orders)
          new-order (gen/generate (s/gen ::order/invoice))
          new-customer (gen/generate (s/gen ::customer/person))
          new-product (gen/generate (s/gen ::product/sku))]
      (testing "adding order"
        (dosync
         (alter customers assoc 1 new-customer)
         (alter products assoc 1 new-product)
         (alter orders assoc 1 new-order))
        (is (= 1 (count @orders)))))))

(def g (comp gen/generate s/gen))

(deftest c-test
  (testing "Domain"
    (let [p (g ::customer/person)
          ;; _ (is (nil? p))
          new-order (-> (order/new)
                        (order/add-line (g ::product/sku) (g ::order/quantity) (g ::order/price))
                        (order/add-line (g ::order/description) (g ::order/price)))
          ;; new-order (order/set-customer new-order p)
          ]
      (is (nil? (s/explain-data ::order/invoice new-order)))
      (is (order/price-matches-total? new-order)))))
