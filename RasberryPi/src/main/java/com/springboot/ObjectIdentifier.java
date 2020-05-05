package com.springboot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ObjectIdentifier {

	public void objid(String path) throws IOException {
		 File old_file = new File(path);
	        BufferedReader br = new BufferedReader(new FileReader (old_file));
	        Set <String> obj_Set = new HashSet <String> ();
	        String st;
	        while ((st = br.readLine()) != null) {
	            if (st.contains ("%")){
//	                System.out.println(st.split (":")[0]);
	                obj_Set.add (st.split (":")[0]);
	            }
	            if (st.toLowerCase().contains("floating")) {
	            	obj_Set.add (st);
	            }
	        }
//	        System.out.println (obj_Set);
	        File newFile = new File(old_file.getParent ()+"/"+old_file.getName ());
	        BufferedWriter writer = new BufferedWriter(new FileWriter (newFile));
	        writer.write (obj_Set.toString ());
	        writer.close();
	        System.out.println (obj_Set);
	}

}
