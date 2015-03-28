package com.team01.disasterrecovery;

import java.util.List;

import com.vmware.vim25.mo.ServiceInstance;

public class VCenter {
	List<VHost> vhostList;
	private ServiceInstance vCenter;
	
	public VCenter(ServiceInstance vCenter){
		this.vCenter = vCenter;
	}
}
