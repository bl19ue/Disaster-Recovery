package com.team01.disasterrecovery;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.net.URL;

import com.team01.disasterrecovery.managedentities.VCenter;
import com.team01.disasterrecovery.threadmanagement.ThreadFactory;
import com.team01.disasterrecovery.threadmanagement.ThreadInterface;
import com.vmware.vim25.mo.ServiceInstance;

public class AvailabilityManager {
	public static String INFO = "TEAM01: ";
	public static String ERROR = "ERROR: ";
	public static String SNAPSHOT_THREAD = "snapshot_thread";
	public static String HEARTBEAT_THREAD = "heartbeat_thread";
	
	private static String vCenterUrl = "130.65.132.101/sdk";
	private static String userName = "administrator";
	private static String password = "12!@qwQW";
	private static ThreadFactory threadFactory;

	public static void main(String[] args) throws RemoteException, MalformedURLException {
		//Creating a Service Instance object of our vCenter
		ServiceInstance serviceInstance = new ServiceInstance(new URL(vCenterUrl), userName, password, true);
		
		//Creating the new vCenter to get the running vCenter instance
		VCenter vCenter = new VCenter(serviceInstance);
		
		//Creating a thread factory for starting different threads
		threadFactory = new ThreadFactory();
		
		//Starting the heart beat thread
		startHeartbeatThread(vCenter);
		
		//Starting the backup thread
		startBackupThread(vCenter);
	}
	
	//This method starts a thread and regularly checks if the ManagedEntity is down or not.
	public static void startHeartbeatThread(VCenter vCenter) {
		//Using the factory we get an instance of the heartbeatsThread
		ThreadInterface threadInterface = threadFactory.getThread(HEARTBEAT_THREAD, vCenter);
		threadInterface.startThread();
	}
	
	//This method starts  a thread and regularly takes backup of the VMs
	public static void startBackupThread(VCenter vCenter) {
		//Using the factory we get an instance of the backupThread
		ThreadInterface threadInterface = threadFactory.getThread(SNAPSHOT_THREAD, vCenter);
		threadInterface.startThread();
	}
	
}
