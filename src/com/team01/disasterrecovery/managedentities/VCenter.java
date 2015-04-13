package com.team01.disasterrecovery.managedentities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.impl.AvalonLogger;

import com.team01.disasterrecovery.AvailabilityManager;
import com.team01.disasterrecovery.alarm.AlarmHandler;
import com.vmware.vim25.mo.Alarm;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;

public class VCenter {
	//Creating snapshot timeout is 10 minutes;
	private static int snapshotTimeOut = 600;	//600 secs
	private static int reachableTimeOut = 60; 	//60 secs
	private static boolean flag = true;	
	private Alarm offAlarm;
	private ArrayList<VHost> vHostList;
	private static ServiceInstance vCenter;
	public List<VHost> aliveVHostList= new ArrayList<VHost>();
	public List<VHost> deadVHostList = new ArrayList<VHost>();
	public Datacenter dc;
	@SuppressWarnings("unchecked")
	public VCenter(ServiceInstance vCenter) throws Exception{
		this.vCenter = vCenter;
		try{
			//Instantiating the vCenter's alarm
			offAlarm = new AlarmHandler().createAlarm(vCenter);
		}
		catch(Exception e){
			System.out.println(AvailabilityManager.ERROR 
					+ "Could create alarm on the vCenter Server" 
					+ " Reason: " + e.toString());
		}
		
		//Now, as our vCenter consists of all the vHosts in it, we need to 
		//maintain a list of Virtual Hosts in it
		vHostList = new ArrayList<VHost>();
		assignVHostList();
	}

	private void assignVHostList() {
		int count = 0;
		try{
			//Let's use the managed entities in the vCenter
 			ManagedEntity[] managedVHosts = new InventoryNavigator(vCenter.getRootFolder()).searchManagedEntities("HostSystem");
			
			//Now we need to check if even one host exist in the vcenter or not, if it does
			//We need to add it in the vHostList array
			if(managedVHosts.length > 0){
				for(ManagedEntity oneEntity : managedVHosts){
					//Create the instance of the new VHost using the managedEntity we got
					VHost thisVHost = new VHost((HostSystem) oneEntity);
					int flag=0;
					//Add it to the arrayList
					
						
					if(vHostList.isEmpty()){
						vHostList.add(thisVHost);
					}else{
						for(VHost vhost:vHostList){
							String ip= vhost.getIPAddress();
							if(ip.equals(thisVHost.getIPAddress())){
								flag=1;
								break;
							}
						}
							if(flag==0){
								vHostList.add(thisVHost);
								
							}
								
						
					}
					
					count++;
				}
				System.out.println(AvailabilityManager.INFO + count + " VHost(s) found in the vCenter");
			}
			else{
				System.out.println(AvailabilityManager.INFO + "Empty vCenter!");
			}
		}
		catch(Exception e){
			System.out.println(AvailabilityManager.ERROR + "Wasn't able to get the managed entities inside the vCenter" + " Reason: " + e.toString());
		}
	}

	
	//This method will create a snapshot for all the VMs that are present inside a vHost
	// after particular snapshotTimeOut.
	public void createSnapshot() {
		//As the program starts, we never want to stop taking backups, so infinite loop
		while(flag){
			try {
				System.out.println("Creating Snapshot at " + System.currentTimeMillis());
				//For each vHost in the data center, we want to create a snapshot of all the VMs which reside in it.
				for(VHost vHost : vHostList) {
					vHost.createSnapshot();
				}
				
				System.out.println("Will take backup after 10 minutes");
				//Making this thread wait for 10 minutes to take the next snapshot
				Thread.sleep(snapshotTimeOut * 1000);
			}
			catch(Exception e) {
				System.out.println(AvailabilityManager.ERROR + "Error in creating snapshot" + "Reason:" + e.toString());
			}
		}
		
	}

	/*This method will continuously ping all the VMs that are present inside a vHost
	 after particular snapshotTimeOut.
	 */
	
	public void ping() {
		//As the program starts, we never want to stop checking for hear tbeats, so infinite loop
		while(flag){
			try {
				System.out.println("Checking if the Virtual machine is reachable or not");
				//For each vHost in the list we check if the virtual machines inside it are reachable or not
				pingAllVhost();
				for(VHost deadvHost : deadVHostList) {
					//System.out.println("vHost is not reachable. Trying to recover from snapshot.");
					//As the vHost was not reachable, we need to recover this disaster
					//Trying to recover VM
					if(deadvHost.beginHostRecovery()){
						System.out.println(AvailabilityManager.INFO + "vHost recovery successful");
					}
					else{
						System.out.println(AvailabilityManager.ERROR + "Could not recover the vHost " + "Reason: " + "Unknown");
					}
					if(aliveVHostList.size()>0) {
						//Moving deadhost VMs to a aliveHost
						Random rand = new Random();
						int destinationHostIndex = rand. nextInt(aliveVHostList.size());
						VHost destinationHost; 
						if(destinationHostIndex>=0)
							destinationHost = aliveVHostList.get(destinationHostIndex);
						else
							destinationHost = aliveVHostList.get(0);
						//Move VMs to other alive Host											
						deadvHost.registerVMsToDifferentHost(destinationHost);
					}else {
						//add new host and remove dead host
						String deadhostIp = deadvHost.getVHostName();
						System.out.println(deadhostIp);
						List<String> otherVhost = new ArrayList<String>();
						for(String hostname:AvailabilityManager.hostsArray) {
							if(!hostname.equals(deadhostIp))
								otherVhost.add(hostname);
						}
						String vhostName = otherVhost.get(0);
						if(deadvHost.addVHost(vhostName,"root", "12!@qwQW")) {
							ManagedEntity addedVHost = new InventoryNavigator(VCenter.getVCenter().getRootFolder()).searchManagedEntity("HostSystem", vhostName);
							VHost destinationHost = new VHost((HostSystem)addedVHost);
							deadvHost.registerVMsToDifferentHost(destinationHost);
							pingAllVhost();
						}else {
							System.out.println("Unable to add new host.");
						}
						
					}
					
				}
				System.out.println(AvailabilityManager.INFO + "Will wait for 1 minute for next reachability check");
				//Now we must wait for 1 minute for the next heartbeat
				Thread.sleep(reachableTimeOut * 1000);
			}
			catch(Exception e) {
				System.out.println(AvailabilityManager.ERROR + "vHost Reachability check problem " + " Reason: " + e.toString() );
				
			}
		}
	}
	
	public void pingAllVhost() {
		assignVHostList();	
		aliveVHostList.clear();
		deadVHostList.clear();
		for(VHost vHost : this.vHostList) {
			if(vHost.ping()) {
			//System.out.println("Pinging " + vHost.getIPAddress());
				vHost.ifReachable();
				aliveVHostList.add(vHost);
				
			}else {
				deadVHostList.add(vHost);
			}
		}
	
	}
	
	//A Method to get the alarm of the vCenter
	public Alarm getVCenterOffAlarm(){
		return offAlarm;
	}
	
	//A method to get this vCenter instance, [usage: Snapshots]
	public static ServiceInstance getVCenter(){
		return vCenter;
	}	
	
}
