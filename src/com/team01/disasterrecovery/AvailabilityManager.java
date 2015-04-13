package com.team01.disasterrecovery;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.net.URL;

import com.team01.disasterrecovery.managedentities.VCenter;
import com.team01.disasterrecovery.threadmanagement.ThreadFactory;
import com.team01.disasterrecovery.threadmanagement.ThreadInterface;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;

public class AvailabilityManager {
	public static String INFO = "TEAM01: ";
	public static String ERROR = "ERROR: ";
	public static String SNAPSHOT_THREAD = "snapshot_thread";
	public static String HEARTBEAT_THREAD = "heartbeat_thread";
	public static String[] hostsArray = {"130.65.132.131","130.65.132.132","130.65.132.133"};
	
	private static String vCenterUrl = "https://130.65.132.101/sdk";
	private static String userName = "administrator";
	private static String password = "12!@qwQW";
	private static ThreadFactory threadFactory;
	private static String dcname= "T01-DC";
	private static HashMap<String, String> hostMap;

	public static void main(String[] args) throws Exception {
		//Creating a Service Instance object of our vCenter
		ServiceInstance serviceInstance = new ServiceInstance(new URL(vCenterUrl), userName, password, true);
		
		//Creating the new vCenter to get the running vCenter instance
		VCenter vCenter = new VCenter(serviceInstance);
		
		//Let's set the static hosts
		
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
	
	public static String getHostName(String ip){
		hostMap = new HashMap<String, String>();
		hostMap.put("130.65.132.131", "T01-vHost01_132.131");
		hostMap.put("130.65.132.132", "T01-vHost02_132.132");
		hostMap.put("130.65.132.133", "T01-vHost03_132.133");
		String name = hostMap.get(ip);
		//System.out.println(name);
		return name;
	}
	
	public static String getIPVHost(String ip){
		hostMap = new HashMap<String, String>();
		hostMap.put("T01-vHost01_132.131", "130.65.132.131");
		hostMap.put("T01-vHost02_132.132", "130.65.132.132");
		hostMap.put("T01-vHost03_132.133", "130.65.132.133");
		String name = hostMap.get(ip);
		System.out.println(name);
		return name;
	} 
	
	public static String getHostSsl(String hostIp) {
		hostMap = new HashMap<String, String>();
		hostMap.put("130.65.132.131", "66:D8:F5:E8:08:69:99:D5:4D:52:8A:1B:C9:9D:9D:16:4F:FD:2A:64");
		hostMap.put("130.65.132.132", "9A:21:AE:81:B3:59:20:0E:75:D8:45:0F:76:BB:30:38:E9:6E:E2:39");
		hostMap.put("130.65.132.133", "02:05:54:83:FF:3E:4B:45:D1:32:7D:2F:77:74:46:96:31:CD:AD:49");
		String ssl = hostMap.get(hostIp);
		System.out.println(ssl);
		return ssl;
	}
}
