package com.team01.disasterrecovery;

import java.util.List;

import com.vmware.vim25.mo.ServiceInstance;

public class VCenter {
	
	int snapshotTimeInterval = 300;
	private List<VHost> vHostList;
	private ServiceInstance vCenter;
	
	public VCenter(ServiceInstance vCenter){
		this.vCenter = vCenter;
	}

	/*This method will create a snapshot for all the VMs that are present inside a vHost
	 after particular snapshotTimeInterval.
	 */
	
	public void createSnapshot() {
		try {
			System.out.println("Creating Snapshot");
			for(VHost vHost : vHostList) {
				vHost.createSnapshot();
			}
			System.out.println("");
			Thread.sleep(snapshotTimeInterval * 1000);
		}catch(Exception e) {
			System.out.println(AvailabilityManager.ERROR + "Creating Snapshot: " + e.printStackTrace());
			
		}
		
	}

	/*This method will continously ping all the VMs that are present inside a vHost
	 after particular snapshotTimeInterval.
	 */
	
	public void ping() {
		try {
			System.out.println("Pinging VM");
			for(VHost vHost : vHostList) {
				if(vHost.ifReachable())
					System.out.println("vHost is reachable.");
				else {
					System.out.println("vHost is not reachable. Trying to recover from snapshot.");
				}
			}
			
			
		}catch(Exception e) {
			
		}
		
		
	}
}
