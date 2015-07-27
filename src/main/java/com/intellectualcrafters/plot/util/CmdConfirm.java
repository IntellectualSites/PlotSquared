package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.CmdInstance;
import com.intellectualcrafters.plot.object.PlotPlayer;

import java.util.HashMap;

public class CmdConfirm {
	private static HashMap<String, CmdInstance> pending = new HashMap<>();
	
	public static CmdInstance getPending(PlotPlayer player) {
	    if (player == null) {
	        return pending.get("__CONSOLE__");
	    }
	    return pending.get(player.getName());
	}
	
	public static void removePending(PlotPlayer player) {
        if (player == null) {
            pending.remove("__CONSOLE__");
        }
        else {
            pending.remove(player.getName());
        }
    }
	
	public static void removePending(String name) {
	    pending.remove(name); 
    }
	
	public static void addPending(PlotPlayer player, String commandStr, Runnable runnable) {
	    MainUtil.sendMessage(player, C.REQUIRES_CONFIRM, commandStr);
	    CmdInstance cmd = new CmdInstance(runnable);
	    String name;
	    if (player == null) {
	        name = "__CONSOLE__";
	    }
	    else {
	        name = player.getName();
	    }
	    pending.put(name, cmd);
	}
}
