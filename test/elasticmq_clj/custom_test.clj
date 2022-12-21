(ns elasticmq-clj.custom-test
  (:require [clojure.test :refer :all]
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
  (sut/with-elasticmq-fn (.getPath (clojure.java.io/resource "custom.conf")) f))

(use-fixtures :once around-all)

(def client
  (let [creds (new BasicAWSCredentials "x" "x")
        reg (new AwsClientBuilder$EndpointConfiguration "http://localhost:9321" "us-east-1")]
    (-> (AmazonSQSClientBuilder/standard)
        (.withEndpointConfiguration reg)
        (.withCredentials (new AWSStaticCredentialsProvider creds))
        (.build))))

(deftest can-wrap-around
  (testing "using custom conf"
     (-> client
         (.createQueue "wibble"))
     (is (= "{QueueUrls: [http://localhost:9321/000000000000/wibble],}" (str (-> client .listQueues))))))