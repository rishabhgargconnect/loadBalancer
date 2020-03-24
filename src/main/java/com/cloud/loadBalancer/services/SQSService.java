//package com.cloud.loadBalancer.services;
//
//import com.amazonaws.auth.AWSCredentialsProvider;
//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.services.sqs.AmazonSQS;
//import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
//import com.amazonaws.services.sqs.model.SendMessageResult;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//@Service
//public class SQSService {
//    @Value("${sqs.url}")
//    private String sqsUrl;
//
//    @Value("${aws.accessKey}")
//    private String awsAccessKey;
//
//    @Value("${aws.secretKey}")
//    private String awsSecretKey;
//
//    @Value("${aws.region}")
//    private String awsRegion;
//
//    private AmazonSQS amazonSQS;
//
//    public SQSService() {
//        final AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(
//                new BasicAWSCredentials(awsAccessKey, awsSecretKey)
//        );
//
//        this.amazonSQS = AmazonSQSClientBuilder.standard().withCredentials(awsCredentialsProvider).build();
//    }
//
//    public SendMessageResult sendMessage(String sqsUrl, Object message) {
//        //TODO Fix/Adapt this to send api request
//        return this.amazonSQS.sendMessage(sqsUrl, (String) message);
//    }
//
//}
