package com.springboot.cse546;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.springboot.cse546.ObjectIdentifier;

public class VideoDownloadService {
	
	public boolean download(String videoName) {
		
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(
				Ec2clientApplication.access_key, 
				Ec2clientApplication.secret_key
				);
		String folderName = "/home/ubuntu/work";
		AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();
		String fileToUpload = videoName+".txt";
		videoName = videoName+".h264";
		final String videoBucket = "cse546-rpi-videos";
		final String resultBucket = "cse546-result";
		
		System.out.println(videoBucket);
		 
		try {
			 while(!s3.doesObjectExist(videoBucket, videoName)) {
				   	Thread.sleep(2000);
			    	s3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();
			    }
	        ListObjectsV2Result result = s3.listObjectsV2(videoBucket);
	        List<S3ObjectSummary> objects = result.getObjectSummaries();
	        for (S3ObjectSummary os : objects) {
	        System.out.println("* " + os.getKey());
		    }
		    System.out.format("Downloading %s from S3 bucket %s...\n", videoName, videoBucket);
	         
		    //String videoPath=folderName+"/"+videoName;
		   
	   	    S3Object o = s3.getObject(videoBucket, videoName);
	   	    S3ObjectInputStream s3is = o.getObjectContent();
	   
	   	    FileOutputStream fos = new FileOutputStream(new File(folderName+"/"+videoName));
	   	    byte[] read_buf = new byte[1024];
	   	    int read_len = 0;
	   	    while ((read_len = s3is.read(read_buf)) > 0) {
	   	        fos.write(read_buf, 0, read_len);
	   	    }
	   	    s3is.close();
	   	    fos.close();
	   	    RunDarknet rd = new RunDarknet(videoName);
	   	    rd.run();
	   	   	ObjectIdentifier oi = new ObjectIdentifier();
	   		try {
	   			oi.objid(folderName + "/" + fileToUpload);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				
			}
	   	    
	   	    File resultfile=new File(folderName + "/" + fileToUpload);
	   	    if (resultfile.exists()) {
	   	    	PutObjectRequest request = new PutObjectRequest(resultBucket, fileToUpload, new File(folderName + "/" + fileToUpload));
		        ObjectMetadata metadata = new ObjectMetadata();
		        metadata.setContentType("plain/text");
		        request.setMetadata(metadata);
		        s3.putObject(request);
		        return true;
	   	    }
	   	    return false;
	   	    
			
	        		        	
			}
			catch (Exception e) {
	    	   e.printStackTrace();
	    	   //System.exit(1);
	    	   return false;
	    	   
	    	} 
	    	  
		
		
	}

}
