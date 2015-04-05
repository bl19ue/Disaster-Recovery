package com.team01.disasterrecovery.availability;

import com.team01.disasterrecovery.AvailabilityManager;

public class Reachable {
	public static boolean ping(String ip){
		try{
			System.out.println("Pinging "+ ip );
			String pingCommand = "ping " + ip;
			
			//Starting a new process in the OS's runtime and executing the command on a new thread
			Process process = Runtime.getRuntime().exec(pingCommand);
			//Wait for the above thread to complete
			process.waitFor();		
			//return true if it was reachable
			return process.exitValue() == 0;
		}
		catch(Exception e){
			System.out.println(AvailabilityManager.ERROR + "could not ping the virtual machine" + "Reason:" + e.toString());
		}
		return false;
	}
}
