package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.commands.SubCommand;

public class CmdInstance {
	public final SubCommand command;
	public final String[] args;
	public final long timestamp;
	
	public CmdInstance(SubCommand command, String[] args) {
		this.command = command;
		this.args = args;
		this.timestamp = System.currentTimeMillis();
	}
}
