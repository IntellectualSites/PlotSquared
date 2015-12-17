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
import java.util.UUID;

import com.google.common.collect.BiMap;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
command = "debugclaimtest",
description = "If you accidentally delete your database, this command will attempt to restore all plots based on the data from plot signs. Execution time may vary",
category = CommandCategory.DEBUG,
requiredType = RequiredType.CONSOLE,
permission = "plots.debugclaimtest")
public class DebugClaimTest extends SubCommand {
    
    public static boolean claimPlot(final PlotPlayer player, final Plot plot, final boolean teleport) {
        return claimPlot(player, plot, teleport, "");
    }
    
    public static boolean claimPlot(final PlotPlayer player, final Plot plot, final boolean teleport, final String schematic) {
        final boolean result = EventUtil.manager.callClaim(player, plot, false);
        if (result) {
            MainUtil.createPlot(player.getUUID(), plot);
            MainUtil.setSign(player.getName(), plot);
            MainUtil.sendMessage(player, C.CLAIMED);
            if (teleport) {
                MainUtil.teleportPlayer(player, player.getLocation(), plot);
            }
        }
        return !result;
    }
    
    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        if (args.length < 3) {
            return !MainUtil
            .sendMessage(
            null,
            "If you accidentally delete your database, this command will attempt to restore all plots based on the data from the plot signs. \n\n&cMissing world arg /plot debugclaimtest {world} {PlotId min} {PlotId max}");
        }
        final String world = args[0];
        if (!BlockManager.manager.isWorld(world) || !PS.get().isPlotWorld(world)) {
            return !MainUtil.sendMessage(plr, "&cInvalid plot world!");
        }
        PlotId min, max;
        try {
            args[1].split(";");
            args[2].split(";");
            min = PlotId.fromString(args[1]);
            max = PlotId.fromString(args[2]);
        } catch (final Exception e) {
            return !MainUtil.sendMessage(plr,
            "&cInvalid min/max values. &7The values are to Plot IDs in the format &cX;Y &7where X;Y are the plot coords\nThe conversion will only check the plots in the selected area.");
        }
        MainUtil.sendMessage(plr, "&3Sign Block&8->&3PlotSquared&8: &7Beginning sign to plot conversion. This may take a while...");
        MainUtil.sendMessage(plr, "&3Sign Block&8->&3PlotSquared&8: Found an excess of 250,000 chunks. Limiting search radius... (~3.8 min)");
        final PlotManager manager = PS.get().getPlotManager(world);
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        final ArrayList<Plot> plots = new ArrayList<>();
        for (final PlotId id : MainUtil.getPlotSelectionIds(min, max)) {
            final Plot plot = MainUtil.getPlotAbs(world, id);
            if (PS.get().getPlot(world, plot.id) != null) {
                MainUtil.sendMessage(plr, " - &cDB Already contains: " + plot.id);
                continue;
            }
            final Location loc = manager.getSignLoc(plotworld, plot);
            final ChunkLoc chunk = new ChunkLoc(loc.getX() >> 4, loc.getZ() >> 4);
            final boolean result = ChunkManager.manager.loadChunk(world, chunk, false);
            if (!result) {
                continue;
            }
            final String[] lines = BlockManager.manager.getSign(loc);
            if (lines != null) {
                String line = lines[2];
                if ((line != null) && (line.length() > 2)) {
                    line = line.substring(2);
                    final BiMap<StringWrapper, UUID> map = UUIDHandler.getUuidMap();
                    UUID uuid = (map.get(new StringWrapper(line)));
                    if (uuid == null) {
                        for (final StringWrapper string : map.keySet()) {
                            if (string.value.toLowerCase().startsWith(line.toLowerCase())) {
                                uuid = map.get(string);
                                break;
                            }
                        }
                    }
                    if (uuid == null) {
                        uuid = UUIDHandler.getUUID(line, null);
                    }
                    if (uuid != null) {
                        MainUtil.sendMessage(plr, " - &aFound plot: " + plot.id + " : " + line);
                        plot.owner = uuid;
                        plots.add(plot);
                    } else {
                        MainUtil.sendMessage(plr, " - &cInvalid playername: " + plot.id + " : " + line);
                    }
                }
            }
        }
        if (plots.size() > 0) {
            MainUtil.sendMessage(plr, "&3Sign Block&8->&3PlotSquared&8: &7Updating '" + plots.size() + "' plots!");
            DBFunc.createPlotsAndData(plots, new Runnable() {
                @Override
                public void run() {
                    MainUtil.sendMessage(plr, "&6Database update finished!");
                }
            });
            for (final Plot plot : plots) {
                PS.get().updatePlot(plot);
            }
            MainUtil.sendMessage(plr, "&3Sign Block&8->&3PlotSquared&8: &7Complete!");
        } else {
            MainUtil.sendMessage(plr, "No plots were found for the given search.");
        }
        return true;
    }
}
