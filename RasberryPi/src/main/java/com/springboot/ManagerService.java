package com.springboot;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

public class ManagerService implements Runnable {
	
	public void run() {
		String folderName = "/home/pi/work";
		String lockFile = "lock.txt";
		
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(
				DemoApplication.access_key, 
				DemoApplication.secret_key
				);
		final AmazonSQS sqs = AmazonSQSClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();
		final String myQueue = "MainCSE546Queue";
		final String piQueue = "piCSE546Queue";
		//String myQueueUrl = "";
		String piQueueURL = sqs.getQueueUrl(piQueue).getQueueUrl();
		GetQueueUrlResult myQueueUrl=sqs.getQueueUrl(myQueue);
		
		while (DemoApplication.flag) {
			
			if (!DemoApplication.videoQueue.isEmpty()) {
				
				String newVideo = DemoApplication.videoQueue.poll();
				
				/*
				 * Check if the Raspberry Pie is free and allocate the resource
				 */
				
				File tempFile = new File(folderName + "/" + lockFile);
				if (!tempFile.exists()) {
					/*
					 * Service to execute Pie to run algorithm on video
					 * Post results on S3 bucket
					 */
					try {
						tempFile.createNewFile();
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					Thread t = new Thread(new ExecuteOnPie(newVideo));
					t.start();
				}
				else {
	                sqs.sendMessage(myQueueUrl.getQueueUrl(), newVideo);
	                System.out.println("sending message succesfull "+ newVideo );

				}
				
			}
			
			
			else {
				File tempFile = new File(folderName + "/" + lockFile);
				if (!tempFile.exists()) {
					/*
					 * Service to execute Pie to run algorithm on video
					 * Post results on S3 bucket
					 */
					
					final ReceiveMessageRequest piReceiveMessageRequest = new ReceiveMessageRequest(piQueueURL);
					List<Message> pimessages = sqs.receiveMessage(piReceiveMessageRequest).getMessages();
					if (pimessages.size()==0)
						continue;
					String videoName = pimessages.get(0).getBody();
					String messageReceiptHandle = pimessages.get(0).getReceiptHandle();
					sqs.deleteMessage(new DeleteMessageRequest(piQueueURL, messageReceiptHandle));
					
					try {
						tempFile.createNewFile();
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					
					Thread t1 = new Thread(new ExecuteOnPie(videoName));
					t1.start();
				}
				
					
				
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
