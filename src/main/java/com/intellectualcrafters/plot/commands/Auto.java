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

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
command = "auto",
permission = "plots.auto",
category = CommandCategory.CLAIMING,
requiredType = RequiredType.NONE,
description = "Claim the nearest plot",
aliases = { "a" },
usage = "/plot auto")
public class Auto extends SubCommand {
    
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
    
    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        
        String world;
        int size_x = 1;
        int size_z = 1;
        String schematic = "";
        if (PS.get().getPlotWorlds().size() == 1) {
            world = PS.get().getPlotWorlds().iterator().next();
        } else {
            world = plr.getLocation().getWorld();
            if (!PS.get().isPlotWorld(world)) {
                MainUtil.sendMessage(plr, C.NOT_IN_PLOT_WORLD);
                return false;
            }
        }
        if (args.length > 0) {
            if (Permissions.hasPermission(plr, "plots.auto.mega")) {
                try {
                    final String[] split = args[0].split(",");
                    size_x = Integer.parseInt(split[0]);
                    size_z = Integer.parseInt(split[1]);
                    if ((size_x < 1) || (size_z < 1)) {
                        MainUtil.sendMessage(plr, "&cError: size<=0");
                    }
                    if ((size_x > 4) || (size_z > 4)) {
                        MainUtil.sendMessage(plr, "&cError: size>4");
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
            MainUtil.sendMessage(plr, C.CANT_CLAIM_MORE_PLOTS_NUM, Settings.MAX_AUTO_SIZE + "");
            return false;
        }
        final int currentPlots = Settings.GLOBAL_LIMIT ? MainUtil.getPlayerPlotCount(plr) : MainUtil.getPlayerPlotCount(world, plr);
        final int diff = currentPlots - MainUtil.getAllowedPlots(plr);
        if ((diff + (size_x * size_z)) > 0) {
            if (diff < 0) {
                MainUtil.sendMessage(plr, C.CANT_CLAIM_MORE_PLOTS_NUM, (-diff) + "");
            } else {
                MainUtil.sendMessage(plr, C.CANT_CLAIM_MORE_PLOTS);
            }
            return false;
        }
        final PlotWorld pWorld = PS.get().getPlotWorld(world);
        if ((EconHandler.manager != null) && pWorld.USE_ECONOMY) {
            double cost = pWorld.PLOT_PRICE;
            cost = (size_x * size_z) * cost;
            if (cost > 0d) {
                if (EconHandler.manager.getMoney(plr) < cost) {
                    sendMessage(plr, C.CANNOT_AFFORD_PLOT, "" + cost);
                    return true;
                }
                EconHandler.manager.withdrawMoney(plr, cost);
                sendMessage(plr, C.REMOVED_BALANCE, cost + "");
            }
        }
        if (!schematic.equals("")) {
            // if (pWorld.SCHEMATIC_CLAIM_SPECIFY) {
            if (!pWorld.SCHEMATICS.contains(schematic.toLowerCase())) {
                sendMessage(plr, C.SCHEMATIC_INVALID, "non-existent: " + schematic);
                return true;
            }
            if (!Permissions.hasPermission(plr, "plots.claim." + schematic) && !Permissions.hasPermission(plr, "plots.admin.command.schematic")) {
                MainUtil.sendMessage(plr, C.NO_SCHEMATIC_PERMISSION, schematic);
                return true;
            }
            // }
        }
        final String worldname = world;
        final PlotWorld plotworld = PS.get().getPlotWorld(worldname);
        if (plotworld.TYPE == 2) {
            final Location loc = plr.getLocation();
            final Plot plot = MainUtil.getPlotAbs(new Location(worldname, loc.getX(), loc.getY(), loc.getZ()));
            if (plot == null) {
                return sendMessage(plr, C.NOT_IN_PLOT);
            }
            final PlotCluster cluster = plot.getCluster();
            // Must be standing in a cluster
            if (cluster == null) {
                MainUtil.sendMessage(plr, C.NOT_IN_CLUSTER);
                return false;
            }
            final PlotId bot = cluster.getP1();
            final PlotId top = cluster.getP2();
            final PlotId origin = new PlotId((bot.x + top.x) / 2, (bot.y + top.y) / 2);
            PlotId id = new PlotId(0, 0);
            final int width = Math.max((top.x - bot.x) + 1, (top.y - bot.y) + 1);
            final int max = width * width;
            //
            for (int i = 0; i <= max; i++) {
                final PlotId currentId = new PlotId(origin.x + id.x, origin.y + id.y);
                final Plot current = MainUtil.getPlotAbs(worldname, currentId);
                if (MainUtil.canClaim(plr, current) && (current.isMerged() == false) && cluster.equals(current.getCluster())) {
                    Claim.claimPlot(plr, current, true, true);
                    return true;
                }
                id = getNextPlot(id, 1);
            }
            // no free plots
            MainUtil.sendMessage(plr, C.NO_FREE_PLOTS);
            return false;
        }
        boolean br = false;
        if ((size_x == 1) && (size_z == 1)) {
            while (!br) {
                final Plot plot = MainUtil.getPlotAbs(worldname, getLastPlot(worldname));
                if (MainUtil.canClaim(plr, plot)) {
                    Claim.claimPlot(plr, plot, true, true);
                    br = true;
                }
                MainUtil.lastPlot.put(worldname, getNextPlot(getLastPlot(worldname), 1));
            }
        } else {
            while (!br) {
                final PlotId start = getNextPlot(getLastPlot(worldname), 1);
                // Checking if the current set of plots is a viable option.
                MainUtil.lastPlot.put(worldname, start);
                if ((PS.get().getPlot(worldname, start) != null) && (PS.get().getPlot(worldname, start).owner != null)) {
                    continue;
                }
                final PlotId end = new PlotId((start.x + size_x) - 1, (start.y + size_z) - 1);
                if (MainUtil.canClaim(plr, worldname, start, end)) {
                    for (int i = start.x; i <= end.x; i++) {
                        for (int j = start.y; j <= end.y; j++) {
                            final Plot plot = MainUtil.getPlotAbs(worldname, new PlotId(i, j));
                            final boolean teleport = ((i == end.x) && (j == end.y));
                            Claim.claimPlot(plr, plot, teleport, true);
                        }
                    }
                    if (!MainUtil.mergePlots(worldname, MainUtil.getPlotSelectionIds(start, end), true, true)) {
                        return false;
                    }
                    br = true;
                }
            }
        }
        MainUtil.lastPlot.put(worldname, new PlotId(0, 0));
        return true;
    }
    
    public PlotId getLastPlot(final String world) {
        if ((MainUtil.lastPlot == null) || !MainUtil.lastPlot.containsKey(world)) {
            MainUtil.lastPlot.put(world, new PlotId(0, 0));
        }
        return MainUtil.lastPlot.get(world);
    }
}
