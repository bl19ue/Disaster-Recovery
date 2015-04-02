package com.team01.disasterrecovery.alarm;

import com.team01.disasterrecovery.AvailabilityManager;
import com.vmware.vim25.AlarmSetting;
import com.vmware.vim25.AlarmSpec;
import com.vmware.vim25.StateAlarmExpression;
import com.vmware.vim25.StateAlarmOperator;
import com.vmware.vim25.mo.Alarm;
import com.vmware.vim25.mo.AlarmManager;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;

public class AlarmHandler {
	
	private static String TYPE = "VirtualMachine";
	private static String PATH = "runtime.powerState";
	private static String WARNING = "poweredOff";
	private static String NAME = "Power Off Alarm";
	private static String DESCRIPTION = "Triggers Alarm if VM goes down";
	
	private static ManagedEntity[] dataCenters;
	private static AlarmManager alarmManager;
	private static AlarmSpec alarmSpecs;
	private static StateAlarmExpression stateAlarmExpression;
	private static AlarmSetting alarmSetting;
	
	public Alarm createAlarm(ServiceInstance vCenter) throws Exception{
		//Get all the data centers in our vCenter
		dataCenters = vCenter.getRootFolder().getChildEntity();
		
		//Getting the vCenter's alarm instance
		alarmManager = vCenter.getAlarmManager();
		
		//An alarm expression that uses the running state of either a virtual 
		//machine or a host as the condition that triggers the alarm.
		//We require a stateAlarmExpression to create specifications for the alarm
		assignStateAlarmExpression();

		//Create a new AlarmSetting
		createAlarmSetting();
		
		//Assign the alarmSpecifications using the expression that we just obtained
		assignAlarmSpecification();
		
		//Now we have to check if the alarm already exists in these VMs
		//If it does we do not want to create a duplicate one again every time
		//Easier way is first to remove all the alarms which already exists
		checkAndRemoveAlarms();
		
		//Return the final alarm 
		return getFinalAlarm();
		
	}
	
	//Used to create a new StateAlarmExpression on a VM
	private static void assignStateAlarmExpression(){
		stateAlarmExpression = new StateAlarmExpression();
		
		//The Alarm is supposed to be on a virtual machine, so type = VirtualMachine
		stateAlarmExpression.setType(TYPE);
		
		//Setting the operator for warning, if it is equal to powerState{OFF}
		stateAlarmExpression.setOperator(StateAlarmOperator.isEqual);
		
		//Setting the statePath which is on powerState
		stateAlarmExpression.setStatePath(PATH);
		
		//Setting the warning only on PoweredOff
		stateAlarmExpression.setYellow(WARNING);
	}
	
	//Creates the specification for the alarm
	private void assignAlarmSpecification(){
		//Instantiating an Alarms specification instance
		alarmSpecs = new AlarmSpec();
		
		//No action to take as we will be taking action on our own
		alarmSpecs.setAction(null);
		
		//Setting the expression that we just got
		alarmSpecs.setExpression(stateAlarmExpression);
		
		//Name and Description of all the alarms
		alarmSpecs.setName(NAME);
		alarmSpecs.setDescription(DESCRIPTION);
		
		//Ultimately enable the alarm
		alarmSpecs.setEnabled(true);
		
		//Set the settings that we created for the Alarm in the specifications
		alarmSpecs.setSetting(alarmSetting);
	}

	//Creates a new alarm setting
	private static void createAlarmSetting(){
		//AlarmSetting only has to methods as specified below
		alarmSetting = new AlarmSetting();
		
		//Tolerance range for the metric triggers, measured in one hundredth percentage
		//0 means alarm triggers whenever the metric value is + or - the specified value
		alarmSetting.setToleranceRange(0);
		
		//How often the alarm is triggered, measured in seconds
		//0 means alarm is allowed to be triggered as often as possible
		alarmSetting.setReportingFrequency(0);
	}

	//Checks and removes the alarm if it is similar to what we are making
	private static void checkAndRemoveAlarms() throws Exception{

		//*********************For all the Data Centers we need to obtain the Alarms using the alarm manager
		//for(int num_of_datacenter = 0; num_of_datacenter < dataCenters.length; num_of_datacenter++ ){
			//For all the alarms in a particular data center
			for(Alarm alarm : alarmManager.getAlarm(dataCenters[0])){
				//If it is equal to the name we are giving to create a new Alarm
				if(alarm.getAlarmInfo().getName().equals(NAME)){
					//Remove it
					alarm.removeAlarm();
				}
			}
		//}
	}

	//*********************At last we need to create the alarm we configured for all the data centers
	private static Alarm getFinalAlarm() throws Exception{
		//for(int num_of_datacenter = 0; num_of_datacenter < dataCenters.length; num_of_datacenter++ ){
			Alarm alarm = alarmManager.createAlarm(dataCenters[0], alarmSpecs);
			System.out.println(AvailabilityManager.INFO + "Alarm Created");
			return alarm;
		//}
	}
}
