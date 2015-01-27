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

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.ClusterManager;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.PlotHelper;

public class Auto extends SubCommand {
    public Auto() {
        super("auto", "plots.auto", "Claim the nearest plot", "auto", "a", CommandCategory.CLAIMING, true);
    }

    public static PlotId getNextPlot(final PlotId id, final int step) {
        final int absX = Math.abs(id.x);
        final int absY = Math.abs(id.y);
        if (absX > absY) {
            if (id.x > 0) {
                return new PlotId(id.x, id.y + 1);
            } else {
                return new PlotId(id.x, id.y - 1);
            }
        } else if (absY > absX) {
            if (id.y > 0) {
                return new PlotId(id.x - 1, id.y);
            } else {
                return new PlotId(id.x + 1, id.y);
            }
        } else {
            if (id.x.equals(id.y) && (id.x > 0)) {
                return new PlotId(id.x, id.y + step);
            }
            if (id.x == absX) {
                return new PlotId(id.x, id.y + 1);
            }
            if (id.y == absY) {
                return new PlotId(id.x, id.y - 1);
            }
            return new PlotId(id.x + 1, id.y);
        }
    }

    // TODO auto claim a mega plot with schematic
    @Override
    public boolean execute(final Player plr, final String... args) {
        World world;
        int size_x = 1;
        int size_z = 1;
        String schematic = "";
        if (PlotMain.getPlotWorlds().length == 1) {
            world = Bukkit.getWorld(PlotMain.getPlotWorlds()[0]);
        } else {
            if (PlotMain.isPlotWorld(plr.getWorld())) {
                world = plr.getWorld();
            } else {
                PlayerFunctions.sendMessage(plr, C.NOT_IN_PLOT_WORLD);
                return false;
            }
        }
        if (args.length > 0) {
            if (PlotMain.hasPermission(plr, "plots.auto.mega")) {
                try {
                    final String[] split = args[0].split(",");
                    size_x = Integer.parseInt(split[0]);
                    size_z = Integer.parseInt(split[1]);
                    if ((size_x < 1) || (size_z < 1)) {
                        PlayerFunctions.sendMessage(plr, "&cError: size<=0");
                    }
                    if ((size_x > 4) || (size_z > 4)) {
                        PlayerFunctions.sendMessage(plr, "&cError: size>4");
                    }
                    if (args.length > 1) {
                        schematic = args[1];
                    }
                } catch (final Exception e) {
                    size_x = 1;
                    size_z = 1;
                    schematic = args[0];
                    // PlayerFunctions.sendMessage(plr,
                    // "&cError: Invalid size (X,Y)");
                    // return false;
                }
            } else {
                schematic = args[0];
                // PlayerFunctions.sendMessage(plr, C.NO_PERMISSION);
                // return false;
            }
        }

        if ((size_x * size_z) > Settings.MAX_AUTO_SIZE) {
            PlayerFunctions.sendMessage(plr, C.CANT_CLAIM_MORE_PLOTS_NUM, Settings.MAX_AUTO_SIZE + "");
            return false;
        }
        final int diff = PlayerFunctions.getPlayerPlotCount(world, plr) - PlayerFunctions.getAllowedPlots(plr);
        if ((diff + (size_x * size_z)) > 0) {
            if (diff < 0) {
                PlayerFunctions.sendMessage(plr, C.CANT_CLAIM_MORE_PLOTS_NUM, (-diff) + "");
            } else {
                PlayerFunctions.sendMessage(plr, C.CANT_CLAIM_MORE_PLOTS);
            }
            return false;
        }
        final PlotWorld pWorld = PlotMain.getWorldSettings(world);
        if (PlotMain.useEconomy && pWorld.USE_ECONOMY) {
            double cost = pWorld.PLOT_PRICE;
            cost = (size_x * size_z) * cost;
            if (cost > 0d) {
                final Economy economy = PlotMain.economy;
                if (economy.getBalance(plr) < cost) {
                    sendMessage(plr, C.CANNOT_AFFORD_PLOT, "" + cost);
                    return true;
                }
                economy.withdrawPlayer(plr, cost);
                sendMessage(plr, C.REMOVED_BALANCE, cost + "");
            }
        }
        if (!schematic.equals("")) {
            // if (pWorld.SCHEMATIC_CLAIM_SPECIFY) {
            if (!pWorld.SCHEMATICS.contains(schematic.toLowerCase())) {
                sendMessage(plr, C.SCHEMATIC_INVALID, "non-existent: " + schematic);
                return true;
            }
            if (!PlotMain.hasPermission(plr, "plots.claim." + schematic) && !plr.hasPermission("plots.admin.command.schematic")) {
                PlayerFunctions.sendMessage(plr, C.NO_SCHEMATIC_PERMISSION, schematic);
                return true;
            }
            // }
        }
        
        PlotWorld plotworld = PlotMain.getWorldSettings(world);
        if (plotworld.REQUIRE_CLUSTER) {
        	Location loc = plr.getLocation();
        	Plot plot = PlotHelper.getCurrentPlot(loc);
        	if (plot == null) {
        		return sendMessage(plr, C.NOT_IN_PLOT);
        	}
        	PlotCluster cluster = ClusterManager.getCluster(loc);
        }
        
        boolean br = false;
        String worldname = world.getName();
        if ((size_x == 1) && (size_z == 1)) {
            while (!br) {
                final Plot plot = PlotHelper.getPlot(world, getLastPlot(worldname));
                if ((plot.owner == null)) {
                    Claim.claimPlot(plr, plot, true, true);
                    br = true;
                }
                PlotHelper.lastPlot.put(worldname, getNextPlot(getLastPlot(worldname), 1));
            }
        } else {
            boolean lastPlot = true;
            while (!br) {
                final PlotId start = getNextPlot(getLastPlot(worldname), 1);
                // Checking if the current set of plots is a viable option.
                PlotHelper.lastPlot.put(worldname, start);
                if (lastPlot) {
                }
                if ((PlotMain.getPlots(world).get(start) != null) && (PlotMain.getPlots(world).get(start).owner != null)) {
                    continue;
                } else {
                    lastPlot = false;
                }
                final PlotId end = new PlotId((start.x + size_x) - 1, (start.y + size_z) - 1);
                if (isUnowned(world, start, end)) {
                    for (int i = start.x; i <= end.x; i++) {
                        for (int j = start.y; j <= end.y; j++) {
                            final Plot plot = PlotHelper.getPlot(world, new PlotId(i, j));
                            final boolean teleport = ((i == end.x) && (j == end.y));
                            Claim.claimPlot(plr, plot, teleport, true);
                        }
                    }
                    if (!PlotHelper.mergePlots(plr, world, PlayerFunctions.getPlotSelectionIds(start, end))) {
                        return false;
                    }
                    br = true;
                }
            }
        }
        PlotHelper.lastPlot.put(worldname, new PlotId(0, 0));
        return true;
    }

    public PlotId getLastPlot(String world) {
    	if (PlotHelper.lastPlot == null || !PlotHelper.lastPlot.containsKey(world)) {
    		PlotHelper.lastPlot.put(world, new PlotId(0,0));
    	}
    	return PlotHelper.lastPlot.get(world);
    }
    
    public boolean isUnowned(final World world, final PlotId pos1, final PlotId pos2) {
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                final PlotId id = new PlotId(x, y);
                if (PlotMain.getPlots(world).get(id) != null) {
                    if (PlotMain.getPlots(world).get(id).owner != null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
