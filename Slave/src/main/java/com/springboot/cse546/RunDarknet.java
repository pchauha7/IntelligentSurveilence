package com.springboot.cse546;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class RunDarknet {
	
	private String videoName;
	public RunDarknet(String videoName) {
		this.videoName = videoName;
	}
	
	public void run() {
		// TODO Auto-generated method stub
		String executeCommand = "sh /home/ubuntu/darknet_executer.sh " + 
				 videoName.substring(0,videoName.length()-5);
		
		
		try {
			System.out.println("Executing the darknet command");
			Process p = Runtime.getRuntime().exec(executeCommand);
			BufferedReader stdin = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new 
				     InputStreamReader(p.getErrorStream()));
			String s = null;
			while ((s = stdin.readLine()) != null) {
								
			    System.out.println(s);
			    
			}
			System.out.println("Here is the standard error of the command (if any):\n");
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			
			}
			System.out.println("Executing complete");

		}
		catch (Exception e) {
			Process p;
			try {
				p = Runtime.getRuntime().exec(executeCommand);
				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
				BufferedReader serror = new BufferedReader(new 
					     InputStreamReader(p.getErrorStream()));
				System.out.println(in);

			} 
			catch (IOException e1) {
				e1.printStackTrace();
				
			} 
		
		
		}
	
	}
	

}
