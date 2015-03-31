package com.team01.disasterrecovery;

public class ThreadManager implements Runnable {

	private VCenter vcenter;
	private String thread;
	private Thread t1;

	// Constructor of ThreadManager Class
	public ThreadManager(VCenter vcenter, String thread) {
		this.vcenter = vcenter;
		this.thread = thread;
	}

	@Override
	public void run() {

		switch (thread) {
		case "createSnapshot":
			try {
				vcenter.createSnapshot();
			}catch(Exception e) {
				e.printStackTrace();
			}
			break;
		case "ping":
			try {
				vcenter.ping();
			}catch(Exception e) {
				e.printStackTrace();
			}
			break;
		default:
			System.out
					.println("Wrong thread name was passed. Choose from snapshot or heartbeat");
			break;
		}
	}

	public void start() {

		System.out.println("\n********Starting " + thread + " ******");
		if (t1 == null) {
			t1 = new Thread(this, thread);
			t1.start();
		}
	}

}
