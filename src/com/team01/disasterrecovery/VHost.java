package com.team01.disasterrecovery;

import java.util.ArrayList;
import java.util.List;

import com.vmware.vim25.mo.Alarm;
import com.vmware.vim25.mo.AlarmManager;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class VHost {
	List<VM> vmList;
	private HostSystem host;
	private ServiceInstance vCenter;

	public VHost() {
		this.host = host;
		setVmList();
	}

	// returns List of all VMs inside a vHost
	public List<VM> getVmList() {
		return vmList;
	}

	// Getting List of all VMs inside a vHost
	public void setVmList() {
		try {

			// Managed Entity array will store list of VMs inside vHost.
			ManagedEntity[] meArray = new InventoryNavigator(host)
					.searchManagedEntities("VirtualMachine");

			// If size of meArray is zero it shows that no VMs found in that
			// host and
			if (meArray == null) {
				System.out.println(AvailabilityManager.INFO
						+ "No VMs found inside a vHost.");
				return;
			}
			vmList = new ArrayList<VM>();
			//
			for (int i = 0; i < meArray.length; i++) {
				vmList.add(new VM((VirtualMachine) meArray[i]));
			}
		} catch (Exception e) {
			System.out.println(AvailabilityManager.ERROR
					+ "Error setting VM insie ");
		}
	}

	public void createSnapshot() {

	}

	public boolean ifReachable() {
		try {

			// check if vhost is reachable or not
			
			this.setVmList(); // updating the list of all VMs in a host
			List<VM> vmList = this.getVmList(); // getting the list of updated VMs

			// if VMs are found in a host
			//Check if VMs are intentionally shutdown.
			//Check if VMs are Reachable or not
			//if VMs are not Reachable then try to ping vHost 
			if (vmList != null) {
				for (int i = 0; i < vmList.size(); i++) {
					VM vm = vmList.get(i);
					if (vm.ifAlarmTriggered(AlarmHandler.createAlarm(vCenter))) {
						System.out.println(vm.getVirtualMachine().getName()
								+ " was intentionally shutdown.");
						continue;
					}
					if (vm.ifReachable()) {
						System.out.println(vm.getVirtualMachine().getName()
								+ " is Reachable.");
						continue;
					}
					System.out.println(vm.getVirtualMachine().getName()
							+ " is not Reachable.");
					if(this.ping()) {			
					}
				}
			} else {
				//return true if no VMs found inside vHost
				return true;
			}
		} catch (Exception e) {

		}
	}
	

	public boolean ping() {
		//code to ping vHost;

		return true;
	}
}
