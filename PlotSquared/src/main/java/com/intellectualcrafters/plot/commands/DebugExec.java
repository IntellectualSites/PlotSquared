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
	        	case "trim-get-chunks": {
	        	    if (args.length != 2) {
	        	        PlayerFunctions.sendMessage(null, "Use /plot debugexec trim-get-chunks <world>");
	        	        PlayerFunctions.sendMessage(null, "&7 - Generates a list of regions to trim");
	        	        return PlayerFunctions.sendMessage(null, "&7 - Run after plot expiry has run");
	        	    }
	        	    World world = Bukkit.getWorld(args[1]);
	        	    if (world == null || !PlotMain.isPlotWorld(args[1])) {
                        return PlayerFunctions.sendMessage(null, "Invalid world: "+args[1]);
                    }
	        	    ArrayList<ChunkLoc> chunks0 = Trim.getTrimChunks(world);
	        	    PlayerFunctions.sendMessage(null, "BULK MCR: " + chunks0.size());
	        	    ArrayList<ChunkLoc> chunks = Trim.getTrimPlots(world);
	        	    chunks.addAll(chunks0);
	        	    this.chunks = chunks;
	        	    this.world = world;
	        	    PlayerFunctions.sendMessage(null, "MCR: " + chunks.size());
	        	    PlayerFunctions.sendMessage(null, "CHUNKS: " + chunks.size() * 256);
	        	    PlayerFunctions.sendMessage(null, "Calculating size on disk...");
	        	    PlayerFunctions.sendMessage(null, "SIZE (bytes): " + Trim.calculateSizeOnDisk(world, chunks));
	        	    return true;
	        	}
	        	case "trim-check-chunks": {
	        	    if (this.chunks == null) {
	        	        return PlayerFunctions.sendMessage(null, "Please run the 'trim-get-chunks' command first");
	        	    }
	        	    
	        	    PlayerFunctions.sendMessage(null, "Checking MCR files for existing plots:");
	        	    int count = 0;
	        	    for (ChunkLoc loc : chunks) {
	                    int sx = loc.x << 4;
	                    int sz = loc.z << 4;
	                    loop:
	                    for (int x = sx; x < sx + 16; x++) {
	                        for (int z = sz; z < sz + 16; z++) {
	                            Chunk chunk = world.getChunkAt(x, z);
	                            Plot plot = ChunkManager.hasPlot(world, chunk);
	                            if (plot != null) {
	                                PlayerFunctions.sendMessage(null, " - " + plot);
	                                count++;
	                                break loop;
	                            }
	                        }
	                    }
	                }
	        	    PlayerFunctions.sendMessage(null, "Found " + count + "plots.");
	        	}
        	}
        }
    	PlayerFunctions.sendMessage(player, "Possible sub commands: /plot debugexec <" + StringUtils.join(allowed_params, "|") + ">");
        return true;
    }
}
