package com.team01.disasterrecovery;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import com.vmware.vim25.mo.ServiceInstance;

public class ServiceIns {
	
	private static String vCenterUrl = "https://130.65.132.101/sdk";
	private static String userName = "administrator";
	private static String password = "12!@qwQW";
	
	private static ServiceInstance instance = null;
	//Creating a Service Instance object of our vCenter
			
	   /* A private Constructor prevents any other 
	    * class from instantiating.
	    */
	   private ServiceIns(){ }
	   
	   /* Static 'instance' method */
	   public static ServiceInstance getInstance( ) throws RemoteException, MalformedURLException {
		   if(instance==null)
			  instance= new  ServiceInstance(new URL(vCenterUrl), userName, password, true);
	      return instance;
	   }
}
