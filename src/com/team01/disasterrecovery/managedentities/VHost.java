package com.team01.disasterrecovery.managedentities;

import java.util.ArrayList;
import java.util.List;

import com.team01.disasterrecovery.AvailabilityManager;
import com.team01.disasterrecovery.alarm.AlarmHandler;
import com.vmware.vim25.mo.Alarm;
import com.vmware.vim25.mo.AlarmManager;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class VHost {
	List<VM> vmList;
	private HostSystem vhost;
	private ServiceInstance vCenter;
	private AlarmHandler alarmHandler;

	public VHost(HostSystem vhost) {
		this.vhost = vhost;
		
		//On initialization of this vHost, we require all of the VMs inside it
		updateVMList();
		
		//Instantiate the alarmHandler to create alarms on vHosts and VMs
		alarmHandler = new AlarmHandler();
	}

	// returns List of all VMs inside a vHost
	public List<VM> getVmList() {
		return vmList;
	}

	// Getting List of all VMs inside a vHost
	public void updateVMList() {
		try {

			// Managed Entity array will store list of VMs inside vHost.
			ManagedEntity[] vmArray = new InventoryNavigator(vhost)
					.searchManagedEntities("VirtualMachine");

			// If size of vmArray is zero that means, there are  no VMs found in that host
			if (vmArray == null) {
				System.out.println(AvailabilityManager.INFO
						+ "No VMs found inside this vHost.");
				return;
			}
			
			//If there is even 1 VM in the managedEntities, we will initialize the arraylist
			vmList = new ArrayList<VM>();
			
			//Now for each VM that is found inside the host, add it to the VMList
			for(ManagedEntity oneEntity : vmArray){
				VM thisVM = new VM((VirtualMachine) oneEntity);
				vmList.add(thisVM);
			}
		} 
		catch (Exception e) {
			System.out.println(AvailabilityManager.ERROR
					+ "Error getting VM inside the vHost" 
					+ "Reason: " + e.toString());
		}
	}

	public void createSnapshot() {
		//Let's create snapshots for all the VM
		
		//First we need to check if the VMList has any VM or not
		if((vmList == null) || (vmList.isEmpty())){
			return;
		}
	
		
	}
	
	//To check if this instance of VHost's VMs are reachable or not and later
	//checking if the vHost itself is reachable or not.
	public boolean ifReachable() {
		try {
			 // the list of all VMs in a host
			updateVMList();
			List<VM> vmList = this.getVmList(); // getting the list of updated VMs

			// if VMs are found in a host
			//Check if VMs are intentionally shutdown.
			//Check if VMs are Reachable or not
			//if VMs are not Reachable then try to ping vHost 
			if (vmList != null) {
				for (VM thisVirtualMachine : vmList) {
					if (ifVMTrigger(thisVirtualMachine)) {}
					
					else if (thisVirtualMachine.ifReachable()) {
						System.out.println(thisVirtualMachine.getVirtualMachine().getName()
								+ " is Reachable.");
					
					}
					
					else{
						System.out.println(thisVirtualMachine.getVirtualMachine().getName()
								+ " is not reachable.");
						if(this.ping()) {			
							
						}
					}
				}
			} else {
				//return true if no VMs found inside vHost
				return true;
			}
		} catch (Exception e) {

		}
	}
	
	public boolean ifVMTrigger(VM thisVirtualMachine){
		try {
			return thisVirtualMachine.ifAlarmTriggered(alarmHandler.createAlarm(vCenter));
		} 
		catch (Exception e) {
			System.out.println(AvailabilityManager.ERROR + "Could not check alarms for VM" + "Reason: " + e.toString());
		}
		return false;
	}

	public boolean ping() {
		//code to ping vHost;

		return true;
	}
	
	//Gets the name of the Host [usage: Snapshot]
	public String getVHostName(){
		return vhost.getName();
	}
}
