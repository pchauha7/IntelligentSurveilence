package com.springboot;

import java.io.File;
import java.util.HashSet;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;


public class MonitorService implements Runnable {
	
	public void run() {
		HashSet<String> hs = new HashSet<String>();
		String folderName = "/home/pi/work";
		//BasicAWSCredentials awsCreds = new BasicAWSCredentials("ASIA25FZ7XDFTQKIW42I", "7qWaaWpC4LhnBFefW59S+Tias+T9cMPatndZseLn");
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(
				DemoApplication.access_key, 
				DemoApplication.secret_key
				);
		final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();
		final String videoBucket = "cse546-rpi-videos";
				
		while (DemoApplication.flag) {
			
			File[] listFiles = new File(folderName).listFiles();

			for (int i = 0; i < listFiles.length; i++) {

			    if (listFiles[i].isFile()) {
			        String fileName = listFiles[i].getName();
			        if (fileName.startsWith("video") && fileName.endsWith(".h264")) {
			        	
			        	
			            if (!hs.contains(fileName)) {
			            	hs.add(fileName);
			            	
							//Uploading to Priority Queue
			            	long file_length;
			            	while(true) {
			            		
			            		 File file1 = new File (folderName + "/" + fileName);
			            		 file_length=file1.length();
			            		 try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
			            		 File file2 = new File (folderName + "/" + fileName);
			            		 long temp_length=file2.length();
			            		 if (file_length==temp_length) {
			            			 break;
			            		 }
			            		 
			            	}
			                
			                DemoApplication.videoQueue.add(fileName);
			                
			                
			                
			                //Uploading video to S3 bucket
			            	try {
			            		PutObjectRequest request = new PutObjectRequest(videoBucket, fileName, new File(folderName + "/" + fileName));
				                ObjectMetadata metadata = new ObjectMetadata();
				                metadata.setContentType("video");
				                request.setMetadata(metadata);
				                s3.putObject(request);
			            	}
			            	catch(Exception e){
			            		try {
									Thread.sleep(3000);
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
			            		PutObjectRequest request = new PutObjectRequest(videoBucket, fileName, new File(folderName + "/" + fileName));
				                ObjectMetadata metadata = new ObjectMetadata();
				                metadata.setContentType("video");
				                request.setMetadata(metadata);
				                s3.putObject(request);
			            	}
			                
			            	
			                
			            }
			        }
			    }
			}
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
