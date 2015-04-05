package com.team01.disasterrecovery.managedentities;

import java.net.URL;

import com.team01.disasterrecovery.AvailabilityManager;
import com.vmware.vim25.mo.ServiceInstance;

public class VCenter283 {
	private static String VCENTER283_IP = "https://130.65.132.19/sdk";
	private static String USERNAME = "student@vsphere.local";
	private static String PASSWORD = "12!@qwQW";
	private static ServiceInstance vCenter283;
	//To get the top vCenter to see and manage hosts
	public static ServiceInstance getVCenter283(){
		try{
			vCenter283 = new ServiceInstance(new URL(VCENTER283_IP), USERNAME, PASSWORD, true);
			
		}
		catch(Exception e){
			System.out.println(AvailabilityManager.ERROR + "Cannot get the top vCenter's instance");
		}
		return vCenter283;
	}
	
	public static void killVCenter283Session(){
		vCenter283.getServerConnection().logout();
	}
}
