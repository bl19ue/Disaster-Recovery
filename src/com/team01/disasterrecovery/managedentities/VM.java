package com.team01.disasterrecovery.managedentities;

import com.team01.disasterrecovery.AvailabilityManager;
import com.team01.disasterrecovery.availability.Reachable;
import com.team01.disasterrecovery.snapshot.SnapshotInterface;
import com.team01.disasterrecovery.snapshot.VMSnapshot;
import com.vmware.vim25.AlarmState;
import com.vmware.vim25.mo.Alarm;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VM implements VMInterface{

	private static int MAX_TRIES = 2;
	private VirtualMachine virtualMachine;
	private SnapshotInterface snapshotVM;
	
	public VM(VirtualMachine virtualMachine){
		this.virtualMachine = virtualMachine;
		
		//Let's have the snapshot instance ready for this VM
		snapshotVM = new VMSnapshot(this);
	}
	
	//To start the virtual machine
	@Override
	public boolean powerOn() {
		try{
			//Let's create a new task which will powerOn our virtual machine
			//if null is passed as the parameter, the the VM is started on the current instance of the Host
			Task powerOnTask = virtualMachine.powerOnVM_Task(null);
			System.out.println(AvailabilityManager.INFO + "Powering on: " + virtualMachine.getName());
			
			//Check is powering on the VM was a success or a failure
			if(powerOnTask.waitForTask().equals(Task.SUCCESS)){
				Thread.sleep(4000);
				System.out.println("Power on complete for: " + virtualMachine.getName());
			}
			else{
				System.out.println(AvailabilityManager.ERROR + "Could not powerOn the machine" + "Reason:" + "Unknown");
			}
		}
		catch(Exception powerOnException){
			System.out.println(AvailabilityManager.ERROR + "Could not powerOn the machine" + "Reason:" + powerOnException.toString());
		}
		return false;
	}

	//To turn off the virtual machine
	@Override
	public boolean powerOff() {
		try{
			//Let's create a new task which will powerOff our virtual machine
			Task powerOffTask = virtualMachine.powerOffVM_Task();
			System.out.println(AvailabilityManager.INFO + "Powering off: " + virtualMachine.getName());
			
			//Check is powering on the VM was a success or a failure
			if(powerOffTask.waitForTask().equals(Task.SUCCESS)){
				Thread.sleep(4000);
				System.out.println("Power off complete for: " + virtualMachine.getName());
			}
			else{
				System.out.println(AvailabilityManager.ERROR + "Could not powerOff the machine" + "Reason:" + "Unknown");
			}
		}
		catch(Exception powerOnException){
			System.out.println(AvailabilityManager.ERROR + "Could not powerOff the machine" + "Reason:" + powerOnException.toString());
		}
		return false;
	}

	//Ping the machine to check if the machine is reachable
	@Override
	public boolean ifReachable(){
		int maxTries = 0;
		//Try ping limited number of times to check if the machine is reachable or not
		String ip = virtualMachine.getGuest().getIpAddress();
		if(ip!=null){
			while(!Reachable.ping(ip)){
				if(maxTries++ >= MAX_TRIES){
					return false;
				}
				else{
					try {
						//Wait for 1 second  
						Thread.sleep(1000);
					}
					catch (InterruptedException e) {
						System.out.println(AvailabilityManager.ERROR 
								+ "Could not ping the machine with IP Address = " 
								+ virtualMachine.getGuest().getIpAddress() 
								+ "Reason:" + e.toString());
					}
				}
			}
			return true;
		}else{
			System.out.println("Couldn't ping VM as ip is null.");
			return false;
		}
		
	}

	//Check if the alarm got already triggered for this virtual machine
	@Override
	public boolean ifAlarmTriggered(Alarm alarm) {
		//Getting all the triggered alarms for this virtual machine
		AlarmState[] alarmStates = virtualMachine.getTriggeredAlarmState();
		
		//If no alarms triggered, return false 
		if(alarmStates == null){
			return false;
		}
		 
		for(AlarmState alarmState : alarmStates){
			//Check if the VM has powerOff alarm triggered or not
			if(alarm.getMOR().getVal().equals(alarmState.getAlarm().getVal())){
				return true;
			}
		}
		
		return false;
	}

	@Override
	public VirtualMachine getVirtualMachine() {
		return virtualMachine;
	}

	public void createSnapshot(){
		//Let's first remove past snapshot of this VM
		snapshotVM.purgeSnapshot();
		
		//Now take the snapshot
		snapshotVM.takeSnapshot();
	}
	
	public void useSnapshot(){
		snapshotVM.useSnapshot();
	}
	
	public String getIPAddress(){
		return virtualMachine.getGuest().getIpAddress();
	}
	
	public String getName(){
		return virtualMachine.getName();
	}
}
