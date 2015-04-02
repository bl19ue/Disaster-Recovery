package com.team01.disasterrecovery.threadmanagement;

import com.team01.disasterrecovery.AvailabilityManager;
import com.team01.disasterrecovery.managedentities.VCenter;

public class ReachableThread implements ThreadInterface{
	private String TYPE_OF_THREAD = "Heartbeats";
	private VCenter vCenter;
	private Thread reachableThread;
	
	public ReachableThread(VCenter vCenter){
		this.vCenter = vCenter;
	}
	
	@Override
	public void run() {
		try{
			vCenter.ping();
		}
		catch(Exception e){
			System.out.println(AvailabilityManager.ERROR 
					+ "Could not start the thread for " + TYPE_OF_THREAD 
					+ " Reason: " + e.toString());
		}
	}

	@Override
	public void startThread() {
		if(reachableThread == null){
			reachableThread = new Thread(this, TYPE_OF_THREAD);
			
			//Start this thread now
			reachableThread.start();
		}
	}

}
