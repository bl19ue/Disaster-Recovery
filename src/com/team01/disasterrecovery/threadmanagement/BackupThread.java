package com.team01.disasterrecovery.threadmanagement;

import com.team01.disasterrecovery.AvailabilityManager;
import com.team01.disasterrecovery.managedentities.VCenter;

public class BackupThread implements ThreadInterface{
	private String TYPE_OF_THREAD = "Backup";
	private VCenter vCenter;
	private Thread backupThread;
	
	public BackupThread(VCenter vCenter){
		this.vCenter = vCenter;
	}
	
	@Override
	public void run() {
		try{
			vCenter.createSnapshot();
		}
		catch(Exception e){
			System.out.println(AvailabilityManager.ERROR 
					+ "Could not start the thread for " + TYPE_OF_THREAD 
					+ " Reason: " + e.toString());
		}
	}

	@Override
	public void startThread() {
		if(backupThread == null){
			backupThread = new Thread(this, TYPE_OF_THREAD);
			
			//Start this thread now
			backupThread.start();
		}
	}

}
