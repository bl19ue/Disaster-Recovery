package com.team01.disasterrecovery;

public class ThreadManager implements Runnable  {

	
	private VCenter vcenter;
	private String thread;
	private Thread t1;
	
	public ThreadManager(VCenter vcenter, String thread) {
		super();
		this.vcenter = vcenter;
		this.thread = thread;
	}


	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		switch(thread)
		{
		
		case "createSnapshot" :
			    vcenter.createSnapshot();
			    break;
		case "ping" :
			   vcenter.heartbeat();
			   break;
		
		default:
			System.out.println("Wrong thread name was passed. Choose from snapshot or heartbeat");
			break;
		}	
		}
	
public void start()
{

    System.out.println("\n********Starting " +  thread + " ******");
    if (t1 == null)
    {
       t1 = new Thread (this, thread);
       t1.start ();
    }
}

}
