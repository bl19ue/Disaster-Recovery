package com.team01.disasterrecovery.threadmanagement;

import com.team01.disasterrecovery.AvailabilityManager;
import com.team01.disasterrecovery.managedentities.VCenter;

//Factory method for creating different threads
public class ThreadFactory {
	public ThreadInterface getThread(String typeOfThread, VCenter vCenter){
		
		switch(typeOfThread){
			//If the request if for heart beats
			case "heartbeat_thread"   : {
				return new ReachableThread(vCenter);
			}
			
			//If the request if for backup
			case "snapshot_thread"	  : {
				return new BackupThread(vCenter);
			}
			
			default			  		  : {
				System.out.println(AvailabilityManager.ERROR + "Not a valid thread request, sending NULL back");
				return null;
			}
		}
	}
}
