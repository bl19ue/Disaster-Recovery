package com.team01.disasterrecovery.snapshot;

import com.team01.disasterrecovery.AvailabilityManager;
import com.team01.disasterrecovery.availability.Reachable;
import com.team01.disasterrecovery.managedentities.VCenter;
import com.team01.disasterrecovery.managedentities.VHost;
import com.team01.disasterrecovery.managedentities.VM;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VHostSnapshot implements SnapshotInterface{

	private VHost vHost;
	private ServiceInstance vCenter;
	private VirtualMachine virtualHostMachine;
	private static String SNAPSHOT_DESC = "Snapshot for VHost";
	public VHostSnapshot(VHost vHost){
		//Gets the host whose snapshot has to be taken
		this.vHost = vHost;
		
		//Gets the vCenter instance which is only one for whole program
		vCenter = VCenter.getVCenter();
		
		//But we cannot directly create a snapshot using the HostSystem or VHost instance
		//We should remember that vHost is also a virtual machine and hence, we need to
		//get the instance of a virtual machine using this.vHost
		try{
			virtualHostMachine = (VirtualMachine) new InventoryNavigator(vCenter.getRootFolder())
														.searchManagedEntity("VirtualMachine", vHost.getVHostName());
		}
		catch(Exception e){
			System.out.println(AvailabilityManager.ERROR 
					+ "Could not get the VM for the host" 
					+ " Reason: " + e.toString());
		}
		
		
	}
	
	@Override
	public boolean takeSnapshot() {
		if(checkReachability()){
			//As the vHost was reachable, we again have to create a task to take the snapshot
			try{
				//The last two parameters are memory, and quiesce [disable something]
				//Both are false because, if we use memory, it will save the memory dump and keep the snapshot in the turn on state
				//And quiesce is false to let the system not create a space for the snapshot on state
				Task vHostSnapshotTask = virtualHostMachine.createSnapshot_Task(virtualHostMachine.getName(), 
						SNAPSHOT_DESC + virtualHostMachine.getName(),
						false, false);
				
				//As the snapshot is being created, we will have to wait for it and ultimately receive the status
				//if it is success we can proceed as required
				
				//waitForTask() will cause the current thread to wait until the task is finished, and when the task
				//is finished it will return the status
				
				//We couldv'e done it using event listeners, because this method will stop the current thread until the complete
				//snapshot creation is in progress
				
				//Event listeners, for example first we could get the result using
				
				/*String status = task.waitForTask();
		        Object taskResult = task.getTaskInfo().getResult();*/
				
				//And then created a listener like, when we have something in the object taskResult, we could emit a message to it 
				//to proceed further
				//But for less complexity for now, let's proceed like this.
				if(vHostSnapshotTask.waitForTask() == Task.SUCCESS){
					//If the task creation was a success
					System.out.println(AvailabilityManager.INFO + "Snapshot for VHost done");
					
					return true;
				}
				else{
					System.out.println(AvailabilityManager.ERROR 
							+ "Snapshot creation for VHost failed" 
							+ " Reason: " + "Did not succeed");
					
				}
			}
			catch(Exception e){
				System.out.println(AvailabilityManager.ERROR 
						+ "Could not create snapshot task" 
						+ " Reason: " + e.toString());
			}
			
		}
		return false;
	}

	@Override
	public boolean useSnapshot() {
		//Let's just create a new instance of a VM, which reflects a new instance on the vSphere
		VM newVirtualMachine = new VM(virtualHostMachine);
		
		//Let's now create a snapshot recovery task, so that this VM is able to recover from its current snapshot
		/*If a snapshot was taken while a virtual machine was powered on, and this operation is invoked after the 
		 * virtual machine was powered off, the operation causes the virtual machine to power on to reach the 
		 * snapshot state. This parameter can be used to specify a choice of host where the virtual machine 
		 * should power on.
		 * If this parameter is not set, and the vBalance feature is configured for automatic load balancing, 
		 * a host is automatically selected. Otherwise, the virtual machine keeps its existing host affiliation.*/
		try{
			Task hostSnapshotRecoveryTask = newVirtualMachine.getVirtualMachine().revertToCurrentSnapshot_Task(null);
			
			//Let's wait for this task to complete and check if its a success or not
			if(hostSnapshotRecoveryTask.waitForTask() == Task.SUCCESS){
				System.out.println(AvailabilityManager.INFO 
						+ "Recovery of host: " 
						+ virtualHostMachine.getName() + " was successful");
				
				//As it was a success to use the snapshot, let's turn on the machine
				newVirtualMachine.powerOn();
				
				return true;
			}
			else{
				System.out.println(AvailabilityManager.ERROR 
						+ "Could not use the snapshot for recovery of host: " 
						+ virtualHostMachine.getName() 
						+ " Reason: " + "Did not succeed");
			}
		}
		catch(Exception e){
			System.out.println(AvailabilityManager.ERROR 
					+ "Could not use the snapshot for recovery of host: " 
					+ virtualHostMachine.getName() 
					+ " Reason: " + e.toString());
		}
		return false;
	}

	//A method to check if the vHost is reachable or not
	private boolean checkReachability(){
		if(Reachable.ping(virtualHostMachine.getGuest().getIpAddress())){
			return true;
		}
		
		System.out.println(AvailabilityManager.ERROR 
				+ "Wasn't even able to ping the host before creating the snapshot" 
				+ " Reason: " + "Unknown");
		
		return false;
	}
	
	//A method to purge past snapshots of this VM
		@Override
		public boolean purgeSnapshot() {
		try{
			//Let's again create a task for removing past snapshot
			Task purgeSnapshotTask = virtualHostMachine.removeAllSnapshots_Task();
			if(purgeSnapshotTask.waitForTask() ==  Task.SUCCESS){
				System.out.println(AvailabilityManager.INFO 
						+ "Purging snapshot successful for " 
						+ virtualHostMachine.getName());
				return true;
			}
		}
		catch(Exception e){
			System.out.println(AvailabilityManager.ERROR 
					+ "Wasn't able to purge the VHost snapshot" 
					+ " Reason: " + "Unknown");
				
		}
		return false;
	}
}
