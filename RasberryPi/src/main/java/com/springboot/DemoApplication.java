package com.springboot;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Queue;

public class DemoApplication {
	
	
	
	public static Queue<String> videoQueue = new PriorityQueue<String>(new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			String prefix = "video";
			String suffix = ".h264";
			o1 = o1.substring(prefix.length());
			o2 = o2.substring(prefix.length());
			
			return Integer.parseInt(o2.substring(0,o2.length()-suffix.length())) - 
					Integer.parseInt(o1.substring(0,o1.length()-suffix.length()));
		}
	});
	
	public static String access_key;
	public static String secret_key;
	
	public static boolean flag = true;
	
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
		
		try {
			Thread t1 = new Thread(new MonitorService());
			t1.start();
			
			Thread t2 = new Thread(new ManagerService());
			t2.start();
			
			t1.join();
			t2.join();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			flag = false;
		}

	}

}
