(ns elasticmq-clj.default-test
  (:require [clojure.test :refer :all]
            [elasticmq-clj.core :as sut])
  (:import (com.amazonaws.client.builder AwsClientBuilder AwsClientBuilder$EndpointConfiguration)
           [com.amazonaws.services.sqs
            AmazonSQS
            AmazonSQSClientBuilder]

           [com.amazonaws.auth
            BasicAWSCredentials
            AWSStaticCredentialsProvider]

           [com.amazonaws.services.sqs.model
            CreateQueueRequest
            DeleteMessageBatchResultEntry
            DeleteMessageBatchResult
            DeleteMessageResult
            SendMessageBatchResultEntry
            BatchResultErrorEntry
            SendMessageBatchResult
            SendMessageResult
            SendMessageBatchRequestEntry
            SendMessageRequest
            SendMessageBatchRequest
            ReceiveMessageResult
            DeleteMessageRequest
            DeleteMessageBatchRequest
            DeleteMessageBatchRequestEntry
            ReceiveMessageRequest
            Message]))

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
    #p(-> client .listQueues)))

