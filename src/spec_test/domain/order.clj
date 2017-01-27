(ns spec-test.domain.order
  (:require [clojure.spec :as s]
            [spec-test.domain.product :as product]
            [spec-test.domain.customer :as customer]))

(s/def ::delivery-address string?)
(s/def ::billing-address string?)
(s/def ::quantity integer?)
(s/def ::description string?)
(s/def ::price bigdec?)

(s/def ::product (s/keys :req [::product/code] :opt [::product/description]))
;; (s/def ::customer (s/multi-spec customer/type ::customer/type))
(s/def ::charge (s/keys :req [::description ::price] :opt [::product ::quantity])) ;; TODO should be an or?
(s/def ::lines (s/* ::charge))
(s/def ::invoice (s/keys :req [::price] :opt [::customer/customer ::lines ::delivery-address ::billing-address]))

(defn- calculate-total [order]
  (->> order ::lines (map (juxt ::price ::quantity)) (map #(* (first %) (or (second %) 1))) (apply + 0.0M)))

(defn update-total [order]
  (assoc order ::price (calculate-total order)))

(defn price-matches-total? [order]
  (= (::price order) (calculate-total order)))

(defn delivery-address-set? [order]
  (boolean (::delivery-address order)))

(defn billing-address-set? [order]
  (boolean (::billing-address order)))

(defn new []
  {:post (s/valid? ::invoice %)}
  {::price 0.0M})

(defn product-description [{code ::product/code desc ::product/description :as product} quantity]
  (str quantity " x " code (when desc " ") desc))

(defn add-line
  ([order description price]
   (-> order
       (update-in [::lines] concat [{::description description ::price price}])
       update-total))
  ([order product quantity price]
   (-> order
       (update-in [::lines] concat [{::product product ::quantity quantity ::description (product-description product quantity) ::price price}])
       update-total)))

(defmulti set-customer #(::customer/type (second %&)))

(defmethod set-customer ::customer/person [order customer]
  (-> order
      (assoc ::customer customer)
      (assoc ::delivery-address (::customer/address customer))
      (assoc ::billing-address (::customer/address customer))))

(defmethod set-customer ::customer/company [order company]
  (-> order
      (assoc ::customer company)
      (assoc ::delivery-address (first (::customer/delivery-addresses company)))
      (assoc ::billing-address (::customer/address company))))

(s/fdef set-customer
        :args (s/cat :order ::order :customer ::customer/customer)
        :ret (s/and ::invoice delivery-address-set? billing-address-set?))

(s/fdef add-line
        :args (s/or :non-stock-line (s/cat :order       ::invoice
                                           :description ::description
                                           :price       ::price)
                    :product-line (s/cat :order         ::invoice
                                         :product       ::product
                                         :quantity      ::quantity
                                         :price         ::price))
        :ret (s/and ::invoice price-matches-total?))

(s/fdef new
        :args (s/cat)
        :ret (s/and ::invoice price-matches-total?))
