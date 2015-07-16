package com.intellectualcrafters.plot.object;


public class CmdInstance {
	public final Runnable command;
	public final long timestamp;
	
	public CmdInstance(Runnable command) {
		this.command = command;
		this.timestamp = System.currentTimeMillis();
	}
}
