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

import com.google.common.collect.BiMap;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@CommandDeclaration(
command = "debugclaimtest",
description = "If you accidentally delete your database, this command will attempt to restore all plots based on the data from plot signs. Execution time may vary",
category = CommandCategory.DEBUG,
requiredType = RequiredType.CONSOLE,
permission = "plots.debugclaimtest")
public class DebugClaimTest extends SubCommand {
    
    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        if (args.length < 3) {
            return !MainUtil
            .sendMessage(
            null,
            "If you accidentally delete your database, this command will attempt to restore all plots based on the data from the plot signs. \n\n&cMissing world arg /plot debugclaimtest {world} {PlotId min} {PlotId max}");
        }
        PlotArea area = PS.get().getPlotAreaByString(args[0]);
        if (area == null || !WorldUtil.IMP.isWorld(area.worldname)) {
            C.NOT_VALID_PLOT_WORLD.send(plr, args[0]);
            return false;
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
        final PlotManager manager = area.getPlotManager();
        final ArrayList<Plot> plots = new ArrayList<>();
        for (final PlotId id : MainUtil.getPlotSelectionIds(min, max)) {
            final Plot plot = area.getPlotAbs(id);
            if (plot.hasOwner()) {
                MainUtil.sendMessage(plr, " - &cDB Already contains: " + plot.getId());
                continue;
            }
            final Location loc = manager.getSignLoc(area, plot);
            final ChunkLoc chunk = new ChunkLoc(loc.getX() >> 4, loc.getZ() >> 4);
            final boolean result = ChunkManager.manager.loadChunk(area.worldname, chunk, false);
            if (!result) {
                continue;
            }
            final String[] lines = WorldUtil.IMP.getSign(loc);
            if (lines != null) {
                String line = lines[2];
                if (line != null && line.length() > 2) {
                    line = line.substring(2);
                    final BiMap<StringWrapper, UUID> map = UUIDHandler.getUuidMap();
                    UUID uuid = map.get(new StringWrapper(line));
                    if (uuid == null) {
                        for (final Map.Entry<StringWrapper, UUID> stringWrapperUUIDEntry : map.entrySet()) {
                            if (stringWrapperUUIDEntry.getKey().value.toLowerCase().startsWith(line.toLowerCase())) {
                                uuid = stringWrapperUUIDEntry.getValue();
                                break;
                            }
                        }
                    }
                    if (uuid == null) {
                        uuid = UUIDHandler.getUUID(line, null);
                    }
                    if (uuid != null) {
                        MainUtil.sendMessage(plr, " - &aFound plot: " + plot.getId() + " : " + line);
                        plot.owner = uuid;
                        plots.add(plot);
                    } else {
                        MainUtil.sendMessage(plr, " - &cInvalid playername: " + plot.getId() + " : " + line);
                    }
                }
            }
        }
        if (!plots.isEmpty()) {
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
