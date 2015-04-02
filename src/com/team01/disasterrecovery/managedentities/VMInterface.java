package com.team01.disasterrecovery.managedentities;

import com.vmware.vim25.mo.Alarm;
import com.vmware.vim25.mo.VirtualMachine;

public interface VMInterface {
	public boolean powerOn();
	public boolean powerOff();
	public boolean ifReachable();
	public boolean ifAlarmTriggered(Alarm alarm);
	public VirtualMachine getVirtualMachine();
}
