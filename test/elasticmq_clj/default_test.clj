(ns elasticmq-clj.default-test
  (:require [clojure.test :refer :all]
            [cheshire.core :as json]
            [elasticmq-clj.core :as sut])
  (:import (com.amazonaws.client.builder AwsClientBuilder AwsClientBuilder$EndpointConfiguration)
           [com.amazonaws.services.sqs
            AmazonSQSClientBuilder]
           [com.amazonaws.auth
            BasicAWSCredentials
            AWSStaticCredentialsProvider]))

(use-fixtures :once sut/with-elasticmq-fn)

(defn around-all
  [f]
  (sut/with-elasticmq-fn f))

(use-fixtures :once around-all)

(def client
  (let [creds (new BasicAWSCredentials "x" "x")
        reg (new AwsClientBuilder$EndpointConfiguration "http://localhost:9324" "us-east-1")]
    (-> (AmazonSQSClientBuilder/standard)
        (.withEndpointConfiguration reg)
        (.withCredentials (new AWSStaticCredentialsProvider creds))
        (.build))))

(deftest can-wrap-around
  (testing "using defaults"
     (-> client
         (.createQueue "wibble"))
     (is (= "{QueueUrls: [http://localhost:9324/000000000000/wibble],}" (str (-> client .listQueues))))))

