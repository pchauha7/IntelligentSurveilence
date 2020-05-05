package com.springboot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;

public class ExecuteOnPie implements Runnable {
	private String videoName;
	
	public ExecuteOnPie(String videoName) {
		this.videoName = videoName;
	}
	
	public void run() {
		String folderName = "/home/pi/work";
		String fileToUpload = videoName.substring(0, videoName.length() -5) + ".txt";
		String executeCommand = "sh /home/pi/darknet_executer.sh " + 
				 videoName.substring(0,videoName.length()-5);
		//String parserFile = "";
		String lockFile = "lock.txt";
		
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(
				DemoApplication.access_key, 
				DemoApplication.secret_key
				);
		final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();
		final String resultBucket = "cse546-result";
		File tempFile = new File(folderName + "/" + lockFile);

		try {
			Thread.sleep(2000);
			System.out.println("Executing the main command");
			Process p = Runtime.getRuntime().exec(executeCommand);
			BufferedReader stdin = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new 
				     InputStreamReader(p.getErrorStream()));
			String s = null;
			while ((s = stdin.readLine()) != null) {
			    System.out.println(s);
			}

			// Read any errors from the attempted command
			System.out.println("Here is the standard error of the command (if any):\n");
			while ((s = stdError.readLine()) != null) {
			    System.out.println(s);
			}
			System.out.println("Execution complete");
			
			ObjectIdentifier oi = new ObjectIdentifier();
			try {
				oi.objid(folderName + "/" + fileToUpload);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				
			}
			
			//Uploading video to S3 bucket
	    	
	    	PutObjectRequest request = new PutObjectRequest(resultBucket, fileToUpload,new File(folderName + "/" + fileToUpload));
	        ObjectMetadata metadata = new ObjectMetadata();
	        metadata.setContentType("plain/text");
	        request.setMetadata(metadata);
	        s3.putObject(request);
			
			
		} catch (Exception e) {
				e.printStackTrace();
			
			
			
		 }
		
		finally {
			
			
	        try {
				boolean result = Files.deleteIfExists(tempFile.toPath());
				if (result)
					System.out.println("Successfully dropped lock file");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
        
        
		
	}

}
