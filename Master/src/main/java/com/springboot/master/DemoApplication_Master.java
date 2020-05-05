package com.springboot.master;

import java.util.List;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.ec2.model.Filter;


public class DemoApplication_Master {
	
	public static void main(String[] args) {
		
		Properties prop = new Properties();
		
		try (InputStream input = new FileInputStream("src/main/resources/config.properties")) {

            // load a properties file
            prop.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
		
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(
				prop.getProperty("aws_access_key_id"), 
				prop.getProperty("aws_secret_access_key")
				);
		
		final AmazonSQS sqs = AmazonSQSClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();
		final String mainQueue = "MainCSE546Queue";
		final String piQueue = "piCSE546Queue";
		final String slaveQueue = "slaveCSE546Queue";
		
		String mainQueueURL = "";
		String piQueueURL = "";
		String slaveQueueUrl = "";
		int slaveCount = 0;
		HashSet<String> hs = new HashSet<String>();
		HashSet<String> instance_tracker = new HashSet<String>();
		
		try {
			mainQueueURL = sqs.getQueueUrl(mainQueue).getQueueUrl();
			piQueueURL = sqs.getQueueUrl(piQueue).getQueueUrl();
			slaveQueueUrl = sqs.getQueueUrl(slaveQueue).getQueueUrl();
        } catch (QueueDoesNotExistException queueDoesNotExistException) {
            System.out.println("Queues do not exist. Please create them");
        }
		
		try {
	
			while (true) {
				final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(mainQueueURL);
				List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
				System.out.println("Main queue size: " + messages.size());
				
				
				int mainMessageSize = messages.size();
				System.out.println("Main queue size: " + mainMessageSize);
				if (mainMessageSize >0) {
					
					if (hs.contains(messages.get(0).getBody()))
						continue;
					
					final ReceiveMessageRequest piReceiveMessageRequest = new ReceiveMessageRequest(piQueueURL);
					List<Message> pimessages = sqs.receiveMessage(piReceiveMessageRequest).getMessages();
					
					System.out.println("PI queue size: " + pimessages.size());
					
					if (pimessages.size() <=1 && slaveCount>=5) {
						
						// Delete message in Main Queue
						String videoName = messages.get(0).getBody();
						String messageReceiptHandle = messages.get(0).getReceiptHandle();
						sqs.deleteMessage(new DeleteMessageRequest(mainQueueURL, messageReceiptHandle));
						
						
						//Sending message to pi Queue
						
						SendMessageRequest sqsRequest = new SendMessageRequest().withQueueUrl(piQueueURL).withMessageBody(videoName).withDelaySeconds(0);
		                sqs.sendMessage(sqsRequest).getMessageId();
		                hs.add(videoName);
						
						System.out.println("Successfully sent: messageId " + videoName);
						//slaveCount =0;
						
						
								
					}
					else {

						List <String> instanceIds = new ArrayList<String>();
						
						List <Instance> slaveInstances = new ArrayList<Instance>();
						DescribeInstancesRequest request = new DescribeInstancesRequest();
						List<String> valueT = new ArrayList<String>();
						valueT.add("Executor_EC2");
						Filter filter1 = new Filter("tag:Name", valueT);

						DescribeInstancesResult result = ec2.describeInstances(request.withFilters(filter1));
						List<Reservation> reservations = result.getReservations();
						boolean flag = false;
						for (Reservation reservation : reservations) {
							List<Instance> instances = reservation.getInstances();
							if (flag)
								break;
						
						for (Instance instance : instances) {
							slaveInstances.add(instance);
							System.out.println(instance.getInstanceId() + instance.getState().getName());
							if (instance.getState().getName().equalsIgnoreCase("stopped")) {
								if (instance_tracker.contains(instance.getInstanceId()))
										continue;
								instanceIds.add(instance.getInstanceId());
								instance_tracker.add(instance.getInstanceId());
								flag = true;
								break;
							}
								
							
							}
						}
						
						if (instanceIds.size()==0)
							continue;
						
						
						
						StartInstancesRequest startInstancesRequest = new StartInstancesRequest().withInstanceIds(instanceIds);
						
						System.out.println(instanceIds);
						
						
								 
						ec2.startInstances(startInstancesRequest);
						
						
							
						String videoName = messages.get(0).getBody();
						String messageReceiptHandle = messages.get(0).getReceiptHandle();
						sqs.deleteMessage(new DeleteMessageRequest(mainQueueURL, messageReceiptHandle));
						
						SendMessageRequest sqsRequest = new SendMessageRequest().withQueueUrl(slaveQueueUrl).withMessageBody(videoName).withDelaySeconds(0);
		                sqs.sendMessage(sqsRequest).getMessageId();
		                slaveCount++;
					
						
					}
					
					
					
					
					
				}
				
				Thread.sleep(500);
					
				
				
			}
			
		}	
		catch(Exception e) {
			e.printStackTrace();
		}

	}
}

