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
                                               ::customer/address "my house"
                                               ::customer/type ::customer/person})))

  #_ (is (nil? (s/explain-data ::customer/company {::customer/name "test"
                                                   ::customer/address "my house"
                                                   ::customer/contacts [{::customer/name "test"
                                                                         ::customer/honorific "Mr"
                                                                         ::customer/address "my house"}]
                                                   ::customer/delivery-addresses []
                                                   ::customer/type ::customer/company})))

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
    (let [p (g customer/type)
          ;; _ (is (nil? p))
          new-order (-> (order/new)
                        (order/add-line (g ::product/sku) (g ::order/quantity) (g ::order/price))
                        (order/add-line (g ::order/description) (g ::order/price)))
          ;; new-order (order/set-customer new-order p)
          ]
      (is (nil? (s/explain-data ::order/invoice new-order)))
      (is (order/price-matches-total? new-order))
      (is (nil? (s/explain-data ::order/invoice new-order)))
      (is (nil? (s/explain-data ::customer/customer p)))
      (is (nil? (s/explain-data ::order/invoice (order/set-customer new-order p)))))))


;; In: [0] val:
;; ({:spec-test.domain.order/price -453.905890562222340861M, :spec-test.domain.order/lines ({:spec-test.domain.order/product {:spec-test.domain.product/code "50sUoS1952kc9ruVe", :spec-test.domain.product/description "9oO3UP6rRwUP23826NTXC7J4W"}, :spec-test.domain.order/quantity 7346, :spec-test.domain.order/description "7346 x 50sUoS1952kc9ruVe 9oO3UP6rRwUP23826NTXC7J4W", :spec-test.domain.order/price -0.061786429781932384M} {:spec-test.domain.order/description "321s5psyz4", :spec-test.domain.order/price -0.022777384147047997M})}
;;  {:spec-test.domain.customer/name "Zhz04", :spec-test.domain.customer/address "Hp", :spec-test.domain.customer/honorific "Miss"})

;; fails at: [:args] predicate: (cat :order :spec-test.domain.order/order :customer :spec-test.domain.customer/customer),  Extra input
