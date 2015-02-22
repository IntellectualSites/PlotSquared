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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;

public class Condense extends SubCommand {
    public static boolean TASK = false;
    
    public Condense() {
        super("condense", "plots.admin", "Condense a plotworld", "condense", "", CommandCategory.DEBUG, false);
    }
    
    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (plr != null) {
            MainUtil.sendMessage(plr, (C.NOT_CONSOLE));
            return false;
        }
        if ((args.length != 2) && (args.length != 3)) {
            MainUtil.sendMessage(plr, "/plot condense <world> <start|stop|info> [radius]");
            return false;
        }
        final String worldname = args[0];
        if (!BlockManager.manager.isWorld(worldname) || !PlotSquared.isPlotWorld(worldname)) {
            MainUtil.sendMessage(plr, "INVALID WORLD");
            return false;
        }
        switch (args[1].toLowerCase()) {
            case "start": {
                if (args.length == 2) {
                    MainUtil.sendMessage(plr, "/plot condense " + worldname + " start <radius>");
                    return false;
                }
                if (TASK) {
                    MainUtil.sendMessage(plr, "TASK ALREADY STARTED");
                    return false;
                }
                if (args.length == 2) {
                    MainUtil.sendMessage(plr, "/plot condense " + worldname + " start <radius>");
                    return false;
                }
                if (!StringUtils.isNumeric(args[2])) {
                    MainUtil.sendMessage(plr, "INVALID RADIUS");
                    return false;
                }
                final int radius = Integer.parseInt(args[2]);
                final Collection<Plot> plots = PlotSquared.getPlots(worldname).values();
                final int size = plots.size();
                final int minimum_radius = (int) Math.ceil((Math.sqrt(size) / 2) + 1);
                if (radius < minimum_radius) {
                    MainUtil.sendMessage(plr, "RADIUS TOO SMALL");
                    return false;
                }
                final List<PlotId> to_move = new ArrayList<>(getPlots(plots, radius));
                final List<PlotId> free = new ArrayList<>();
                PlotId start = new PlotId(0, 0);
                while ((start.x <= minimum_radius) && (start.y <= minimum_radius)) {
                    final Plot plot = MainUtil.getPlot(worldname, start);
                    if (!plot.hasOwner()) {
                        free.add(plot.id);
                    }
                    start = Auto.getNextPlot(start, 1);
                }
                MainUtil.move(worldname, to_move.get(0), free.get(0), new Runnable() {
                    @Override
                    public void run() {
                        if (!TASK) {
                            sendMessage("CONDENSE TASK CANCELLED");
                            return;
                        }
                        to_move.remove(0);
                        free.remove(0);
                        int index = 0;
                        for (final PlotId id : to_move) {
                            final Plot plot = MainUtil.getPlot(worldname, id);
                            if (plot.hasOwner()) {
                                break;
                            }
                            index++;
                        }
                        for (int i = 0; i < index; i++) {
                            to_move.remove(0);
                        }
                        index = 0;
                        for (final PlotId id : free) {
                            final Plot plot = MainUtil.getPlot(worldname, id);
                            if (!plot.hasOwner()) {
                                break;
                            }
                            index++;
                        }
                        for (int i = 0; i < index; i++) {
                            free.remove(0);
                        }
                        if (to_move.size() == 0) {
                            sendMessage("TASK COMPLETE. PLEASE VERIFY THAT NO NEW PLOTS HAVE BEEN CLAIMED DURING TASK.");
                            TASK = false;
                            return;
                        }
                        if (free.size() == 0) {
                            sendMessage("TASK FAILED. NO FREE PLOTS FOUND!");
                            TASK = false;
                            return;
                        }
                        sendMessage("MOVING " + to_move.get(0) + " to " + free.get(0));
                        MainUtil.move(worldname, to_move.get(0), free.get(0), this);
                    }
                });
                TASK = true;
                MainUtil.sendMessage(plr, "TASK STARTED...");
                return true;
            }
            case "stop": {
                if (!TASK) {
                    MainUtil.sendMessage(plr, "TASK ALREADY STOPPED");
                    return false;
                }
                TASK = false;
                MainUtil.sendMessage(plr, "TASK STOPPED");
                return true;
            }
            case "info": {
                if (args.length == 2) {
                    MainUtil.sendMessage(plr, "/plot condense " + worldname + " info <radius>");
                    return false;
                }
                if (!StringUtils.isNumeric(args[2])) {
                    MainUtil.sendMessage(plr, "INVALID RADIUS");
                    return false;
                }
                final int radius = Integer.parseInt(args[2]);
                final Collection<Plot> plots = PlotSquared.getPlots(worldname).values();
                final int size = plots.size();
                final int minimum_radius = (int) Math.ceil((Math.sqrt(size) / 2) + 1);
                if (radius < minimum_radius) {
                    MainUtil.sendMessage(plr, "RADIUS TOO SMALL");
                    return false;
                }
                final int max_move = getPlots(plots, minimum_radius).size();
                final int user_move = getPlots(plots, radius).size();
                MainUtil.sendMessage(plr, "=== DEFAULT EVAL ===");
                MainUtil.sendMessage(plr, "MINIMUM RADIUS: " + minimum_radius);
                MainUtil.sendMessage(plr, "MAXIMUM MOVES: " + max_move);
                MainUtil.sendMessage(plr, "=== INPUT EVAL ===");
                MainUtil.sendMessage(plr, "INPUT RADIUS: " + radius);
                MainUtil.sendMessage(plr, "ESTIMATED MOVES: " + user_move);
                MainUtil.sendMessage(plr, "ESTIMATED TIME: " + "No idea, times will drastically change based on the system performance and load");
                MainUtil.sendMessage(plr, "&e - Radius is measured in plot width");
                return true;
            }
        }
        MainUtil.sendMessage(plr, "/plot condense " + worldname + " <start|stop|info> [radius]");
        return false;
    }
    
    public Set<PlotId> getPlots(final Collection<Plot> plots, final int radius) {
        final HashSet<PlotId> outside = new HashSet<>();
        for (final Plot plot : plots) {
            if ((plot.id.x > radius) || (plot.id.x < -radius) || (plot.id.y > radius) || (plot.id.y < -radius)) {
                outside.add(plot.id);
            }
        }
        return outside;
    }
    
    public static void sendMessage(final String message) {
        PlotSquared.log("&3PlotSquared -> Plot condense&8: &7" + message);
    }
}
