(ns spec-test.core-test
  (:require [clojure.test :refer [deftest is testing are]]
            [clojure.test.check :as tc]
            [clojure.spec :as s]
            [clojure.spec.test :as st]
            [clojure.spec.gen :as gen]
            [spec-test.domain.product :as product]
            [spec-test.domain.order :as order]
            [spec-test.domain.customer :as customer]
            [spec-test.domain.repository :as repo]))

(deftest a-test
  (is (nil? (s/explain-data ::product/sku {::product/code "abc"})))

  (is (nil? (s/explain-data ::product/bundle {::product/code     "123"
                                              ::product/quantity 4})))
  
  (is (nil? (s/explain-data ::product/composite {::product/code     "abcdef"
                                                 ::product/contents [{::product/code "abc" ::product/quantity 1}
                                                                     {::product/code "def" ::product/quantity 1}]})))

  (let [person  {::customer/name      "test"
                 ::customer/honorific "Mr"
                 ::customer/address   "my house"
                 ::customer/type      ::customer/person}
        company {::customer/name               "test"
                 ::customer/address            "my house"
                 ::customer/contacts           [person]
                 ::customer/delivery-addresses []
                 ::customer/type               ::customer/company}]
    (is (nil? (s/explain-data ::customer/customer person)))
    (is (nil? (s/explain-data customer/customer-type person)))
    (is (nil? (s/explain-data ::customer/customer company)))
    (is (nil? (s/explain-data customer/customer-type company))))

  (is (nil? (s/explain-data ::order/invoice {::order/customer {::customer/name      "12"
                                                               ::customer/address   "my test house"
                                                               ::customer/honorific "Mr"
                                                               ::customer/type      ::customer/person}
                                             ::order/lines    []
                                             ::order/price    0.0M}))))

(deftest b-test
  (testing "Repository"
    (let [customers    (repo/initial-customers)
          products     (repo/initial-products)
          orders       (repo/initial-orders)
          new-order    (gen/generate (s/gen ::order/invoice))
          new-customer (gen/generate (s/gen ::customer/customer))
          new-product  (gen/generate (s/gen ::product/sku))]
      (testing "adding order"
        (dosync
         (alter customers assoc 1 new-customer)
         (alter products assoc 1 new-product)
         (alter orders assoc 1 new-order))
        (is (= 1 (count @orders)))))))

(def g (comp gen/generate s/gen))

(deftest c-test
  (testing "Domain"
    (let [p         (g ::customer/person)
          new-order (-> (order/new)
                        (order/add-line (g ::product/sku) (g ::order/quantity) (g ::order/price))
                        (order/add-line (g ::order/description) (g ::order/price)))]
      (is (nil? (s/explain-data ::order/invoice new-order)))
      (is (order/price-matches-total? new-order))
      (is (nil? (s/explain-data ::order/invoice new-order)))
      (is (nil? (s/explain-data customer/customer-type p)))
      (is (nil? (s/explain-data ::customer/person p)))
      (is (nil? (s/explain-data ::order/invoice (order/set-customer new-order p)))))))

(s/fdef order/set-customer
        :args (s/cat :order ::order/invoice :customer ::customer/customer)
        :ret (s/and ::invoice order/delivery-address-set? order/billing-address-set?))

(s/fdef order/add-line
        :args (s/or :non-stock-line (s/cat :order       ::order/invoice
                                           :description ::order/description
                                           :price       ::order/price)
                    :product-line (s/cat :order         ::order/invoice
                                         :product       ::order/product
                                         :quantity      ::order/quantity
                                         :price         ::order/price))
        :ret (s/and ::order/invoice order/price-matches-total?) ;TODO spec is defined in terms of domain - ciruclar reasoning mistake??
        :fn (fn [{{:keys [lines]} :ret {:keys [non-stock-line product-line]} :args}]
              (let [content-matches? (cond non-stock-line #(.contains % (str (:description non-stock-line)))                                                                
                                           product-line   #(and (.contains % (str (::product/quantity (:product product-line))))
                                                                (.contains % (str (::product/code (:product product-line))))
                                                                (or (nil? (::product/description (:product product-line)))
                                                                    (.contains % (str (::product/description (:product product-line))))))
                                           :else          (constantly nil))]
                (or (empty? lines) (->> lines (map :description) (some content-matches?))))))

(s/fdef order/new
        :args (s/cat)
        :ret (s/and ::invoice order/price-matches-total?))

(st/instrument)

(deftest d-test
  (testing "generators"
    (is (= (st/summarize-results (st/check (st/enumerate-namespace 'spec-test.domain.order)))
           {:total 3, :check-passed 3}))
    (is (= (st/summarize-results (st/check (st/enumerate-namespace 'spec-test.domain.customer)))
           {:total 0}))
    (is (= (st/summarize-results (st/check (st/enumerate-namespace 'spec-test.domain.product)))
           {:total 0}))
    (is (= (st/summarize-results (st/check (st/enumerate-namespace 'spec-test.domain.repository)))
           {:total 0}))))
