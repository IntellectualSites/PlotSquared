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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.ExpireManager;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.sk89q.worldedit.regions.Region;

public class DebugExec extends SubCommand {

    private ArrayList<ChunkLoc> chunks = null;
    private World world;

    public DebugExec() {
    	super("debugexec", "plots.admin", "Multi-purpose debug command", "debugexec", "exec", CommandCategory.DEBUG, false);
    }

    @Override
    public boolean execute(final Player player, final String... args) {
    	List<String> allowed_params = Arrays.asList(new String[]{"stop-expire","start-expire", "show-expired", "update-expired", "seen", "trim-check-chunks", "trim-get-chunks"});
        if (args.length > 0) {
        	String arg = args[0].toLowerCase();
        	switch (arg) {
	        	case "stop-expire": {
	        		if (ExpireManager.task != -1) {
	        			Bukkit.getScheduler().cancelTask(ExpireManager.task);
	        		}
	        		else {
	        			return PlayerFunctions.sendMessage(null, "Task already halted");
	        		}
	        		ExpireManager.task = -1;
	        		return PlayerFunctions.sendMessage(null, "Cancelled task.");
	        	}
	        	case "start-expire": {
	        		if (ExpireManager.task == -1) {
	        			ExpireManager.runTask();
	        		}
	        		else {
	        			return PlayerFunctions.sendMessage(null, "Plot expiry task already started");
	        		}
	        		return PlayerFunctions.sendMessage(null, "Started plot expiry task");
	        	}
	        	case "update-expired": {
	        		if (args.length > 1) {
	        			World world = Bukkit.getWorld(args[1]);
	        			if (world == null) {
	        				return PlayerFunctions.sendMessage(null, "Invalid world: "+args[1]);
	        			}
	        			PlayerFunctions.sendMessage(null, "Updating expired plot list");
	        			ExpireManager.updateExpired(args[1]);
	        			return true;
	        		}
	        		return PlayerFunctions.sendMessage(null, "Use /plot debugexec update-expired <world>");
	        	}
	        	case "show-expired": {
	        		if (args.length > 1) {
	        			World world = Bukkit.getWorld(args[1]);
	        			if (world == null || !ExpireManager.expiredPlots.containsKey(args[1])) {
	        				return PlayerFunctions.sendMessage(null, "Invalid world: "+args[1]);
	        			}
	        			PlayerFunctions.sendMessage(null, "Expired plots (" + ExpireManager.expiredPlots.get(args[1]).size() + "):");
	        			for (Entry<Plot, Long> entry : ExpireManager.expiredPlots.get(args[1]).entrySet()) {
	        			    Plot plot = entry.getKey();
	        			    Long stamp = entry.getValue();
	        				PlayerFunctions.sendMessage(null, " - " + plot.world + ";" + plot.id.x + ";" + plot.id.y + ";" + UUIDHandler.getName(plot.owner) +" : " + stamp);
	        			}
	        			return true;
	        		}
	        		return PlayerFunctions.sendMessage(null, "Use /plot debugexec show-expired <world>");
	        	}
	        	case "seen": {
	        	    if (args.length != 2) {
	        	        return PlayerFunctions.sendMessage(null, "Use /plot debugexec seen <player>");
	        	    }
	        	    UUID uuid = UUIDHandler.getUUID(args[1]);
	        	    if (uuid == null) {
	        	        return PlayerFunctions.sendMessage(null, "player not found: " + args[1]);
	        	    }
	        	    OfflinePlayer op = UUIDHandler.uuidWrapper.getOfflinePlayer(uuid);
	        	    if (op == null || !op.hasPlayedBefore()) {
	        	        return PlayerFunctions.sendMessage(null, "player hasn't connected before: " + args[1]);
	        	    }
	        	    Timestamp stamp = new Timestamp(op.getLastPlayed());
	        	    Date date = new Date(stamp.getTime());
	        	    PlayerFunctions.sendMessage(null, "PLAYER: " + args[1]);
	        	    PlayerFunctions.sendMessage(null, "UUID: " + uuid);
	        	    PlayerFunctions.sendMessage(null, "Object: " + date.toGMTString());
	        	    PlayerFunctions.sendMessage(null, "GMT: " + date.toGMTString());
	        	    PlayerFunctions.sendMessage(null, "Local: " + date.toLocaleString());
	        	    return true;
	        	}
	        	case "trim-check": {
	        	    if (args.length != 2) {
	        	        PlayerFunctions.sendMessage(null, "Use /plot debugexec trim-get-chunks <world>");
	        	        PlayerFunctions.sendMessage(null, "&7 - Generates a list of regions to trim");
	        	        return PlayerFunctions.sendMessage(null, "&7 - Run after plot expiry has run");
	        	    }
	        	    final World world = Bukkit.getWorld(args[1]);
	        	    if (world == null || !PlotMain.isPlotWorld(args[1])) {
                        return PlayerFunctions.sendMessage(null, "Invalid world: "+args[1]);
                    }
	        	    final ArrayList<ChunkLoc> empty = new ArrayList<>();
	        	    Trim.getTrimRegions(empty, world, new Runnable() {
                        @Override
                        public void run() {
                            Trim.sendMessage("Processing is complete! Here's how many chunks would be deleted:");
                            Trim.sendMessage(" - MCA #: " + empty.size());
                            Trim.sendMessage(" - CHUNKS: " + (empty.size() * 256) + " (max)");
                            Trim.sendMessage("Exporting log for manual approval...");
                            final File file = new File(PlotMain.getMain().getDataFolder() + File.separator + "trim.txt");
                            PrintWriter writer;
                            try {
                                writer = new PrintWriter(file);
                                String worldname = world.getName();
                                for (ChunkLoc loc : empty) {
                                    writer.println(worldname +"/region/r." + loc.x + "." + loc.z +".mca" );
                                }
                                writer.close();
                                Trim.sendMessage("File saved");
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                Trim.sendMessage("File failed to save! :(");
                            }
                            Trim.sendMessage("How to get the chunk coords from a region file:");
                            Trim.sendMessage(" - Locate the x,z values for the region file (the two numbers which are separated by a dot)");
                            Trim.sendMessage(" - Multiply each number by 32; this gives you the starting position");
                            Trim.sendMessage(" - Add 31 to each number to get the end position");
                        }
                    });
	        	    return true;
	        	}
        	}
        }
    	PlayerFunctions.sendMessage(player, "Possible sub commands: /plot debugexec <" + StringUtils.join(allowed_params, "|") + ">");
        return true;
    }
}
