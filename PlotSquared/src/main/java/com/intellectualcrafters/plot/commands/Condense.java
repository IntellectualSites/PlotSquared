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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.generator.HybridPlotManager;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.ExpireManager;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.PlotHelper;
import com.intellectualcrafters.plot.util.TaskManager;

public class Condense extends SubCommand {

    public static boolean TASK = false;
    private static int TASK_ID = 0;
    
    public Condense() {
        super("condense", "plots.admin", "Condense a plotworld", "condense", "", CommandCategory.DEBUG, false);
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        if (plr != null) {
            PlayerFunctions.sendMessage(plr, (C.NOT_CONSOLE));
            return false;
        }
        if (args.length != 2 && args.length != 3) {
            PlayerFunctions.sendMessage(plr, "/plot condense <world> <start|stop|info> [radius]");
            return false;
        }
        String worldname = args[0];
        World world = Bukkit.getWorld(worldname);
        if (world == null || !PlotMain.isPlotWorld(worldname)) {
            PlayerFunctions.sendMessage(plr, "INVALID WORLD");
            return false;
        }
        switch (args[1].toLowerCase()) {
            case "start": {
                if (args.length == 2) {
                    PlayerFunctions.sendMessage(plr, "/plot condense " + worldname + " start <radius>");
                    return false;
                }
                PlayerFunctions.sendMessage(plr, "NOT IMPLEMENTED");
                return true;
            }
            case "stop": {
                PlayerFunctions.sendMessage(plr, "NOT IMPLEMENTED");
                return true;
            }
            case "info": {
                if (args.length == 2) {
                    PlayerFunctions.sendMessage(plr, "/plot condense " + worldname + " info <radius>");
                    return false;
                }
                if (!StringUtils.isNumeric(args[2])) {
                    PlayerFunctions.sendMessage(plr, "INVALID RADIUS");
                    return false;
                }
                int radius = Integer.parseInt(args[2]);
                Collection<Plot> plots = PlotMain.getPlots(worldname).values();
                int size = plots.size();
                int minimum_radius = (int) Math.ceil((Math.sqrt(size)/2) + 1);
                int max_move = getPlots(plots, minimum_radius).size();
                int user_move = getPlots(plots, radius).size();
                PlayerFunctions.sendMessage(plr, "=== DEFAULT EVAL ===");
                PlayerFunctions.sendMessage(plr, "MINIMUM RADIUS: " + minimum_radius);
                PlayerFunctions.sendMessage(plr, "MAXIMUM MOVES: " + max_move);
                PlayerFunctions.sendMessage(plr, "=== INPUT EVAL ===");
                PlayerFunctions.sendMessage(plr, "INPUT RADIUS: " + radius);
                PlayerFunctions.sendMessage(plr, "ESTIMATED MOVES: " + user_move);
                PlayerFunctions.sendMessage(plr, "&e - Radius is measured in plot width");
                return true;
            }
        }
        PlayerFunctions.sendMessage(plr, "/plot condense " + worldname + " start <radius>");
        return false;
    }
    
    public Set<Plot> getPlots(Collection<Plot> plots, int radius) {
        HashSet<Plot> outside = new HashSet<>();
        for (Plot plot : plots) {
            if (plot.id.x > radius || plot.id.x < -radius || plot.id.y > radius || plot.id.y < -radius) {
                outside.add(plot);
            }
        }
        return outside;
    }
    
    public static void sendMessage(final String message) {
        PlotMain.sendConsoleSenderMessage("&3PlotSquared -> Plot condense&8: &7" + message);
    }
    
}
