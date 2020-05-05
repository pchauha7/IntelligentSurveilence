package com.springboot.cse546;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

public class Ec2clientApplication {
	
	public static String access_key;
	public static String secret_key;

	public static void main(String[] args) {
		
		Properties prop = new Properties();
		
		try (InputStream input = new FileInputStream("src/main/resources/config.properties")) {

            // load a properties file
            prop.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
		access_key = prop.getProperty("aws_access_key_id");
		secret_key = prop.getProperty("aws_secret_access_key");
		
		
		 
		 BasicAWSCredentials awsCreds = new BasicAWSCredentials(
				 access_key, secret_key);
		
		AmazonSQS sqsClient = AmazonSQSClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();
		
		
		 
		 final String slaveQueue = "slaveCSE546Queue";
		 String slaveQueueUrl = "";
		 String videoName="";
		 try {
				slaveQueueUrl = sqsClient.getQueueUrl(slaveQueue).getQueueUrl();
	     } 
		 catch (QueueDoesNotExistException queueDoesNotExistException) {
	            System.out.println("Queues do not exist. Please create them");
	     }
		 
		 
		 
		 while (true) {
			 	 final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(slaveQueueUrl);
				 List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).getMessages();
				 
				 System.out.println("Slave queue size: " + messages.size());
				 if(messages.size()==0) {
					 continue;
				 }
				 
				 videoName = messages.get(0).getBody();
					
				 
				 System.out.println(videoName);
				 String messageReceiptHandle = messages.get(0).getReceiptHandle();
				 try {
				 if (sqsClient.deleteMessage(new DeleteMessageRequest(slaveQueueUrl, messageReceiptHandle)) != null) {
					 System.out.println("Delete message successful");
					 break;
				 }
				
				 }
				 catch(Exception e) {
					 System.out.println("Exception occured");
					// e.printStackTrace();
					 continue;
				 }
				 
				 System.out.println("Waiting for video");
			
		 }
		 System.out.println("Out of while loop");
		 VideoDownloadService vds= new VideoDownloadService();
		 boolean flag=vds.download(videoName.substring(0, videoName.length()-5));
		 if(flag==false) {
			 final String piQueue = "piCSE546Queue";
			 String piQueueUrl = "";
			 try {
				 piQueueUrl = sqsClient.getQueueUrl(piQueue).getQueueUrl();
				 sqsClient.sendMessage(piQueueUrl, videoName);
			 }
			 catch (Exception e) {
				 System.out.println("Exception occured");
				 e.printStackTrace();
			 }
			 
		 }
		
	}

}
