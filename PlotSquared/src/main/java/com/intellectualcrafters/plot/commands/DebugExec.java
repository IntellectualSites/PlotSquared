////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualcrafters.plot.commands;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.util.ExpireManager;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.UUIDHandler;

public class DebugExec extends SubCommand {

    public DebugExec() {
    	super("debugexec", "plots.admin", "Multi-purpose debug command", "debugexec", "exec", CommandCategory.DEBUG, false);
    }

    @Override
    public boolean execute(final Player player, final String... args) {
    	List<String> allowed_params = Arrays.asList(new String[]{"stop-expire","start-expire", "show-expired", "update-expired"});
        if (args.length > 0) {
        	String arg = args[0].toLowerCase();
        	switch (arg) {
	        	case "stop-expire":
	        		if (ExpireManager.task != -1) {
	        			Bukkit.getScheduler().cancelTask(ExpireManager.task);
	        		}
	        		else {
	        			return PlayerFunctions.sendMessage(null, "Task already halted");
	        		}
	        		ExpireManager.task = -1;
	        		return PlayerFunctions.sendMessage(null, "Cancelled task.");
	        	case "start-expire":
	        		if (ExpireManager.task == -1) {
	        			ExpireManager.runTask();
	        		}
	        		else {
	        			return PlayerFunctions.sendMessage(null, "Plot expiry task already started");
	        		}
	        		return PlayerFunctions.sendMessage(null, "Started plot expiry task");
	        	case "update-expired":
	        		if (args.length > 1) {
	        			World world = Bukkit.getWorld(args[1]);
	        			if (world == null) {
	        				return PlayerFunctions.sendMessage(null, "Invalid world: "+args[1]);
	        			}
	        			ExpireManager.updateExpired(args[1]);
	        			return PlayerFunctions.sendMessage(null, "Updating expired plot list");
	        		}
	        		return PlayerFunctions.sendMessage(null, "Use /plot debugexec update-expired <world>");
	        	case "show-expired":
	        		if (args.length > 1) {
	        			World world = Bukkit.getWorld(args[1]);
	        			if (world == null || !ExpireManager.expiredPlots.containsKey(args[1])) {
	        				return PlayerFunctions.sendMessage(null, "Invalid world: "+args[1]);
	        			}
	        			PlayerFunctions.sendMessage(null, "Expired plots (" + ExpireManager.expiredPlots.get(args[1]).size() + "):");
	        			for (Plot plot : ExpireManager.expiredPlots.get(args[1])) {
	        				PlayerFunctions.sendMessage(null, " - " + plot.world + ";" + plot.id.x + ";" + plot.id.y + ";" + UUIDHandler.getName(plot.owner));
	        			}
	        			return true;
	        		}
	        		return PlayerFunctions.sendMessage(null, "Use /plot debugexec show-expired <world>");
        	}
        }
    	PlayerFunctions.sendMessage(player, "Possible sub commands: /plot debugexec <" + StringUtils.join(allowed_params, "|") + ">");
        return true;
    }
}
