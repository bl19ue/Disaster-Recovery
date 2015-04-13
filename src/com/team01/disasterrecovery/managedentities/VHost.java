package com.team01.disasterrecovery.managedentities;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.team01.disasterrecovery.AvailabilityManager;
import com.team01.disasterrecovery.alarm.AlarmHandler;
import com.team01.disasterrecovery.availability.Reachable;
import com.team01.disasterrecovery.snapshot.SnapshotInterface;
import com.team01.disasterrecovery.snapshot.VHostSnapshot;
import com.vmware.vim25.ComputeResourceConfigSpec;
import com.vmware.vim25.HostConnectSpec;
import com.vmware.vim25.mo.Alarm;
import com.vmware.vim25.mo.AlarmManager;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VHost {
	private List<VM> vmList;
	private static int RECONNECT_ATTEMPTS = 3;
	private HostSystem vhost;
	//private ServiceInstance vCenter;
	private AlarmHandler alarmHandler;
	private SnapshotInterface snapshotVHost;
	private boolean addNewHostFlag = true;

	public VHost(HostSystem vhost) {
		this.vhost = vhost;

		// Instantiate the alarmHandler to create alarms on vHosts and VMs
		alarmHandler = new AlarmHandler();

		// Instantiate the Snapshot system for this vHost
		snapshotVHost = new VHostSnapshot(this);

		// On initialization of this vHost, we require all of the VMs inside it
		updateVMList();
	}

	public HostSystem getHost() {
		return vhost;
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

			// If size of vmArray is zero that means, there are no VMs found in
			// that host
			if (vmArray == null) {
				System.out.println(AvailabilityManager.INFO
						+ "No VMs found inside this vHost.");
				return;
			}

			// If there is even 1 VM in the managedEntities, we will initialize
			// the arraylist
			vmList = new ArrayList<VM>();

			// Now for each VM that is found inside the host, add it to the
			// VMList
			for (ManagedEntity oneEntity : vmArray) {
				VM thisVM = new VM((VirtualMachine) oneEntity);
				vmList.add(thisVM);
			}
		} catch (Exception e) {
			System.out.println(AvailabilityManager.ERROR
					+ "Error getting VM inside the vHost" + "Reason: "
					+ e.toString());
		}
	}

	public void createSnapshot() {
		// Let's create snapshots for all the VM in this vHost and for itself
		// too
		snapshotVHost.purgeSnapshot();
		snapshotVHost.takeSnapshot();

		// First we need to check if the VMList has any VM or not
		if ((vmList != null) && (!vmList.isEmpty())) {
			// If it consist any VM, we first need to create the snapshot of the
			// Vhost
			// First we should remove the last snapshot to save the space

			// Now let's take all the VM's backup
			for (VM virtualMachine : vmList) {
				virtualMachine.createSnapshot();
			}
		}

	}

	// To check if this instance of VHost's VMs are reachable or not and later
	// checking if the vHost itself is reachable or not.
	public boolean ifReachable() {
		try {
			System.out.println(AvailabilityManager.INFO
					+ "VHost is reachable through its IP:"
					+ this.getIPAddress());
			addNewHostFlag = false;
			updateVMList();
			List<VM> vmList = this.getVmList(); // getting the list of updated
												// VMs
			if (vmList.size() > 0) { // if VMs are found in a host
				for (VM thisVirtualMachine : vmList) {
					if (!checkVMAvailability(thisVirtualMachine)) {
						thisVirtualMachine.useSnapshot(); // Trying to recover
															// VM from its
															// snapshot
						for (int i = 0; i < 5; i++) {
							
							if (thisVirtualMachine.getIPAddress() != null) {
								break;
							}
							Thread.sleep(1 * 1000);
						
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println(AvailabilityManager.ERROR
					+ "Error checking VHost and VMs availability " + "Reason: "
					+ e.toString());
		}
		return true;
	}

	// this method checks if the alarm was triggered or not on any VM
	public boolean ifVMTrigger(VM thisVirtualMachine) {
		try {
			return thisVirtualMachine.ifAlarmTriggered(alarmHandler
					.createAlarm(VCenter.getVCenter()));
		} catch (Exception e) {
			System.out.println(AvailabilityManager.ERROR
					+ "Could not check alarms for VM" + "Reason: "
					+ e.toString());
		}
		return false;
	}

	@SuppressWarnings("unused")
	public boolean ping() {
		// this method uses the reachable class to ping the vHost
		String ip = this.getVHostName();
		// ip = null;
		if (ip != null) {
			return Reachable.ping(ip);
		} else {
			System.out.println("Couldn't ping VHost as ip is null.");
			return false;
		}
	}

	public boolean checkVMAvailability(VM thisVirtualMachine) {

		if (ifVMTrigger(thisVirtualMachine)) { // if poweroff alarm is triggered
			System.out.println(AvailabilityManager.INFO
					+ "Turned off on purpose");
			return true;
		} else if (thisVirtualMachine.ifReachable()) { // If VM is reachable
			System.out.println(thisVirtualMachine.getVirtualMachine().getName()
					+ " is Reachable.");
			return true;
		} else { // If VM is not reachable
			System.out.println(thisVirtualMachine.getVirtualMachine().getName()
					+ " is not reachable.");
			return false;
		}
	}

	public String getIPAddress() {
		String ip = this.getHost().getConfig().getNetwork().getVnic()[0]
				.getSpec().getIp().getIpAddress();
		return ip;
	}

	// Gets the name of the Host [usage: Snapshot]
	public String getVHostName() {
		String name = this.getHost().getName();
		return name;
	}

	// Recovers the VHost
	public boolean beginHostRecovery() {
		//boolean recovered = snapshotVHost.useSnapshot();
		if (true) {
			System.out.println(AvailabilityManager.INFO
					+ "Recovery of Host completed");

			try {
				// Now as the host has been completely recovered
				// We need to try reconnection to it for limited number of
				// attempts
				for (int i = 0; i < RECONNECT_ATTEMPTS; i++) {
					// Let's create a task again for reconnection, as the API
					// provides it
					Task reconnectTask = vhost.reconnectHost_Task(null);
					System.out.println(AvailabilityManager.INFO
							+ "Host Reconnection started for: "
							+ this.getVHostName());

					// We should wait atleast for 1 minute
					Thread.sleep(60 * 1000);

					// Now let's check if it was a success or not
					if (reconnectTask.waitForTask() == Task.SUCCESS) {
						System.out.println(AvailabilityManager.INFO
								+ "Host reconnection accomplished for: "
								+ this.getVHostName());
						snapshotVHost.useSnapshot();
						// Now as all the VM's were down, let us start them
						for (VM virtualMachine : vmList) {
							virtualMachine.powerOn();
							// Let's wait for the VM to start
							while (true) {
								if (virtualMachine.getIPAddress() != null) {
									break;
								}
							}
						}
						return true;
					} else {
						System.out.println(AvailabilityManager.ERROR
								+ "Error in reconnecting this host "
								+ "Reason: " + "Unknown");
					}
				}
			} catch (Exception e) {
				System.out.println(AvailabilityManager.ERROR
						+ "Error in recovering this host " + "Reason: "
						+ e.toString());
			}
		}
		return false;
	}

	public void registerVMsToDifferentHost(VHost destinationHost)
			throws Exception {
		System.out.println(AvailabilityManager.INFO + "Moving VMs of VHost "
				+ this.getIPAddress() + " to :"
				+ destinationHost.getIPAddress());
		this.updateVMList();
		List<VM> vmList = this.getVmList(); // getting the list of updated VMs
		List<String> vmNameList = new ArrayList<String>();
		for(VM vm:vmList) {
			String vmname = vm.getName();
			vmNameList.add(vmname);
			System.out.println(vmname  + " added to vmNameList");
		}
		

		Folder rootFolder = VCenter.getVCenter().getRootFolder();
		Datacenter dc = (Datacenter) new InventoryNavigator(rootFolder)
				.searchManagedEntity("Datacenter", "T01-DC");
		ComputeResource cr = (ComputeResource) destinationHost.getHost()
				.getParent();
		ResourcePool rp = cr.getResourcePool();
		
		// disconnect vHost from vCenter(T01-DC)
		Task disconnectHostTask = (this).getHost().disconnectHost();

		// removing vHost from vCenter(T01-DC)
		System.out.println("Removing dead vHost.");
		Task destroyHostTask = (this).getHost().getParent().destroy_Task(); //remove the Vhost
		
		if (destroyHostTask.waitForTask() == Task.SUCCESS) {
			System.out.println("Host removed from vCenter");
			if (vmList.size() > 0) { // if VMs are found in a host
				int index = 0;
				for (VM thisVirtualMachine : vmList) {
					
					String vmName = vmNameList.get(index);
					System.out.println("VM in the dead vhost"+vmName);
					String vmxPath = "[nfs3team01]" + vmName + "/" + vmName	+ ".vmx";

					//thisVirtualMachine.getVirtualMachine().unregisterVM(); // disconnecting VHost from vCenter
					Task registerVM = dc.getVmFolder().registerVM_Task(vmxPath,
							vmName, false, rp, destinationHost.getHost());
					if (registerVM.waitForTask() == Task.SUCCESS) {
						System.out.println(AvailabilityManager.INFO + "VM: "
								+ thisVirtualMachine.getName() + " moved to "
								+ destinationHost.getIPAddress());
						thisVirtualMachine.powerOn();

					}
					index++;
				}
			}
		}
	}
	
	public boolean addVHost(String hostName, String hostUserName, String hostPassword) {
			try {
				System.out.println("Adding Standalone host to DC : HostName - " + hostName);
				HostConnectSpec newHost = new HostConnectSpec();
				
				newHost.setHostName(hostName);
				newHost.setUserName(hostUserName);
				newHost.setPassword(hostPassword);
				//newHost.setSslThumbprint("9A:21:AE:81:B3:59:20:0E:75:D8:45:0F:76:BB:30:38:E9:6E:E2:39");

				String sslThumbprint = AvailabilityManager.getHostSsl(hostName);
				if (sslThumbprint != null) {
					newHost.setSslThumbprint(sslThumbprint);
				} else {
					System.out.println(AvailabilityManager.ERROR + "Unable to get ssl information for new Host: " + hostName);
					return false;
				}

				System.out.println();						
				Folder rootFolder = VCenter.getVCenter().getRootFolder();
				Datacenter dc = (Datacenter) new InventoryNavigator(rootFolder)
						.searchManagedEntity("Datacenter", "T01-DC");
				
				Task addHostTask = dc.getHostFolder().addStandaloneHost_Task(
						newHost, new ComputeResourceConfigSpec(), true);

				if (addHostTask.waitForTask() == Task.SUCCESS) {
					System.out.println("Standalone Host (HostName) - " + hostName + " added to DC");
					
					return true;
				} else {
					System.out.println("Unable to add standalone Host -" + hostName);
					
				}

			} catch (Exception e) {
				System.out.println(AvailabilityManager.ERROR + "Getting error while trying to add a Host - " + hostName +  ". " + e.toString());
			}
		
		return false;
	}
}
