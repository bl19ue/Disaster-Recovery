package com.team01.disasterrecovery;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import java.net.URL;

import com.vmware.vim25.mo.ServiceInstance;

public class AvailabilityManager {
	public static String INFO = "TEAM01: ";
	public static String ERROR = "ERROR: ";
	public static String vCenterUrl = "130.65.132.101/sdk";
	public static String userName = "administrator";
	public static String password = "12!@qwQW";

	public static void main(String[] args) throws RemoteException, MalformedURLException {
			
		//Creating a Service Instance object of our vCenter
		ServiceInstance si = new ServiceInstance(new URL(vCenterUrl), userName, password, true);
		VCenter vc = new VCenter(si);
		pingVM(vc);
		takeSnapshot(vc);
	}
	
	//This method creates a thread for taking backups after specific time
	public static void takeSnapshot(VCenter vc) {
		ThreadManager vmSnapshot = new ThreadManager(vc, "createSnapshot");
		vmSnapshot.start();
	}
	
	//This method creates a thread which continously pings a VM to check its availability	
	public static void pingVM(VCenter vc) {
		ThreadManager vmHeartbeat = new ThreadManager(vc, "ping"); 
		vmHeartbeat.start();
		
	}

}
