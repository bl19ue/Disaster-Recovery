package com.team01.disasterrecovery;

import com.vmware.vim25.AlarmState;
import com.vmware.vim25.mo.Alarm;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VM implements VMInterface{

	private VirtualMachine virtualMachine;
	
	public VM(VirtualMachine virtualMachine){
		this.virtualMachine = virtualMachine;
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
		while(!Reachable.ping(virtualMachine.getGuest().getIpAddress())){
			if(maxTries++ >= 3){
				return false;
			}
			else{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
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

}
