package com.team01.disasterrecovery.managedentities;

import java.util.List;

import com.oracle.webservices.internal.literal.ArrayList;
import com.team01.disasterrecovery.AvailabilityManager;
import com.team01.disasterrecovery.alarm.AlarmHandler;
import com.vmware.vim25.mo.Alarm;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;

public class VCenter {
	//Creating snapshot timeout is 10 minutes;
	private static int snapshotTimeOut = 600;
	private static int reachableTimeOut = 60;
	private static boolean flag = true;
	
	private Alarm offAlarm;
	private List<VHost> vHostList;
	private static ServiceInstance vCenter;
	
	@SuppressWarnings("unchecked")
	public VCenter(ServiceInstance vCenter){
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
		vHostList = (List<VHost>) new ArrayList();
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
					
					//Add it to the arrayList
					vHostList.add(thisVHost);
					
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
				for(VHost vHost : vHostList) {
					if(vHost.ifReachable()){
						System.out.println("vHost is reachable.");
					}
					else {
						System.out.println("vHost is not reachable. Trying to recover from snapshot.");
						//As the vHost was not reachable, we need to recover this disaster
						if(vHost.beginHostRecovery()){
							System.out.println(AvailabilityManager.INFO + "vHost recovery successful");
						}
						else{
							System.out.println(AvailabilityManager.ERROR + "Could not recover the vHost " + "Reason: " + "Unknown");
						}
					}
				}
				
				System.out.println(AvailabilityManager.INFO + "Will wait for 1 minute for next reachability check");
				//Now we must wait for 1 minute for the next heartbeat
				Thread.sleep(reachableTimeOut * 1000);
			}
			catch(Exception e) {
				System.out.println(AvailabilityManager.ERROR + "vHost Reachability check problem " + "Reason: " + e.toString() );
				
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
